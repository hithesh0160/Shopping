package com.amazon.utils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

public class BaseClass {
    @BeforeClass
    public void setUpClass() {
        System.out.println("[TestNG] BeforeClass: setUpClass()");
        // Add class-level setup code here
    }

    @AfterClass
    public void tearDownClass() {
        System.out.println("[TestNG] AfterClass: tearDownClass()");
        // Add class-level teardown code here
    }

    @BeforeMethod
    public void setUpMethod() {
        System.out.println("[TestNG] BeforeMethod: setUpMethod()");
        // Add method-level setup code here
    }

    @AfterMethod
    public void tearDownMethod() {
        System.out.println("[TestNG] AfterMethod: tearDownMethod()");
        // Add method-level teardown code here
    }
}
