package ru.mtc.parser.parsers.impls;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mtc.parser.entities.FilmEntity;
import ru.mtc.parser.parsers.FilmDetailsParser;
import ru.mtc.parser.services.JsoupService;

@Service
@RequiredArgsConstructor
public class KinopoiskFilmDetailsParser implements FilmDetailsParser {

    private final JsoupService jsoupService;

    @Value("${parser.kinopoisk.film-details.base-url}")
    private String filmBaseUrl;

    @Override
    public FilmEntity getFilm(long id) {
        Document document = jsoupService.getDocumentByUrl(getFilmUrl(id), true);
        if (document == null) return null;

        var film = new FilmEntity();
        film.setName(getName(document));
        film.setRating(getRating(document));
        film.setPosterUrl(getPosterUrl(document));

        details(document);

        return film;
    }

    private String getName(Document document) {
        Elements elements = document.getElementsByClass("styles_title___itJ6");
        if (elements.isEmpty()) return null;

        String elementText = elements.getFirst().text();
        int index = elementText.indexOf("(");

        if (index > 0) {
            return elementText.substring(0, index).trim();
        } else {
            return elementText;
        }
    }

    private Float getRating(Document document) {
        Elements elements = document.getElementsByClass("styles_ratingKpTop__84afd");
        if (elements.isEmpty()) return null;
        try {
            return Float.parseFloat(elements.getFirst().text());
        }catch (NumberFormatException ignore) {
            return null;
        }
    }

    private String getPosterUrl(Document document) {
        Elements elements = document.getElementsByClass("film-poster");
        if (elements.isEmpty()) return null;
        return "https:" + elements.getFirst().attr("src");
    }

    private String getDescription(Document document) {
        Elements elements = document.getElementsByClass("styles_paragraph__wEGPz");
        if (elements.isEmpty()) return null;
        return elements.getFirst().text();
    }

    private void details(Document document) {
        Elements elements = document.getElementsByClass("styles_rowDark__ucbcz");
        for (Element element : elements) {
            String key = element.getElementsByClass("styles_titleDark___tfMR").getFirst().text();
            String value = element.getElementsByClass("styles_valueDark__BCk93").getFirst().text();
            System.out.println(key + "=" + value);
        }
    }

    private String getFilmUrl(long id) {
        return filmBaseUrl + id;
    }
}
