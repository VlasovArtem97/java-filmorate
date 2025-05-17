package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController();
    }

    @Test
    void validation_shouldThrowException_whenNameIsNull() {
        Film film = new Film(); // Предполагаем, что все поля по умолчанию равны null.
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(90L); // Предполагаем, что все поля по умолчанию равны null.


        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.validation(film));
        assertEquals("Название фильма не должно быть пустым", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenNameIsBlank() {
        Film film = new Film();
        film.setName(""); // Пустое название
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(90L);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.validation(film));
        assertEquals("Название фильма не должно быть пустым", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("D".repeat(201)); // Описание больше 200 символов
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(90L);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.validation(film));
        assertEquals("Описание фильма не должно превышать 200 символов", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenReleaseDateIsBefore1895() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // До 28 декабря 1895
        film.setDuration(90L);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.validation(film));
        assertEquals("Дата релиза фильма должна быть позже данной даты - 28.12.1895", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenDurationIsZero() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(0L); // Продолжительность 0

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.validation(film));
        assertEquals("Продолжительность фильма не должна быть отрицательным числом", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(-10L); // Отрицательная продолжительность

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.validation(film));
        assertEquals("Продолжительность фильма не должна быть отрицательным числом", exception.getMessage());
    }

    @Test
    void validation_shouldCompleteSuccessfully_whenFilmIsValid() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(90L); // Корректная продолжительность

        assertDoesNotThrow(() -> filmController.validation(film));
    }
}