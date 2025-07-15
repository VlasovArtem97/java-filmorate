package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.UserStorage;

@Service
@RequiredArgsConstructor
public class DataCleanupService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewService reviewService;
    private final GenreService genreService;
    private final DirectorService directorService;

    /** Полное удаление фильма и всех связанных данных */
    @Transactional
    public void deleteFilmCompletely(Long filmId) {
        filmStorage.removeAllFilmLikes(filmId);
        filmStorage.removeAllFilmGenres(filmId);
        reviewService.deleteReviewRatingsByFilm(filmId);
        reviewService.deleteReviewsByFilm(filmId);
        directorService.removeDirectorsFromFilm(filmId);
        filmStorage.deleteFilm(filmId);
    }

    /** Полное удаление пользователя и всех связанных данных */
    @Transactional
    public void deleteUserCompletely(Long userId) {
        userStorage.removeAllFriendships(userId);
        userStorage.removeAllLikesByUser(userId);
        reviewService.deleteReviewRatingsByUser(userId);
        reviewService.deleteReviewsByUser(userId);
        userStorage.deleteUser(userId);
    }
}
