package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.FilmStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final RatingService ratingService;
    private final DirectorService directorService;
    private final EventService eventService;

    public Collection<Film> gettingFilms() {
        Collection<Film> films = filmStorage.gettingFilms();
        films.forEach(this::addingFields);
        return films;
    }

    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновления фильма - {}", film);
        gettingAMovieById(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(genre -> genreService.getGenreById(genre.getId()));
            genreService.removeFromGenresByFilmsId(film.getId());
            genreService.addGenresFilm(film.getId(), film.getGenres());
        } else {
            genreService.removeFromGenresByFilmsId(film.getId());
        }
        Optional<RatingMpa> ratingMpa = Optional.ofNullable(film.getMpa());
        if (ratingMpa.isPresent()) {
            ratingService.getRatingById(film.getMpa().getId());
        }
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> directorService.getDirectorByID(director.getId()));
            directorService.removeDirectorsFromFilm(film.getId());
            directorService.addDirectorsToFilm(film.getId(), film.getDirectors());
        }
        Film filmUpdate = filmStorage.updateFilm(film);
        addingFields(filmUpdate);
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
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> directorService.getDirectorByID(director.getId()));
        }
        Film newFilm = filmStorage.addFilm(film);
        directorService.addDirectorsToFilm(newFilm.getId(), film.getDirectors());
        genreService.addGenresFilm(newFilm.getId(), film.getGenres());
        newFilm.setMpa(ratingService.getRatingById(newFilm.getMpa().getId()));
        newFilm.setGenres(new LinkedHashSet<>(genreService.getAListOfGenres(newFilm.getId())));
        newFilm.setDirectors(directorService.getDirectorsOfFilm(newFilm.getId()));
        log.info("Добавленный фильм - {}", newFilm);
        return newFilm;
    }

    public void addingLikes(Long filmId, Long userId) {
        log.info("Получен запрос по выставлению оценки (лайка) фильму c id - {} пользователем с id - {}",
                filmId, userId);
        userService.gettingAUserById(userId);
        gettingAMovieById(filmId);
        filmStorage.addingLikes(filmId, userId);
        eventService.addUserSetLikeEvent(userId, filmId);
    }

    public Film gettingAMovieById(Long filmId) {
        Film film1 = filmStorage.findFilmById(filmId);
        addingFields(film1);
        log.info("Полученный фильм - {}", film1);
        return film1;
    }

    public void removingALike(Long filmId, Long userId) {
        log.info("Получен запрос на удаления лайка с фильма по id - {} пользователем с id - {}", filmId, userId);
        userService.gettingAUserById(userId);
        gettingAMovieById(filmId);
        filmStorage.removingALike(filmId, userId);
        eventService.addUserRemoveLikeEvent(userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        log.info("Получен запрос на получения - {} популярных фильмов", count);
        List<Film> films = filmStorage.getPopularFilms(count, genreId, year);
        films.forEach(this::addingFields);
        log.info("Список популярных фильмов - {}", films);
        return films;
    }

    public Collection<Film> getRecommendations(Long userId) {
        log.info("Получен запрос на получение списка рекомендаций для пользователя с id - {}", userId);
        userService.gettingAUserById(userId);
        var films = filmStorage.getRecommendedMovies(userId);
        films.forEach(this::addingFields);
        log.info("Получен список из {} рекомендаций для пользователя {}", films.size(), userId);
        return films;
    }


    public Collection<Film> getCommonFilms(long userId, long friendId) {
        log.info("Получен запрос списка общих фильмов пользователей {} и {}", userId, friendId);
        userService.gettingAUserById(userId);
        userService.gettingAUserById(friendId);
        Collection<Film> commonFilms = filmStorage.listOfCommonFilms(userId, friendId);
        commonFilms.forEach(this::addingFields);
        log.info("Возвращён список фильмов длиной {}", commonFilms.size());
        return commonFilms;
    }

    public Collection<Film> getFilmsByDirectorId(Long id, String sortBy) {
        log.info("Поступил GET-запрос на получение списка фильмов sortBy={}, directorId={}", sortBy, id);
        var films = filmStorage.getFilmsByDirectorId(id, sortBy);
        films.forEach(this::addingFields);
        return films;
    }

    //Метод для поиска фильма по названию фильма или режиссеру
    public Collection<Film> getFilmsByQuery(String query, String[] by) {
        log.info("Получен запрос на поиск фильма по подстроке - {} в названии фильма или в имени режиссера - {}",
                query, by);
        Collection<Film> films = filmStorage.getFilmsByQuery(query, by);
        films.forEach(this::addingFields);
        log.info("В соответствии подстроки - {}, полученный список фильмов: {}", query, films);
        return films;
    }

    //Метод для установки значений для возвращаемого объекта - film
    public void addingFields(Film film) {
        film.setMpa(ratingService.getRatingById(film.getMpaId()));
        film.setGenres(new LinkedHashSet<>(genreService.getAListOfGenres(film.getId())));
        film.setDirectors(directorService.getDirectorsOfFilm(film.getId()));
    }

    public void deleteFilm(Long filmId) {
        log.info("Запрос на удаление фильма с id={}", filmId);
        filmStorage.removeAllFilmLikes(filmId);
        filmStorage.removeAllFilmGenres(filmId);
        filmStorage.deleteFilm(filmId);
    }

}
