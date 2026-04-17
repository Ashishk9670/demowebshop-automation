# QA Automation Take-Home Assessment

This repository contains a Selenium Java automation framework for the Demo Web Shop end-to-end purchase flow required in the assignment. The implementation uses Selenium WebDriver, TestNG, WebDriverManager, Page Object Model, config-driven test data, screenshots on failure, and an Extent report.

## Scope Covered

The automated scenario covers:

- Launch browser and open the Demo Web Shop site
- Log in with the provided credentials
- Search for a product using the configured search term
- Open a selected product from search results
- Add the product to the cart
- Verify the cart contains the selected item
- Proceed through checkout
- Fill billing details from `config.properties`
- Select available shipping and payment methods
- Confirm the order
- Verify the order success message
- Log out

## Tech Stack

- Java 17
- Maven
- Selenium WebDriver
- TestNG
- WebDriverManager
- ExtentReports
- SLF4J Simple Logger

## Project Structure

```text
src
|-- main/java/com/demowebshop
|   |-- config
|   |-- core
|   |-- models
|   |-- pages
|   |-- reporting
|   `-- utils
|-- test/java/com/demowebshop
|   |-- listeners
|   `-- tests
`-- test/resources
    `-- config.properties
```

## Prerequisites

- JDK 17 or later installed and added to `PATH`
- Maven 3.9 or later installed and added to `PATH`
- Chrome, Edge, or Firefox installed locally
- Internet access to reach [Demo Web Shop](https://demowebshop.tricentis.com/)

## How To Run

Run the default suite:

```bash
mvn clean test
```

Run with a custom config file:

```bash
mvn clean test -Dconfig.file=src/test/resources/config.properties
```

Run headless:

```bash
mvn clean test -Dheadless=true
```

Run with parallel execution (3 threads):

```bash
mvn clean test -DparallelExecution=true
```

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. **Tests Timeout at Checkout Step**
**Issue:** Tests hang at `completeBillingAddress()` or checkout validation steps.

**Root Cause:** Demo Web Shop checkout uses heavy AJAX; page state changes can take 5-10+ seconds.

**Solutions:**
- Increase explicit wait timeout in `config.properties`:
  ```properties
  explicit.wait.seconds=30
  ```
- Check network connectivity - slow connections cause AJAX delays
- Verify billing form fields exist - site structure may have changed
- Check browser console for JavaScript errors in test report screenshots

#### 2. **StaleElementReferenceException During Checkout**
**Issue:** Test fails with "Element is no longer attached to the DOM"

**Root Cause:** AJAX refreshes the DOM during checkout steps, invalidating element references.

**Solutions:**
- Framework already retries with fresh element lookup (automatic)
- If still failing, increase `explicit.wait.seconds` to 30+
- Check if browser version is up to date - outdated browsers have timing issues
- Verify `config.properties` billing data is correct - invalid data causes validation retries

#### 3. **Element Not Found or Displayed**
**Issue:** `NoSuchElementException` or element is hidden despite visibility wait

**Root Cause:** Page structure changed; locators no longer valid or element behind modal/overlay

**Solutions:**
- Update locators in page object if site structure changed:
  ```java
  private final By billingFirstName = By.id("BillingNewAddress_FirstName");
  ```
- Check if modal/loader is blocking element - add wait for overlay to disappear:
  ```java
  waitForInvisibility(loadingOverlay);
  ```
- Verify config values match site expectations (e.g., country name spelling)

#### 4. **Tests Pass Locally but Fail in GitHub Actions**
**Issue:** Tests work in local environment but fail in CI/CD

**Root Cause:** Headless mode differences, network latency, or missing dependencies

**Solutions:**
- Increase waits for CI environment:
  ```bash
  mvn clean test -Dexplicit.wait.seconds=40
  ```
- Verify GitHub Actions runner has all required browsers:
  ```yaml
  # .github/workflows/daily-tests.yml
  - run: apt-get update && apt-get install -y chromium-browser
  ```
- Check test artifacts in GitHub Actions "Run tests with Maven" step for failure details
- Enable headless debug mode locally to replicate CI environment:
  ```bash
  mvn clean test -Dheadless=true
  ```

#### 5. **Cannot Select Payment/Shipping Method**
**Issue:** `clickFirstEnabled()` doesn't select any radio button

**Root Cause:** Radio button is selected or disabled, or locator doesn't match available options

**Solutions:**
- Check available payment methods at Demo Web Shop (may have restrictions)
- Verify locator in `CheckoutPage`:
  ```java
  private final By paymentMethodOptions = By.cssSelector("#checkout-step-payment-method input[name='paymentmethod']");
  ```
- Check if payment method section is visible before clicking:
  ```java
  if (isVisible(paymentMethodSection)) { ... }
  ```

#### 6. **Screenshot or Report Not Generated**
**Issue:** No test report or screenshot in `test-output/` or `target/surefire-reports/`

**Root Cause:** Test listener didn't execute or file permissions issue

**Solutions:**
- Check test execution completed (check exit code in terminal)
- Verify directories have write permissions:
  ```bash
  chmod -R 755 test-output/ target/
  ```
- Check `TestListener.java` is properly configured in `testng.xml`:
  ```xml
  <listeners>
      <listener class-name="com.demowebshop.listeners.TestListener" />
  </listeners>
  ```

#### 7. **Login Fails with Invalid Credentials**
**Issue:** Login step fails despite correct email/password in config

**Root Cause:** Session timeout, wrong credentials, or account locked

**Solutions:**
- Verify credentials in `config.properties`:
  ```properties
  user.email=your.email@example.com
  user.password=your_password
  ```
- Check if Demo Web Shop account is active (not deleted/locked)
- Clear browser cookies between runs (handled by WebDriver.quit())
- Try manual login at [demowebshop.tricentis.com](https://demowebshop.tricentis.com/) to verify credentials

#### 8. **Port Already in Use (Local Execution)**
**Issue:** "Address already in use" error when running multiple parallel instances

**Root Cause:** WebDriver ChromeDriver instances competing for ports

**Solutions:**
- Disable parallel execution:
  ```bash
  mvn clean test
  ```
- Kill existing Chrome processes:
  ```bash
  # Windows
  taskkill /F /IM chromedriver.exe
  # Linux/Mac
  pkill -f chromedriver
  ```
- Reduce thread count in `pom.xml`:
  ```xml
  <threadCount>2</threadCount>
  ```

#### 9. **Browser Not Found**
**Issue:** "Chrome/Firefox driver not found" error

**Root Cause:** WebDriverManager failed to download or browser not installed

**Solutions:**
- Ensure browser is installed:
  - Chrome: https://www.google.com/chrome/
  - Firefox: https://www.mozilla.org/firefox/
  - Edge: https://www.microsoft.com/edge/
- Force WebDriverManager to re-download driver:
  ```bash
  mvn clean test -Dwebdrivermanager.forceDownload=true
  ```
- Check internet connection - WebDriverManager downloads drivers from GitHub

#### 10. **Assertion Failures in Test Report**
**Issue:** Tests fail with assertion errors

**Solutions:**
- Check `test-output/ExtentReport.html` for detailed failure info
- Verify product name exists in search:
  ```properties
  search.term=laptop
  product.name=14" Laptop
  ```
- Check if Demo Web Shop inventory has the product
- Review screenshot attached to failed test step in Extent report

### Performance Tuning

**For Faster Test Execution:**
1. Reduce explicit waits (but ensure site stability):
   ```properties
   explicit.wait.seconds=15
   ```

2. Enable parallel execution:
   ```bash
   mvn clean test -DparallelExecution=true
   ```

3. Use headless mode (20-30% faster):
   ```bash
   mvn clean test -Dheadless=true
   ```

**For More Reliable Test Execution:**
1. Increase waits:
   ```properties
   explicit.wait.seconds=30
   ```

2. Disable parallel (eliminates race conditions):
   ```bash
   mvn clean test
   ```

3. Add retry logic in testng.xml:
   ```xml
   <suite name="Demo Web Shop Automation Suite" parallel="tests" thread-count="1">
       <test retry-analyzer="com.demowebshop.listeners.RetryAnalyzer">
   ```

### Debug Mode

Enable detailed logging for troubleshooting:

1. Check `src/test/resources/simplelogger.properties` for log level
2. Increase driver logging: Set `--verbose` flag in browser options
3. Capture network traffic: Use browser DevTools in Extent report screenshots
4. Review stacktrace in Extent report for exact failure location

### Reporting Issues

When reporting test failures:
1. Include Extent report (`test-output/ExtentReport.html`)
2. Include surefire report (`target/surefire-reports/`)
3. Include screenshot from failed step
4. Specify environment (Windows/Mac/Linux, browser version)
5. Include values from `config.properties` (without password)



- Page Object Model is used for readability and maintainability.
- PageFactory is used to satisfy the assignment bonus preference.
- WebDriverManager handles browser driver setup automatically.
- Configuration is externalized in `config.properties`.
- A TestNG listener captures screenshots on failure and writes an Extent report to `test-output/ExtentReport.html`.
- The test is parameterized through a TestNG `DataProvider`.

## Deliverables Mapping

- Selenium-based automation scripts: included
- POM design pattern: included
- TestNG runner: included
- Test report output: included through TestNG + ExtentReports
- Config file for credentials and URLs: included
- Readable modular code: included
- README with setup instructions: included
- AI usage documentation: included in [AI_USAGE.md](/C:/Users/ashis/Documents/New%20project/AI_USAGE.md)

## Selenium MCP Note

The assignment request mentioned Selenium MCP. This workspace did not expose a Selenium MCP server or MCP resources, so the deliverable is implemented as a standard Selenium Java framework. If a Selenium MCP server is connected later, it can be used to validate locators and flows during maintenance without changing the test architecture.

