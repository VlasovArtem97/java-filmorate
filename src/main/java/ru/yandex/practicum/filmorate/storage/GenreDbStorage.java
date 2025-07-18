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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Collection<Genre> getAllGenres() {
        log.info("Получен запрос на получения списка всех жанров");
        String query = "SELECT * FROM genres";
        return jdbcTemplate.query(query, genreRowMapper);
    }

    @Override
    public Genre getGenreById(Long id) {
        log.info("Начинается поиск жанра по id - {}", id);
        String query = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(query, genreRowMapper, id);
            log.debug("Жанр с id - {} успешно найден: {}", id, genre);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильм с указанным id - {} не найден", id);
            throw new NotFoundException("Жанр с ID - " + id + " не найден");
        }
    }

    @Override
    public void addGenresFilm(Long id, Set<Genre> genre) {
        log.info("Начинаем добавлять жанры в таблицу genres_films в соответствии с film_id и genre_id");
        String query = "INSERT INTO genres_films (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> objects = genre.stream()
                .map(genres -> new Object[]{id, genres.getId()})
                .collect(Collectors.toList());

        // Выполнение пакетной вставки
        jdbcTemplate.batchUpdate(query, objects);
        log.debug("film_id и genre_id успешно добавлены");
    }

    //Метод для удаления жанров из таблицы genres_films по filmId
    @Override
    public void removeFromGenresByFilmsId(Long filmId) {
        log.info("Начинаем удалять жанры из таблицы genres_films в соответствии с id фильма: {}", filmId);
        String query = "DELETE FROM genres_films WHERE film_id = ?";
        int count = jdbcTemplate.update(query, filmId);
        if (count == 0) {
            log.error("Не удалось удалить жанры из таблицы genres_films в соответствии с id фильма: {}", filmId);
        } else {
            log.debug("Жанры в соответствии с id фильма - {} успешно удалены", filmId);
        }
    }

    @Override
    public Collection<Genre> getAListOfGenres(Long filmId) {
        log.info("Поиск жанров фильма с id - {}", filmId);
        String query = "SELECT g.genre_id, g.name FROM genres AS g " +
                "JOIN genres_films AS gf ON g.genre_id = gf.genre_id " +
                "WHERE gf.film_id = ?";
        List<Genre> genre = jdbcTemplate.query(query, genreRowMapper, filmId);
        log.debug("Получен список жанров фильма с id - {}: {}", filmId, genre);
        return genre;
    }

    @Override
    public List<Genre> getGenreByIds(Set<Genre> genres) {
        log.info("Начинаем поиск по предоставленному списку id жанров");
        List<Long> genreId = genres.stream()
                .map(Genre::getId)
                .toList();
        String query = "SELECT * FROM genres WHERE genre_id IN (:genre_id)";
        try {
            MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource("genre_id", genreId);
            List<Genre> genreList = namedParameterJdbcTemplate.query(query, sqlParameterSource, genreRowMapper);
            log.debug("Список полученных жанров: {}", genreList);
            return genreList;
        } catch (DataAccessException e) {
            log.error("Не удалось получить список жанров из таблицы genres: {}", e.getMessage());
            throw new IllegalStateException("Не удалось получить список жанров из таблицы genres " + e.getMessage());
        }
    }

    @Override
    public void getFilmsWithGenres(List<Film> films) {
        log.info("Начинаем заполнять жанры для возвращаемых объектов film - {}", films);
        Map<Long, Film> filmIds = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        String sql = """
                SELECT gf.film_id, g.genre_id, g.name
                FROM genres_films AS gf
                LEFT JOIN genres AS g ON gf.genre_id = g.genre_id
                WHERE gf.film_id IN (:film_ids)
                """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("film_ids", filmIds.keySet());
            namedParameterJdbcTemplate.query(sql, params, rs -> {
                Film film = filmIds.get(rs.getLong("film_id"));
                Long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(genreId, rs.getString("name")));
                }
            });
            log.debug("Список фильмов с установленными жанрами: {}", films);
        } catch (DataAccessException e) {
            log.error("Не удалось установить значения жанров для списка фильмов: {}. причина: {}",
                    films, e.getMessage());
            throw new IllegalStateException("Не удалось установить значения жанров для списка фильмов " +
                    e.getMessage());
        }
    }
}
