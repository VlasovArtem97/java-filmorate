package ru.yandex.practicum.filmorate.storage.olddatabase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.UserStorage;

import java.util.*;

@Deprecated
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

    @Override
    public void addingAFriend(Long userId, Long userFriendId) {

    }

    @Override
    public void unfriending(Long userId, Long userFriendId) {

    }

    @Override
    public Collection<User> friendsList(Long userId) {
        return List.of();
    }

    @Override
    public Collection<User> mutualFriendsList(Long userId, Long otherId) {
        return List.of();
    }

    @Override
    public void removeAllFriendships(Long userId) {
        // 1) Убираем всех друзей у самого пользователя
        User user = userMap.get(userId);
        if (user != null) {
            user.getFriends().clear();
        }
        // 2) Убираем userId из списка друзей всех остальных пользователей
        for (User other : userMap.values()) {
            other.getFriends().remove(userId);
        }
    }

    @Override
    public void removeAllLikesByUser(Long userId) {
        // В InMemoryUserStorage лайки не хранятся — оставляем пустым,
        // их хранит InMemoryFilmStorage
    }

    @Override
    public void deleteUser(Long userId) {
        // Сначала удаляем все «дружбы»
        removeAllFriendships(userId);
        // (если нужно — удалить и лайки, но здесь метод пустой)
        removeAllLikesByUser(userId);

        // Наконец удаляем самого пользователя
        User removed = userMap.remove(userId);
        if (removed == null) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }
    }

}
