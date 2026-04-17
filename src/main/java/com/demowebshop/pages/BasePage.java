package com.demowebshop.pages;

import com.demowebshop.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(ConfigReader.getLong("explicit.wait.seconds", 20))
        );
        PageFactory.initElements(driver, this);
    }

    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(WebElement element, String value) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(value);
    }

    protected String getText(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element)).getText().trim();
    }

    protected void selectByVisibleText(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        new Select(element).selectByVisibleText(text);
    }

    protected void selectByVisibleText(By locator, String text) {
        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator))).selectByVisibleText(text);
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (TimeoutException exception) {
            return false;
        }
    }

    protected boolean isPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    protected boolean isVisible(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (Exception exception) {
            return false;
        }
    }

    protected void waitForUrlContains(String partialUrl) {
        wait.until(ExpectedConditions.urlContains(partialUrl));
    }

    protected void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    protected void safeClick(By locator) {
        WebElement element = waitForClickable(locator);
        scrollIntoView(element);
        element.click();
    }

    protected void safeClick(WebElement element) {
        WebElement clickableElement = wait.until(ExpectedConditions.elementToBeClickable(element));
        scrollIntoView(clickableElement);
        clickableElement.click();
    }

    protected void fillInputIfVisible(By locator, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (isPresent(locator) && driver.findElement(locator).isDisplayed()) {
            WebElement element = waitForVisible(locator);
            element.clear();
            element.sendKeys(value);
        }
    }

    protected void clickFirstEnabled(By locator) {
        List<WebElement> elements = wait.until(driver -> {
            List<WebElement> currentElements = driver.findElements(locator);
            return currentElements.isEmpty() ? null : currentElements;
        });

        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed() && element.isSelected()) {
                    return;
                }
            } catch (StaleElementReferenceException ignored) {
                // Retry on the refreshed lookup below.
            }
        }

        for (int i = 0; i < elements.size(); i++) {
            try {
                WebElement element = driver.findElements(locator).get(i);
                if (element.isDisplayed() && element.isEnabled() && !element.isSelected()) {
                    scrollIntoView(element);
                    try {
                        element.click();
                    } catch (ElementClickInterceptedException exception) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    }
                    return;
                }
            } catch (StaleElementReferenceException ignored) {
                // Retry with a fresh lookup on the next iteration.
            }
        }

        List<WebElement> refreshedElements = driver.findElements(locator);
        if (!refreshedElements.isEmpty()) {
            WebElement firstElement = refreshedElements.get(0);
            if (firstElement.isDisplayed() && firstElement.isEnabled() && !firstElement.isSelected()) {
                scrollIntoView(firstElement);
                try {
                    firstElement.click();
                } catch (ElementClickInterceptedException exception) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstElement);
                }
            }
        }
    }

    protected void waitForDocumentReady() {
        wait.until(driver -> "complete".equals(
                ((JavascriptExecutor) driver).executeScript("return document.readyState")
        ));
    }

    protected boolean doesElementTextContain(By locator, String expectedText) {
        try {
            String actualText = waitForVisible(locator).getText();
            return actualText != null && actualText.contains(expectedText);
        } catch (StaleElementReferenceException exception) {
            return waitForVisible(locator).getText().contains(expectedText);
        }
    }
}
