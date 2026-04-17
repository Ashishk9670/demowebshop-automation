package com.demowebshop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class SearchResultsPage extends BasePage {
    @FindBy(css = "div.search-results")
    private WebElement searchResultsContainer;

    @FindBy(css = "h2.product-title a")
    private List<WebElement> productLinks;

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public int getResultCount() {
        waitForVisible(By.cssSelector("div.search-results"));
        return productLinks.size();
    }

    public ProductDetailsPage openProduct(String productName) {
        By productLink = By.xpath("//h2[@class='product-title']/a[normalize-space()='" + productName + "']");
        safeClick(productLink);
        return new ProductDetailsPage(driver);
    }

    public boolean isResultsContainerDisplayed() {
        return searchResultsContainer.isDisplayed();
    }
}
