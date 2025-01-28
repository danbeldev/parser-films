package ru.mtc.parser.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtc.parser.entities.CountryEntity;
import ru.mtc.parser.repositories.CountryRepository;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    @Transactional
    public CountryEntity findOrCreate(String name) {
        return countryRepository.findByName(name).orElseGet(() -> {
            var country = new CountryEntity();
            country.setName(name);
            return countryRepository.save(country);
        });
    }
}
