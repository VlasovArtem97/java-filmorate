package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.interfaces.Marker;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.DataCleanupService;

import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FilmService filmService;
    private final DataCleanupService cleanupService;

    @GetMapping("/{id}")
    public User gettingAUserById(@Positive @PathVariable("id") Long userId) {
        return userService.gettingAUserById(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addingAFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
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

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@Positive @PathVariable("id") Long userId) {
        return filmService.getRecommendations(userId);
    }

    @GetMapping("/{id}/feed")
    public Collection<Event> getUserEvents(@Positive @PathVariable("id") Long userId) {
        return userService.getUserEvents(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@Positive @PathVariable("id") Long userId) {
        cleanupService.deleteUserCompletely(userId);
    }
}
