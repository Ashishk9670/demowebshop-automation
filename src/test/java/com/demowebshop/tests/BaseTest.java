package com.demowebshop.tests;

import com.demowebshop.config.ConfigReader;
import com.demowebshop.core.DriverFactory;
import com.demowebshop.core.DriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
    protected WebDriver driver;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);
        logger.info("Launching browser: {}", ConfigReader.get("browser", "chrome"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            logger.info("Closing browser session.");
            driver.quit();
        }
        DriverManager.unload();
    }
}

