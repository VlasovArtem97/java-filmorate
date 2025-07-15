package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataCleanupService {

    private final FilmService filmService;
    private final UserService userService;

    /** Полное удаление фильма и всех связанных данных */
    @Transactional
    public void deleteFilmCompletely(Long filmId) {
        filmService.deleteFilm(filmId);
    }

    /** Полное удаление пользователя и всех связанных данных */
    @Transactional
    public void deleteUserCompletely(Long userId) {
        userService.deleteUser(userId);
    }
}
