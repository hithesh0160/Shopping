package com.instamart.testscript;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.instamart.utils.BaseClass;
import com.instamart.utils.InstamartUtil;

import locators.InstamartLocators;

public class SmartphonesTest extends BaseClass {

    @Test
    public void testSmartphones() throws Exception {
        InstamartUtil.testSearchProduct("Smartphone");
        sortSmartphonesOnUI();
        InstamartUtil.sortElementsByDiscount();
    }

    private void sortSmartphonesOnUI() throws Exception {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.sortByButton)));
    WebElement element = driver.findElement(By.xpath(InstamartLocators.sortByButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        // Thread.sleep(5000);
        element.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.sortByDiscountOption)));
        driver.findElement(By.xpath(InstamartLocators.sortByDiscountOption)).click();
        Thread.sleep(5000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.smartphonesSortButton)));
        driver.findElement(By.xpath(InstamartLocators.smartphonesSortButton)).click();
        // Thread.sleep(5000);
    }
}
