package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.interfacedatabase.RatingStorage;

import java.util.Collection;
import java.util.List;

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

    public void getFilmsWithRatings(List<Film> films) {
        ratingStorage.getFilmsWithRatings(films);
    }
}
