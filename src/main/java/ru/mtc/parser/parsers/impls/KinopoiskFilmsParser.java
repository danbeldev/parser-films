package ru.mtc.parser.parsers.impls;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mtc.parser.entities.CountryEntity;
import ru.mtc.parser.entities.FilmEntity;
import ru.mtc.parser.entities.GenreEntity;
import ru.mtc.parser.entities.PersonEntity;
import ru.mtc.parser.parsers.FilmsParser;
import ru.mtc.parser.services.JsoupService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class KinopoiskFilmsParser implements FilmsParser {

    private static final String MAX_PAGE_ELEMENT_CLASSNAME = "styles_page__zbGy7";
    private static final String FILM_ITEM_ELEMENT_CLASSNAME = "styles_root__ti07r";
    private static final String FILM_POSTER_ELEMENT_CLASSNAME = "styles_poster__gJgwz styles_root__wgbNq";
    private static final String FILM_POSTER_URL_ELEMENT_CLASSNAME = "styles_image__gRXvn styles_mediumSizeType__fPzdD image styles_root__DZigd";
    private static final String FILM_NAME_ELEMENT_CLASSNAME = "base-movie-main-info_mainInfo__ZL_u3";
    private static final String FILM_ONLINE_BUTTON_ELEMENT_CLASSNAME = "styles_onlineButton__ER9Vt";
    private static final String FILM_LIST_INFO_ELEMENT_CLASSNAME = "desktop-list-main-info_additionalInfo__Hqzof";
    private static final String FILM_RATING_ELEMENT_CLASSNAME = "styles_kinopoiskValueNeutral__4c8gP";
    private static final String FILM_RATING_TOP_250_ELEMENT_CLASSNAME = "styles_kinopoiskValuePositive__7AAZG";
    private static final String FILM_RATING_COUNT_ELEMENT_CLASSNAME = "styles_kinopoiskCount__PT7ZX";
    private static final String FILM_YEAR_DURATION_INFO_ELEMENT_CLASSNAME = "desktop-list-main-info_secondaryText__M_aus";
    private static final String FILM_RELEASE_DATE_ELEMENT_CLASSNAME = "desktop-list-main-info_releaseDate__3RUfU";

    private final Pattern filmListInfoPattern = Pattern.compile("^(.*?) • (.*?)\\s+Режиссёр: (.+)$");
    private final Pattern filmYeadDuractionInfoPattern = Pattern.compile("(\\d{4}), (?:(\\d+) ч )?(?:(\\d+) мин)?");
    private final Pattern filReleaseDafePattern = Pattern.compile("\\b(\\d{4})\\b");
    private final Pattern horsPattern = Pattern.compile("(\\d+)\\s*ч");
    private final Pattern minutePattern = Pattern.compile("(\\d+)\\s*мин");

    @Value("${parser.kinopoisk.films.url}")
    private String filmsUrl;

    private final JsoupService jsoupService;

    @Override
    public List<FilmEntity> getFilms() {
        Document firstFilmsDocument = jsoupService.getDocumentByUrl(filmsUrl);

        if (firstFilmsDocument == null) {
            return Collections.emptyList();
        }

        return Stream.concat(
                        Stream.of(firstFilmsDocument),
                        IntStream.range(2, getMaxPage(firstFilmsDocument) + 1)
                                .mapToObj(this::getFilmsPageUrl)
                                .map(jsoupService::getDocumentByUrl)
                                .filter(Objects::nonNull)
                )
                .map(this::getFilms)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public List<FilmEntity> getFilms(int page) {
        Document filmsDocument = jsoupService.getDocumentByUrl(getFilmsPageUrl(page));
        if (filmsDocument == null) return List.of();
        return getFilms(filmsDocument);
    }

    private int getMaxPage(Document document) {
        Elements pages = document.getElementsByClass(MAX_PAGE_ELEMENT_CLASSNAME);
        if (pages.isEmpty() || pages.last() == null) {
            throw new IllegalStateException("Не удалось определить максимальную страницу.");
        }
        return Integer.parseInt(Objects.requireNonNull(pages.last()).text());
    }

    private String getFilmsPageUrl(int page) {
        return String.format("%s?page=%d", filmsUrl, page);
    }

    private Long extractFilmId(String url) {
        if (url == null) return null;
        try {
            return Long.parseLong(url.substring(url.lastIndexOf("/film/") + 6, url.lastIndexOf("/")));
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    private List<FilmEntity> getFilms(Document document) {
        Elements cardFilms = document.getElementsByClass(FILM_ITEM_ELEMENT_CLASSNAME);
        List<FilmEntity> films = new ArrayList<>();

        for (Element cardFilm : cardFilms) {
            FilmEntity film = new FilmEntity();
            film.setName(getName(cardFilm));
            film.setPosterUrl(getPosterUrl(cardFilm));
            film.setId(extractFilmId(getFilmUrl(cardFilm)));
            film.setRating(getRating(cardFilm));
            film.setRatingsCount(getRatingsCount(cardFilm));

            int[] yearAndDuration = getYearAndDuration(cardFilm);

            if (yearAndDuration[0] != 0) {
                film.setYear(yearAndDuration[0]);
            } else {
                film.setYear(getYearRelease(cardFilm));
            }

            if (yearAndDuration[1] != 0) {
                film.setDuration(yearAndDuration[1]);
            } else {
                film.setDuration(getDuration(cardFilm));
            }

            String[] countryAndGenreAndDirector = getCountryAndGenreAndDirector(cardFilm);
            if (countryAndGenreAndDirector != null) {
                film.setCountry(new CountryEntity(countryAndGenreAndDirector[0]));
                film.setGenre(new GenreEntity(countryAndGenreAndDirector[1]));
                film.setDirector(new PersonEntity(countryAndGenreAndDirector[2]));
            }

            film.setActors(getActors(cardFilm));
            film.setIsAvailableOnline(getIsAvailableOnline(cardFilm));

            films.add(film);
        }

        return films;
    }

    private String getName(Element element) {
        return element.getElementsByClass(FILM_NAME_ELEMENT_CLASSNAME).text();
    }

    private String getPosterUrl(Element element) {
        return "https:" + element.getElementsByClass(FILM_POSTER_URL_ELEMENT_CLASSNAME).attr("src");
    }

    private String getFilmUrl(Element element) {
        return element.getElementsByClass(FILM_POSTER_ELEMENT_CLASSNAME).attr("href");
    }

    private Float getRating(Element element) {
        try {
            String ratingStr = element.getElementsByClass(FILM_RATING_ELEMENT_CLASSNAME).text();
            if (ratingStr.isEmpty())
                ratingStr = element.getElementsByClass(FILM_RATING_TOP_250_ELEMENT_CLASSNAME).text();
            if (ratingStr.isEmpty()) return 0.0f;
            return Float.parseFloat(ratingStr);
        } catch (NullPointerException ignore) {
            return 0.0f;
        }
    }

    private Integer getRatingsCount(Element element) {
        try {
            String countStr = element.getElementsByClass(FILM_RATING_COUNT_ELEMENT_CLASSNAME).text();
            if (countStr.isEmpty()) return 0;
            return Integer.parseInt(countStr.replace(" ", "").replace("K", ""));
        } catch (NullPointerException ignore) {
            return 0;
        }
    }

    private int[] getYearAndDuration(Element element) {
        String input = element.getElementsByClass(FILM_YEAR_DURATION_INFO_ELEMENT_CLASSNAME).text();
        if (input.isEmpty()) return new int[]{0, 0};

        if (input.charAt(0) == ',') {
            input = input.substring(2);
        }

        Matcher matcher = filmYeadDuractionInfoPattern.matcher(input);

        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));

            int hours = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            int minutes = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

            int totalMinutes = hours * 60 + minutes;

            return new int[]{year, totalMinutes};
        }

        return new int[]{0, 0};
    }

    private Integer getYearRelease(Element element) {
        String input = element.getElementsByClass(FILM_RELEASE_DATE_ELEMENT_CLASSNAME).text();
        if (input.isEmpty()) return null;

        Matcher matcher = filReleaseDafePattern.matcher(input);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    private Integer getDuration(Element element) {
        String input = element.getElementsByClass(FILM_YEAR_DURATION_INFO_ELEMENT_CLASSNAME).text();

        if (input.isEmpty()) return null;

        if (input.charAt(0) == ',') {
            input = input.substring(2);
        }

        int totalMinutes = 0;

        Matcher hourMatcher = horsPattern.matcher(input);
        if (hourMatcher.find()) {
            int hours = Integer.parseInt(hourMatcher.group(1));
            totalMinutes += hours * 60;
        }

        Matcher minuteMatcher = minutePattern.matcher(input);
        if (minuteMatcher.find()) {
            int minutes = Integer.parseInt(minuteMatcher.group(1));
            totalMinutes += minutes;
        }

        return totalMinutes;
    }

    private String[] getCountryAndGenreAndDirector(Element element) {
        Elements el = element.getElementsByClass(FILM_LIST_INFO_ELEMENT_CLASSNAME);
        if (el.isEmpty()) return null;

        String input = el.getFirst().text();
        if (input.isEmpty()) return null;

        Matcher matcher = filmListInfoPattern.matcher(input);

        if (matcher.find()) {
            String country = matcher.group(1).trim();
            String genre = matcher.group(2).trim();
            String director = matcher.group(3).trim();

            return new String[]{country, genre, director};
        }

        return null;
    }

    private List<PersonEntity> getActors(Element element) {
        Elements elRoles = element.getElementsByClass(FILM_LIST_INFO_ELEMENT_CLASSNAME);
        if (elRoles.size() < 2) return List.of();
        return Arrays.stream(elRoles.get(1).text().substring(9).split(","))
                .map(PersonEntity::new).toList();
    }

    private Boolean getIsAvailableOnline(Element element) {
        return !element.getElementsByClass(FILM_ONLINE_BUTTON_ELEMENT_CLASSNAME).isEmpty();
    }
}
