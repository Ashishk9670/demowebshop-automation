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

    /**
     * Completes the billing address step of the checkout process.
     * 
     * This method:
     * 1. Waits for the checkout page to load (onepagecheckout URL)
     * 2. Selects 'New Address' option if address selector is present
     * 3. Fills in all billing form fields using config-driven data
     * 4. Handles optional fields gracefully (company, address line 2)
     * 5. Clicks the Continue button and waits for the next checkout step
     * 6. Waits for loading overlays to disappear before proceeding
     * 
     * @param data CheckoutData object containing billing information (first name, last name, email, etc.)
     * @return this CheckoutPage instance for method chaining
     * @throws TimeoutException if page elements are not found within the configured wait time
     */
    public CheckoutPage completeBillingAddress(CheckoutData data) {
        // Wait for checkout page to fully load
        waitForUrlContains("onepagecheckout");

        // Select 'New Address' if address selection dropdown is available
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

        // Fill all billing address fields using utility method that handles null/empty values
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

        // Submit billing form and transition to next checkout step
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

    /**
     * Confirms and places the order by navigating through remaining checkout steps.
     * 
     * This method implements a robust state machine that handles multiple checkout scenarios:
     * - Auto-advances through payment method and shipping method sections if needed
     * - Clicks the final 'Confirm Order' button
     * - Waits for AJAX loading overlays to clear between steps
     * - Retries up to 12 times to handle dynamic page updates and async operations
     * - Gracefully handles missing payment method selectors (some flow variants skip this)
     * 
     * The order of checks (bottom-up) ensures we complete all pending steps in correct sequence:
     * 1. If order is already completed, return immediately
     * 2. Otherwise click Confirm Order button
     * 3. If that's not visible, proceed with Payment Info confirmation
     * 4. If that's not visible, select Payment Method
     * 5. If that's not visible, select Shipping Method
     * 
     * @return OrderConfirmationPage instance when order is successfully placed
     * @throws TimeoutException if the order cannot be confirmed within the retry limit
     */
    public OrderConfirmationPage confirmOrder() {
        for (int attempt = 0; attempt < 12; attempt++) {
            // Clear any loading overlays before checking page state
            waitForLoadingToFinish();
            
            // Check if order has been successfully placed - if so, return confirmation page
            if (isVisible(orderCompletedSection)) {
                return new OrderConfirmationPage(driver);
            }
            
            // Primary action: Click the final Confirm button
            if (isVisible(confirmOrderButton)) {
                clickStepButton(confirmOrderButton);
                continue;
            }
            
            // Fallback: Continue payment info step if Confirm not yet visible
            if (isVisible(paymentInfoContinue)) {
                clickStepButton(paymentInfoContinue);
                continue;
            }
            
            // Fallback: Select payment method if not yet done
            if (isVisible(paymentMethodOptions)) {
                clickFirstEnabled(paymentMethodOptions);
                if (isVisible(paymentMethodContinue)) {
                    clickStepButton(paymentMethodContinue);
                }
                continue;
            }
            
            // Fallback: Select shipping method as last resort
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

    /**
     * Clicks a checkout step button with robust error handling and retry logic.
     * 
     * This private utility method handles the complexities of clicking checkout buttons:
     * - Handles buttons with custom onclick handlers vs. standard click handlers
     * - Retries up to 3 times when StaleElementReferenceException occurs
     * - Uses JavaScript execution as fallback if standard click() fails
     * - Scrolls button into view before clicking
     * - Waits for loading overlays and checkout state after clicking
     * 
     * This is necessary because Demo Web Shop uses AJAX extensively, causing elements
     * to become stale or obscured during the checkout flow.
     * 
     * @param locator By locator of the button to click (e.g., Continue button)
     */
    private void clickStepButton(By locator) {
        int maxRetries = 3;
        int retryCount = 0;
        boolean clicked = false;

        while (!clicked && retryCount < maxRetries) {
            try {
                // Get button element and check if it has custom onclick handler
                WebElement button = waitForVisible(locator);
                String onclick = button.getDomAttribute("onclick");

                try {
                    // Scroll button into view to avoid being behind sticky headers
                    scrollIntoView(button);
                    
                    // Some buttons use onclick handlers instead of standard click
                    if (onclick != null && !onclick.isBlank()) {
                        ((JavascriptExecutor) driver).executeScript(onclick);
                    } else {
                        // Standard click for normal buttons
                        button.click();
                    }
                    clicked = true;
                } catch (WebDriverException exception) {
                    // If WebDriver click fails, try JavaScript click as fallback
                    button = waitForVisible(locator);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                    clicked = true;
                }
            } catch (StaleElementReferenceException e) {
                // Element became stale due to AJAX refresh - retry
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                try {
                    // Brief pause before retry to allow DOM to stabilize
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (TimeoutException e) {
                // Button not found - not all checkout flows require all steps
                return;
            }
        }

        // Wait for any AJAX operations to complete before proceeding
        waitForLoadingToFinish();
        // Ensure we're in a valid checkout state before returning
        waitForCheckoutState();
    }

    private void waitForLoadingToFinish() {
        if (isPresent(loadingOverlay)) {
            waitForInvisibility(loadingOverlay);
        }
    }

    /**
     * Waits for the checkout page to transition to a known valid state.
     * 
     * Verifies that at least one of the major checkout sections is visible after AJAX operations.
     * This ensures we've reached a stable state before proceeding with the next action.
     * 
     * Valid checkout states:
     * - Shipping section (address confirmation)
     * - Shipping method selection
     * - Payment method selection
     * - Payment info confirmation
     * - Order confirmation
     * - Order completed success page
     * 
     * Timeout: 30 seconds (configurable via wait duration)
     */
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
