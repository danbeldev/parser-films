package ru.mtc.parser.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.mtc.parser.parsers.FilmsParser;
import ru.mtc.parser.services.FilmService;

@ShellComponent
@RequiredArgsConstructor
public class ParserCommands {

    private final FilmsParser filmsParser;
    private final FilmService filmService;

    @ShellMethod(key = "films")
    public Integer getFilms(int page) {
        var films = filmsParser.getFilms(page);
        System.out.println(films);
        return filmService.save(films).size();
    }

    @ShellMethod(key = "films-all-page")
    public Integer getFilms() {
        var films = filmsParser.getFilms();
        System.out.println(films);
        return filmService.save(films).size();
    }
}
