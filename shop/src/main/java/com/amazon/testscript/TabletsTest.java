package com.amazon.testscript;

import com.amazon.utils.HtmlBodyExtractor;
import com.amazon.constants.AmazonConstants;
import com.amazon.constants.AmazonLocators;
import com.amazon.utils.ExtractDataFromResponse;
import com.amazon.utils.UrlGenerator;
import org.testng.annotations.Test;

public class TabletsTest {
    
    public static void main(String[] args) throws Exception {
        // Use UrlGenerator to parameterize the new Amazon URL structure
        String url = UrlGenerator.generateUrlWithParams(
            "tablets", // k
            "Smartphones", // i
            AmazonConstants.Discount50PercentOff, // rh
            "price-asc-rank", // s
            "dc", // dc
            "v1%3A1LnjVlgfcmLpzkRTyvVg9drUER1%2BtdJTV1L4PXz3uw4" // ds
        );

        String htmlBody = HtmlBodyExtractor.getHtmlBodyFromUrl(url);
//        System.out.println("\n--- htmlBody ---");
//        System.out.println(htmlBody);

        // Print the extracted data
        System.out.println("Locators: "+ AmazonLocators.productName +" price"+ AmazonLocators.productPrice );        
//        System.out.println("\n--- Extracted Data ---");
        ExtractDataFromResponse.extractData(htmlBody, AmazonLocators.productName, AmazonLocators.productPrice);
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

        String htmlBody = HtmlBodyExtractor.getHtmlBodyFromUrl(url);
        System.out.println("\n--- htmlBody ---");
        System.out.println(htmlBody);

        // Print the extracted data
        System.out.println("\n--- Extracted Data ---");
        System.out.println("Locators: "+ AmazonLocators.productName +" price"+ AmazonLocators.productPrice );        
        ExtractDataFromResponse.extractData(htmlBody, AmazonLocators.productName, AmazonLocators.productPrice);
    }
}
