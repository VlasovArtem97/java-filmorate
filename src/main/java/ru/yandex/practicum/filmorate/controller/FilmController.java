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
import java.util.List;

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
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                      @RequestParam(required = false) Integer genreId,
                                      @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
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

    @GetMapping("/common") // userId={userId}&friendId={friendId}
    public Collection<Film> getCommonFilms(@Positive @RequestParam long userId,
                                           @Positive @RequestParam long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{id}")
    public Collection<Film> getFilmByDirectorId(@PathVariable("id") Long id, @RequestParam String sortBy) {
        return filmService.getFilmsByDirectorId(id, sortBy);
    }

    // search?query=крад&by=director,title
    @GetMapping("/search")
    public Collection<Film> getFilmsByQuery(@RequestParam String query, @RequestParam String[] by) {
        return filmService.getFilmsByQuery(query, by);
    }
}
