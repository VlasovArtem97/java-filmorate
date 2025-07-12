package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.interfaces.Marker;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private Long reviewId;

    @NotNull
    private String content;

    //jackson при сериализации выводит positive (а нам нужно isPositive для тестов), пришлось добавить аннотацию
    @JsonProperty("isPositive")
    private Boolean isPositive;

    private Long userId;

    private Long filmId;

    private Long useful = 0L;
}
