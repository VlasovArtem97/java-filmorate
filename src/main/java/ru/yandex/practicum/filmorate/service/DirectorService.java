package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirectorByID(Long id) {
        return directorStorage.getDirectorByID(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Long id) {
        directorStorage.deleteDirector(id);
    }

    public void addDirectorsToFilm(Long filmId, List<Director> directors) {
        directorStorage.addDirectorsToFilm(filmId, directors);
    }

    public List<Director> getDirectorByIds(List<Director> directors) {
        return directorStorage.getDirectorByIds(directors);
    }

    public void removeDirectorsFromFilm(Long filmId) {
        directorStorage.removeDirectorsFromFilm(filmId);
    }

    public List<Director> getDirectorsOfFilm(Long filmId) {
        return directorStorage.getDirectorsOfFilm(filmId);
    }
}
