import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class Methods {
    // Define static variables for username, password, and search page URL
    static String
            username = System.getProperty("Login"),
            password = System.getProperty("Password"),
            searchPageUrl = "https://www.linkedin.com/search/results/people/";

    // Define Selenide elements for various buttons and fields
    static SelenideElement
            contactButton = $x("//button[contains(@class, 'artdeco-button--secondary')]/span[text()='Connect']"),
            contactButtonInMessage = $x("//button[@aria-label='Connect']"),
            sendButton = $x("//span[text()='Send']"),
            errorToast = $("div[data-test-artdeco-toast-item-type='error']"),
            otherButton = $("button[aria-label='Other']"),
            limitAllert = $("[aria-labelledby='ip-fuse-limit-alert__header']"),
            cancelButtonIcon = $("li-icon[type='cancel-icon']"),
            searchInput = $("input.search-global-typeahead__input"),
            globalNavigation = $("#global-nav");
    // Method to login into LinkedIn
    public static void login() {
        open("/login");
        $("#username").sendKeys(username);
        $("#password").sendKeys(password);
        $x("//*[@type='submit']").click();
        globalNavigation.shouldBe(visible);
    }
    // Method to scroll to the bottom of the page
    public static void scrollToBottom() {
        JavascriptExecutor js = (JavascriptExecutor) Selenide.webdriver().driver().getWebDriver();
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }
    // Method to get a specific page button element
    public static SelenideElement page(int pageNumber) {
        return $x("//button[@aria-label='Page " + pageNumber + "']");
    }
    // Method to perform a search
    public static void search(String searchRequest) {
        open(searchPageUrl);
        searchInput.sendKeys(searchRequest);
        searchInput.pressEnter();
    }
    // Method to get the session token for LinkedIn
    public static String getSessionToken() {
        String tokenFilePath = "src/test/resources/session_token.txt"; // Path to the file where the token will be stored
        File tokenFile = new File(tokenFilePath);

        // Check if the token file exists
        if (tokenFile.exists()) {
            try {
                // Read the token from the file
                String sessionToken = new String(Files.readAllBytes(Paths.get(tokenFilePath)));
                return sessionToken;
            } catch (IOException e) {
                throw new RuntimeException("Error reading the token from the file", e);
            }
        }

        login(); // Your login method

        Cookie sessionCookie = getWebDriver().manage().getCookieNamed("li_at");
        if (sessionCookie == null) {
            try {
                throw new Exception("Failed to get session token");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String sessionToken = sessionCookie.getValue();

        // Write the token to the file
        try (FileWriter writer = new FileWriter(tokenFilePath)) {
            writer.write(sessionToken);
        } catch (IOException e) {
            throw new RuntimeException("Error writing the token to the file", e);
        }

        return sessionToken;
    }
}