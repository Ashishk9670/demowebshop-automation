package com.demowebshop.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {
    }

    public static String capture(WebDriver driver, String testName) {
        Path screenshotDirectory = Path.of("test-output", "screenshots");
        String fileName = testName + "_" + LocalDateTime.now().format(FORMATTER) + ".png";
        Path targetPath = screenshotDirectory.resolve(fileName);

        try {
            Files.createDirectories(screenshotDirectory);
            Path sourcePath = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toAbsolutePath().toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save screenshot for test: " + testName, exception);
        }
    }
}
