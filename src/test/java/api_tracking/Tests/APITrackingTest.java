package api_tracking.Tests;

import api_tracking.Base.BaseTest;
import api_tracking.Pages.LoginPage;
import api_tracking.Pages.LandingPage;
import api_tracking.Utils.APIListener;
//import api_tracking.Utils.FileWriteUtil;
import api_tracking.Utils.ExcelReportUtil;
import io.github.cdimascio.dotenv.Dotenv;
import api_tracking.Utils.EmailSender;

import org.testng.annotations.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class APITrackingTest extends BaseTest {

	Dotenv dotenv = Dotenv.load();

	String applicationURL = dotenv.get("APP_URL");

    
    @Test
    public void trackAPIs() throws InterruptedException {

        APIListener listener = new APIListener();
        listener.enableNetworkTracking(devTools);

        driver.get(applicationURL);
        System.out.println("Opened Login Page...");

        // LOGIN
        listener.startSection("LOGIN");
        LoginPage login = new LoginPage(driver);
        driver.get(applicationURL);
        String username = dotenv.get("APP_USERNAME");
        String password = dotenv.get("APP_PASSWORD");
        login.enterUsername(username);
        login.enterPassword(password);
        login.clickLogin();
        Thread.sleep(4000);
        listener.endSection("LOGIN");

        LandingPage landing = new LandingPage(driver);
        Thread.sleep(20000);

        // Element Repo
        listener.startSection("ELEMENT REPO");
        landing.clickDesignStudio();
        Thread.sleep(20000);
        listener.endSection("ELEMENT REPO");

        // Test Case
        listener.startSection("TEST CASE");
        landing.clickTestCase();
        Thread.sleep(20000);
        listener.endSection("TEST CASE");

        // Execution
        listener.startSection("EXECUTION");
        landing.clickExecution();
        Thread.sleep(20000);
        listener.endSection("EXECUTION");

        // Reports
        listener.startSection("REPORTS");
        landing.clickReports();
        Thread.sleep(20000);
        listener.endSection("REPORTS");
        

        // Save JSON Output
        //FileWriteUtil.writeApiData("All_APIs.json", listener.getApiCapturedData());
        

        // Prepare BUILD_INFO
        String executionDateTime =
                new SimpleDateFormat("dd-MMM-yyyy & hh:mm:ss a").format(new Date());

        Map<String, String> buildInfo = new HashMap<>();
        buildInfo.put("Application URL", applicationURL);
        buildInfo.put("Build Version", buildVersion);
        buildInfo.put("Execution Date & Time", executionDateTime);
        buildInfo.put("Browser", browserName);
        
        ExcelReportUtil.setBuildInfo(buildInfo);
        

        // Generate Excel Locally
        try {
            ExcelReportUtil.generateReport(
                    "API_Report.xlsx",
                    listener.getSectionWiseData()
            );
            System.out.println(" Excel Generated Successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        // Logic of moving the Excel file to OneDrive Folder
        try {
            String outputPath = ExcelReportUtil.generateOneDriveReportPath();
            ExcelReportUtil.generateReport(outputPath, listener.getSectionWiseData());

            System.out.println("Excel Generated Successfully!");
            System.out.println("Report saved to: " + outputPath);

        } catch (IOException e) {
        	System.out.println("Failed to generate Excel: " + e.getMessage());
            e.printStackTrace();
        }        
        System.out.println("\n Excel Report Generated: API_Report.xlsx");
        

        // Sending email to multiple users/recipients
        String[] recipients = {
        };
        EmailSender.sendEmailWithAttachment(
                recipients,
                "API_Report.xlsx"
        );
        System.out.println("\n Email Sent Successfully to All Recipients");
        
        
        // Console Summary
        System.out.println("\n======= FINAL API SUMMARY =======");
        for (Map.Entry<String, Map<String, Object>> entry : listener.getApiCapturedData().entrySet()) {
            System.out.println("URL: " + entry.getKey());
            entry.getValue().forEach((k, v) ->
                    System.out.println("   " + k + " : " + v)
            );
        }
        System.out.println("=================================\n");

    }
}