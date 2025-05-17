package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> userMap = new HashMap<>();

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Получен запрос на добавление пользователя - {}", user);
        User user1 = validation(user);
        if (!(user1.getId() == null)) {
            log.error("Ошибка валидации пользователя - {}, при добавлении пользователя id указан", user);
            throw new ValidationException("При добавлении фильма id фильма не должен быть указан");
        }
        user1.setId(getNextId());
        userMap.put(user1.getId(), user1);
        log.info("Пользователь - {} успешно добавлен", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление данных пользователя - {}", user);
        Optional<User> user1 = Optional.ofNullable(userMap.get(user.getId()));
        if (user1.isPresent()) {
            userMap.put(user.getId(), user);
            log.info("Данные пользователя - {} успешно обновлены", user);
            return user;
        } else {
            log.error("Пользователь с указанным id - {} не найден", user.getId());
            throw new ValidationException("Пользователь с указанным id не найден");
        }
    }

    @GetMapping
    public Collection<User> gettingUser() {
        log.info("Получен запрос на получения списка всех пользователей");
        return new ArrayList<>(userMap.values());
    }

    private long getNextId() {
        long currentMaxId = userMap.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public User validation(User user) {
        log.info("Начинается валидация пользователя - {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации пользователя - {}: Электронная почта пустая либо отсутствует символ - @", user);
            throw new ValidationException("Электронная почта пользователя не должна быть пустой и должна содержать " +
                    "символ - @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации пользователя - {}: Логин пустой либо присутствуют пробелы", user);
            throw new ValidationException("Логин пользователя не должен быть пустым");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя - {} пустое, используем его логин - {}, " +
                    "как имя", user, user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Ошибка валидации пользователя - {}: Дата рождения указана в будущем времени", user);
            throw new ValidationException("Дата рождения пользователя не должна быть указана в будущем времени");
        }
        return user;
    }
}
