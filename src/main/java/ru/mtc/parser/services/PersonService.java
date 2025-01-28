package ru.mtc.parser.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtc.parser.entities.CountryEntity;
import ru.mtc.parser.entities.PersonEntity;
import ru.mtc.parser.repositories.PersonRepository;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    @Transactional
    public PersonEntity findOrCreate(String firstName, String lastName) {
        return personRepository.findByFirstNameAndLastName(firstName, lastName).orElseGet(() -> {
            var person = new PersonEntity();
            person.setFirstName(firstName);
            person.setLastName(lastName);
            return personRepository.save(person);
        });
    }
}
