package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;

public interface GenreStorage {

    Collection<Genre> getAllGenres();

    Genre getGenreById(Long id);

    void addGenresFilm(Long id, Set<Genre> genreSet);

    Collection<Genre> getAListOfGenres(Long filmId);

    void removeFromGenresByFilmsId(Long filmId);
}
