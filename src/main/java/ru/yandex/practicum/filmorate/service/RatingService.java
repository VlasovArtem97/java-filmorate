package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.RatingStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingStorage ratingStorage;

    public RatingMpa getRatingById(Long id) {
        return ratingStorage.getRatingById(id);
    }

    public Collection<RatingMpa> getAllRatings() {
        return ratingStorage.getAllRatings();
    }
}
