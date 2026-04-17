package com.demowebshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProductDetailsPage extends BasePage {
    private final By productTitle = By.cssSelector("div.product-name h1");
    private final By successNotification = By.cssSelector("div.bar-notification.success");
    private final By closeNotificationButton = By.cssSelector("div.bar-notification.success span.close");

    @FindBy(css = "input.button-1.add-to-cart-button")
    private WebElement addToCartButton;

    @FindBy(css = "a.ico-cart")
    private WebElement cartLink;

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
    }

    public String getProductName() {
        return waitForVisible(productTitle).getText().trim();
    }

    public ProductDetailsPage addProductToCart() {
        safeClick(addToCartButton);
        waitForVisible(successNotification);
        return this;
    }

    public String getSuccessMessage() {
        return waitForVisible(successNotification).getText().trim();
    }

    public CartPage goToCart() {
        if (isPresent(successNotification) && isDisplayed(successNotification)) {
            click(closeNotificationButton);
        }
        safeClick(cartLink);
        return new CartPage(driver);
    }
}
