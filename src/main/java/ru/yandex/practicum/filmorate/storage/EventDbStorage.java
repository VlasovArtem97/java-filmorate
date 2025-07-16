package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.EventStorage;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.sql.Timestamp;
import java.util.Collection;

@Repository("EventDbStorage")
@Slf4j
@RequiredArgsConstructor
@Primary
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    @Override
    public Collection<Event> getUserEvents(Long userId) {
        final String sql = """
                SELECT * FROM events WHERE user_id = ? ORDER BY timestamp;
                """;
        try {
            return jdbcTemplate.query(sql, eventRowMapper, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка получения событий пользователя {}", userId);
            throw new IllegalStateException(e);
        }
    }

    private void addEvent(Event event) {
        final String sql = """
                INSERT INTO events (timestamp, user_id, event_type, operation, entity_id)
                VALUES (?, ?, ?, ?, ?);
                """;
        jdbcTemplate.update(sql, new Timestamp(event.getTimestamp()), event.getUserId(),
                event.getEventType().name(), event.getOperation().name(), event.getEntityId());
        log.info("Зафиксировано действие пользователя {}", event);
    }

    @Override
    public void addUserSetLikeEvent(Long userId, Long filmId) {
        addEvent(new Event(userId, Event.Type.LIKE, Event.Operation.ADD, filmId));
    }

    @Override
    public void addUserRemoveLikeEvent(Long userId, Long filmId) {
        addEvent(new Event(userId, Event.Type.LIKE, Event.Operation.REMOVE, filmId));
    }

    @Override
    public void addUserAddFriendEvent(Long userId, Long friendId) {
        addEvent(new Event(userId, Event.Type.FRIEND, Event.Operation.ADD, friendId));
    }

    @Override
    public void addUserRemoveFriendEvent(Long userId, Long friendId) {
        addEvent(new Event(userId, Event.Type.FRIEND, Event.Operation.REMOVE, friendId));
    }

    @Override
    public void addUserAddReviewEvent(Long userId, Long reviewId) {
        addEvent(new Event(userId, Event.Type.REVIEW, Event.Operation.ADD, reviewId));
    }

    @Override
    public void addUserUpdateReviewEvent(Long userId, Long reviewId) {
        addEvent(new Event(userId, Event.Type.REVIEW, Event.Operation.UPDATE, reviewId));
    }

    @Override
    public void addUserRemoveReviewEvent(Long userId, Long reviewId) {
        addEvent(new Event(userId, Event.Type.REVIEW, Event.Operation.REMOVE, reviewId));
    }

    @Override
    public void eraseUserReferencedEvents(Long userId) {
        final String sql = """
                DELETE FROM events
                WHERE user_id = ?;
                """;
        int n = jdbcTemplate.update(sql, userId);
        log.info("Удалено {} записей ленты событий, связанных с пользователем {}", n, userId);
    }

    @Override
    public void eraseFilmReferencedEvents(Long filmId) {
        final String sql = """
                DELETE FROM events
                WHERE (event_type = 'LIKE' AND entity_id = ?);
                """;
        int n = jdbcTemplate.update(sql, filmId);
        log.info("Удалено {} записей ленты событий, связанных с фильмом {}", n, filmId);
    }
}
