package api_tracking.Base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class BaseTest {

    public static WebDriver driver;
    public static DevTools devTools;

    
    public static String buildVersion = "";

    public static String browserName = "Chrome";

    @BeforeClass
    public void setUp() {
    	driver = new ChromeDriver(); // Selenium Manager auto handles driver
        devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();
        driver.manage().window().maximize();

        // Fetch build version before starting test
        fetchBuildVersion("fis-beta.avoassure.ai");
    }

    /**
     * Fetch Build Version using REST API call:
     * url = https://{domain}/getClientConfig
     */
    public void fetchBuildVersion(String domain) {
        try {
            String urlString = "https://" + domain + "/getClientConfig";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            System.out.println("GET /getClientConfig → status: " + status);

            if (status != 200) {
                System.out.println(" Failed to fetch build config");
                return;
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            // JSON Parsing
            JSONObject json = new JSONObject(sb.toString());

            if (json.has("version")) {
                buildVersion = json.get("version").toString();
                System.out.println(" Build Version Fetched: " + buildVersion);
            } else {
                System.out.println(" 'version' field not found in response");
            }

        } catch (Exception e) {
            System.out.println(" Error fetching build version: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}