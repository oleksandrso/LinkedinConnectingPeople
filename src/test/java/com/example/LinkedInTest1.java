package com.example;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.example.utils.loadProperties;

public class LinkedInTest1 {
    private static Playwright playwright;
    private static Browser browser;
    private static Page page;
    private int coin = 0;


    Properties props = loadProperties();
    String login = props.getProperty("linkedin.login");
    String password = props.getProperty("linkedin.password");
    String searchAccess = props.getProperty("linkedin.searchAccess");
    String code = props.getProperty("linkedin.code");

    @BeforeAll
    public static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        page = context.newPage();
    }

    @AfterAll
    public static void teardown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void addPeople() {
        page.navigate("https://www.linkedin.com/login");

        // Ввести логин
        page.fill("input#username", login);
        // Ввести пароль
        page.fill("input#password", password);

        // Нажать на кнопку входа
        page.click("button[type=submit]");

        // Пройти проверку с помощью приложения-аутентификатора
        page.click("text=Verify using authenticator app");
        page.fill("input[aria-label='Please enter the code here']", code);
        page.press("input[aria-label='Please enter the code here']", "Enter");

        // Выполнить поиск

        page.fill("input[placeholder='Search']", searchAccess);
        page.press("input[placeholder='Search']", "Enter");

        page.waitForSelector("div.search-results-container", new Page.WaitForSelectorOptions().setTimeout(30000));
        sleep(1500);
        page.waitForSelector("button.artdeco-pill--choice:has-text('People')", new Page.WaitForSelectorOptions().setTimeout(30000));
        page.click("button.artdeco-pill--choice:has-text('People')");
        sleep(2500);
        scrollToBottom();

        // Начать со страницы
        int startPage = 1;
        page.click("button[aria-label='Page " + startPage + "']");

        for (int i = startPage + 1; i < 100; i++) {
            sleep(2500);


            while (page.isVisible("button:has-text('Connect')")) {
                page.hover("button:has-text('Connect')");
                page.click("button:has-text('Connect')");
                sleep(500);


                if (page.isVisible("div[role='alertdialog'] h2:has-text('Withdraw invitation')")) {
                    page.click("button:has-text('Withdraw')");
                    sleep(500);

                }
                if(page.isVisible("//div[contains(@class, 'artdeco-modal__header')]//h2[contains(@class, 'ip-fuse-limit-alert__header')]")){
                    break;
                }

//                if (page.isVisible("button:has-text('Other')")) {
//                    page.click("button:has-text('Other')");
//                    page.click("button:has-text('Add a note')");
//                    sleep(500);
//                }

                if (page.isVisible("div[role='alert']")) {
                    page.click("button[aria-label='Dismiss']");
                    sleep(500);
                    break;
                }

//                page.waitForSelector("button:has-text('Send without a note')");
//                page.click("button:has-text('Send without a note')");
                page.waitForSelector("button:has-text('Send')");
                page.click("button:has-text('Send')");
                sleep(200);

                if (page.isVisible("//button[span/text()='Got it']")) {
                    page.click("//button[span/text()='Got it']");
                    sleep(100);
                }
                if (page.isVisible("div[role='alert'][aria-label='Limit reached']")) {
                    break;
                }

                coin++;
                System.out.println("I have already added: " + coin + " members");
            }
            page.click("button[aria-label='Page " + i + "']");
            System.out.println("Page: " + i);
        }

        System.out.println("I have added: " + coin + " members in this session");
        coin = 0;
    }

    private void scrollToBottom() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("error sleep");
        }
    }
}


