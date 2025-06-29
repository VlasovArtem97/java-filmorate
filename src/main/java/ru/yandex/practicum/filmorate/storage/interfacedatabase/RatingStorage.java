package ru.yandex.practicum.filmorate.storage.interfacedatabase;

import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.Collection;

public interface RatingStorage {

    Collection<RatingMpa> getAllRatings();

    RatingMpa getRatingById(Long id);
}
