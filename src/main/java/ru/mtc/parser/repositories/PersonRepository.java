package ru.mtc.parser.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtc.parser.entities.PersonEntity;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    Optional<PersonEntity> findByFirstNameAndLastName(String firstName, String lastName);
}
