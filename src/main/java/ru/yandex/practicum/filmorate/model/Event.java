package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.interfaces.Marker;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    public enum Type {
        LIKE, FRIEND, REVIEW
    }

    public enum Operation {
        ADD, UPDATE, REMOVE
    }

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private Long eventId;

    @NotNull(groups = Marker.OnCreate.class)
    private Long timestamp;

    @NotNull(groups = Marker.OnCreate.class)
    private Long userId;

    @NotNull(groups = Marker.OnCreate.class)
    private Type eventType;

    @NotNull(groups = Marker.OnCreate.class)
    private Operation operation;

    private Long entityId;

    public Event(Long userId, Type eventType, Operation operation, Long entityId) {
        this(0L, Timestamp.from(Instant.now()).getTime(), userId, eventType, operation, entityId);
    }
}
