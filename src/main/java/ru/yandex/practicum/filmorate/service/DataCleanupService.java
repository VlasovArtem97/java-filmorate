package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataCleanupService {

    //    private final FilmStorage filmStorage;
//    private final UserStorage userStorage;
    private final ReviewService reviewService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final EventService eventService;
    private final UserService userService;
    private final FilmService filmService;

    /**
     * Полное удаление фильма и всех связанных данных
     */
    @Transactional
    public void deleteFilmCompletely(Long filmId) {
        filmService.deleteFilm(filmId);
        reviewService.deleteReviewRatingsByFilm(filmId);
        reviewService.deleteReviewsByFilm(filmId);
        directorService.removeDirectorsFromFilm(filmId);
    }

    /**
     * Полное удаление пользователя и всех связанных данных
     */
    @Transactional
    public void deleteUserCompletely(Long userId) {
        eventService.eraseUserReferencedEvents(userId);
        userService.deleteUser(userId);
        reviewService.deleteReviewRatingsByUser(userId);
        reviewService.deleteReviewsByUser(userId);
    }
}