package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> gettingFilms();

    Film findFilmById(Long filmId);

    void addingLikes(Long filmId, Long userId);

    void removingALike(Long filmId, Long userId);

    Collection<Film> listOfPopularMovies(int count);

    void removeAllFilmLikes(Long filmId);

    void removeAllFilmGenres(Long filmId);

    void deleteFilm(Long filmId);
}
