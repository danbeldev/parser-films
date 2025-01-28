package ru.mtc.parser.configs;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    @Value("${webdriver.chrome.driver}")
    private String chromeDriverPath;

    private WebDriver driver;

    @PostConstruct
    public void init() {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        driver = new ChromeDriver();
        System.out.println("WebDriver создан и инициализирован.");
    }

    @Bean
    public WebDriver webDriver() {
        return driver;
    }

    @PreDestroy
    public void cleanUp() {
        if (driver != null) {
            driver.quit();
            System.out.println("WebDriver закрыт.");
        }
    }
}
