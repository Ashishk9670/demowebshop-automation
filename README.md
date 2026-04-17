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

Note:

- The current environment used to generate this project did not have Java or Maven installed, so the framework was prepared but not executed locally here.
- The `config.properties` file contains the assignment credentials and test data. Update billing data if the target environment requires different values.

## Key Design Choices

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

