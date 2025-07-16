package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

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
            log.info("Жанр с id - {} успешно найден", id);
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
        log.info("film_id и genre_id успешно добавлены");
    }

    //метод под вопросом
    @Override
    public void removeFromGenresFilms(Long id, Set<Genre> genre) {
        log.info("Начинаем удалять жанры из таблицы genres_films в соответствии с film_id и genre_id");
        String query = "DELETE FROM genres_films WHERE film_id = ? AND genre_id = ?";
        List<Object[]> objects = genre.stream()
                .map(genres -> new Object[]{id, genres.getId()})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(query, objects);
        log.info("film_id и genre_id успешно удалены");
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
            log.info("Жанры в соответствии с id фильма - {} успешно удалены", filmId);
        }
    }

    @Override
    public Collection<Genre> getAListOfGenres(Long filmId) {
        log.info("Поиск жанров фильма с id - {}", filmId);
        String query = "SELECT g.genre_id, g.name FROM genres AS g " +
                "JOIN genres_films AS gf ON g.genre_id = gf.genre_id " +
                "WHERE gf.film_id = ?";
        List<Genre> genre = jdbcTemplate.query(query, genreRowMapper, filmId);
        log.info("Получен список жанров фильма с id - {}: {}", filmId, genre);
        return genre;
    }
}
