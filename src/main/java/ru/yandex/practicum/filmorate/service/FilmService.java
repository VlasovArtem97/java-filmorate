package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.FilmStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final RatingService ratingService;

    public Collection<Film> gettingFilms() {
        return filmStorage.gettingFilms();
    }

    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновления фильма - {}", film);
        gettingAMovieById(film.getId());
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreService.getGenreById(genre.getId()));
            genreService.removeFromGenresFilms(film.getId(), film.getGenres());
            genreService.addGenresFilm(film.getId(), film.getGenres());
        }
        Optional<RatingMpa> ratingMpa = Optional.ofNullable(film.getMpa());
        if (ratingMpa.isPresent()) {
            ratingService.getRatingById(film.getMpa().getId());
        }
        Film filmUpdate = filmStorage.updateFilm(film);
        filmUpdate.setMpa(ratingService.getRatingById(filmUpdate.getMpaId()));
        filmUpdate.setGenres(new LinkedHashSet<>(genreService.getAListOfGenres(film.getId())));
        log.info("Обновленный фильм - {}", filmUpdate);
        return filmUpdate;
    }

    public Film addFilm(Film film) {
        log.info("Получен запрос на добавление фильма - {}", film);
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreService.getGenreById(genre.getId()));
        }
        Optional<RatingMpa> ratingMpa = Optional.ofNullable(film.getMpa());
        if (ratingMpa.isPresent()) {
            film.setMpa(ratingService.getRatingById(film.getMpa().getId()));
        }
        Film newFilm = filmStorage.addFilm(film);
        genreService.addGenresFilm(newFilm.getId(), film.getGenres());
        newFilm.setMpa(ratingService.getRatingById(newFilm.getMpa().getId()));
        newFilm.setGenres(new LinkedHashSet<>(genreService.getAListOfGenres(newFilm.getId())));
        log.info("LДобавленный фильм - {}", newFilm);
        return newFilm;
    }

    public void addingLikes(Long filmId, Long userId) {
        log.info("Получен запрос по выставлению оценки (лайка) фильму c id - {} пользователем с id - {}",
                filmId, userId);
        userService.gettingAUserById(userId);
        gettingAMovieById(filmId);
        filmStorage.addingLikes(filmId, userId);
    }

    public Film gettingAMovieById(Long filmId) {
        Film film1 = filmStorage.findFilmById(filmId);
        film1.setMpa(ratingService.getRatingById(film1.getMpaId()));
        film1.setGenres(new LinkedHashSet<>(genreService.getAListOfGenres(film1.getId())));
        log.info("Полученный фильм - {}", film1);
        return film1;
    }

    public void removingALike(Long filmId, Long userId) {
        log.info("Получен запрос на удаления лайка с фильма по id - {} пользователем с id - {}", filmId, userId);
        userService.gettingAUserById(userId);
        gettingAMovieById(filmId);
        filmStorage.removingALike(filmId, userId);
    }

    public Collection<Film> listOfPopularMovies(int count) {
        log.info("Получен запрос на получения - {} популярных фильмов", count);
        Collection<Film> films = filmStorage.listOfPopularMovies(count);
        films.forEach(film -> {
            film.setMpa(ratingService.getRatingById(film.getMpaId()));
            film.setGenres(new LinkedHashSet<>(genreService.getAListOfGenres(film.getId())));
        });
        log.info("Список популярных фильмов - {}", films);
        return films;
    }
}
