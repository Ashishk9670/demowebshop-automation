package com.demowebshop.tests;

import com.demowebshop.config.ConfigReader;
import com.demowebshop.models.CheckoutData;
import com.demowebshop.pages.CartPage;
import com.demowebshop.pages.CheckoutPage;
import com.demowebshop.pages.HomePage;
import com.demowebshop.pages.OrderConfirmationPage;
import com.demowebshop.pages.ProductDetailsPage;
import com.demowebshop.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PurchaseFlowTest extends BaseTest {

    @DataProvider(name = "purchaseData")
    public Object[][] purchaseData() {
        return new Object[][]{
                {
                        ConfigReader.get("search.term"),
                        ConfigReader.get("product.name")
                }
        };
    }

    @Test(description = "Validate a logged-in user can complete the Demo Web Shop purchase flow.",
            dataProvider = "purchaseData")
    public void shouldCompleteEndToEndPurchaseFlow(String searchTerm, String productName) {
        String baseUrl = ConfigReader.get("base.url");
        String email = ConfigReader.get("user.email");
        String password = ConfigReader.get("user.password");
        CheckoutData checkoutData = ConfigReader.getCheckoutData();

        HomePage homePage = new HomePage(driver).open(baseUrl);
        logger.info("Opening home page and logging in with configured account.");

        HomePage loggedInHomePage = homePage.clickLogin().login(email, password);
        Assert.assertTrue(loggedInHomePage.isLogoutVisible(), "Logout link should be visible after login.");
        Assert.assertEquals(loggedInHomePage.getLoggedInAccountEmail(), email,
                "Logged-in account email should match the configured email.");

        logger.info("Clearing cart to ensure a clean checkout state.");
        loggedInHomePage.openCart().clearCart();
        loggedInHomePage.open(baseUrl);

        logger.info("Searching for product '{}' using search term '{}'.", productName, searchTerm);
        SearchResultsPage searchResultsPage = loggedInHomePage.search(searchTerm);
        Assert.assertTrue(searchResultsPage.isResultsContainerDisplayed(),
                "Search results section should be displayed.");
        Assert.assertTrue(searchResultsPage.getResultCount() > 0,
                "Search should return at least one result.");

        ProductDetailsPage productDetailsPage = searchResultsPage.openProduct(productName);
        Assert.assertEquals(productDetailsPage.getProductName(), productName,
                "Product details page should match the selected item.");

        productDetailsPage.addProductToCart();
        Assert.assertTrue(productDetailsPage.getSuccessMessage().contains("The product has been added to your"),
                "Add to cart success notification should be shown.");

        CartPage cartPage = productDetailsPage.goToCart();
        Assert.assertTrue(cartPage.isProductPresent(productName),
                "Selected product should be present in the shopping cart.");

        logger.info("Proceeding through checkout with config-driven billing data.");
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        OrderConfirmationPage orderConfirmationPage = checkoutPage
                .completeBillingAddress(checkoutData)
                .selectShippingMethod()
                .selectPaymentMethod()
                .confirmPaymentInformation()
                .confirmOrder();

        String successMessage = orderConfirmationPage.getSuccessMessage();
        Assert.assertTrue(successMessage.contains("successfully processed"),
                "Order confirmation message should indicate success.");

        HomePage finalHomePage = orderConfirmationPage.continueAfterOrder();
        finalHomePage.logout();
        Assert.assertTrue(new HomePage(driver).open(baseUrl).isLoginVisible(),
                "Login link should be visible after logout.");
    }
}
