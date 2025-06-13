package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User gettingAUserById(@Positive @PathVariable("id") Long userId) {
        return userService.gettingAUserById(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addingAFriend(@Positive @PathVariable("id") Long userId, @Positive @PathVariable Long friendId) {
        userService.addingAFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void unfriendingById(@Positive @PathVariable("id") Long userId, @Positive @PathVariable Long friendId) {
        userService.unfriending(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> friendList(@Positive @PathVariable("id") Long userId) {
        return userService.friendsList(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> mutualFriendsList(@Positive @PathVariable("id") Long userId,
                                              @Positive @PathVariable Long otherId) {
        return userService.mutualFriendsList(userId, otherId);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    @Validated(Marker.OnUpdate.class)
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    public Collection<User> gettingUser() {
        return userService.gettingUser();
    }
}
