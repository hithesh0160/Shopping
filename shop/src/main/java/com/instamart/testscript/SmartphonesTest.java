package com.instamart.testscript;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.instamart.utils.BaseClass;

import locators.InstamartLocators;

public class SmartphonesTest extends BaseClass {

    class ProductData {
        String name;
        String currentPrice;
        String previousPrice;
        String discountText;
        int discountValue;

        ProductData(String name, String currentPrice, String previousPrice, String discountText) {
            this.name = name;
            this.currentPrice = currentPrice;
            this.previousPrice = previousPrice;
            this.discountText = discountText;
            String numericDiscount = discountText.replaceAll("[^\\d]", "");
            this.discountValue = numericDiscount.isEmpty() ? 0 : Integer.parseInt(numericDiscount);
        }
    }

    @Test(priority = 1)
    public void testSmartphones() throws Exception {
        testSearchProduct("Smartphone");
        sortSmartphonesOnUI();
        sortElementsByDiscount();
    }

    @Test(priority = 2)
    public void testVegetables() throws Exception {
        testSearchProduct("Vegetable");
        // sortVegetablesOnUI();
        sortElementsByDiscount();
    }

    public void testSearchProduct(String name) throws Exception {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.searchBox)));
        driver.findElement(By.xpath(InstamartLocators.searchBox)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.searchBox2)));
        driver.findElement(By.xpath(InstamartLocators.searchBox2)).sendKeys(name);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.searchResult)));
        driver.findElement(By.xpath(InstamartLocators.searchResult)).click();
        Thread.sleep(500);
        // wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.gotItButton)));
        driver.findElement(By.xpath(InstamartLocators.gotItButton)).click();
    }

    public void sortSmartphonesOnUI() throws Exception {
    WebElement element = driver.findElement(By.xpath(InstamartLocators.sortByButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        Thread.sleep(500);
        element.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.sortByDiscountOption)));
        driver.findElement(By.xpath(InstamartLocators.sortByDiscountOption)).click();
        Thread.sleep(500);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.smartphonesSortButton)));
        driver.findElement(By.xpath(InstamartLocators.smartphonesSortButton)).click();
        Thread.sleep(500);
    }

    public void sortElementsByDiscount() {
        System.out.println("Product Details:");

        List<WebElement> productName = driver.findElements(By.xpath(InstamartLocators.productName));
        List<WebElement> productPrice = driver.findElements(By.xpath(InstamartLocators.productPrice));
        List<WebElement> previousPrice = driver.findElements(By.xpath(InstamartLocators.previousPrice));
        List<WebElement> productDiscount = driver.findElements(By.xpath(InstamartLocators.productDiscountPercentage));

        List<ProductData> tempList = new ArrayList<>();

        for (int i = 0; i < productName.size(); i++) {
            try {
                String nameText = productName.get(i).getText();
                String currentPriceText = productPrice.get(i).getText().replaceAll("[^\\d.,]", "");
                String previousPriceText = previousPrice.get(i).getText().replaceAll("[^\\d.,]", "");
                String discountText = productDiscount.get(i).getText();

                tempList.add(new ProductData(nameText, currentPriceText, previousPriceText, discountText));

            } catch (StaleElementReferenceException e) {
                System.out.println("Stale element at index " + i + " — re-fetching...");
                productName = driver.findElements(By.xpath(InstamartLocators.productName));
                productPrice = driver.findElements(By.xpath(InstamartLocators.productPrice));
                previousPrice = driver.findElements(By.xpath(InstamartLocators.previousPrice));
                productDiscount = driver.findElements(By.xpath(InstamartLocators.productDiscountPercentage));

                String nameText = productName.get(i).getText();
                String currentPriceText = productPrice.get(i).getText().replaceAll("[^\\d.,]", "");
                String previousPriceText = previousPrice.get(i).getText().replaceAll("[^\\d.,]", "");
                String discountText = productDiscount.get(i).getText();

                tempList.add(new ProductData(nameText, currentPriceText, previousPriceText, discountText));
            }
        }

        // Sort by discount descending
        tempList.sort((p1, p2) -> Integer.compare(p2.discountValue, p1.discountValue));

        // Print sorted products
        for (ProductData p : tempList) {
            System.out.println("Product: " + p.name);
            System.out.println("Current Price: Rs. " + p.currentPrice +
                    ", Previous Price: Rs. " + p.previousPrice);
            System.out.println("Discount: " + p.discountText);
            System.out.println();
        }
    }
}
