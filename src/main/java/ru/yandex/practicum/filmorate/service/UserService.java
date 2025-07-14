package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final ReviewService reviewService;

    public void addingAFriend(Long userId, Long userFriendId) {
        log.info("Получен запрос на добавление в список друзей от пользователя c id - {} с " +
                "пользователем c id - {}", userId, userFriendId);
        gettingAUserById(userId);
        gettingAUserById(userFriendId);
        userStorage.addingAFriend(userId, userFriendId);
    }

    public void unfriending(Long userId, Long userFriendId) {
        log.info("Получен запрос от пользователя c id - {} на удаление из списка друзей пользователя c id - {}",
                userId, userFriendId);
        gettingAUserById(userId);
        gettingAUserById(userFriendId);
        userStorage.unfriending(userId, userFriendId);
    }

    public Collection<User> friendsList(Long userId) {
        log.info("Получен запрос на получения списка друзей пользователя с id - {}", userId);
        gettingAUserById(userId);
        return userStorage.friendsList(userId);
    }

    public User gettingAUserById(Long userId) {
        return userStorage.findUserById(userId);
    }

    public Collection<User> mutualFriendsList(Long userId, Long otherId) {
        log.info("Получен запрос на получение списка общих друзей пользователя с id - {}" +
                " и пользователя с id - {}", userId, otherId);
        gettingAUserById(userId);
        gettingAUserById(otherId);
        return userStorage.mutualFriendsList(userId, otherId);
    }

    public User addUser(User user) {
        log.info("Получен запрос на добавление пользователя - {}", user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.info("Получен запрос на обновление данных пользователя - {}", user);
        gettingAUserById(user.getId());
        return userStorage.updateUser(user);
    }

    public Collection<User> gettingUser() {
        log.info("Получен запрос на получения списка пользователей");
        return userStorage.gettingUser();
    }

    public void deleteUser(Long userId) {
        log.info("Запрос на удаление пользователя с id={}", userId);
        // предварительно очистить все зависимости
        userStorage.removeAllFriendships(userId);
        userStorage.removeAllLikesByUser(userId);
        // удалить все лайки/дизлайки к отзывам этого пользователя
        reviewService.deleteReviewRatingsByUser(userId);
        // удалить все сами отзывы, которые он оставил
        reviewService.deleteReviewsByUser(userId);
        userStorage.deleteUser(userId);
    }
}
