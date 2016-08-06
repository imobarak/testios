package com.projecttoe.app;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;



import java.net.MalformedURLException;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;
import org.openqa.selenium.remote.DesiredCapabilities;

import org.testng.annotations.*;
import org.testng.Assert.*;

import static org.testng.Assert.*;
import static org.testng.Assert.assertTrue;

/**
 * Created by imobarak on 3/31/16.
 */
public class AppTest {
    IOSDriver driver;
    DesiredCapabilities capabilities;
    WebElement nav_bar;
    WebElement tab_bar;
    WebDriverWait wait;
    String postDate;
    Boolean isNotificationSuccess;
    String adminUsername = "imobarak3";
    String nonAdminUsername = "name";
    String password = "sky1";
    String signUpTestUser;
    String currentUser;
    String privateGroupJoined;
    String newlyAddedGroup = "";
    Boolean loggedin=false;
    //should have @Before @After if tests are independent and i'd want
    // to execute certain actions before/after every test
    //but in our case they depend on login to continue
    // @BeforeGroups("allTests")

    @BeforeTest(groups = "mainSimulator")
    public void setupSimulator() throws MalformedURLException {


        capabilities = new DesiredCapabilities();
        capabilities.setCapability("app", "/Users/imobarak/Microdoers/ipa/Project Toe.app");
        capabilities.setCapability("appium-version", "1.0");
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("platformVersion", "9.3");
        capabilities.setCapability("deviceName", "iPhone 5");
        capabilities.setCapability("launchTimeout", 40000);
        capabilities.setCapability("noReset", "true");
        capabilities.setCapability("fullReset", "false");
        capabilities.setCapability("nativeInstrumentsLib", "true");

        //capabilities.setCapability("waitForAppScript", "if (target.frontMostApp().alert().name()=='\"Project Toe\" Would Like to Send You Notifications') {$.acceptAlert(); true;}");
        driver = new IOSDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
        try{
            //accepting push notifications
            wait = new WebDriverWait(driver, 60);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert errorDialog = driver.switchTo().alert();
            errorDialog.accept();
            errorDialog.getText().contains("would like to");
            capabilities.setCapability("waitForAppScript", "$.delay(3000);$.acceptAlert()");


        }catch (Exception e){
            //did not handle the ios notification
        }

        System.out.println("setup done");
        //since no reset and simulator doesn't login every time set currentUser to adminUsername.
        currentUser = adminUsername;
    }
    //@AfterTest(groups = "allTests", alwaysRun = true )
    @AfterTest(groups =  { "main", "mainSimulator" }, alwaysRun = true )
    public void teardown(){
        driver.quit();
        System.out.println("clean up done");
    }



    @Test(groups =  { "main", "mainSimulator" }, priority = 1)
    public  void signIn() throws Exception {
        //replace here to make test fail

        //initializing wait and implicitwait for the rest of the execution
        wait = new WebDriverWait(driver, 15);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        //nav_bar = driver.findElementByClassName("UIANavigationBar");
        //tab_bar = driver.findElementByClassName("UIATabBar");
        System.out.println("sign in");
        try{
            WebElement element = driver.findElement(By.name("Sign In"));
            assertNotNull(element);
            element.click();
        }catch (Exception e){
            loggedin = true;
            System.out.println("Sign in not found - if noReset = true and fullReset = false then user probably logged in and test will resume normally starting goToNewsfeed");
        }

    }




    @Test(groups = "adminLogin", priority=9, dependsOnMethods = "signIn")
    public void loginAdmin() throws MalformedURLException {
        //replace here to make test fail
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        if(!loggedin) {
            if (!nav_bar.getAttribute("name").equals("Login")) {
                nav_bar.findElement(By.name("BACK")).click();
            }
            currentUser = adminUsername;
            try {
                driver.findElement(By.xpath("//UIATableCell[1]/UIATextField")).sendKeys(currentUser);
                driver.findElement(By.xpath("//UIATableCell[2]/UIASecureTextField")).sendKeys(password);
                driver.findElement(By.name("SUBMIT")).click();
                MobileElement successView = (MobileElement) new WebDriverWait(driver, 30).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//UIANavigationBar/UIAStaticText")));
                System.out.println("validlogin done");
            } catch (Exception e) {

            }
        }
        tab_bar = driver.findElementByClassName("UIATabBar");

    }

    @Test(groups = "newsfeed", priority=12)
    public void makePost() throws Exception {
        //replace here to make test fail
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.getAttribute("name").equals("Newsfeed"))
                nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e){

        }
        if(nav_bar.getAttribute("name").equals("Newsfeed")) {

            nav_bar.findElement(By.name("Post")).click();
            wait = new WebDriverWait(driver, 15);
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//UIANavigationBar/UIAStaticText"), "All Groups"));
            WebElement element = nav_bar.findElement(By.className("UIAStaticText"));
            //wait.until(ExpectedConditions.textToBePresentInElement(element,"All Groups"));
            postDate = new SimpleDateFormat("dd-MM-YY hh:mm").format(new Date());

            driver.findElementByClassName("UIATextView").sendKeys("automated post " + postDate);
            nav_bar.findElement(By.name("Post")).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//UIANavigationBar/UIAStaticText"), "Newsfeed"));
            System.out.println("Making a new post successful");
            checkPostSuccessful();
        } else
            fail("Failed to start test. Not on correct page due to failure from previous tests.");
    }

    public void checkPostSuccessful() throws Exception {
        //replace here to make test fail
        //first view in UICatalog is a table
        IOSElement table = (IOSElement)driver.findElementByClassName("UIATableView");
        assertNotNull(table);
        //is number of cells/rows inside table correct
        List<MobileElement> rows = table.findElementsByClassName("UIATableCell");

        //check that username is correct and message is correct
        WebElement element = rows.get(0).findElements(By.className("UIAStaticText")).get(0);
        assertEquals(currentUser, element.getText());
        element = rows.get(0).findElements(By.className("UIAStaticText")).get(1);
        assertEquals("automated post " + postDate, element.getText());
        System.out.println("Finding post just added successful");

    }


}
