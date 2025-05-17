package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> filmMap = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма - {}", film);
        validation(film);
        if (!(film.getId() == null)) {
            log.error("Ошибка валидации фильма - {}, при добавлении фильма указан id", film);
            throw new ValidationException("При добавлении фильма id фильма не должен быть указан");
        }
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        log.info("Фильм - {} успешно добавлен", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновления фильма - {}", film);
        validation(film);
        if (film.getId() == null) {
            log.error("Ошибка валидации фильма - {}, при обновлении данных фильма id отсутствует", film);
            throw new ValidationException("При обновлении данных фильма id фильма должен быть указан");
        }
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

    public void validation(Film film) {
        log.info("Начинается валидация фильма - {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации фильма - {}, название фильма пустое", film);
            throw new ValidationException("Название фильма не должно быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.error("Ошибка валидации фильма - {}, Описание фильма превышает 200 символов", film);
            throw new ValidationException("Описание фильма не должно превышать 200 символов");
        }
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма - {}, раньше установленного времени 28.12.1895", film.getReleaseDate());
            throw new ValidationException("Дата релиза фильма должна быть позже данной даты - 28.12.1895");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Продолжительность фильма - {} является отрицательным числом", film.getDuration());
            throw new ValidationException("Продолжительность фильма не должна быть отрицательным числом");
        }
        log.info("Валидация фильма - {} завершена", film);
    }
}
