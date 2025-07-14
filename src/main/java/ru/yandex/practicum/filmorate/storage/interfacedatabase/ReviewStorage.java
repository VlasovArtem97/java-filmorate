package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    //Добавление нового отзыва
    Review createReview(Review review);

    //Редактирование уже имеющегося отзыва.
    Review updateReview(Review review);

    //Удаление уже имеющегося отзыва.
    void deleteReview(Long reviewId);

    //Получение отзыва по идентификатору.
    Review findReviewById(Long reviewId);

    //Получение count отзывов при наличии ID фильма
    Collection<Review> getReviewsByFilmId(Long filmId, int count);

    //Получение count отзывов при отсутствии ID фильма
    Collection<Review> getCountReviews(int count);

    //пользователь ставит лайк отзыву
    Review addLikeToReview(Long reviewId, Long userId);

    //пользователь ставит дизлайк отзыву.
    Review addDislikeToReview(Long reviewId, Long userId);

    //пользователь удаляет лайк отзыву.
    void deleteReviewLike(Long reviewId, Long userId);

    //пользователь удаляет дизлайк отзыву
    void deleteReviewDislike(Long reviewId, Long userId);

    // Удалить все лайки/дизлайки (evaluation) ко всем отзывам данного пользователя
    void deleteReviewRatingsByUser(Long userId);

    // Удалить все отзывы данного пользователя
    void deleteReviewsByUser(Long userId);

    // Удалить все лайки/дизлайки ко всем отзывам для данного фильма
    void deleteReviewRatingsByFilm(Long filmId);

    // Удалить все отзывы для данного фильма
    void deleteReviewsByFilm(Long filmId);

}
