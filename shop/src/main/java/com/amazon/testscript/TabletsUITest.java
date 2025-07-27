package com.amazon.testscript;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.amazon.locators.AmazonLocators;
import com.amazon.utils.BaseClass;

public class TabletsUITest extends BaseClass {
    
    @Test
    public void testExtractTabletData() throws Exception {
        List<WebElement> productName = driver.findElements(By.xpath(AmazonLocators.productName));
        List<WebElement> productPrice = driver.findElements(By.xpath(AmazonLocators.productPrice));
        
        // Print product name and price pairs
        for (int i = 0; i < Math.min(productName.size(), productPrice.size()); i++) {
            String name = productName.get(i).getText();
            String price = productPrice.get(i).getText();
            System.out.println(name);
            // Print price with "Rs." prefix, removing any existing currency symbols
            String formattedPrice = price.replaceAll("[^\\d.,]", "");
            System.out.println("Rs." + formattedPrice + "\n");
        }
    }
}
