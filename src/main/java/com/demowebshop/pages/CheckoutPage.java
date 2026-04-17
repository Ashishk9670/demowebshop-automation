package com.demowebshop.pages;

import com.demowebshop.models.CheckoutData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CheckoutPage extends BasePage {
    private final By billingAddressSelect = By.id("billing-address-select");
    private final By billingFirstName = By.id("BillingNewAddress_FirstName");
    private final By billingLastName = By.id("BillingNewAddress_LastName");
    private final By billingEmail = By.id("BillingNewAddress_Email");
    private final By billingCompany = By.id("BillingNewAddress_Company");
    private final By billingCountry = By.id("BillingNewAddress_CountryId");
    private final By billingCity = By.id("BillingNewAddress_City");
    private final By billingAddress1 = By.id("BillingNewAddress_Address1");
    private final By billingAddress2 = By.id("BillingNewAddress_Address2");
    private final By billingZip = By.id("BillingNewAddress_ZipPostalCode");
    private final By billingPhone = By.id("BillingNewAddress_PhoneNumber");
    private final By loadingOverlay = By.cssSelector("div.ajax-loading-block-window");

    private final By shippingSection = By.id("checkout-step-shipping");
    private final By shippingMethodSection = By.id("checkout-step-shipping-method");
    private final By paymentMethodSection = By.id("checkout-step-payment-method");
    private final By paymentInfoSection = By.id("checkout-step-payment-info");
    private final By confirmOrderSection = By.id("checkout-step-confirm-order");
    private final By orderCompletedSection = By.cssSelector("div.section.order-completed, div.order-completed");

    private final By shippingAddressContinue = By.cssSelector("#checkout-step-shipping input[value='Continue']");
    private final By shippingMethodOptions = By.cssSelector("#checkout-step-shipping-method input[name='shippingoption']");
    private final By shippingMethodContinue = By.cssSelector("#checkout-step-shipping-method input[value='Continue']");
    private final By paymentMethodOptions = By.cssSelector("#checkout-step-payment-method input[name='paymentmethod']");
    private final By paymentMethodContinue = By.cssSelector("#checkout-step-payment-method input[value='Continue']");
    private final By paymentInfoContinue = By.cssSelector("#checkout-step-payment-info input[value='Continue']");
    private final By confirmOrderButton = By.cssSelector("#checkout-step-confirm-order input[value='Confirm']");

    @FindBy(css = "input.button-1.new-address-next-step-button")
    private WebElement billingContinueButton;

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutPage completeBillingAddress(CheckoutData data) {
        waitForUrlContains("onepagecheckout");

        if (isPresent(billingAddressSelect)) {
            Select addressSelect = new Select(waitForVisible(billingAddressSelect));
            List<WebElement> options = addressSelect.getOptions();
            for (WebElement option : options) {
                if ("New Address".equalsIgnoreCase(option.getText().trim())) {
                    addressSelect.selectByVisibleText("New Address");
                    break;
                }
            }
        }

        fillInputIfVisible(billingFirstName, data.getFirstName());
        fillInputIfVisible(billingLastName, data.getLastName());
        fillInputIfVisible(billingEmail, data.getEmail());
        fillInputIfVisible(billingCompany, data.getCompany());
        if (isPresent(billingCountry)) {
            selectByVisibleText(billingCountry, data.getCountry());
        }
        fillInputIfVisible(billingCity, data.getCity());
        fillInputIfVisible(billingAddress1, data.getAddressLine1());
        fillInputIfVisible(billingAddress2, data.getAddressLine2());
        fillInputIfVisible(billingZip, data.getZipCode());
        fillInputIfVisible(billingPhone, data.getPhoneNumber());

        safeClick(billingContinueButton);
        waitForLoadingToFinish();
        waitForCheckoutState();
        return this;
    }

    public CheckoutPage selectShippingMethod() {
        if (isVisible(shippingAddressContinue)) {
            clickStepButton(shippingAddressContinue);
        }

        if (isVisible(shippingMethodOptions)) {
            clickFirstEnabled(shippingMethodOptions);
            clickStepButton(shippingMethodContinue);
        }
        return this;
    }

    public CheckoutPage selectPaymentMethod() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> isVisible(paymentMethodOptions));
            clickFirstEnabled(paymentMethodOptions);
            clickStepButton(paymentMethodContinue);
        } catch (Exception e) {
            // Payment methods might not be available, retry with confirmOrder
        }
        return this;
    }

    public CheckoutPage confirmPaymentInformation() {
        // Wait up to 20 seconds for the payment info continue button to become visible
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> isVisible(paymentInfoContinue));
            clickStepButton(paymentInfoContinue);
        } catch (Exception e) {
            // If button is not found, it might already be at the next step
        }
        return this;
    }

    public OrderConfirmationPage confirmOrder() {
        for (int attempt = 0; attempt < 12; attempt++) {
            waitForLoadingToFinish();
            
            if (isVisible(orderCompletedSection)) {
                return new OrderConfirmationPage(driver);
            }
            
            if (isVisible(confirmOrderButton)) {
                clickStepButton(confirmOrderButton);
                continue;
            }
            
            if (isVisible(paymentInfoContinue)) {
                clickStepButton(paymentInfoContinue);
                continue;
            }
            
            if (isVisible(paymentMethodOptions)) {
                clickFirstEnabled(paymentMethodOptions);
                if (isVisible(paymentMethodContinue)) {
                    clickStepButton(paymentMethodContinue);
                }
                continue;
            }
            
            if (isVisible(shippingMethodOptions)) {
                clickFirstEnabled(shippingMethodOptions);
                if (isVisible(shippingMethodContinue)) {
                    clickStepButton(shippingMethodContinue);
                }
                continue;
            }
            
            if (isVisible(shippingAddressContinue)) {
                clickStepButton(shippingAddressContinue);
                continue;
            }
            
            // If no button is visible, wait for state change
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitForCheckoutState();
        }
        return new OrderConfirmationPage(driver);
    }

    private void clickStepButton(By locator) {
        int maxRetries = 3;
        int retryCount = 0;
        boolean clicked = false;

        while (!clicked && retryCount < maxRetries) {
            try {
                WebElement button = waitForVisible(locator);
                String onclick = button.getDomAttribute("onclick");

                try {
                    scrollIntoView(button);
                    if (onclick != null && !onclick.isBlank()) {
                        ((JavascriptExecutor) driver).executeScript(onclick);
                    } else {
                        button.click();
                    }
                    clicked = true;
                } catch (WebDriverException exception) {
                    // Re-fetch the button in case it became stale
                    button = waitForVisible(locator);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                    clicked = true;
                }
            } catch (StaleElementReferenceException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (TimeoutException e) {
                // Button not found, move to next state
                return;
            }
        }

        waitForLoadingToFinish();
        waitForCheckoutState();
    }

    private void waitForLoadingToFinish() {
        if (isPresent(loadingOverlay)) {
            waitForInvisibility(loadingOverlay);
        }
    }

    private void waitForCheckoutState() {
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> isVisible(shippingSection)
                        || isVisible(shippingMethodSection)
                        || isVisible(paymentMethodSection)
                        || isVisible(paymentInfoSection)
                        || isVisible(confirmOrderSection)
                        || isVisible(orderCompletedSection));
    }
}
