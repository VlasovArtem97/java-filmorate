package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
            log.debug("Режиссер с id = {} не найден", id);
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
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public void deleteDirector(Long id) {
        //удаляем режиссера из таблицы film_director
        removeDirectorFromFilmDirector(id);
        getDirectorByID(id);
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Режиссер с id={} удалён", id);
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
            log.debug("Удалено {} записей режиссеров фильма {}", n, filmId);
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
            log.debug("Получен список из {} режиссеров фильма {}", directors.size(), filmId);
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
            log.debug("Режиссер с id - {} из таблицы film_director успешно удален", directorId);
        } else {
            log.warn("Режиссер с id - {} в таблице film_director не найден", directorId);
        }
    }

    @Override
    public List<Director> getDirectorByIds(List<Director> directors) {
        log.info("Начинаем поиск по предоставленному списку id режиссеров");
        List<Long> director = directors.stream()
                .map(Director::getId)
                .toList();
        String value = director.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        String query = "SELECT * FROM directors WHERE id IN (" + value + ")";
        try {
            List<Director> genreList = jdbcTemplate.query(query, directorRowMapper, director.toArray());
            log.debug("Список полученных режиссеров: {}", genreList);
            return genreList;
        } catch (DataAccessException e) {
            log.error("Не удалось получить список режиссеров из таблицы directors: {}", e.getMessage());
            throw new IllegalStateException("Не удалось получить список режиссеров из таблицы directors "
                    + e.getMessage());
        }
    }

    @Override
    public void getFilmsWithDirectors(List<Film> films) {
        log.info("Начинаем заполнять режиссеров для возвращаемых объектов film - {}", films);
        Map<Long, Film> filmIds = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        String sql = """
                SELECT fd.film_id, d.id, d.name
                FROM film_director AS fd
                LEFT JOIN directors AS d ON fd.director_id = d.id
                WHERE fd.film_id IN (:film_ids)
                """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("film_ids", filmIds.keySet());
            namedParameterJdbcTemplate.query(sql, params, rs -> {
                Film film = filmIds.get(rs.getLong("film_id"));
                Long directorId = rs.getLong("id");
                if (!rs.wasNull()) {
                    film.getDirectors().add(new Director(directorId, rs.getString("name")));
                }
            });
            log.debug("Список фильмов с установленными режиссерами: {}", films);
        } catch (DataAccessException e) {
            log.error("Не удалось установить значения режиссеров для списка фильмов: {}. причина: {}",
                    films, e.getMessage());
            throw new IllegalStateException("Не удалось установить значения режиссеров для списка фильмов " +
                    e.getMessage());
        }
    }
}
