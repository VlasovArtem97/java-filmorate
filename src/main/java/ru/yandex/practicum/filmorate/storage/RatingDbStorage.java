package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.RatingStorage;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;

    @Override
    public Collection<RatingMpa> getAllRatings() {
        log.info("Получен запрос на получения списка всех рейтингов");
        String query = "SELECT * FROM ratings_mpa";
        List<RatingMpa> ratingMpas = jdbcTemplate.query(query, ratingRowMapper);
        log.info("Список рейтингов получен: {}", ratingMpas);
        return ratingMpas;
    }

    @Override
    public RatingMpa getRatingById(Long id) {
        log.info("Начинается поиск рейтинга по id - {}", id);
        String query = "SELECT * FROM ratings_mpa WHERE rating_id = ?";
        try {
            RatingMpa ratingMpa = jdbcTemplate.queryForObject(query, ratingRowMapper, id);
            log.info("Рейтинг с id - {} успешно найден", id);
            return ratingMpa;
        } catch (EmptyResultDataAccessException e) {
            log.error("Рейтинг с указанным id - {} не найден", id);
            throw new NotFoundException("Рейтинг с ID - " + id + " не найден " + e.getMessage());
        }
    }

}