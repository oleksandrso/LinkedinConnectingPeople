import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.Cookie;

import static com.codeborne.selenide.Configuration.*;
import static com.codeborne.selenide.Selenide.*;

public class TestBase {
    @BeforeAll  // Annotation to indicate that this method should run before all tests
    static void beforeAll() {
        browser = "chrome";  // Set the browser to Microsoft Edge
        browserSize = "1920x1080";  // Set the browser window size
        timeout = 7000;  // Set the timeout for waiting for elements
        headless = true;  // Run tests in headless mode
        baseUrl = "https://www.linkedin.com";  // Set the base URL for tests
        open("");  // Open the base URL

        // Add a session cookie to stay logged in
        WebDriverRunner.getWebDriver().manage().addCookie(new Cookie("li_at", Methods.getSessionToken()));
        refresh();  // Refresh the page to apply the cookie
    }

    @AfterAll
    static void afterAll() {
        closeWebDriver();
    }
}