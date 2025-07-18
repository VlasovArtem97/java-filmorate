package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.GenreStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreDbStorage;

    public Collection<Genre> getAllGenres() {
        return genreDbStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        return genreDbStorage.getGenreById(id);
    }

    public void addGenresFilm(Long filmId, Set<Genre> genreSet) {
        genreDbStorage.addGenresFilm(filmId, genreSet);
    }

    public Collection<Genre> getAListOfGenres(Long filmId) {
        return genreDbStorage.getAListOfGenres(filmId);
    }

    //метод для удаления жанров в соответствии id фильма
    public void removeFromGenresByFilmsId(Long filmId) {
        genreDbStorage.removeFromGenresByFilmsId(filmId);
    }

    public List<Genre> getGenreByIds(Set<Genre> genres) {
        return genreDbStorage.getGenreByIds(genres);
    }

    public void getFilmsWithGenres(List<Film> films) {
        genreDbStorage.getFilmsWithGenres(films);
    }
}
