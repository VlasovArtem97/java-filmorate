package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor

public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewMapper reviewMapper;

    @Override
    public Review createReview(Review review) {
        String query = "INSERT INTO reviews (content, is_positive, user_id, film_id)" +
                "VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            return stmt;
        }, generatedKeyHolder);
        Number key = generatedKeyHolder.getKey();
        if (key == null) {
            log.error("Не удалось получить сгенерированный ID отзыва - {}", review);
            throw new IllegalStateException("Не удалось получить сгенерированный ID отзыва");
        }
        Long id = key.longValue();
        review.setReviewId(id);
        log.info("Отзыв - {} успешно добавлен", review);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String query = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int update = jdbcTemplate.update(query, review.getContent(), review.getIsPositive(), review.getReviewId());
        if (update == 0) {
            log.error("Не удалось обновить данные отзыва - {}", review);
            throw new IllegalStateException("Не удалось обновить данные отзыва");
        }
        log.info("Данные отзыва - {} успешно обновлены", review);
        return findReviewById(review.getReviewId());
    }

    @Override
    public void deleteReview(Long reviewId) {
        String query = "DELETE FROM reviews WHERE review_id = ?";
        int count = jdbcTemplate.update(query, reviewId);
        if (count == 0) {
            log.error("Не удалось удалить отзыв с id - {}", reviewId);
            throw new IllegalStateException("Не удалось удалить отзыв");
        } else {
            log.info("Отзыв с ID - {} успешно удален", reviewId);
            //Удаляем данные из таблицы reviews_ratings_films_by_users (связь многие ко многим).
            deleteRatingToFilmByUser(reviewId);
        }
    }

    @Override
    public Review findReviewById(Long reviewId) {
        String query = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            Review review = jdbcTemplate.queryForObject(query, reviewMapper, reviewId);
            log.debug("Найденный отзыв: {}", review);
            return review;
        } catch (EmptyResultDataAccessException e) {
            log.error("Не удалось получить отзыв по ID - {}", reviewId);
            throw new NotFoundException("Не удалось получить отзыв c ID - " + reviewId + ": " + e.getMessage());
        }
    }

    @Override
    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        String query = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";
        List<Review> reviews = jdbcTemplate.query(query, reviewMapper, filmId, count);
        log.debug("Список из {} отзывов к фильму с id - {} получен: {}", count, filmId, reviews);
        return reviews;
    }

    @Override
    public Collection<Review> getCountReviews(int count) {
        log.info("Получен запрос на получение {} отзывов", count);
        String query = "SELECT * FROM reviews LIMIT ?";
        List<Review> reviews = jdbcTemplate.query(query, reviewMapper, count);
        log.debug("Список из {} отзывов получен: {}", count, reviews);
        return reviews;
    }

    @Override
    public Review addDislikeToReview(Long reviewId, Long userId) {
        String queryInsert = "INSERT INTO reviews_ratings_films_by_users (user_id, review_id, evaluation) VALUES (?, ?, ?)";
        String queryUpdate = "UPDATE reviews_ratings_films_by_users SET evaluation = ? WHERE user_id = ? AND review_id = ?";
        try {
            //Получаем оценку из базы данных reviews_ratings_films_by_users
            Optional<String> evaluationTable = getUserEvaluation(userId, reviewId);
            if (evaluationTable.isPresent()) {
                String evaluation = evaluationTable.get();
                if ("DISLIKE".equals(evaluation)) {
                    log.info("Пользователь с id {} уже ставил дизлайк отзыву с id - {}", userId, reviewId);
                    throw new IllegalStateException("Пользователь уже ставил дизлайк этому отзыву");
                } else if ("LIKE".equals(evaluation)) {
                    //изменяем значение с лайка на дизлайк
                    jdbcTemplate.update(queryUpdate, "DISLIKE", userId, reviewId);
                    //Уменьшаем значение поля useful в таблице reviews
                    addRatingToUtilityRating(reviewId, -2L);
                    log.info("Оценка изменена с лайка на дизлайк для отзыва c id {} пользователем c id {}", reviewId, userId);
                }
            } else {
                // Если оценки нет добавляем DISLIKE в таблицу reviews_ratings_films_by_users
                jdbcTemplate.update(queryInsert, userId, reviewId, "DISLIKE");
                //Уменьшаем значение поля useful в таблице reviews
                addRatingToUtilityRating(reviewId, -1L);
                log.info("Дизлайк успешно поставлен отзыву с id - {} пользователем с id - {}", reviewId, userId);
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при установке дизлайка отзыву id {} пользователем id {}: {}", reviewId, userId, e.getMessage());
            throw new IllegalStateException("Не удалось поставить дизлайк отзыву с id " + reviewId + " пользователем с id " + userId, e);
        }
        return findReviewById(reviewId);
    }

    @Override
    public Review addLikeToReview(Long reviewId, Long userId) {
        String insertQuery = "INSERT INTO reviews_ratings_films_by_users (user_id, review_id, evaluation) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE reviews_ratings_films_by_users SET evaluation = ? WHERE user_id = ? AND review_id = ?";

        try {
            //Получаем оценку в базе данных reviews_ratings_films_by_users
            Optional<String> evaluationTable = getUserEvaluation(userId, reviewId);
            if (evaluationTable.isPresent()) {
                String evaluation = evaluationTable.get();
                if ("LIKE".equals(evaluation)) {
                    log.info("Пользователь с id {} уже ставил лайк отзыву с id - {}", userId, reviewId);
                    throw new IllegalStateException("Пользователь уже ставил лайк этому отзыву");
                } else if ("DISLIKE".equals(evaluation)) {
                    // изменяем значение с дизлайка на лайк
                    jdbcTemplate.update(updateQuery, "LIKE", userId, reviewId);
                    //увеличиваем значение поле useful в таблице reviews
                    addRatingToUtilityRating(reviewId, 2L);
                    log.info("Оценка изменена с дизлайка на лайк для отзыва c id {} пользователем c id {}", reviewId, userId);
                }
            } else {
                // Если оценки нет, вставляем лайк и увеличиваем поле useful на 1
                jdbcTemplate.update(insertQuery, userId, reviewId, "LIKE");
                addRatingToUtilityRating(reviewId, 1L);
                log.info("Лайк успешно поставлен отзыву с id - {} пользователем с id - {}", reviewId, userId);
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при установке лайка отзыву id {} пользователем id {}: {}", reviewId, userId, e.getMessage());
            throw new IllegalStateException("Не удалось поставить лайк отзыву с id " + reviewId + " пользователем с id " + userId, e);
        }

        return findReviewById(reviewId);
    }

    @Override
    public void deleteReviewLike(Long reviewId, Long userId) {
        //Проверяем наличия лайка от пользователя
        String query = "SELECT COUNT(*) FROM reviews_ratings_films_by_users WHERE review_id = ? AND user_id = ? " +
                "AND evaluation = 'LIKE'";
        int count = jdbcTemplate.queryForObject(query, Integer.class, reviewId, userId);
        if (count == 0) {
            log.error("Лайк от пользователя с id {} к отзыву с id {} не найден", userId, reviewId);
            throw new IllegalStateException("Лайк не найден для удаления");
        }
        //Удаляем взаимосвязь в таблице reviews_ratings_films_by_users
        String deleteQuery = "DELETE FROM reviews_ratings_films_by_users WHERE review_id = ? AND user_id = ? AND evaluation = 'LIKE'";
        int deleted = jdbcTemplate.update(deleteQuery, reviewId, userId);
        if (deleted == 0) {
            log.error("Не удалось удалить лайк от пользователя с id {} к отзыву с id {}", userId, reviewId);
            throw new IllegalStateException("Не удалось удалить лайк");
        }
        //Удаляем лайк в поле useful в Reviews таблице
        addRatingToUtilityRating(reviewId, -1L);
        log.info("Лайк от пользователя с id {} успешно удалён для отзыва с id {}", userId, reviewId);
    }

    @Override
    public void deleteReviewDislike(Long reviewId, Long userId) {
        //Проверяем наличия дизлайка от пользователя
        String query = "SELECT * FROM reviews_ratings_films_by_users WHERE review_id = ? AND user_id = ? " +
                "AND evaluation = 'DISLIKE'";
        int count = jdbcTemplate.queryForObject(query, Integer.class, reviewId, userId);
        if (count == 0) {
            log.error("Дизлайк от пользователя с id {} к отзыву с id {} не найден", userId, reviewId);
            throw new IllegalStateException("Дизлайк не найден для удаления");
        }
        //Удаляем взаимосвязь в таблице reviews_ratings_films_by_users
        String deleteQuery = "DELETE FROM reviews_ratings_films_by_users WHERE review_id = ? AND user_id = ? AND evaluation = 'LIKE'";
        int deleted = jdbcTemplate.update(deleteQuery, reviewId, userId);
        if (deleted == 0) {
            log.error("Не удалось удалить дизлайк от пользователя с id {} к отзыву с id {}", userId, reviewId);
            throw new IllegalStateException("Не удалось удалить дизлайк");
        }
        //Удаляем дизлайк в поле useful в Reviews таблице
        addRatingToUtilityRating(reviewId, 1L);
        log.info("Дизлайк от пользователя с id {} успешно удалён для отзыва с id {}", userId, reviewId);
    }

    //Метод для удаления данных из таблицы reviews_ratings_films_by_users. Удаляется id пользователя и id отзыва
    private void deleteRatingToFilmByUser(Long reviewId) {
        log.info("Начинаем удалять данные из таблицы reviews_ratings_films_by_users по ID отзыва - {}",
                reviewId);
        String query = "DELETE FROM reviews_ratings_films_by_users WHERE review_id = ?";
        int count = jdbcTemplate.update(query, reviewId);
        if (count == 0) {
            log.error("Не удалось удалить данные из таблицы reviews_ratings_films_by_users: отзыв с ID - {}", reviewId);
        } else {
            log.info("Отзыв с ID - {} и id пользователей, кто поставил лайк успешно удалены из таблицы " +
                    "reviews_ratings_films_by_users", reviewId);
        }
    }

    //Метод, который изменяет рейтинг полезности в таблице reviews
    private void addRatingToUtilityRating(Long reviewId, Long up) {
        String query = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        int update = jdbcTemplate.update(query, up, reviewId);
        if (update == 0) {
            log.error("Не удалось обновить поле useful отзыва c id - {}", reviewId);
            throw new IllegalStateException("Не удалось обновить данные пользователя поле useful");
        }
        log.info("поле useful отзыва с id - {} успешно обновлены", reviewId);
    }

    //метод, который ищет оценку (Like, Dislike или ничего) в базе данных reviews_ratings_films_by_users
    private Optional<String> getUserEvaluation(Long userId, Long reviewId) {
        String querySelect = "SELECT evaluation FROM reviews_ratings_films_by_users WHERE user_id = ? AND review_id = ?";
        try {
            String evaluation = jdbcTemplate.queryForObject(querySelect, String.class, userId, reviewId);
            return Optional.ofNullable(evaluation);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteReviewRatingsByUser(Long userId) {
        jdbcTemplate.update(
                "DELETE FROM reviews_ratings_films_by_users WHERE user_id = ?",
                userId
        );
    }

    @Override
    public void deleteReviewsByUser(Long userId) {
        // сначала удалить все голосования, затем сами отзывы
        deleteReviewRatingsByUser(userId);
        jdbcTemplate.update(
                "DELETE FROM reviews WHERE user_id = ?",
                userId
        );
    }

    @Override
    public void deleteReviewRatingsByFilm(Long filmId) {
        // найдём все отзывы этого фильма
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT review_id FROM reviews WHERE film_id = ?",
                Long.class, filmId
        );
        // удалим все оценки к каждому
        ids.forEach(rid -> jdbcTemplate.update(
                "DELETE FROM reviews_ratings_films_by_users WHERE review_id = ?",
                rid
        ));
    }

    @Override
    public void deleteReviewsByFilm(Long filmId) {
        // сначала удалить голосования, потом отзывы
        deleteReviewRatingsByFilm(filmId);
        jdbcTemplate.update(
                "DELETE FROM reviews WHERE film_id = ?",
                filmId
        );
    }

}
