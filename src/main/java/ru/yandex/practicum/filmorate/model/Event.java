package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.interfaces.Marker;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private Long id;

    @NotNull(groups = Marker.OnCreate.class)
    private Long timestamp;

    @NotNull(groups = Marker.OnCreate.class)
    private Long userId;

    @NotBlank(groups = Marker.OnCreate.class)
    private String eventType;

    @NotBlank(groups = Marker.OnCreate.class)
    private String operation;

    private Long entityId;

    public Event(Long userId, String eventType, String operation, Long entityId) {
        this(0L, Instant.now().toEpochMilli(), userId, eventType, operation, entityId);
    }
}
