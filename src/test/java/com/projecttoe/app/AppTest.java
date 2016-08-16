package com.projecttoe.app;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.internal.TouchAction;
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
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
    private static Logger log = Logger.getLogger(AppTest.class);
    //should have @Before @After if tests are independent and i'd want
    // to execute certain actions before/after every test
    //but in our case they depend on login to continue
    // @BeforeGroups("allTests")

    private  Process process;
    private  String APPIUMSERVERSTART = "/usr/local/bin/appium";

    public void startAppiumServer() throws IOException, InterruptedException {


        CommandLine command = new CommandLine("/Applications/Appium.app/Contents/Resources/node/bin/node");
        command.addArgument("/Applications/Appium.app/Contents/Resources/node_modules/appium/bin/appium.js", false);
        command.addArgument("--address", false);
        command.addArgument("127.0.0.1");
        command.addArgument("--port", false);
        command.addArgument("4723");
        command.addArgument("--full-reset", false);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.execute(command, resultHandler);

        Thread.sleep(5000);
        System.out.println("Appium server started");
    }

    public  void stopAppiumServer() throws IOException {
        String[] command ={"/usr/bin/killall","-KILL","node"};
        Runtime.getRuntime().exec(command);
        System.out.println("Appium server stop");
    }

    @BeforeTest(groups = "mainSimulator")
    public void setupSimulator() throws IOException, InterruptedException {
        log.setLevel(Level.INFO);
        try {
            //get app path from testSettings.properties
            getPropValues();
        } catch (IOException e) {
            log.fatal("Could not find app file in path: " + appPath);
        }

        //startAppiumServer();
        capabilities = new DesiredCapabilities();
       // capabilities.setCapability("app", "/Users/imobarak/Microdoers/ipa/Project Toe.app");

        capabilities.setCapability("app", appPath);
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("platformVersion", 9.3);
        capabilities.setCapability("deviceName", "iPhone 5");
        capabilities.setCapability("launchTimeout", 40000);
        capabilities.setCapability("noReset", true);
        capabilities.setCapability("fullReset", false);
        capabilities.setCapability("nativeInstrumentsLib", true);
        log.info("Starting simulator setup: desired capabilities = " + capabilities.toString());
        //capabilities.setCapability("waitForAppScript", "if (target.frontMostApp().alert().name()=='\"Project Toe\" Would Like to Send You Notifications') {$.acceptAlert(); true;}");
        driver = new IOSDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
        try{
            log.info("waiting for push notifications ios alert to accept.");
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

        log.info("setup done");
        //since no reset and simulator doesn't login every time set currentUser to adminUsername.
        currentUser = adminUsername;
    }
    //@AfterTest(groups = "allTests", alwaysRun = true )
    @AfterTest(groups ="mainSimulator" , alwaysRun = true )
    public void teardown() throws IOException {
        //stopAppiumServer();
        driver.quit();
        log.info("clean up done");
    }



    @Test(groups =  "mainSimulator" , priority = 1)
    public  void signIn() throws Exception {
        //replace here to make test fail
        startTestCase("signIn");
        //initializing wait and implicitwait for the rest of the execution
        wait = new WebDriverWait(driver, 15);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        try{
            WebElement element = driver.findElement(By.name("Sign In"));
            assertNotNull(element);
            element.click();
        }catch (Exception e){
            loggedin = true;
            log.info("Sign in not found - if noReset = true and fullReset = false then user probably logged in and test will resume normally starting goToNewsfeed");
        }
        endTestCase("signIn");
    }




    @Test(groups = "adminLogin", priority=9)
    public void loginAdmin() throws MalformedURLException {
        //replace here to make test fail
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        if(!loggedin) {
            startTestCase("loginAdmin");
            if (!nav_bar.getAttribute("name").equals("Login")) {
                nav_bar.findElement(By.name("BACK")).click();
            }
            currentUser = adminUsername;
            try {
                driver.findElement(By.xpath("//UIATableCell[1]/UIATextField")).sendKeys(currentUser);
                driver.findElement(By.xpath("//UIATableCell[2]/UIASecureTextField")).sendKeys(password);
                driver.findElement(By.name("SUBMIT")).click();
                MobileElement successView = (MobileElement) new WebDriverWait(driver, 30).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//UIANavigationBar/UIAStaticText")));
                log.info("admin login done");
            } catch (Exception e) {
                log.error(e.getMessage());
                fail("unable to log in");
            }
        }
        tab_bar = driver.findElementByClassName("UIATabBar");
        endTestCase("loginAdmin");

    }
    @Test(groups = "newsfeed", priority = 10, enabled = true)
    public void goToNewsFeedTab() throws Exception {
        //replace here to make test fail
        //there is nav bar inside the app
        startTestCase("goToNewsFeedTab");
        driver.getPageSource();
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        tab_bar = driver.findElementByClassName("UIATabBar");
        tab_bar.findElement(By.name("Newsfeed")).click();
        assertTrue(tab_bar.findElement(By.name("Newsfeed")).isSelected());
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        assertEquals(nav_bar.findElement(By.className("UIAStaticText")).getText(), "Newsfeed");
        log.info("Open news feed tab successful");
        endTestCase("goToNewsFeedTab");
    }

    @Test(groups = "newsfeed", priority = 11,  enabled = true)
    public void loadNewsfeed() throws Exception {
        //replace here to make test fail
        startTestCase("loadNewsfeed");
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.getAttribute("name").equals("Newsfeed"))
                nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e){

        }
        if(nav_bar.getAttribute("name").equals("Newsfeed")) {
            //first view in UICatalog is a table
            IOSElement table = (IOSElement) driver.findElementByClassName("UIATableView");
            assertNotNull(table);
            //is number of cells/rows inside table correct
            List<MobileElement> rows = table.findElementsByClassName("UIATableCell");
            assertTrue(rows.size()>5);
            //is the username loaded correctly for now i just check if not empty
            assertNotSame("", rows.get(0).findElement(By.className("UIAStaticText")).getText());
            log.info("Load news feed and displaying more than 5 records");
        }else
            fail("Failed to start test. Not on correct page due to failure from previous tests.");
        endTestCase("loadNewsfeed");

    }

    @Test(groups = "newsfeed", priority=12)
    public void makePost() throws Exception {
        //replace here to make test fail
        startTestCase("makePost");
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.getAttribute("name").equals("Newsfeed"))
                nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e){

        }
        if(nav_bar.getAttribute("name").equals("Newsfeed")) {
        log.info("Click:Post");
            nav_bar.findElement(By.name("Post")).click();
            wait = new WebDriverWait(driver, 45);
            log.info("wait until nav title = All Groups");
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//UIANavigationBar/UIAStaticText"), "All Groups"));

            WebElement element = nav_bar.findElement(By.className("UIAStaticText"));
            //wait.until(ExpectedConditions.textToBePresentInElement(element,"All Groups"));
            postDate = new SimpleDateFormat("dd-MM-YY hh:mm").format(new Date());
            log.info("write text message");
            driver.findElementByClassName("UIATextView").sendKeys("automated post " + postDate);
            log.info("click:Post");
            nav_bar.findElement(By.name("Post")).click();
            log.info("wait until nav title = Newsfeed");
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//UIANavigationBar/UIAStaticText"), "Newsfeed"));
            log.info("Making a new post successful");
            checkPostSuccessful();
        } else
            fail("Failed to start test. Not on correct page due to failure from previous tests.");
        endTestCase("makePost");
    }

    public void checkPostSuccessful() throws Exception {
        //replace here to make test fail
        //first view in UICatalog is a table
        IOSElement table = (IOSElement)driver.findElementByClassName("UIATableView");
        assertNotNull(table);
        //is number of cells/rows inside table correct
        List<MobileElement> rows = table.findElementsByClassName("UIATableCell");
        if(rows.size()  == 0){
            Assert.fail("No posts displayed");
        }
        //check that username is correct and message is correct
        WebElement element = rows.get(0).findElements(By.className("UIAStaticText")).get(0);
        assertEquals(currentUser, element.getText());
        element = rows.get(0).findElements(By.className("UIAStaticText")).get(1);
        assertEquals("automated post " + postDate, element.getText());
        log.info("Finding post just added successful");

    }

    @Test(groups = "newsfeed", priority = 13)
    public void hugPost() throws Exception {
        //replace here to make test fail
        startTestCase("hugPost");
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.getAttribute("name").equals("Newsfeed")){
                try{
                    nav_bar.findElement(By.name("Back")).click();
                }catch (Exception e){
                    nav_bar.findElement(By.name("Cancel")).click();
                }
            }
        }catch (Exception e){

        }
        if(nav_bar.getAttribute("name").equals("Newsfeed")) {
            //first view in UICatalog is a table
            IOSElement table = (IOSElement)driver.findElementByClassName("UIATableView");
            List<MobileElement> rows = table.findElementsByClassName("UIATableCell");
            WebElement element = rows.get(0).findElements(By.className("UIAButton")).get(2);
            Integer numOfHugsBefore =Integer.parseInt(element.getText().substring(0,1));
            log.info("Top post current hugs = "+numOfHugsBefore);
            MobileElement button =rows.get(0).findElements(By.className("UIAButton")).get(1);
            //MobileElement button = rows.get(0).findElement(By.xpath("//button[@name='hug default' and not(@disabled)]"));
            Actions moveTo = new Actions(driver);
            moveTo.moveToElement(button).click().perform();

            Integer numOfHugsAfter = Integer.parseInt(element.getText().substring(0, 1));
            assertEquals(new Long(numOfHugsAfter), new Long(numOfHugsBefore + 1));
            log.info("Like post successful. Hugs now = "+ numOfHugsAfter);
        } else
            fail("Failed to start test. Not on correct page due to failure from previous tests.");
        endTestCase("hugPost");
    }

    @Test(groups = "newsfeed", priority = 14)
    public void commentPost() throws Exception {
        //replace here to make test fail
        startTestCase("commentPost");
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.getAttribute("name").equals("Newsfeed")){
                try{
                    nav_bar.findElement(By.name("Back")).click();
                }catch (Exception e){
                    nav_bar.findElement(By.name("Cancel")).click();
                }
            }
        }catch (Exception e){

        }
        if(nav_bar.getAttribute("name").equals("Newsfeed")) {
            IOSElement table = (IOSElement) driver.findElementByClassName("UIATableView");

            List<MobileElement> rows = table.findElementsByClassName("UIATableCell");
            rows.get(0).findElements(By.className("UIAButton")).get(0).click();

            //first view in UICatalog is a table
            Integer numOfCellsBefore = ((List<MobileElement>) driver.findElementsByXPath("//UIATableView[1]/UIATableCell")).size();
            table = (IOSElement) driver.findElementsByClassName("UIATableView").get(1);
            log.info("Current row size in post = " + numOfCellsBefore);
            rows = table.findElementsByClassName("UIATableCell");
            rows.get(0).findElement(By.className("UIATextView")).sendKeys("Comment through Appium " + postDate);
            rows.get(0).findElement(By.className("UIAButton")).click();
            //driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            table = (IOSElement) driver.findElementsByClassName("UIATableView").get(0);
            rows = table.findElementsByClassName("UIATableCell");
            assertEquals(numOfCellsBefore + 1, rows.size());
            log.info("after adding comment row size in post = " + rows.size());

            log.info("comment post successful");
        } else
            fail("Failed to start test. Not on correct page due to failure from previous tests.");
        endTestCase("commentPost");
    }


    @Test(groups = "groupsTab", priority = 20, enabled = true)
    public void goToGroupsTab() throws Exception {
        //replace here to make test fail
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        tab_bar = driver.findElementByClassName("UIATabBar");
        startTestCase("goToGroupsTab");
        tab_bar.findElement(By.name("Groups")).click();
        assertTrue(tab_bar.findElement(By.name("Groups")).isSelected());
        log.info("Click: Group icon in tab");
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        assertEquals(nav_bar.findElement(By.className("UIAStaticText")).getText(), "Support Groups");
        endTestCase("goToGroupsTab");
    }

    @Test(groups = "groupsTab",  priority = 21, enabled = true)
    public void loadGroups() throws Exception {
        //replace here to make test fail
        startTestCase("loadGroups");
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.getAttribute("name").equals("Groups"))
                nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e){

        }
        if(nav_bar.getAttribute("name").equals("Support Groups")) {
            List<MobileElement> tableGroups = driver.findElements(By.xpath("//UIATableView/UIATableGroup"));
            //assert that there are 3 sections
            assertEquals(tableGroups.size(), 3);
            log.info("TableGroups count correct");
            //assert that this section exists
            WebElement supportGroups = driver.findElementByName("YOUR SUPPORT GROUPS");
            assertNotNull(supportGroups);

            //assert that there are groups under this section - not empty
            List<MobileElement> cells = driver.findElements(By.xpath("//UIATableCell[preceding-sibling::UIATableGroup[1]/@name = 'YOUR SUPPORT GROUPS']"));
            assertNotEquals(cells.size(), 0);
            log.info("Your support groups = "+cells.size());
            //assert that this section exists
            supportGroups = driver.findElementByName("RECOMMENDED SUPPORT GROUPS");
            assertNotNull(supportGroups);

            //assert that there are groups under this section - not empty
            cells = driver.findElements(By.xpath("//UIATableCell[preceding-sibling::UIATableGroup[1]/@name = 'RECOMMENDED SUPPORT GROUPS']"));
            assertNotEquals(cells.size(), 0);
            log.info("Recommended support groups currently loaded = "+cells.size());
        } else
            fail("Failed to start test. Not on correct page due to failure from previous tests.");

        endTestCase("loadGroups");
    }

    @Test(groups = "groupsTab", priority = 22, enabled = true)
    public void searchGroup() throws Exception {
        //replace here to make test fail
        startTestCase("searchGroup");
        goBack();
        tab_bar.findElement(By.name("Groups")).click();
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        try{
            tableView.scrollTo("Join a Support Group").click();
        }catch (NotFoundException e){
            fail("Unable to start search group");
            throw e;
        }
        //nav_bar = driver.findElementByClassName("UIANavigationBar");
        nav_bar.findElement(By.className("UIASearchBar")).sendKeys("a");
        //driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        nav_bar.findElement(By.className("UIASearchBar")).sendKeys("nxiety101");
        //driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        //if( driver.findElementsByXPath("//UIATableView/UIATableCell").size() > 0){
        try{
            IOSElement button = (IOSElement) tableView.findElementByXPath("//UIATableCell");
            Actions moveTo = new Actions(driver);
            moveTo.moveToElement(button).click().perform();
            button.click();
            assertTrue(nav_bar.getAttribute("name").equals("Group"), "Segue to group");
        }catch (Exception e) {
            fail("Could not reach required search result");
        }
        endTestCase("searchGroup");
    }

    @Test(groups = "groupsTab", priority = 23, enabled = true)
    public void addGroup() throws Exception {
        //replace here to make test fail
        startTestCase("addGroup");
        goBack();
        tab_bar.findElement(By.name("Groups")).click();
        IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        try{
            tableView.scrollTo("Start a Support Group").click();
        }catch (NotFoundException e){
            fail("Unable to start add group");
            throw e;
        }
        List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
        String dateNow = new SimpleDateFormat("dd-MM-YY_hh:mm").format(new Date());
        newlyAddedGroup = "Appium_Group_" + dateNow;
        tableCells.get(1).findElementByClassName("UIATextField").sendKeys(newlyAddedGroup);
        tableCells.get(2).findElementByClassName("UIATextView").sendKeys("Appium description");
        tableCells.get(3).findElementByClassName("UIATextView").sendKeys("Appium keywords");
        tableCells.get(4).findElementByClassName("UIASwitch").click();
        tableCells.get(5).findElementByName("Add").click();
        log.info("Filled info. Click: Add");
        try{
            //handling alert
            WebDriverWait wait = new WebDriverWait(driver, 15);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert errorDialog = driver.switchTo().alert();
            String errorString = errorDialog.getText();
            errorDialog.accept();
            assertNotEquals(errorString.toLowerCase(), "sorry, this group name already exists.", "Group name already exists");
        }catch (Exception e){
            //no alerts
        }
        try{
            wait = new WebDriverWait(driver, 15);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("Ready to add \"Helpers?\"")));
            driver.findElementByName("No Thanks").click();
            log.info("ready to add helpers? Click: No thanks");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("Want us to get you members faster by promoting your Group?")));
            driver.findElementByName("No Thanks").click();
            log.info("promote your group? Click: No thanks");

        }catch (Exception e){
            fail("Did not add group successfully");
            throw e;
        }
        endTestCase("searchGroup");
    }

    @Test(groups = "groupsTab", priority = 24, enabled = true)
    public void checkGroupDetails() throws Exception {
        //replace here to make test fail
        startTestCase("checkGroupDetails");
        goBack();
        tab_bar.findElement(By.name("Groups")).click();
        IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        try{
          /*  TouchAction act = new TouchAction(driver);
        act.press(driver.findElementByAccessibilityId("id")).moveTo(driver.findElementByAccessibilityId("id")).release().perform();*/

            if (!newlyAddedGroup.isEmpty())

                tableView.scrollTo(newlyAddedGroup).click();
            else {
                tableView.scrollTo("Appium_Group_").click();
            }
        }catch (NotFoundException e){
            fail("Unable to find group");
            throw e;
        }

        List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
        try{
            String reviewsCount = tableCells.get(2).findElementByClassName("UIAStaticText").getText();
            if(reviewsCount.contains("Review")){
                log.info("Group has no reviews");
            }else{

                reviewsCount = reviewsCount.substring(reviewsCount.indexOf("(") + 1);
                reviewsCount = reviewsCount.substring(0, reviewsCount.indexOf(")"));

                log.info("Group has " + reviewsCount + "reviews");
            }

        }catch(Exception e){
            fail("Could not define group reviews");
        }
        assertTrue(tableCells.get(4).findElementByClassName("UIAStaticText").getText().contains("View Group Wall"), "View Group Wall button not found");

        assertTrue(tableCells.get(6).findElementByClassName("UIAStaticText").getText().contains(newlyAddedGroup), "Group name incorrect");
        assertTrue(tableCells.get(6).findElementByClassName("UIATextView").getText().contains("Appium description"), "Group description incorrect");
        //this is a private group and not helper so we should find the privateIcon and no helperModeIcon
        try{
            tableCells.get(6).findElementByName("privateIcon");
            log.info("Group is private");
        }catch (NoSuchElementException e){
            fail("Private icon not found this group should be private");
        }

        try{
            tableCells.get(6).findElementByName("helperIcon");
            fail("Helper icon is found in this group although it should not be in helper mode");
        }catch (NoSuchElementException e){
            log.info("Group is not in helper mode");
        }
        List<IOSElement> tableGroups = driver.findElementsByXPath("//UIATableView/UIATableGroup");
        try {
            String usersCount = tableGroups.get(2).findElementByClassName("UIAStaticText").getText();

            usersCount = usersCount.substring(usersCount.indexOf("(") + 1);
            usersCount = usersCount.substring(0, usersCount.indexOf(")"));
            log.info("Group has " + usersCount + " users");
        }
        catch (Exception e){
            fail("Could not define group users count");
        }

        try {
            for(int i=8; i<=10; i++) {
                assertTrue(tableCells.get(i).findElementByClassName("UIASwitch").isEnabled(), "Switch for " + tableCells.get(i).findElementByClassName("UIAStaticText").getText() + " is not visible");
            }
        }catch (Exception e){
            fail("Could not define group alerts");
        }
        endTestCase("checkGroupDetails");
    }

    @Test(groups = "groupsTab", priority = 24, enabled = true)
    public void checkPremiumGroup() throws Exception {
        //replace here to make test fail
        startTestCase("checkPremiumGroup");
        goBack();
        tab_bar.findElement(By.name("Groups")).click();
        IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        try{
            //tableView.scrollTo("Appium_Premium").click();
            List<MobileElement> cells = driver.findElements(By.xpath("//UIATableCell[preceding-sibling::UIATableGroup[1]/@name = 'RECOMMENDED SUPPORT GROUPS']"));

            cells.get(0).click();
        }catch (NotFoundException e){
            fail("Unable to find group");
            throw e;
        }

        List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
        try {

            assertTrue(tableCells.get(5).isDisplayed(), "Premium badge row should be visible");
            assertTrue(driver.findElementByXPath("//UIATableView/UIATableGroup[2]").getAttribute("name").equals("BADGES"),"Premium badge row should be visible");
        }catch (Exception e){
            fail("Premium badge row should be visible");
        }
        endTestCase("checkPremiumGroup");
    }

    //region adminPanel
    @Test(groups = "adminPanel", priority = 60, enabled = true)
    public void canBlockUnblockUsers() throws Exception {
        //replace here to make test fail
        startTestCase("canBlockUnblockUsers");
        goToAdminPanel("Appium_Public");
        try{
            ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Blocked Users").click();
            List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
            if(tableCells.size() == 0)
                fail("Blocked users is empty");
            else{
                MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                element.findElement(By.name("Block")).click();
                try{
                    //handling alert
                    WebDriverWait wait = new WebDriverWait(driver, 15);
                    wait.until(ExpectedConditions.alertIsPresent());
                    Alert errorDialog = driver.switchTo().alert();
                    errorDialog.accept();
                    fail("Could not block user alert is: " + errorDialog.getText());
                }catch (Exception e){
                    //no alerts = worked as expected
                }
                try{
                    element = ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                    element.findElement(By.name("Block"));
                    fail("Block failed user is still in unblocked list");
                }catch (Exception e){
                    //not found: worked as expected
                }
                element = ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                element.findElement(By.name("Unblock")).click();
                try{
                    //handling alert
                    WebDriverWait wait = new WebDriverWait(driver, 15);
                    wait.until(ExpectedConditions.alertIsPresent());
                    Alert errorDialog = driver.switchTo().alert();
                    errorDialog.accept();
                    fail("Could not unblock user alert is: " + errorDialog.getText());
                }catch (Exception e){
                    //no alerts = worked as expected
                }
                try{
                    element = ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                    element.findElement(By.name("Unblock")).click();
                    fail("Unblock failed user is still blocked");
                }catch (Exception e){
                    //not found: worked as expected
                }
            }
        }catch (Exception e){
            fail("Did not block user successfully: "+ e.getLocalizedMessage());
            throw e;
        }
endTestCase("canBlockUnblockUsers");
    }

    @Test(groups = "adminPanel", priority = 61, enabled = true)
    public void canBroadcastMessage() throws Exception {
        //replace here to make test fail
        startTestCase("canBroadcastMessage");
        goBack();
        tab_bar.findElement(By.name("Groups")).click();
        IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        try{
            tableView.scrollTo("Appium_Public").click();
        }catch (NotFoundException e){
            fail("Unable to find group");
            throw e;
        }


        nav_bar = driver.findElementByClassName("UIANavigationBar");
        nav_bar.findElement(By.name("more")).click();

        List<IOSElement> actionCells = driver.findElementsByXPath("//UIAActionSheet/UIACollectionView");
        Boolean adminPanelFound = false;
        for(IOSElement actionCell : actionCells){
            try{
                actionCell.findElementByName("Admin Panel").click();
                adminPanelFound = true;
                try{
                    List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
                    tableCells.get(0).findElementByName("Message All Users").click();
                    postDate = new SimpleDateFormat("dd-MM-YY hh:mm").format(new Date());
                    driver.findElementByClassName("UIATextView").sendKeys("Group broadcast through Appium " + postDate);
                    nav_bar.findElement(By.name("Post")).click();
                    WebDriverWait wait = new WebDriverWait(driver, 15);
                    wait.until(ExpectedConditions.textToBePresentInElementValue(driver.findElement(By.xpath("//UIANavigationBar/UIAStaticText")),"Group Wall"));

                    //first view in UICatalog is a table
                    IOSElement table = (IOSElement)driver.findElementByClassName("UIATableView");
                    List<MobileElement> rows = table.findElementsByClassName("UIATableCell");

                    //check that username is correct and message is correct
                    WebElement element = rows.get(0).findElements(By.className("UIAStaticText")).get(0);
                    assertEquals("Appium_Public_Group", element.getText());
                    element = rows.get(0).findElements(By.className("UIAStaticText")).get(1);
                    assertEquals("Group broadcast through Appium " + postDate, element.getText());
                    System.out.println("broadcast message successful");
                }catch (Exception e){
                    fail("Did not broadcast successfully: "+ e.getLocalizedMessage());
                    throw e;
                }
                break;
            }catch (Exception e){
                //not admin panel action
            }
        }
        assertTrue(adminPanelFound, "Could not find 'admin panel' in actionsheet");
        endTestCase("canBroadcastMessage");
    }

    @Test(groups = "adminPanel", priority = 63, enabled = true)
    public void canAddRemoveHelper() throws Exception {
        //replace here to make test fail
        startTestCase("canAddRemoveHelper");
        goToAdminPanel("Appium_Public");
        try{
            ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Group Admins").click();
            List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
            if(tableCells.size() == 0)
                fail("members list is empty");
            else{
                try{
                    MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                    element.findElement(By.name("Add Helper")).click();
                    try{
                        //handling alert
                        WebDriverWait wait = new WebDriverWait(driver, 15);
                        wait.until(ExpectedConditions.alertIsPresent());
                        Alert errorDialog = driver.switchTo().alert();
                        errorDialog.accept();
                        fail("Could not add helper alert is: " + errorDialog.getText());
                    }catch (Exception e){
                        //no alerts = worked as expected
                    }
                }catch (Exception e){
                    fail("Could not add helper. " + e.getLocalizedMessage());
                }

                try{
                    MobileElement element = ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                    element.findElement(By.name("Remove Helper")).click();
                    try{
                        //handling alert
                        WebDriverWait wait = new WebDriverWait(driver, 15);
                        wait.until(ExpectedConditions.alertIsPresent());
                        Alert errorDialog = driver.switchTo().alert();
                        errorDialog.accept();
                        fail("Could not add helper alert is: " + errorDialog.getText());
                    }catch (Exception e){
                        //no alerts = worked as expected
                    }
                    try{
                        element = ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                        element.findElement(By.name("Add Helper"));
                    }catch (Exception e){
                        fail("could not verify user is not helper after clicking 'remove helper'" + e.getLocalizedMessage());
                    }

                }catch (Exception e){
                    fail("could not remove helper" + e.getLocalizedMessage());
                }
            }
        }catch (Exception e){
            fail("Did not block user successfully: "+ e.getLocalizedMessage());
        }
        endTestCase("canAddRemoveHelper");
    }

    @Test(groups = "adminPanel", priority = 64, enabled = true)
    public void canTogglePrivateSwitch() throws Exception {
        startTestCase("canTogglePrivateSwitch");
        //replace here to make test fail
        goToAdminPanel("Appium_Public");

        try{
            MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo("Make Group Private");
            element.findElementByClassName("UIASwitch").click();
            try{
                //handling alert
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.alertIsPresent());
                Alert errorDialog = driver.switchTo().alert();
                errorDialog.accept();
                fail("Could not make group private alert is: " + errorDialog.getText());
            }catch (Exception e){
                //no alerts = worked as expected
            }
        }catch (Exception e){
            fail("Could click on make group private switch. " + e.getLocalizedMessage());
        }

        try{
            ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Pending Requests");
        }catch (Exception e){
            fail("Group not set to private after pressing the switch: pending requests cell is not found");
        }
        try {
            MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo("Make Group Private");
            element.findElementByClassName("UIASwitch").click();
            try{
                //handling alert
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.alertIsPresent());
                Alert errorDialog = driver.switchTo().alert();
                errorDialog.accept();
                fail("Could not make group private alert is: " + errorDialog.getText());
            }catch (Exception e){
                //no alerts = worked as expected
            }
        }catch (Exception e){
            fail("Could click on make group private switch. " + e.getLocalizedMessage());
        }
        try{
            ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Pending Requests");
            fail("Group not set to public after pressing the switch: pending requests cell is found");
        }catch (Exception e){
            //not found = working as expected
        }
        endTestCase("canTogglePrivateSwitch");
    }

    @Test(groups = "adminPanel", priority = 65, enabled = true)
    public void nonPremiumHelperSwitch() throws Exception {
        //replace here to make test fail
        startTestCase("nonPremiumHelperSwitch");
        goToAdminPanel("Appium_Public");

        try{
            MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo("Helper-Mode");
            element.findElementByClassName("UIASwitch").click();
            try{
                //handling alert
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.alertIsPresent());
                Alert errorDialog = driver.switchTo().alert();
                errorDialog.accept();
                fail("Could not make group in helper mode alert is: " + errorDialog.getText());
                return;
            }catch (Exception e){
                //no alerts = worked as expected
            }
        }catch (Exception e){
            fail("Could click on helper mode switch. " + e.getLocalizedMessage());
            throw e;
        }
        try{
            driver.findElementByName("Upgrade to Premium");
            driver.findElementByName("No Thanks").click();
            System.out.println("This is not a premium group could not toggle switch");
            nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e) {
            System.out.println("Did not get \"upgrade to premium alert \"");
            fail();
        }
        endTestCase("nonPremiumHelperSwitch");
    }

    @Test(groups = "adminPanel", priority = 65, enabled = true)
    public void premiumHelperSwitch() throws Exception {
        //replace here to make test fail
       startTestCase("premiumHelperSwitch");
        goToAdminPanel("Appium_Premium");

        try{
            MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo("Helper-Mode");
            element.findElementByClassName("UIASwitch").click();
            try{
                //handling alert
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.alertIsPresent());
                Alert errorDialog = driver.switchTo().alert();
                errorDialog.accept();
                fail("Could not make group in helper mode alert is: " + errorDialog.getText());
                return;
            }catch (Exception e){
                //no alerts = worked as expected
            }
        }catch (Exception e){
            fail("Could click on helper mode switch. " + e.getLocalizedMessage());
            throw e;
        }
        try{
            driver.findElementByName("Upgrade to Premium");
            driver.findElementByName("No Thanks").click();
            System.out.println("This is not a premium group could not toggle switch");
            nav_bar.findElement(By.name("Back")).click();
            fail();
        }catch (Exception e) {
            System.out.println("Did not get \"upgrade to premium alert \"");
        }
        nav_bar.findElement(By.name("Back")).click();
        try{
            List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
            tableCells.get(5).findElementByName("helperIcon");
            System.out.println("Group is in helper mode");
        }catch (NoSuchElementException e){
            fail("Helper icon is not found");
            throw e;
        }

        try {
            nav_bar.findElement(By.name("more")).click();
            List<IOSElement> actionCells = driver.findElementsByXPath("//UIAActionSheet/UIACollectionView");
            for (IOSElement actionCell : actionCells) {
                try {
                    actionCell.findElementByName("Admin Panel").click();
                    break;
                } catch (Exception e) {
                    //not admin panel action
                }
            }
            MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo("Helper-Mode");
            element.findElementByClassName("UIASwitch").click();
            try{
                //handling alert
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.alertIsPresent());
                Alert errorDialog = driver.switchTo().alert();
                errorDialog.accept();
                fail("Could not make group non-helper alert is: " + errorDialog.getText());
                return;
            }catch (Exception e){
                //no alerts = worked as expected
            }
        }catch (Exception e){
            fail("Could not click on helper-mode switch. " + e.getLocalizedMessage());
            throw e;
        }
        try{
            nav_bar.findElement(By.name("Back")).click();
            List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
            tableCells.get(5).findElementByName("helperIcon");
            fail("Helper icon is found although we set it back to non-helper");
            return;
        }catch (NoSuchElementException e){
            System.out.println("Group is not in helper mode");
        }
        endTestCase("premiumHelperSwitch");
    }

    //Assumes the signup test user joined Appium_Private_TestGroup successfully
    @Test(groups = "adminPanel", priority = 66, enabled = true)
    public void canAddPending() throws Exception {
        //replace here to make test fail
        startTestCase("canAddPending");
        goToAdminPanel("Appium_Private");
        /*try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.findElement(By.className("UIAStaticText")).getText().contains("Admin Panel"))
                nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e){

        }
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        if(!nav_bar.findElement(By.className("UIAStaticText")).getText().contains("Admin Panel")) {
            tab_bar.findElement(By.name("Groups")).click();
            IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
            try {
                tableView.scrollTo("Appium_Private").click();
            } catch (NotFoundException e) {
                fail("Unable to find group");
                throw e;
            }


            //ensure that this is now a private group through checking the private icon
            try{
                List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
                tableCells.get(2).findElementByName("privateIcon");
                System.out.println("Group is private");
            }catch (NoSuchElementException e){
                fail("Private icon not found this group should be private");
                throw e;
            }

            nav_bar = driver.findElementByClassName("UIANavigationBar");
            nav_bar.findElement(By.name("more")).click();

            List<IOSElement> actionCells = driver.findElementsByXPath("//UIAActionSheet/UIACollectionView");
            Boolean adminPanelFound = false;
            for (IOSElement actionCell : actionCells) {
                try {
                    actionCell.findElementByName("Admin Panel").click();
                    adminPanelFound = true;
                    break;
                } catch (Exception e) {
                    //not admin panel action
                }
            }
            assertTrue(adminPanelFound, "Could not find 'admin panel' in actionsheet");
        }
*/
        try{
            ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Pending Requests").click();
            List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
            if(tableCells.size() == 0) {
                fail("pending list is empty");
            }
            else {
                try {
                    MobileElement element = ((IOSElement) driver.findElementByXPath("//UIATableView")).scrollTo(nonAdminUsername);
                    element.findElement(By.name("Accept")).click();
                    try {
                        //handling alert
                        WebDriverWait wait = new WebDriverWait(driver, 15);
                        wait.until(ExpectedConditions.alertIsPresent());
                        Alert errorDialog = driver.switchTo().alert();
                        errorDialog.accept();
                        if(!errorDialog.getText().contains("joined")) {
                            fail("Could not add pending user alert is: " + errorDialog.getText());
                            return;
                        }
                    } catch (Exception e) {
                        //no alerts = worked as expected
                    }
                } catch (Exception e) {
                    fail("Could not add pending user " + e.getLocalizedMessage());
                }

            }
        }catch (Exception e){
            fail("Did not add pending user successfully: "+ e.getLocalizedMessage());
        }
        endTestCase("canAddPending");
    }

    @Test(groups = "adminPanel", priority = 67, enabled = true)
    public void canChangeGroupKeywords() throws Exception {
        //replace here to make test fail
        startTestCase("canChangeGroupKeywords");
        goBack();
        tab_bar.findElement(By.name("Groups")).click();
        IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
        try{
            tableView.scrollTo("Appium_Public").click();
        }catch (NotFoundException e){
            fail("Unable to find group");
            throw e;
        }


        nav_bar = driver.findElementByClassName("UIANavigationBar");
        nav_bar.findElement(By.name("more")).click();

        List<IOSElement> actionCells = driver.findElementsByXPath("//UIAActionSheet/UIACollectionView");
        Boolean adminPanelFound = false;
        for(IOSElement actionCell : actionCells) {
            try {
                actionCell.findElementByName("Admin Panel").click();
                adminPanelFound = true;

                List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
                ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Change Group Keywords").click();
                driver.findElementByClassName("UIATextView").sendKeys(" ,appiumKeyWordAdded");
                nav_bar.findElement(By.name("SAVE")).click();
            } catch (Exception e) {
                fail("Could not edit keywords error is: " + e.getLocalizedMessage());
                throw e;
            }
            try {
                //handling alert
                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.alertIsPresent());
                Alert errorDialog = driver.switchTo().alert();
                errorDialog.accept();
                fail("Could not add pending user alert is: " + errorDialog.getText());
            } catch (Exception e) {
                //no alerts = worked as expected
            }
            try {
                List<IOSElement> tableCells = driver.findElementsByXPath("//UIATableView/UIATableCell");
                ((IOSElement)driver.findElementByXPath("//UIATableView")).scrollTo("Change Group Keywords");
            }catch (Exception e){
                fail("Something went wrong. Did not go back to admin panel" + e.getLocalizedMessage());
                throw e;
            }
        }
        endTestCase("canChangeGroupKeywords");
    }

    //endregion

    public void goBack() {
        try {
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            nav_bar.findElement(By.name("Back")).click();
        } catch (Exception e) {

        }
    }

    public void goToAdminPanel(String groupName){
        try{
            nav_bar = driver.findElementByClassName("UIANavigationBar");
            if(!nav_bar.findElement(By.className("UIAStaticText")).getText().contains("Admin Panel"))
                nav_bar.findElement(By.name("Back")).click();
        }catch (Exception e){

        }
        nav_bar = driver.findElementByClassName("UIANavigationBar");
        if(!nav_bar.findElement(By.className("UIAStaticText")).getText().contains("Admin Panel")) {
            tab_bar.findElement(By.name("Groups")).click();
            IOSElement tableView = (IOSElement) driver.findElementByXPath("//UIATableView");
            try {
                tableView.scrollTo(groupName).click();
            } catch (NotFoundException e) {
                fail("Unable to find group");
                throw e;
            }

            nav_bar = driver.findElementByClassName("UIANavigationBar");
            nav_bar.findElement(By.name("more")).click();

            List<IOSElement> actionCells = driver.findElementsByXPath("//UIAActionSheet/UIACollectionView");
            Boolean adminPanelFound = false;
            for (IOSElement actionCell : actionCells) {
                try {
                    actionCell.findElementByName("Admin Panel").click();
                    adminPanelFound = true;
                    break;
                } catch (Exception e) {
                    //not admin panel action
                }
            }
            assertTrue(adminPanelFound, "Could not find 'admin panel' in actionsheet");
        }
    }

    // This is to print log for the beginning of the test case, as we usually run so many test cases as a test suite
    public static void startTestCase(String sTestCaseName){
        log.info("********************************************************************");
        log.info("***************     "+sTestCaseName+ "     *************************");
        log.info("********************************************************************");
    }

    //This is to print log for the ending of the test case
    public static void endTestCase(String sTestCaseName){
        log.info("####################################################################");

    }


        String appPath = "";
        InputStream inputStream;

        public void getPropValues() throws IOException {

            try {
                Properties prop = new Properties();
                String propFileName = "testSettings.properties";

                inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

                if (inputStream != null) {
                    prop.load(inputStream);
                } else {
                    throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
                }


                appPath = prop.getProperty("testSettings.appPath");

            } catch (Exception e) {
                System.out.println("Exception: " + e);
            } finally {
                inputStream.close();
            }
        }
    }

