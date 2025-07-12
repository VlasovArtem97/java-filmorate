package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface EventStorage {

    Collection<Event> getUserEvents(Long userId);

    void addUserSetLikeEvent(Long userId, Long filmId);

    void addUserRemoveLikeEvent(Long userId, Long filmId);

    void addUserAddReviewEvent(Long userId, Long reviewId);

    void addUserUpdateReviewEvent(Long userId, Long reviewId);

    void addUserRemoveReviewEvent(Long userId, Long reviewId);

    void addUserAddFriendEvent(Long userId, Long friend);

    void addUserRemoveFriendEvent(Long userId, Long friend);

    void eraseUserReferencedEvents(Long userId);

    void eraseFilmReferencedEvents(Long filmId);
}
