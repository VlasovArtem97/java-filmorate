package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    //Добавление нового отзыва
    @Validated(Marker.OnCreate.class)
    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewService.createReview(review);
    }

    //Редактирование уже имеющегося отзыва.
    @Validated(Marker.OnUpdate.class)
    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    //Удаление уже имеющегося отзыва.
    @DeleteMapping("/{id}")
    public void deleteReview(@Positive @PathVariable("id") Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

    //Получение отзыва по идентификатору.
    @GetMapping("/{id}")
    public Review findReviewById(@Positive @PathVariable("id") Long reviewId) {
        return reviewService.findReviewById(reviewId);
    }

    //Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10.
    @GetMapping()
    public Collection<Review> getReviewsByFilmId(@Positive @RequestParam(required = false) Long filmId,
                                                 @Positive @RequestParam(defaultValue = "10") int count) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    //пользователь ставит лайк отзыву
    @PutMapping("/{id}/like/{userId}")
    public Review addLikeToReview(@Positive @PathVariable("id") Long reviewId,
                                  @Positive @PathVariable Long userId) {
        return reviewService.addLikeToReview(reviewId, userId);
    }

    //пользователь ставит дизлайк отзыву.
    @PutMapping("/{id}/dislike/{userId}")
    public Review addDislikeToReview(@Positive @PathVariable("id") Long reviewId,
                                     @Positive @PathVariable Long userId) {
        return reviewService.addDislikeToReview(reviewId, userId);
    }

    //пользователь удаляет лайк отзыву.
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteReviewLike(@Positive @PathVariable("id") Long reviewId,
                                 @Positive @PathVariable Long userId) {
        reviewService.deleteReviewLike(reviewId, userId);
    }

    //пользователь удаляет дизлайк отзыву
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteReviewDislike(@Positive @PathVariable("id") Long reviewId,
                                    @Positive @PathVariable Long userId) {
        reviewService.deleteReviewDislike(reviewId, userId);
    }
}
