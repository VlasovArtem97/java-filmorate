package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Validated
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> filmMap = new HashMap<>();

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма - {}", film);
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        log.info("Фильм - {} успешно добавлен", film);
        return film;
    }

    @PutMapping
    @Validated(Marker.OnUpdate.class)
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновления фильма - {}", film);
        Optional<Film> film1 = Optional.ofNullable(filmMap.get(film.getId()));
        if (film1.isPresent()) {
            filmMap.put(film.getId(), film);
            log.info("Фильм - {} успешно обновлен", film);
            return film;
        } else {
            log.error("Фильм- {} с указанным id - {} не найден", film, film.getId());
            throw new ValidationException("Фильм с указанным id не найден");
        }
    }

    @GetMapping
    public Collection<Film> gettingFilms() {
        log.info("Получен запрос на получения списка всех фильмов");
        return new ArrayList<>(filmMap.values());
    }

    private long getNextId() {
        long currentMaxId = filmMap.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
