package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.RatingStorage;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Collection<RatingMpa> getAllRatings() {
        log.info("Получен запрос на получения списка всех рейтингов");
        String query = "SELECT * FROM ratings_mpa";
        List<RatingMpa> ratingMpas = jdbcTemplate.query(query, ratingRowMapper);
        log.debug("Список рейтингов получен: {}", ratingMpas);
        return ratingMpas;
    }

    @Override
    public RatingMpa getRatingById(Long id) {
        log.info("Начинается поиск рейтинга по id - {}", id);
        String query = "SELECT * FROM ratings_mpa WHERE rating_id = ?";
        try {
            RatingMpa ratingMpa = jdbcTemplate.queryForObject(query, ratingRowMapper, id);
            log.debug("Рейтинг с id - {} успешно найден: {}", id, ratingMpa);
            return ratingMpa;
        } catch (EmptyResultDataAccessException e) {
            log.error("Рейтинг с указанным id - {} не найден", id);
            throw new NotFoundException("Рейтинг с ID - " + id + " не найден " + e.getMessage());
        }
    }

    @Override
    public void getFilmsWithRatings(List<Film> films) {
        log.info("Начинаем заполнять рейтинг для возвращаемых объектов film - {}", films);
        Map<Long, Film> filmIds = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        String sql = """
                SELECT f.film_id, rm.rating_id, rm.name
                FROM films AS f
                LEFT JOIN ratings_mpa AS rm ON f.rating_id = rm.rating_id
                WHERE f.film_id IN (:film_ids)
                """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("film_ids", filmIds.keySet());
            namedParameterJdbcTemplate.query(sql, params, rs -> {
                Film film = filmIds.get(rs.getLong("film_id"));
                Long ratingId = rs.getLong("rating_id");
                if (!rs.wasNull()) {
                    film.setMpa(new RatingMpa(ratingId, rs.getString("name")));
                }
            });
            log.debug("Список фильмов с установленным рейтингом: {}", films);
        } catch (DataAccessException e) {
            log.error("Не удалось установить значение рейтинга для списка фильмов: {}. причина: {}",
                    films, e.getMessage());
            throw new IllegalStateException("Не удалось установить значение рейтинга для списка фильмов " +
                    e.getMessage());
        }
    }
}