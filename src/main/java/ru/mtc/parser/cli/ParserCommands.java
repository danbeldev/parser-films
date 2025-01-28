package ru.mtc.parser.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.mtc.parser.entities.FilmEntity;
import ru.mtc.parser.parsers.FilmsParser;
import ru.mtc.parser.services.FilmService;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class ParserCommands {

    private final FilmsParser filmsParser;
    private final FilmService filmService;

    @ShellMethod(key = "films")
    public List<FilmEntity> getFilms(int page) {
        return filmService.save(filmsParser.getFilms(page));
    }

    @ShellMethod(key = "films-all-page")
    public List<FilmEntity> getFilms() {
        var films = filmsParser.getFilms();
        return filmService.save(films);
    }
}
