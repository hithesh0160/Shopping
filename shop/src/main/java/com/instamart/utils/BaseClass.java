package com.instamart.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public class BaseClass {

    public static WebDriver driver;
    public static ChromeOptions options;
    public static WebDriverWait wait;

    @BeforeClass
    public void setUpClass() throws IOException {
        // Add class-level setup code here
        options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/114.0.5735.133 Safari/537.36");
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        options.addArguments("--disable-blink-features=AutomationControlled"); // 👈 helps bypass bot detection

        Path userDataDir = Files.createTempDirectory(java.util.UUID.randomUUID().toString());
        options.addArguments("--user-data-dir=" + userDataDir.toString());

        driver = new ChromeDriver(options);
        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
    }

    @AfterClass
    public void tearDownClass() {
        // Add class-level teardown code here
        driver.close();
    }

    @BeforeMethod
    public void setUpMethod() throws InterruptedException {

        driver.get("https://www.swiggy.com/instamart/city/bangalore");

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

        System.out.println("Title: " + driver.getTitle());
        // setKodathiLocationAddress();
    }

    @AfterMethod
    public void tearDownMethod() {
        // Add method-level teardown code here
    }

}
