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
        return filmService.save(filmsParser.getFilms(page)).size();
    }

    @ShellMethod(key = "films-all-page")
    public Integer getFilms() {
        try {
            var films = filmsParser.getFilms();
            return filmService.save(films).size();
        }catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
