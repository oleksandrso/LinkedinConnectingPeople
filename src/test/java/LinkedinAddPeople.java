import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class LinkedinAddPeople extends TestBase{
    int coin = 0, startPage = 1;  // Initialize counters for added connections and the starting page

    @Test
    void addPeople() {
        Methods.search("it recruiter kyiv");  // Search for "it hr ukraine" on LinkedIn
        sleep(1500);  // Wait for 1.5 seconds
        Methods.scrollToBottom();  // Scroll to the bottom of the page
        actions().scrollToElement(Methods.page(startPage)).click();  // Scroll to and click on the starting page

        // Label for outer loop
        startCircle:
        for (int i = startPage + 1; i < 100; i++) {  // Loop through pages
            $(".entity-result").shouldBe(visible, Duration.ofSeconds(10));  // Wait for search results to be visible
            sleep(2500);  // Wait for 2.5 seconds

            // Label for inner loop
            insideCircle:
            while (Methods.contactButton.isDisplayed()) {  // While the "Connect" button is displayed
                Methods.contactButton.hover().click();  // Hover over and click the "Connect" button
                sleep(500);  // Wait for 0.5 seconds

                // Handle the "Other" button if displayed
                if (Methods.otherButton.isDisplayed()) {
                    Methods.otherButton.click();
                    Methods.contactButtonInMessage.click();
                    sleep(500);
                }
                // Handle error messages
                if (Methods.errorToast.isDisplayed()) {
                    Methods.cancelButtonIcon.click();  // Click the cancel button
                    sleep(500);
                    break insideCircle;  // Break the inner loop
                }
                Methods.sendButton.shouldBe(visible);  // The "Send" button should be visible
                Methods.sendButton.click();  // Click the "Send" button
                sleep(500);  // Wait for 0.5 seconds

                // Handle connection limit alerts
                if (Methods.limitAllert.isDisplayed()) {
                    break startCircle;  // Break the outer loop
                }

                coin++;  // Increment the counter for added connections
                System.out.println("I have already added: " + coin + " members");  // Print the number of added connections
            }
            Methods.page(i).click();  // Click to go to the next page
            System.out.println("Page: " + i);  // Print the current page number
        }

        System.out.println("I have added: " + coin + " members in this session");  // Print the total number of added connections in this session
        coin = 0;  // Reset the counter
    }
}