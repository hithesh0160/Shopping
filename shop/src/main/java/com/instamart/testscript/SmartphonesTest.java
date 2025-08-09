package com.instamart.testscript;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.instamart.utils.BaseClass;

import locators.InstamartLocators;

public class SmartphonesTest extends BaseClass {
    
    @Test
    public void testSmartphones() throws Exception{
        testSearchProduct("Smartphones", "Discount");
    }

    public void testSearchProduct(String name,String sortBy) throws Exception {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.searchBox)));
        driver.findElement(By.xpath(InstamartLocators.searchBox)).click();;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.searchBox2)));
        driver.findElement(By.xpath(InstamartLocators.searchBox2)).sendKeys(name);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.searchResult)));
        driver.findElement(By.xpath(InstamartLocators.searchResult)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.gotItButton)));
        driver.findElement(By.xpath(InstamartLocators.gotItButton)).click();
        WebElement scrollableDiv = driver.findElement(By.xpath(InstamartLocators.sortByButton));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollableDiv);
        driver.findElement(By.xpath(InstamartLocators.sortByButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.sortByDiscountOption)));
        driver.findElement(By.xpath(InstamartLocators.sortByDiscountOption)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.productName)));
        System.out.println("Product Details:");
        List<WebElement> productName = driver.findElements(By.xpath(InstamartLocators.productName));
        List<WebElement> productPrice = driver.findElements(By.xpath(InstamartLocators.productPrice));
        List<WebElement> productDiscount = driver.findElements(By.xpath(InstamartLocators.productDiscountPercentage));

        try {
            for (int i = 0; i < productName.size(); i++) {
            String nameText = productName.get(i).getText();
            String priceText = productPrice.get(i).getText();
            String discountText = productDiscount.get(i).getText();
            System.out.println("Product: " + nameText + ", Price: " + priceText + ", Discount: " + discountText);
            System.out.println(" ");
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
 
}
}
