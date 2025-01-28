package ru.mtc.parser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtc.parser.entities.CountryEntity;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<CountryEntity, Long> {

    Optional<CountryEntity> findByName(String name);
}
