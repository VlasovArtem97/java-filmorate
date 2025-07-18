package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface GenreStorage {

    Collection<Genre> getAllGenres();

    Genre getGenreById(Long id);

    void addGenresFilm(Long id, Set<Genre> genreSet);

    Collection<Genre> getAListOfGenres(Long filmId);

    void removeFromGenresByFilmsId(Long filmId);

    List<Genre> getGenreByIds(Set<Genre> genres);

    void getFilmsWithGenres(List<Film> films);
}
