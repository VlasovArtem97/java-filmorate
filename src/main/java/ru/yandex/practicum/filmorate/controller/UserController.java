package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> userMap = new HashMap<>();

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public User addUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя - {}", user);
        validateUserName(user);
        user.setId(getNextId());
        userMap.put(user.getId(), user);
        log.info("Пользователь - {} успешно добавлен", user);
        return user;
    }

    @PutMapping
    @Validated(Marker.OnUpdate.class)
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление данных пользователя - {}", user);
        Optional<User> user1 = Optional.ofNullable(userMap.get(user.getId()));
        if (user1.isPresent()) {
            validateUserName(user);
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

    public void validateUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя - {} пустое, используем его логин - {}, " +
                    "как имя", user, user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
