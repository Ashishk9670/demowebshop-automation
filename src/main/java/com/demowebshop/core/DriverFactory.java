package com.demowebshop.core;

import com.demowebshop.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        String browser = ConfigReader.get("browser", "chrome").toLowerCase();
        boolean headless = Boolean.parseBoolean(System.getProperty(
                "headless",
                ConfigReader.get("headless", "false")
        ));
        long windowWidth = ConfigReader.getLong("window.width", 1440);
        long windowHeight = ConfigReader.getLong("window.height", 900);
        long pageLoadTimeout = ConfigReader.getLong("page.load.timeout.seconds", 60);

        WebDriver driver;
        switch (browser) {
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--remote-allow-origins=*");
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                driver = new EdgeDriver(edgeOptions);
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--start-maximized");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                    chromeOptions.addArguments("--window-size=" + windowWidth + "," + windowHeight);
                }
                driver = new ChromeDriver(chromeOptions);
                break;
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        if (!headless) {
            driver.manage().window().maximize();
        }
        return driver;
    }
}

