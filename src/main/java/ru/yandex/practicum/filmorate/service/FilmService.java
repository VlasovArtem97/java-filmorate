package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;


    public Collection<Film> gettingFilms() {
        return filmStorage.gettingFilms();
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public void addingLikes(Long filmId, Long userId) {
        log.info("Получен запрос по выставлению оценки (лайка) фильму c id - {} пользователем с id - {}", filmId, userId);
        User user = userService.gettingAUserById(userId);
        Film film = filmStorage.findFilmById(filmId);

        film.getLikes().add(user.getId());
        log.info("Оценка фильма с id - {} от пользователя с id - {} успешна выставлена", filmId, userId);
    }

    public Film gettingAMovieById(Long filmId) {
        return filmStorage.findFilmById(filmId);
    }

    public void removingALike(Long filmId, Long userId) {
        log.info("Получен запрос на удаления лайка с фильма по id - {} пользователем с id - {}", filmId, userId);
        User user = userService.gettingAUserById(userId);
        Film film = filmStorage.findFilmById(filmId);

        film.getLikes().remove(userId);
        log.info("Лайк с фильма по id - {} пользователем с id - {} успешно удален", filmId, userId);
    }

    public Collection<Film> listOfPopularMovies(int count) {
        log.info("Получен запрос на получения - {} популярных фильмов", count);
        List<Film> films = new ArrayList<>(filmStorage.gettingFilms());
        films.sort(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed());
        return films.subList(0, Math.min(count, films.size()));
    }
}
