package ru.yandex.practicum.filmorate.storage.olddatabase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.FilmStorage;

import java.util.*;

@Deprecated
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> filmMap = new HashMap<>();

    @Override
    public Collection<Film> gettingFilms() {
        log.info("Получен запрос на получения списка всех фильмов");
        return new ArrayList<>(filmMap.values());
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновления фильма - {}", film);
        Film film1 = findFilmById(film.getId());
        filmMap.put(film1.getId(), film);
        log.info("Фильм - {} успешно обновлен", film);
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Получен запрос на добавление фильма - {}", film);
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        log.info("Фильм - {} успешно добавлен", film);
        return film;
    }

    @Override
    public Film findFilmById(Long filmId) {
        log.info("Начинается поиск фильма по id - {}", filmId);
        Optional<Film> film = Optional.ofNullable(filmMap.get(filmId));
        if (film.isPresent()) {
            log.info("Фильм с id - {} успешно найден", filmId);
            return film.get();
        } else {
            log.error("Фильм с указанным id - {} не найден", filmId);
            throw new NotFoundException("Фильм с ID - " + film + " не найден");
        }
    }

    @Override
    public void addingLikes(Long filmId, Long userId) {

    }

    @Override
    public void removingALike(Long filmId, Long userId) {

    }

    @Override
    public Collection<Film> listOfPopularMovies(int count) {
        return List.of();
    }

    private long getNextId() {
        long currentMaxId = filmMap.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Collection<Film> getRecommendedMovies(Long userId) {
        return List.of();
    }
}
