package ru.mtc.parser.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JsoupService {

    private final WebDriver driver;

    public Document getDocumentByUrl(String url) {
        return getDocumentByUrl(url, false);
    }

    @SneakyThrows
    public Document getDocumentByUrl(String url, boolean manuallyCaptcha) {
        try {
            driver.get(url);

            if (isCaptchaPage()) {
                System.out.println("CAPTCHA обнаружена");
                if (!handleCaptcha(() -> driver.get(url), manuallyCaptcha)) {
                    return null;
                }
            }

            Map<String, String> cookies = driver.manage().getCookies()
                    .stream()
                    .collect(Collectors.toMap(org.openqa.selenium.Cookie::getName, org.openqa.selenium.Cookie::getValue));

            return Jsoup.connect(url)
                    .cookies(cookies)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:114.0) Gecko/20100101 Firefox/114.0")
                    .get();
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке страницы: " + e.getMessage());
            throw e;
        }
    }

    @SneakyThrows
    private boolean handleCaptcha(Runnable refresh, boolean manuallyCaptcha) {
        int maxAttempts = 1;
        int attempts = 0;
        int waitTimeMillis = 5_000;

        while (attempts < maxAttempts) {
            solveCaptcha();
            System.out.println("Попытка #" + (attempts + 1));

            Thread.sleep(waitTimeMillis);

            if (!isCaptchaPage()) {
                System.out.println("CAPTCHA решена!");
                return true;
            }

            refresh.run();

            if (!isCaptchaPage()) {
                System.out.println("CAPTCHA решена!");
                return true;
            }

            attempts++;
        }

        if (manuallyCaptcha) {
            System.out.println("Решите CAPTCHA и нажмите Enter...");
            new java.util.Scanner(System.in).nextLine(); // Ожидаем, пока пользователь решит CAPTCHA вручную
            return true;
        }

        return false;
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
