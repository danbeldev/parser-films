package ru.mtc.parser.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtc.parser.entities.GenreEntity;
import ru.mtc.parser.repositories.GenreRepository;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional
    public GenreEntity findOrCreate(String name) {
        return genreRepository.findByName(name).orElseGet(() -> {
            var genre = new GenreEntity();
            genre.setName(name);
            return genreRepository.save(genre);
        });
    }
}
