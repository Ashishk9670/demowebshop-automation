package com.demowebshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {
    @FindBy(css = "a.ico-login")
    private WebElement loginLink;

    @FindBy(css = "a.ico-logout")
    private WebElement logoutLink;

    @FindBy(css = "a.account")
    private WebElement accountLink;

    @FindBy(id = "small-searchterms")
    private WebElement searchInput;

    @FindBy(css = "input.button-1.search-box-button")
    private WebElement searchButton;

    @FindBy(css = "a.ico-cart")
    private WebElement shoppingCartLink;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open(String url) {
        driver.get(url);
        waitForDocumentReady();
        return this;
    }

    public LoginPage clickLogin() {
        click(loginLink);
        return new LoginPage(driver);
    }

    public boolean isLoginVisible() {
        return loginLink.isDisplayed();
    }

    public SearchResultsPage search(String searchTerm) {
        type(searchInput, searchTerm);
        click(searchButton);
        return new SearchResultsPage(driver);
    }

    public String getLoggedInAccountEmail() {
        return getText(accountLink);
    }

    public boolean isLogoutVisible() {
        return logoutLink.isDisplayed();
    }

    public CartPage openCart() {
        safeClick(shoppingCartLink);
        return new CartPage(driver);
    }

    public HomePage logout() {
        click(logoutLink);
        return this;
    }
}
