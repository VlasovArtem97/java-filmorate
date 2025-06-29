package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    Collection<User> gettingUser();

    User findUserById(Long userID);

    void addingAFriend(Long userId, Long userFriendId);

    void unfriending(Long userId, Long userFriendId);

    Collection<User> friendsList(Long userId);

    Collection<User> mutualFriendsList(Long userId, Long otherId);
}
