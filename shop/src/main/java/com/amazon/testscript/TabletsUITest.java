package com.amazon.testscript;

import com.amazon.utils.HtmlBodyExtractor;
import com.amazon.constants.AmazonConstants;
import com.amazon.locators.AmazonLocators;
import com.amazon.utils.ExtractDataFromResponse;
import com.amazon.utils.UrlGenerator;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TabletsUITest {
    
    public static void main(String[] args) throws Exception {
       TabletsUITest test = new TabletsUITest();
       test.testExtractTabletData();
    }

    @Test
    public void testExtractTabletData() throws Exception {
        // Use UrlGenerator to parameterize the new Amazon URL structure
        String url = UrlGenerator.generateUrlWithParams(
            "tablets", // k
            "Smartphones", // i
            AmazonConstants.Discount50PercentOff, // rh
            "price-asc-rank", // s
            "dc", // dc
            "v1%3A1LnjVlgfcmLpzkRTyvVg9drUER1%2BtdJTV1L4PXz3uw4" // ds
        );

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        Page page = browser.newPage();
        page.navigate(url);
        page.waitForLoadState();
        Assert.assertTrue(page.url().contains("Amazon"), "URL does not contain 'tablets'");
        List<String> productNames = page.locator(AmazonLocators.productName).allTextContents();
        List<String> productPrices = page.locator(AmazonLocators.productPrice).allTextContents();

        for (int i = 0; i < Math.min(productNames.size(), productPrices.size()); i++) {
            System.out.println("Product Name: " + productNames.get(i));
            System.out.println("Product Price: " + productPrices.get(i));
            System.out.println();
        }        
    }
}
