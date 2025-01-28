package ru.mtc.parser.parsers;

import ru.mtc.parser.entities.FilmEntity;

import java.util.List;

public interface FilmsParser {

    List<FilmEntity> getFilms(int page);

    List<FilmEntity> getFilms();
}
