package com.demowebshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CartPage extends BasePage {
    private final By cartTable = By.cssSelector("table.cart");
    private final By cartRows = By.cssSelector("table.cart tbody tr");
    private final By removeCheckboxes = By.cssSelector("input[name='removefromcart']");
    private final By updateCartButton = By.cssSelector("input[name='updatecart']");
    private final By emptyCartMessage = By.cssSelector("div.order-summary-content");

    @FindBy(id = "termsofservice")
    private WebElement termsOfServiceCheckbox;

    @FindBy(id = "checkout")
    private WebElement checkoutButton;

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isProductPresent(String productName) {
        By productLocator = By.xpath("//table[contains(@class,'cart')]//a[normalize-space()='" + productName + "']");
        return isDisplayed(cartTable) && isPresent(productLocator) && doesElementTextContain(productLocator, productName);
    }

    public boolean hasItems() {
        return isPresent(cartRows);
    }

    public CartPage clearCart() {
        if (!hasItems()) {
            return this;
        }

        for (WebElement checkbox : driver.findElements(removeCheckboxes)) {
            if (checkbox.isDisplayed() && !checkbox.isSelected()) {
                checkbox.click();
            }
        }
        safeClick(updateCartButton);
        waitForDocumentReady();
        waitForVisible(emptyCartMessage);
        return this;
    }

    public CheckoutPage proceedToCheckout() {
        if (!termsOfServiceCheckbox.isSelected()) {
            click(termsOfServiceCheckbox);
        }
        safeClick(checkoutButton);
        return new CheckoutPage(driver);
    }
}
