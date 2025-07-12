package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.EventStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;
    private final UserService userService;

    public Collection<Event> getUserEvents(Long userId) {
        log.info("Запрос на ленту событий пользователя {}", userId);
        userService.gettingAUserById(userId);
        Collection<Event> userEvents = eventStorage.getUserEvents(userId);
        log.info("Запрос на ленту событий пользователя {} вернул список длиной {}", userId, userEvents.size());
        return userEvents;
    }

    public void addUserSetLikeEvent(Long userId, Long filmId) {
        eventStorage.addUserSetLikeEvent(userId, filmId);
    }

    public void addUserRemoveLikeEvent(Long userId, Long filmId) {
        eventStorage.addUserRemoveLikeEvent(userId, filmId);
    }

    public void addUserAddFriendEvent(Long userId, Long friendId) {
        eventStorage.addUserAddFriendEvent(userId, friendId);
    }

    public void addUserRemoveFriendEvent(Long userId, Long friendId) {
        eventStorage.addUserRemoveFriendEvent(userId, friendId);
    }

    public void addUserAddReviewEvent(Long userId, Long reviewId) {
        eventStorage.addUserAddReviewEvent(userId, reviewId);
    }

    public void addUserUpdateReviewEvent(Long userId, Long reviewId) {
        eventStorage.addUserUpdateReviewEvent(userId, reviewId);
    }

    public void addUserRemoveReviewEvent(Long userId, Long reviewId) {
        eventStorage.addUserRemoveReviewEvent(userId, reviewId);
    }
}
