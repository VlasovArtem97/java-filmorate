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

    public Collection<Event> getUserEvents(Long userId) {
        return eventStorage.getUserEvents(userId);
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

    /**
     * Удаляет все записи ленты событий, прямо или косвенно связанные с указанным пользователем.<br>
     * Удаляются следующие записи:<br>
     * <li> Записи действий данного пользователя;</li>
     * <li> Записи действий, связанные с добавлением в друзья или исключением из них данного пользователя;</li>
     * <b>Предполагается, что любые действия с отзывами может выполнять только автор этого отзыва</b>
     * @param userId ID пользователя
     */
    public void eraseUserReferencedEvents(Long userId) {
        eventStorage.eraseUserReferencedEvents(userId);
    }

    /**
     * Удаляет все записи ленты событий, прямо или косвенно связанные с указанным фильмом.<br>
     * Удаляются следующие записи:<br>
     * <li> Записи установки или удаления лайка к данному фильму;</li>
     * <b>Удаление записей действий с отзывами к данному фильму невозможно,
     * поскольку после удаления отзыва, запись об отзыве должна остаться в ленте</b>
     * @param filmId ID фильма
     */
    public void eraseFilmReferencedEvents(Long filmId) {
        eventStorage.eraseFilmReferencedEvents(filmId);
    }
}
