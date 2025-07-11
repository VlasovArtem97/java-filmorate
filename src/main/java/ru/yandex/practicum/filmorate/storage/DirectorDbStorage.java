package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

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
        getDirectorByID(id);
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.info("Режиссер с id={} удалён", id);
    }
}
