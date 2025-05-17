package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    public void setUp() {
        userController = new UserController();
    }

    @Test
    void validation_shouldThrowException_whenEmailIsNull() {
        User user = new User();
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Электронная почта пользователя не должна быть пустой и должна содержать символ - @",
                exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenEmailIsBlank() {
        User user = new User();
        user.setEmail(""); // Пустая электронная почта
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Электронная почта пользователя не должна быть пустой и должна содержать символ - @",
                exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenEmailDoesNotContainAt() {
        User user = new User();
        user.setEmail("userexample.com"); // Электронная почта без символа '@'
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Электронная почта пользователя не должна быть пустой и должна содержать символ - @",
                exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenLoginIsNull() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Логин пользователя не должен быть пустым", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenLoginIsBlank() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(""); // Пустой логин
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Логин пользователя не должен быть пустым", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user Login"); // Логин с пробелами
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Логин пользователя не должен быть пустым", exception.getMessage());
    }

    @Test
    void validation_shouldThrowException_whenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата рождения в будущем

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validation(user));
        assertEquals("Дата рождения пользователя не должна быть указана в будущем времени",
                exception.getMessage());
    }

    @Test
    void validation_shouldUseLoginAsName_whenNameIsNullOrBlank() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setName(null); // Имя не указано
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User validatedUser = userController.validation(user);

        assertEquals("userLogin", validatedUser.getName()); // Имя должно быть заменено на логин
    }

    @Test
    void validation_shouldCompleteSuccessfully_whenUserIsValid() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> userController.validation(user));
    }

    @Test
    void addUser_shouldAddUserSuccessfully() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User addedUser = userController.addUser(user);
        assertNotNull(addedUser.getId());
        assertEquals("user@example.com", addedUser.getEmail());
        assertEquals("userLogin", addedUser.getLogin());
        assertEquals(LocalDate.of(2000, 1, 1), addedUser.getBirthday());
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        User addedUser = userController.addUser(user);

        addedUser.setEmail("updatedUser@example.com");
        User updatedUser = userController.updateUser(addedUser);

        assertEquals("updatedUser@example.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        User user = new User();
        user.setId(999L); // Используем несуществующий ID
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.updateUser(user));
        assertEquals("Пользователь с указанным id не найден", exception.getMessage());
    }
}