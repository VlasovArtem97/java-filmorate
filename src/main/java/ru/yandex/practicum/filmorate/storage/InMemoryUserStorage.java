package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> userMap = new HashMap<>();

    @Override
    public User addUser(User user) {
        log.info("Получен запрос на добавление пользователя - {}", user);
        validateUserName(user);
        user.setId(getNextId());
        userMap.put(user.getId(), user);
        log.info("Пользователь - {} успешно добавлен", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Получен запрос на обновление данных пользователя - {}", user);
        User user1 = findUserById(user.getId());
        validateUserName(user);
        userMap.put(user1.getId(), user);
        log.info("Данные пользователя - {} успешно обновлены", user);
        return user;
    }

    @Override
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
        log.info("Начинается валидация имени пользователя - {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя - {} пустое, используем его логин - {}, " +
                    "как имя", user, user.getLogin());
            user.setName(user.getLogin());
        }
        log.info("Валидация имени пользователя - {} успешно пройдена", user);
    }

    @Override
    public User findUserById(Long userId) {
        log.info("Начинается поиск пользователя по id - {}", userId);
        Optional<User> user = Optional.ofNullable(userMap.get(userId));
        if (user.isPresent()) {
            log.info("Пользователь с id - {} успешно найден", userId);
            return user.get();
        } else {
            log.error("Пользователь с указанным id - {} не найден", userId);
            throw new NotFoundException("Пользователь с ID - " + userId + " не найден");
        }
    }
}
