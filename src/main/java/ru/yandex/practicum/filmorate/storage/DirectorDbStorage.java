package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, directorRowMapper);
    }

    @Override
    public Director getDirectorByID(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM directors WHERE id = ?", directorRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.info("Режиссер с id = {} не найден", id);
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
    }

    @Override
    public Director updateDirector(Director director) {
        getDirectorByID(director.getId());
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE id = ?",
                director.getName(),
                director.getId());
        return director;
    }

    @Override
    public Director createDirector(Director director) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", director.getName());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");

        director.setId((long) simpleJdbcInsert.executeAndReturnKey(values).intValue());
        log.info("Добавлен режиссер {}", director);
        return director;
    }

    @Override
    public void deleteDirector(Long id) {
        //удаляем режиссера из таблицы film_director
        removeDirectorFromFilmDirector(id);
        getDirectorByID(id);
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.info("Режиссер с id={} удалён", id);
    }

    @Override
    public void addDirectorsToFilm(Long filmId, List<Director> directors) {
        final String sql = "INSERT INTO film_director(film_id, director_id) VALUES (?, ?);";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, directors.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
    }

    @Override
    public void removeDirectorsFromFilm(Long filmId) {
        final String sql = "DELETE FROM film_director WHERE film_id = ?;";
        try {
            int n = jdbcTemplate.update(sql, filmId);
            log.info("Удалено {} записей режиссеров фильма {}", n, filmId);
        } catch (DataAccessException e) {
            final String msg = "Отказ операции удаления режиссеров по фильму";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    @Override
    public List<Director> getDirectorsOfFilm(Long filmId) {
        final String sql = """
                SELECT d.*
                FROM directors d INNER JOIN film_director fd ON d.id = fd.director_id
                WHERE fd.film_id = ?;
                """;
        try {
            List<Director> directors = jdbcTemplate.query(sql, new DirectorRowMapper(), filmId);
            log.info("Получен список из {} режиссеров фильма {}", directors.size(), filmId);
            return directors;
        } catch (DataAccessException e) {
            throw new IllegalStateException("Отказ операции получения списка режиссеров", e);
        }
    }

    private void removeDirectorFromFilmDirector(Long directorId) {
        String query = """
                DELETE FROM film_director WHERE director_id = ?
                """;
        int count = jdbcTemplate.update(query, directorId);
        if (count > 0) {
            log.info("Режиссер с id - {} из таблицы film_director успешно удален", directorId);
        } else {
            log.warn("Режиссер с id - {} в таблице film_director не найден", directorId);
        }
    }
}
