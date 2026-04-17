package com.demowebshop.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {
    @FindBy(id = "Email")
    private WebElement emailInput;

    @FindBy(id = "Password")
    private WebElement passwordInput;

    @FindBy(css = "input.button-1.login-button")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public HomePage login(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        click(loginButton);
        return new HomePage(driver);
    }
}

