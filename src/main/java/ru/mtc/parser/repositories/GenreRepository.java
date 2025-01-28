package ru.mtc.parser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtc.parser.entities.GenreEntity;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<GenreEntity, Long> {

    Optional<GenreEntity> findByName(String name);
}
