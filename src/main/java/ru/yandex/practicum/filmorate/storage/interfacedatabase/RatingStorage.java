package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.Collection;
import java.util.List;

public interface RatingStorage {

    Collection<RatingMpa> getAllRatings();

    RatingMpa getRatingById(Long id);

    void getFilmsWithRatings(List<Film> films);
}
