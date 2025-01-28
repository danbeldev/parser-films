package ru.mtc.parser.parsers;

import ru.mtc.parser.entities.FilmEntity;

public interface FilmDetailsParser {

    FilmEntity getFilm(long id);
}
