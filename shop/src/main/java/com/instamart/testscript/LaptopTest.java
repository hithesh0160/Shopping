package com.instamart.testscript;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.instamart.utils.BaseClass;
import com.instamart.utils.InstamartUtil;

import locators.InstamartLocators;

public class LaptopTest extends BaseClass {

    @Test
    public void testLaptops() throws Exception {
        InstamartUtil.testSearchProduct("Laptop");
        sortLaptopsOnUI();
        InstamartUtil.sortElementsByDiscount();
    }

    private void sortLaptopsOnUI() throws Exception {
    WebElement element = driver.findElement(By.xpath(InstamartLocators.sortByButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        Thread.sleep(500);
        element.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.sortByDiscountOption)));
        driver.findElement(By.xpath(InstamartLocators.sortByDiscountOption)).click();
        Thread.sleep(500);
    WebElement element2 = driver.findElement(By.xpath(InstamartLocators.sortByTypeButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element2);
        Thread.sleep(500);
        element2.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.pcAndAccessoriesSortButton)));
        driver.findElement(By.xpath(InstamartLocators.pcAndAccessoriesSortButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(InstamartLocators.applyFiltersButton)));
        driver.findElement(By.xpath(InstamartLocators.applyFiltersButton)).click();
        Thread.sleep(500);
    }
}
