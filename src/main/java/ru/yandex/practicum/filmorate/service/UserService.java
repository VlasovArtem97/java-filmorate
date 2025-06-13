package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addingAFriend(Long userId, Long userFriendId) {
        log.info("Получен запрос на добавление в список друзей от пользователя c id - {} с " +
                "пользователем c id - {}", userId, userFriendId);
        User user1 = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(userFriendId);

        user1.getFriends().add(userFriendId);
        friend.getFriends().add(userId);
        log.info("Пользователь c id - {} и пользователь c id - {} добавлены друг другу в список друзей", userId, userFriendId);
    }

    public void unfriending(Long userId, Long userFriendId) {
        log.info("Получен запрос на удаление из списка друзей от пользователя c id - {} с " +
                "пользователем c id - {}", userId, userFriendId);
        User user1 = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(userFriendId);

        user1.getFriends().remove(userFriendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь c id - {} и пользователь c id - {} удалены у друг друга из списка" +
                " друзей", userId, userFriendId);
    }

    public Collection<User> friendsList(Long userId) {
        log.info("Получен запрос на получения списка друзей пользователя с id - {}", userId);
        User user1 = userStorage.findUserById(userId);

        Set<Long> friendsId = user1.getFriends();
        Collection<User> usersList = userStorage.gettingUser();

        return usersList.stream()
                .filter(user -> friendsId.contains(user.getId()))
                .collect(Collectors.toList());
    }

    public User gettingAUserById(Long userId) {
        return userStorage.findUserById(userId);
    }

    public Collection<User> mutualFriendsList(Long userId, Long otherId) {
        log.info("Получен запрос на получение списка общих друзей пользователя с id - {}" +
                " и пользователя с id - {}", userId, otherId);
        User user1 = userStorage.findUserById(userId);
        User user2 = userStorage.findUserById(otherId);

        Set<Long> users1 = user1.getFriends();
        Set<Long> users2 = user2.getFriends();

        Set<Long> commonFriends = new HashSet<>(users1);
        commonFriends.retainAll(users2);

        return userStorage.gettingUser().stream()
                .filter(user -> commonFriends.contains(user.getId()))
                .toList();
    }


    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public Collection<User> gettingUser() {
        return userStorage.gettingUser();
    }
}
