INSERT INTO genres (name)
    SELECT 'Комедия'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name='Комедия');
INSERT INTO genres (name)
    SELECT 'Драма'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name='Драма');
INSERT INTO genres (name)
    SELECT 'Мультфильм'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name='Мультфильм');
INSERT INTO genres (name)
    SELECT 'Триллер'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name='Триллер');
INSERT INTO genres (name)
    SELECT 'Документальный'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name='Документальный');
INSERT INTO genres (name)
    SELECT 'Боевик'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name='Боевик');

INSERT INTO ratings_mpa (name, description)
    SELECT 'G', 'у фильма нет возрастных ограничений'
    WHERE NOT EXISTS (SELECT 1 FROM ratings_mpa WHERE name='G');
INSERT INTO ratings_mpa (name, description)
    SELECT 'PG', 'детям рекомендуется смотреть фильм с родителями'
    WHERE NOT EXISTS (SELECT 1 FROM ratings_mpa WHERE name='PG');
INSERT INTO ratings_mpa (name, description)
    SELECT 'PG-13', 'детям до 13 лет просмотр не желателен'
    WHERE NOT EXISTS (SELECT 1 FROM ratings_mpa WHERE name='PG-13');
INSERT INTO ratings_mpa (name, description)
    SELECT 'R', 'лицам до 17 лет просматривать фильм можно только в присутствии взрослого'
    WHERE NOT EXISTS (SELECT 1 FROM ratings_mpa WHERE name='R');
INSERT INTO ratings_mpa (name, description)
    SELECT 'NC-17', 'лицам до 18 лет просмотр запрещён'
    WHERE NOT EXISTS (SELECT 1 FROM ratings_mpa WHERE name='NC-17');