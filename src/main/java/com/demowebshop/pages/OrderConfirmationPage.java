package com.demowebshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OrderConfirmationPage extends BasePage {
    private final By orderSuccessTitle = By.cssSelector("div.section.order-completed div.title, div.section.order-completed strong");

    @FindBy(css = "input.button-2.order-completed-continue-button")
    private WebElement continueButton;

    public OrderConfirmationPage(WebDriver driver) {
        super(driver);
    }

    public String getSuccessMessage() {
        return waitForVisible(orderSuccessTitle).getText().trim();
    }

    public HomePage continueAfterOrder() {
        safeClick(continueButton);
        return new HomePage(driver);
    }
}
