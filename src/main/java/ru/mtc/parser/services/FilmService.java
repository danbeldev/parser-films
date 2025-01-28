package ru.mtc.parser.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtc.parser.entities.FilmEntity;
import ru.mtc.parser.repositories.FilmRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;

    private final CountryService countryService;
    private final GenreService genreService;
    private final PersonService personService;

    @Transactional
    public List<FilmEntity> save(List<FilmEntity> films) {
        return filmRepository.saveAll(
                films.stream()
                        .filter(film -> !filmRepository.existsById(film.getId()))
                        .map(this::processAndSaveFilm)
                        .toList()
        );
    }

    private FilmEntity processAndSaveFilm(FilmEntity film) {
        if (film.getDirector() != null) {
            film.setDirector(personService.findOrCreate(
                    film.getDirector().getFirstName(),
                    film.getDirector().getLastName()
            ));
        }

        if (film.getGenre() != null) {
            film.setGenre(genreService.findOrCreate(film.getGenre().getName()));
        }

        if (film.getCountry() != null) {
            film.setCountry(countryService.findOrCreate(film.getCountry().getName()));
        }

        if (film.getActors() != null) {
            film.setActors(
                    film.getActors().stream()
                            .filter(actor -> actor.getFirstName() != null && actor.getLastName() != null)
                            .map(actor -> personService.findOrCreate(actor.getFirstName(), actor.getLastName()))
                            .toList()
            );
        }
        return film;
    }
}
