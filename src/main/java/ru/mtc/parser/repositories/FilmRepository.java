package ru.mtc.parser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtc.parser.entities.FilmEntity;

public interface FilmRepository extends JpaRepository<FilmEntity, Long> {}
