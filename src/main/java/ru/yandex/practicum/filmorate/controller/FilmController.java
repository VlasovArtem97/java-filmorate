package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PutMapping("/{id}/like/{userId}")
    public void addingLike(@Positive @PathVariable("id") Long filmId, @Positive @PathVariable Long userId) {
        filmService.addingLikes(filmId, userId);
    }

    @GetMapping("/{id}")
    public Film gettingAMovieById(@Positive @PathVariable("id") Long filmId) {
        return filmService.gettingAMovieById(filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removingALike(@Positive @PathVariable("id") Long filmId, @Positive @PathVariable Long userId) {
        filmService.removingALike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> listOfPopularMovies(@Positive @RequestParam(required = false, defaultValue = "10")
                                                int count) {
        return filmService.listOfPopularMovies(count);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    @Validated(Marker.OnUpdate.class)
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public Collection<Film> gettingFilms() {
        return filmService.gettingFilms();
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@Positive @PathVariable("id") Long filmId) {
        filmService.deleteFilm(filmId);
    }
}
