package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

@Repository("FilmDbStorage")
@Slf4j
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film findFilmById(Long filmId) {
        log.info("Начинается поиск фильма по id - {}", filmId);
        String query = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(query, filmRowMapper, filmId);
            log.info("Фильм с id - {} успешно найден", filmId);
            return film;
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильм с указанным id - {} не найден", filmId);
            throw new NotFoundException("Фильм с ID - " + filmId + " не найден");
        }
    }

    @Override
    public Collection<Film> gettingFilms() {
        log.info("Получен запрос на получения списка всех фильмов");
        String query = "SELECT * FROM films";
        return jdbcTemplate.query(query, filmRowMapper);
    }

    @Override
    public Film updateFilm(Film film) {
        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";
        int update = jdbcTemplate.update(query, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (update == 0) {
            log.error("Не удалось обновить данные фильма - {}", film);
            throw new IllegalStateException("Не удалось обновить данные фильма");
        }
        return findFilmById(film.getId());
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration, rating_id)" +
                "VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stms = connection.prepareStatement(query, new String[]{"film_id"});
            stms.setString(1, film.getName());
            stms.setString(2, film.getDescription());
            stms.setDate(3, Date.valueOf(film.getReleaseDate()));
            stms.setLong(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                stms.setLong(5, film.getMpa().getId());
            } else {
                stms.setNull(5, Types.BIGINT);
            }
            return stms;
        }, generatedKeyHolder);
        Number key = generatedKeyHolder.getKey();
        if (key == null) {
            log.error("Не удалось получить сгенерированный ID фильма - {}", film);
            throw new IllegalStateException("Не удалось получить сгенерированный ID фильма");
        }
        Long id = key.longValue();
        film.setId(id);
        log.info("Фильм успешно добавлен");
        return film;
    }

    /*@Override
    public Collection<Film> listOfPopularMovies(int count) {
        String query = "SELECT f.* FROM films AS f " +
                "JOIN (SELECT film_id " +
                "FROM film_likes " +
                "GROUP BY film_id " +
                "ORDER BY COUNT(*) DESC " +
                "LIMIT ?) AS popular_film ON f.film_id = popular_film.film_id;";
        try {
            List<Film> films = jdbcTemplate.query(query, filmRowMapper, count);
            log.info("Список из {} популярных фильмов получен: {}", count, films);
            return films;
        } catch (EmptyResultDataAccessException e) {
            log.error("Не удалось получить список из - {} популярных фильмов", count);
            throw new IllegalStateException("Не удалось получить список из + " + count +
                    "популярных фильмов" + e.getMessage());
        }
    }*/
    @Override
    public Collection<Film> listOfPopularMovies(int count) {
        String sql = """
        SELECT f.film_id, f.name, f.description, f.release_date,
               f.duration, f.rating_id
        FROM films AS f
        LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
        GROUP BY f.film_id, f.name, f.description,
                 f.release_date, f.duration, f.rating_id
        ORDER BY COUNT(fl.user_id) DESC
        LIMIT ?
        """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        log.info("Список из {} популярных (включая без лайков) фильмов получен: {}", count, films);
        return films;
    }

    @Override
    public void removingALike(Long filmId, Long userId) {
        String query = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int count = jdbcTemplate.update(query, filmId, userId);
        if (count == 0) {
            log.error("Не удалось удалить лайк с фильма по id - {} пользователем с id - {}", filmId, userId);
            throw new IllegalStateException("Не удалось удалить лайк с фильма");
        } else {
            log.info("Лайк с фильма по id - {} успешно удален пользователем с id - {}", filmId, userId);
        }
    }

    @Override
    public void addingLikes(Long filmId, Long userId) {
        String query = "INSERT INTO film_likes (user_id, film_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(query, userId, filmId);
            log.info("Лайк успешно поставлен фильму с id - {} пользователем с id - {}", filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.error("Не удалось поставить лайк фильму с id - {} пользователем с id - {}", filmId, userId);
            throw new IllegalStateException("Не удалось поставить лайк фильму с id " +
                    filmId + "пользователем с id - " + userId + " " + e.getMessage());
        }
    }

    @Override
    public void removeAllFilmLikes(Long filmId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", filmId);
    }

    @Override
    public void removeAllFilmGenres(Long filmId) {
        jdbcTemplate.update("DELETE FROM genres_films WHERE film_id = ?", filmId);
    }

    @Override
    public void deleteFilm(Long filmId) {
        int count = jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", filmId);
        if (count == 0) {
            throw new NotFoundException("Фильм с ID=" + filmId + " не найден");
        }
    }
}
