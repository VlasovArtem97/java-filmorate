package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.RatingStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreStorage genreStorage;
    private final RatingStorage ratingStorage;

    public Collection<Film> gettingFilms() {
        return filmStorage.gettingFilms();
    }

    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновления фильма - {}", film);
        gettingAMovieById(film.getId());
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreStorage.getGenreById(genre.getId()));
            genreStorage.removeFromGenresFilms(film.getId(), film.getGenres());
            genreStorage.addGenresFilm(film.getId(), film.getGenres());
        }
        Optional<RatingMpa> ratingMpa = Optional.ofNullable(film.getMpa());
        if (ratingMpa.isPresent()) {
            ratingStorage.getRatingById(film.getMpa().getId());
        }
        return filmStorage.updateFilm(film);
    }

    public Film addFilm(Film film) {
        log.info("Получен запрос на добавление фильма - {}", film);
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreStorage.getGenreById(genre.getId()));
        }
        Optional<RatingMpa> ratingMpa = Optional.ofNullable(film.getMpa());
        if (ratingMpa.isPresent()) {
            film.setMpa(ratingStorage.getRatingById(film.getMpa().getId()));
        }
        Film film1 = filmStorage.addFilm(film);
        genreStorage.addGenresFilm(film1.getId(), film.getGenres());
        film1.setGenres(new LinkedHashSet<>(genreStorage.getAListOfGenres(film1.getId())));
        return film1;
    }

    public void addingLikes(Long filmId, Long userId) {
        log.info("Получен запрос по выставлению оценки (лайка) фильму c id - {} пользователем с id - {}",
                filmId, userId);
        userService.gettingAUserById(userId);
        gettingAMovieById(filmId);
        filmStorage.addingLikes(filmId, userId);
    }

    public Film gettingAMovieById(Long filmId) {
        return filmStorage.findFilmById(filmId);
    }

    public void removingALike(Long filmId, Long userId) {
        log.info("Получен запрос на удаления лайка с фильма по id - {} пользователем с id - {}", filmId, userId);
        userService.gettingAUserById(userId);
        gettingAMovieById(filmId);
        filmStorage.removingALike(filmId, userId);
    }

    public Collection<Film> listOfPopularMovies(int count) {
        log.info("Получен запрос на получения - {} популярных фильмов", count);
        return filmStorage.listOfPopularMovies(count);
    }
}
