package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {

    List<Director> getAllDirectors();

    Director getDirectorByID(Long id);

    Director updateDirector(Director director);

    Director createDirector(Director director);

    void deleteDirector(Long id);

    void addDirectorsToFilm(Long filmId, List<Director> directors);

    void removeDirectorsFromFilm(Long filmId);

    List<Director> getDirectorsOfFilm(Long filmId);

    List<Director> getDirectorByIds(List<Director> directors);

    void getFilmsWithDirectors(List<Film> films);
}
