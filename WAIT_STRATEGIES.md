# Wait Strategies and Locator Documentation

This document explains the wait strategies, locator patterns, and error handling implemented in `BasePage.java`.

## Overview

The framework uses **explicit waits** as the primary strategy to handle element synchronization in AJAX-heavy applications like Demo Web Shop.

### Why Explicit Waits?

- **Predictable**: Wait until a specific condition is met
- **Configurable**: Timeout and polling intervals can be adjusted
- **Fast**: Don't wait full timeout if condition met early
- **Better than Implicit Waits**: Implicit waits cause 30+ second delays on missing elements

**Configuration**: Edit `config.properties`
```properties
explicit.wait.seconds=20
```

---

## Wait Strategies

### 1. Visibility Waits

#### `waitForVisible(By locator)`
```java
// Waits for element to be:
// 1. Present in DOM
// 2. Displayed in viewport
// 3. Within configured timeout (default: 20 seconds)

WebElement element = waitForVisible(By.id("productName"));
// Throws: TimeoutException if not visible
```

**Use When:**
- Need to interact with element (read text, click, type)
- Element may not be immediately available
- Waiting for AJAX to render content

**Do NOT Use When:**
- Element might not exist (use `isVisible()` instead)
- Want conditional behavior (use `isPresent()` instead)

---

#### `waitForClickable(By locator)`
```java
// Strictest wait condition - requires:
// 1. Element visible
// 2. Element enabled (not disabled attribute)
// 3. No other elements overlapping it
// 4. Element not intercepted by modal

WebElement button = waitForClickable(By.cssSelector("button.submit"));
button.click();
```

**Use When:**
- About to click an element
- Element might be behind loading overlay
- Element might be disabled initially

**Example - Checkout Button:**
```java
// Safe approach:
waitForClickable(By.id("checkoutButton")).click();

// Instead of (unsafe):
driver.findElement(By.id("checkoutButton")).click();  // May fail if disabled
```

---

#### `waitForInvisibility(By locator)`
```java
// Waits for element to disappear from DOM or become hidden
// Used for AJAX loading spinners and overlays

waitForInvisibility(By.cssSelector("div.ajax-loading-block-window"));
// Continues only when loading spinner is gone
```

**Use When:**
- Waiting for AJAX operation to complete
- Spinner/overlay needs to disappear before next action
- Page is transitioning between states

**Example - After Checkout:**
```java
// User clicks "Proceed" button, which triggers AJAX
clickStepButton(By.id("proceedButton"));

// Wait for loading to finish
waitForInvisibility(loadingOverlay);

// Now safe to interact with next form
```

---

#### `waitForUrlContains(String partialUrl)`
```java
// Waits for page navigation - URL must contain substring
// Indicates successful page transition

waitForUrlContains("onepagecheckout");
// Confirms checkout page has loaded
```

**Use When:**
- Verifying page navigation
- Ensuring URL changed after link click
- Multi-step flows need confirmation of each step

**Example - Login Verification:**
```java
login(email, password);
waitForUrlContains("home");  // Confirms login redirected home
```

---

#### `waitForDocumentReady()`
```java
// Waits for JavaScript to finish loading and executing
// Checks: document.readyState == "complete"

waitForDocumentReady();
// Continues only after all scripts loaded
```

**Use When:**
- After navigation where JS initializes content
- Before complex AJAX interactions
- Need to ensure full page load

---

### 2. Non-Blocking Check Methods

These return `boolean` instead of throwing exceptions - perfect for conditionals.

#### `isPresent(By locator)`
```java
// Fastest check - just looks in DOM, doesn't wait for visibility
// Returns: true if element exists, false otherwise

if (isPresent(By.id("optionalField"))) {
    type(optionalField, "value");
}
```

**Use When:**
- Element might not exist (optional fields)
- Want to skip element if not present
- Need quick check without waiting

**Performance:**
- Immediate return (no wait)
- Fastest locator check

---

#### `isVisible(By locator)`
```java
// Checks if element exists AND is displayed (no waiting)
// Returns: true if element visible, false otherwise

if (isVisible(By.id("successMessage"))) {
    takeScreenshot("success");
}
```

**Use When:**
- Want both existence and visibility check
- Don't want to wait
- Using in conditional logic

**vs. isDisplayed():**
- `isVisible()` - No wait, immediate check
- `isDisplayed()` - Waits then checks (up to 20s by default)

---

#### `isDisplayed(By locator)`
```java
// Waits for visibility, returns false if timeout instead of throwing
// Useful for elements that appear after AJAX

if (isDisplayed(By.id("confirmationMessage"))) {  // Waits up to 20s
    String msg = getText(confirmationMessageElement);
}
```

**Use When:**
- Element might appear after delay
- Don't want test to fail if not visible
- Waiting up to timeout is acceptable

---

#### `doesElementTextContain(By locator, String expectedText)`
```java
// Waits for element visible, then checks text content
// Returns: true if element text contains substring

if (doesElementTextContain(By.id("message"), "successfully")) {
    // Order confirmation found
}
```

**Use When:**
- Verifying partial text matches
- Message text might change
- Substring search sufficient

---

## Locator Strategies

### 1. By.id() - PREFERRED

```java
// Stable, unique identifier - best for Demo Web Shop
private final By billingFirstName = By.id("BillingNewAddress_FirstName");
private final By countryDropdown = By.id("BillingNewAddress_CountryId");

// Usage
fillInputIfVisible(billingFirstName, "John");
selectByVisibleText(countryDropdown, "United States");
```

**Pros:**
- Most stable - IDs rarely change
- Fast selector
- Unique within page

**Cons:**
- Not available for all elements
- Requires HTML inspection

---

### 2. By.cssSelector() - FLEXIBLE

```java
// CSS selectors for complex selections
private final By shippingMethodOptions = 
    By.cssSelector("#checkout-step-shipping-method input[name='shippingoption']");

private final By loadingOverlay = 
    By.cssSelector("div.ajax-loading-block-window");

// Usage
clickFirstEnabled(shippingMethodOptions);
waitForInvisibility(loadingOverlay);
```

**Pros:**
- More flexible than ID
- Can select by class, attribute, hierarchy
- Fast in most browsers

**Common Patterns:**
```java
By.cssSelector("button.submit")              // Class selector
By.cssSelector("input[name='email']")         // Attribute selector
By.cssSelector("div.card > button")           // Child selector
By.cssSelector("input[value='Continue']")     // Attribute value
```

---

### 3. By.name() - FORM ELEMENTS

```java
// Good for form groups (radio buttons, checkboxes)
private final By paymentMethodOptions = 
    By.cssSelector("#checkout-step-payment-method input[name='paymentmethod']");

private final By shippingOptions = 
    By.cssSelector("input[name='shippingoption']");

// Usage - automatically finds first enabled
clickFirstEnabled(paymentMethodOptions);
```

**Pros:**
- Designed for form elements
- Multiple elements can share name (radio groups)
- Stable across UI changes

---

### 4. By.xpath() - LAST RESORT

```java
// Use only for complex hierarchical selections
// Hard to maintain, slower than CSS
private final By complexElement = 
    By.xpath("//div[@class='section']//button[contains(text(), 'Continue')]");

// AVOID if CSS selector works
```

**Use When:**
- No ID or class available
- Complex hierarchical relationship needed
- CSS selector too complicated

**Performance:** Slower than CSS, use sparingly

---

## Safe Interaction Methods

### `click(WebElement element)`
```java
// Standard Selenium click with wait
// Waits for clickable condition before clicking

protected void click(WebElement element) {
    wait.until(ExpectedConditions.elementToBeClickable(element)).click();
}

// Usage
HomePage homePage = new HomePage(driver);
click(homePage.loginButton);
```

---

### `click(By locator)`
```java
// Click by locator instead of pre-found element
// Combines find + wait + click

protected void click(By locator) {
    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
}

// Usage
click(By.id("submitButton"));
```

---

### `safeClick(WebElement element)`
```java
// Robust click that handles overlays and sticky headers
// Process:
// 1. Wait for element clickable
// 2. Scroll into view
// 3. Click

protected void safeClick(WebElement element) {
    WebElement clickableElement = wait.until(ExpectedConditions.elementToBeClickable(element));
    scrollIntoView(clickableElement);
    clickableElement.click();
}

// Usage - for elements behind sticky headers
safeClick(checkoutButton);
```

---

### `safeClick(By locator)`
```java
// Same as above but by locator

protected void safeClick(By locator) {
    WebElement element = waitForClickable(locator);
    scrollIntoView(element);
    element.click();
}

// Usage
safeClick(By.id("finalSubmitButton"));
```

---

### `clickFirstEnabled(By locator)`
```java
// Clicks first visible, enabled element from group
// Designed for radio buttons and checkboxes
// Handles stale elements and overlays

// Use for:
// - Multiple radio buttons with same name
// - Checkbox groups
// - Multiple submit buttons

// Example - select payment method
clickFirstEnabled(By.cssSelector("input[name='paymentmethod']"));
// Finds all payment method radios, clicks first unselected enabled one
```

---

### `type(WebElement element, String value)`
```java
// Type text with automatic clear
// Waits for visibility before typing

protected void type(WebElement element, String value) {
    wait.until(ExpectedConditions.visibilityOf(element));
    element.clear();
    element.sendKeys(value);
}

// Usage
type(emailInput, "john@example.com");
```

---

### `fillInputIfVisible(By locator, String value)`
```java
// Fills input only if it exists and is visible
// Skips if value is null or empty
// Graceful for optional fields

protected void fillInputIfVisible(By locator, String value) {
    if (value == null || value.isBlank()) return;
    if (isPresent(locator) && driver.findElement(locator).isDisplayed()) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(value);
    }
}

// Usage - optional form field
fillInputIfVisible(By.id("companyName"), data.getCompany());
// Does nothing if company name is null or field doesn't exist
```

---

### `selectByVisibleText(WebElement element, String text)`
```java
// Selects dropdown option by visible text
// Waits for visibility before selecting

protected void selectByVisibleText(WebElement element, String text) {
    wait.until(ExpectedConditions.visibilityOf(element));
    new Select(element).selectByVisibleText(text);
}

// Usage
selectByVisibleText(countryDropdown, "United States");
```

---

## Error Handling Patterns

### Stale Element Reference
```java
// Element became stale due to AJAX DOM refresh
// Solution: Catch and retry with fresh lookup

try {
    element.click();
} catch (StaleElementReferenceException e) {
    // Re-fetch and retry
    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
}

// Already implemented in clickStepButton() with 3 retries
```

---

### Element Click Intercepted
```java
// Another element is covering the target
// Solution: Use JavaScript click as fallback

try {
    element.click();
} catch (ElementClickInterceptedException e) {
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
}

// Already implemented in clickFirstEnabled()
```

---

### Timeout Exception
```java
// Element not found or didn't become visible
// Solution: Check boolean return or catch exception

// Option 1: Use boolean check method
if (isDisplayed(By.id("element"))) {
    // Element was found
}

// Option 2: Catch exception
try {
    waitForVisible(By.id("element"));
} catch (TimeoutException e) {
    // Handle missing element
}
```

---

## Best Practices Summary

### DO:
✅ Use explicit waits for all interactions
✅ Use By.id() when available
✅ Store locators as final class-level constants
✅ Use isVisible()/isPresent() for conditionals
✅ Use safeClick() for problematic elements
✅ Increase wait timeout for CI/CD environments
✅ Log important steps for debugging

### DON'T:
❌ Use implicit waits
❌ Use Thread.sleep() (wait instead)
❌ Mix locator strategies in same page object
❌ Hard-code timeout values
❌ Use XPath as first choice
❌ Ignore StaleElementReference exceptions
❌ Click without waiting for clickable

---

## Configuration Tuning

### For Slow Networks or CI/CD:
```properties
explicit.wait.seconds=30
```

### For Fast Local Execution:
```properties
explicit.wait.seconds=10
```

### For Debugging (More Wait for Element Inspection):
```properties
explicit.wait.seconds=60
```

---

## Troubleshooting

**Test hangs at checkout:**
- Increase `explicit.wait.seconds` to 30+
- Check network connectivity
- Look for JavaScript errors in screenshots

**StaleElementReferenceException:**
- Already handled with retry logic
- If still failing, increase wait timeout
- Check if site structure changed

**Element not found:**
- Verify locator in browser DevTools
- Check if element is inside iframe
- Use isPresent() to check existence first

**Click fails despite wait:**
- Use safeClick() instead of click()
- Check for overlays with DevTools
- Increase timeout or try JavaScript click
