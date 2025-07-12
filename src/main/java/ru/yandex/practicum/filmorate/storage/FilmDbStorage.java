package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.DirectorStorage;
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
    private final DirectorStorage directorStorage;

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

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        try {
        List<Film> films;
        String sql;
        if (genreId == null && year == null) {
            // без жанра и года
            sql = """
                    SELECT f.* FROM films AS f
                    LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
                    GROUP BY f.film_id
                    ORDER BY COUNT(fl.user_id) DESC
                    LIMIT ?
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, count);
        } else if (genreId != null && year == null) {
            // с жанром, но без года
            sql = """
                    SELECT f.* FROM films AS f
                    LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
                    RIGHT JOIN genres_films as gf ON gf.film_id = f.film_id
                    WHERE gf.genre_id = ?
                    GROUP BY f.film_id
                    ORDER BY COUNT(fl.user_id) DESC
                    LIMIT ?
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, genreId, count);
        } else if (genreId == null && year != null) {
            // без жанра, но с годом
            sql = """
                    SELECT f.* FROM films AS f
                    LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
                    WHERE EXTRACT(YEAR FROM f.release_date) = ?
                    GROUP BY f.film_id
                    ORDER BY COUNT(fl.user_id) DESC
                    LIMIT ?
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, year, count);
        } else {
            // с жанром, с годом
            sql = """
                    SELECT f.* FROM films AS f
                    LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
                    RIGHT JOIN genres_films as gf ON gf.film_id = f.film_id
                    WHERE gf.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ?
                    GROUP BY f.film_id
                    ORDER BY COUNT(fl.user_id) DESC
                    LIMIT ?
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, genreId, genreId, year, count);
        }
        log.info("Получен список популярных фильмов. Количество популярных фильмов = {}", films.size());
        return films;
        } catch (EmptyResultDataAccessException e) {
            log.error("Не удалось получить список из - {} популярных фильмов", count);
            throw new IllegalStateException("Не удалось получить список из + " + count +
                    "популярных фильмов" + e.getMessage());
        }
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
    public Collection<Film> getRecommendedMovies(Long userId) {

        /*
         Принцип работы алгоритма:

         1. Находим максимально похожего пользователя
         1.1 Находим фильмы, которые лайкнул пользователь userId
         1.2 Для каждого пользователя (кроме userId) рассчитываем число поставленных лайков этих фильмов;
         1.3 Сортируем по уменьшению этого числа;
         1.4 Получаем id первого пользователя из полученного списка

         2. Формируем список рекомендуемых фильмов
         2.1 Получаем перечень id фильмов, которые лайкнул пользователь id, но не лайкнул пользователь userId
         2.2 Получаем данные этих фильмов
        */

        final String query1 = """
            SELECT uc.uid FROM (
                SELECT u.user_id AS uid, count(fl.*) AS cnt
                FROM users u INNER JOIN film_likes fl ON u.user_id = fl.USER_ID
                WHERE u.user_id != ? AND (fl.film_id IN (SELECT film_id FROM film_likes WHERE user_id = ?))
                GROUP BY u.user_id
                ORDER BY cnt desc
                LIMIT 1) as uc;
            """;
        // Здесь для анализа по типу Slope One можно (путем настройки LIMIT) отрегулировать
        // число близких пользователей и применить более серьезную методику
        var sameUserIdList = jdbcTemplate.queryForList(query1, Long.class, userId, userId);
        if (sameUserIdList.isEmpty()) {
            log.info("Для пользователя {} получен пустой список рекомендованных фильмов", userId);
            return List.of();
        }

        String query2 = """
            SELECT * FROM films WHERE film_id IN (
                SELECT f.film_id
                FROM films f INNER JOIN film_likes fl ON f.film_id = fl.film_id
                WHERE fl.user_id = ?
                EXCEPT
                SELECT f.film_id
                FROM films f INNER JOIN film_likes fl ON f.film_id = fl.film_id
                WHERE fl.user_id = ?);
            """;
        long sameUserId = sameUserIdList.getFirst();
        List<Film> films = jdbcTemplate.query(query2, filmRowMapper, sameUserId, userId);
        log.info("Для пользователя {} получен список из {} рекомендованных фильмов", userId, films.size());
        return films;
    }

    @Override
    public Collection<Film> listOfCommonFilms(long userId, long friendId) {
        String query = """
            SELECT f.* FROM films AS f JOIN (
                SELECT film_id FROM film_likes GROUP BY film_id ORDER BY COUNT(*) DESC
            ) AS pf ON f.film_id = pf.film_id
            WHERE f.film_id IN (
                SELECT film_id from FILM_LIKES WHERE USER_ID = ?
                INTERSECT
                SELECT FILM_ID from FILM_LIKES WHERE USER_ID = ?
            );
            """;
        try {
            List<Film> films = jdbcTemplate.query(query, filmRowMapper, userId, friendId);
            log.info("Получен список из общих фильмов длиной {}", films.size());
            return films;
        } catch (DataAccessException e) {
            String msg = "Не удалось получить список общих фильмов";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    @Override
    public List<Film> getFilmsByDirectorId(Long id, String sortBy) {
        directorStorage.getDirectorByID(id);
        String sql;
        if ("year".equalsIgnoreCase(sortBy)) {
            sql = """
                SELECT f.*
                FROM films f INNER JOIN film_director fd ON f.film_id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY f.release_date;
                """;
        } else {
            sql = """
                SELECT f.*
                FROM films f INNER JOIN film_director fd ON f.film_id = fd.film_id
                INNER JOIN film_likes fl ON fl.film_id = f.film_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id
                ORDER BY count(fl.*) DESC;
                """;
        }
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        log.info("Получен список фильмов длиной {} режиссера {}", films.size(), id);
        return films;
    }
}
