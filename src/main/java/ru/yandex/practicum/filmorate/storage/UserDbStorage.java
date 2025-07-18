package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

@Repository("UserDbStorage")
@Slf4j
@RequiredArgsConstructor
@Primary
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User addUser(User user) {
        validateUserName(user);
        String query = "INSERT INTO users (name, login, email, birthday)" +
                "VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(query, new String[]{"user_id"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, generatedKeyHolder);
        Number key = generatedKeyHolder.getKey();
        if (key == null) {
            log.error("Не удалось получить сгенерированный ID пользователя - {}", user);
            throw new IllegalStateException("Не удалось получить сгенерированный ID пользователя");
        }
        Long id = key.longValue();
        user.setId(id);
        log.debug("Пользователь - {} успешно добавлен", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        validateUserName(user);
        String query = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE user_id = ?";
        int update = jdbcTemplate.update(query, user.getName(), user.getLogin(), user.getEmail(), user.getBirthday(),
                user.getId());
        if (update == 0) {
            log.error("Не удалось обновить данные пользователя - {}", user);
            throw new IllegalStateException("Не удалось обновить данные пользователя");
        }
        log.debug("Данные пользователя - {} успешно обновлены", user);
        return findUserById(user.getId());
    }

    @Override
    public Collection<User> gettingUser() {
        String query = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(query, userRowMapper);
        log.debug("Список получен: {}", users);
        return users;
    }

    @Override
    public void addingAFriend(Long userId, Long userFriendId) {
        if (userId.equals(userFriendId)) {
            log.error("Пользователь пытается добавить себя в друзья");
            throw new IllegalArgumentException("Пользователь не может добавить себя в друзья");
        }
        String query1 = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.queryForObject(query1, Integer.class, userId, userFriendId);
        if (count > 0) {
            log.error("Запрос на дружбу от пользователя с ID - {} с пользователем ID - {} был ранее отправлен",
                    userId, userFriendId);
            throw new IllegalStateException("Ранее уже был отправлен запрос на дружбу");
        } else {
            String query = "INSERT INTO friendship (user_id, friend_id)" +
                    "VALUES (?, ?)";
            jdbcTemplate.update(query, userId, userFriendId);
            log.debug("Пользователь c id - {} успешно отправил запрос на дружбу пользователю c id - {}", userId, userFriendId);
        }
    }

    @Override
    public void unfriending(Long userId, Long userFriendId) {
        String query = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.update(query, userId, userFriendId);
        if (count == 0) {
            log.error("Пользователя с Id - {} нет в списке друзей пользователя - {}", userFriendId, userId);
        } else {
            log.debug("Пользователь c id - {} удалил из списка друзей пользователя c id - {}", userId, userFriendId);
        }
    }

    @Override
    public Collection<User> friendsList(Long userId) {
        String query = "SELECT u.* FROM users AS u JOIN friendship AS f ON u.user_id = f.friend_id WHERE f.user_id = ?";
        try {
            List<User> users = jdbcTemplate.query(query, userRowMapper, userId);
            log.debug("Список друзей пользователя с id - {} успешно получен: {}", userId, users);
            return users;
        } catch (EmptyResultDataAccessException e) {
            log.error("Не удалось получить список друзей пользователя с id - {}", userId);
            throw new NotFoundException("Не удалось получить список друзей" + e.getMessage());
        }
    }

    @Override
    public Collection<User> mutualFriendsList(Long userId, Long otherId) {
        String query = "SELECT u.* FROM users AS u " +
                "JOIN (" +
                "SELECT f1.friend_id FROM friendship AS f1 " +
                "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                " WHERE f1.user_id = ? AND f2.user_id = ?" +
                ") AS mf ON u.user_id = mf.friend_id";
        try {
            List<User> users = jdbcTemplate.query(query, userRowMapper, userId, otherId);
            log.debug("Список общих друзей успешно получен между пользователя с id - {} и id - {}: {}",
                    userId, otherId, users);
            return users;
        } catch (EmptyResultDataAccessException e) {
            log.error("Не удалось найти список общих друзей между пользователями с id - {} и id - {}", userId, otherId);
            throw new NotFoundException("Не удалось получить список общих друзей" + e.getMessage());
        }
    }

    @Override
    public User findUserById(Long userId) {
        log.info("Начинается поиск пользователя по id - {}", userId);
        String query = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(query, userRowMapper, userId);
            log.debug("Пользователь с id - {} успешно найден", userId);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id - {} не найден", userId);
            throw new NotFoundException("Пользователь с ID - " + userId + " не найден");
        }
    }

    private void validateUserName(User user) {
        log.info("Начинается валидация имени пользователя - {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя - {} пустое, используем его логин - {}, " +
                    "как имя", user, user.getLogin());
            user.setName(user.getLogin());
        }
        log.debug("Валидация имени пользователя - {} успешно пройдена", user);
    }

    @Override
    public void removeAllFriendships(Long userId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(sql, userId, userId);
    }

    @Override
    public void removeAllLikesByUser(Long userId) {
        String sql = "DELETE FROM film_likes WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void deleteUser(Long userId) {
        int count = jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", userId);
        if (count == 0) {
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден");
        }
    }
}
