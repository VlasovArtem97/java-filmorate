package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> gettingFilms();

    Film findFilmById(Long filmId);

    void addingLikes(Long filmId, Long userId);

    void removingALike(Long filmId, Long userId);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    Collection<Film> getRecommendedMovies(Long userId);

    Collection<Film> listOfCommonFilms(long userId, long friendId);

    Collection<Film> getFilmsByDirectorId(Long id, String sortBy);
}
