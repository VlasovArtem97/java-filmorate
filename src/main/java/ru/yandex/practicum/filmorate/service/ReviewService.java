package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.ReviewStorage;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final EventService eventService;

    //Добавление нового отзыва
    public Review createReview(Review review) {
        log.info("Получен запрос на создание нового отзыва: {}", review);
        //проверка на наличие фильма по id
        if (review.getFilmId() != null) {
            filmService.gettingAMovieById(review.getFilmId());
        }
        //проверка на наличие пользователя по id
        if (review.getUserId() != null) {
            userService.gettingAUserById(review.getUserId());
        }
        Review createdReview = reviewStorage.createReview(review);
        eventService.addUserAddReviewEvent(review.getUserId(), createdReview.getReviewId());
        return createdReview;
    }

    //Редактирование уже имеющегося отзыва.
    public Review updateReview(Review review) {
        log.info("Получен запрос на обновление отзыва: {}", review);
        //проверка на наличие отзыва по id
        Review reviewOld = findReviewById(review.getReviewId());
        //проверка на наличие фильма по id
        if (review.getFilmId() != null) {
            filmService.gettingAMovieById(review.getFilmId());
            if (!Objects.equals(reviewOld.getFilmId(), review.getFilmId())) {
                log.warn("id - {} фильма указанного в отзыве для обновления не равен id - {} " +
                                "фильма указанного в отзыве, который пытаются обновить", review.getFilmId(),
                        reviewOld.getFilmId());
            }
        }
        //проверка на наличие пользователя по id
        if (review.getUserId() != null) {
            userService.gettingAUserById(review.getUserId());
            if (!Objects.equals(reviewOld.getUserId(), review.getUserId())) {
                log.warn("id - {} пользователя указанного в отзыве для обновления не равен id - {} " +
                                "пользователя указанного в отзыве, который пытаются обновить", review.getUserId(),
                        reviewOld.getUserId());
            }
        }
        Review updatedReview = reviewStorage.updateReview(review);
        eventService.addUserUpdateReviewEvent(reviewOld.getUserId(), updatedReview.getReviewId());
        return updatedReview;
    }

    //Удаление уже имеющегося отзыва.
    public void deleteReview(Long reviewId) {
        log.info("Получен запрос на удаление отзыва c iD - {}", reviewId);
        //проверка на наличие отзыва по id
        Review review = findReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);
        eventService.addUserRemoveReviewEvent(review.getUserId(), reviewId);
    }

    //Получение отзыва по идентификатору.
    public Review findReviewById(Long reviewId) {
        log.info("Получен запрос на получение отзыва c ID - {}", reviewId);
        return reviewStorage.findReviewById(reviewId);
    }

    //Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано, то 10.
    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        if (filmId != null) {
            log.info("Получен запрос на получение {} отзывов фильма с iD - {}", count, filmId);
            //проверка на наличие фильма в соответствии с id
            filmService.gettingAMovieById(filmId);
            return reviewStorage.getReviewsByFilmId(filmId, count);
        } else {
            log.info("Получен запрос на получение {} отзывов ко всем фильмам", count);
            return reviewStorage.getCountReviews(count);
        }
    }

    //пользователь ставит лайк отзыву
    public Review addLikeToReview(Long reviewId, Long userId) {
        log.info("Получен запрос по положительному оцениванию отзыва (добавление лайка отзыву) с ID - {}," +
                "пользователем с ID - {}", reviewId, userId);
        //проверка на наличие отзыва по id
        findReviewById(reviewId);
        //проверка на наличие пользователя по id
        userService.gettingAUserById(userId);
        return reviewStorage.addLikeToReview(reviewId, userId);
    }

    //пользователь ставит дизлайк отзыву.
    public Review addDislikeToReview(Long reviewId, Long userId) {
        log.info("Получен запрос по отрицательному оцениванию отзыва (добавление дизлайк отзыву) с ID - {}," +
                "пользователем с ID - {}", reviewId, userId);
        //проверка на наличие отзыва по id
        findReviewById(reviewId);
        //проверка на наличие пользователя по id
        userService.gettingAUserById(userId);
        return reviewStorage.addDislikeToReview(reviewId, userId);
    }

    //пользователь удаляет лайк отзыву.
    public void deleteReviewLike(Long reviewId, Long userId) {
        log.info("Получен запрос на удаление лайка отзыву с ID - {} от пользователя с ID - {}", reviewId, userId);
        //проверка на наличие отзыва по id
        findReviewById(reviewId);
        //проверка на наличие пользователя по id
        userService.gettingAUserById(userId);
        reviewStorage.deleteReviewLike(reviewId, userId);
    }

    //пользователь удаляет дизлайк отзыву
    public void deleteReviewDislike(Long reviewId, Long userId) {
        log.info("Получен запрос на удаление дизлайка отзыву с ID - {} от пользователя с ID - {}", reviewId, userId);
        //проверка на наличие отзыва по id
        findReviewById(reviewId);
        //проверка на наличие пользователя по id
        userService.gettingAUserById(userId);
        reviewStorage.deleteReviewDislike(reviewId, userId);
    }

    /**
     * Удалить все оценки к отзывам пользователя
     */
    public void deleteReviewRatingsByUser(Long userId) {
        reviewStorage.deleteReviewRatingsByUser(userId);
    }

    /**
     * Удалить все отзывы пользователя
     */
    public void deleteReviewsByUser(Long userId) {
        reviewStorage.deleteReviewsByUser(userId);
    }

    /**
     * Удалить все оценки к отзывам фильма
     */
    public void deleteReviewRatingsByFilm(Long filmId) {
        reviewStorage.deleteReviewRatingsByFilm(filmId);
    }

    /**
     * Удалить все отзывы о фильме
     */
    public void deleteReviewsByFilm(Long filmId) {
        reviewStorage.deleteReviewsByFilm(filmId);
    }

}
