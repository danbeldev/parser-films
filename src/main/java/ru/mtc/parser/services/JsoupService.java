package ru.mtc.parser.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class JsoupService {

    private final WebDriver driver;

    @SneakyThrows
    public Document getDocumentByUrl(String url) {
        try {
            driver.get(url);

            if (isCaptchaPage()) {
                System.out.println("CAPTCHA обнаружена");
                handleCaptcha(() -> driver.get(url));
            }

            String pageHtml = driver.getPageSource();

            return pageHtml != null ? Jsoup.parse(pageHtml) : null;
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке страницы: " + e.getMessage());
            throw e;
        }
    }

    @SneakyThrows
    private void handleCaptcha(Runnable refresh) {
        int maxAttempts = 1;
        int attempts = 0;
        int waitTimeMillis = 5_000;

        while (attempts < maxAttempts) {
            solveCaptcha();
            System.out.println("Попытка #" + (attempts + 1));

            Thread.sleep(waitTimeMillis);

            if (!isCaptchaPage()) {
                System.out.println("CAPTCHA решена!");
                return;
            }

            refresh.run();

            if (!isCaptchaPage()) {
                System.out.println("CAPTCHA решена!");
                return;
            }

            attempts++;
        }

        System.out.println("Решите CAPTCHA и нажмите Enter...");
        new java.util.Scanner(System.in).nextLine();
    }

    private void solveCaptcha() {
        try {
            WebElement captchaButton = driver.findElement(By.id("js-button"));
            if (captchaButton.isDisplayed()) {
                System.out.println("CAPTCHA обнаружена. Нажимаем на кнопку...");
                captchaButton.click();
            }
        } catch (NoSuchElementException e) {
            System.out.println("CAPTCHA не обнаружена.");
        }
    }

    @SneakyThrows
    private boolean isCaptchaPage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl != null && currentUrl.contains("https://www.kinopoisk.ru/showcaptcha");
    }
}
