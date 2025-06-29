package ru.yandex.practicum.filmorate.storage.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.RatingStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final GenreStorage genreStorage;
    private final RatingStorage ratingStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getLong("duration"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setMpa(ratingStorage.getRatingById(rs.getLong("rating_id")));
        film.setGenres(new LinkedHashSet<>(genreStorage.getAListOfGenres(film.getId())));
        return film;
    }
}
