package com.example;

import com.microsoft.playwright.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LinkedInPage {
    private final Page page;

    public LinkedInPage(Page page) {
        this.page = page;
    }

    public void navigateToLogin() {
        page.navigate("https://www.linkedin.com/login");
    }

    public void login(String login, String password) {
        page.fill("#username", login);
        page.fill("#password", password);
        page.click("button[type=submit]");
    }

    public void verifyWithAuthenticator(String code) {
        page.click("a:has-text('authenticator'), a:has-text('аутентифик')");
        page.fill("input[name=pin]", code);
        page.press("input[name=pin]", "Enter");
    }

    public void chooseAuthenticatorMethodIfPrompted() {
        try {
            Locator link = page.locator("a:has-text('authenticator'), a:has-text('аутентифик')").first();
            if (link.isVisible(new Locator.IsVisibleOptions().setTimeout(8000))) {
                link.click();
            }
        } catch (Exception ignored) {
        }
    }

    public boolean clickNext() {
        String[] selectors = {
                "button[aria-label='Next']",
                "button[aria-label='Далее']",
                "button[aria-label='Следующая']"
        };
        for (String sel : selectors) {
            try {
                Locator btn = page.locator(sel).first();
                if (btn.count() == 0 || !btn.isVisible() || !btn.isEnabled()) continue;
                btn.scrollIntoViewIfNeeded();
                btn.click();
                page.waitForLoadState();
                page.waitForTimeout(1200);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public void gotoSearchPage(String searchAccess, int pageNum) {
        String encoded = URLEncoder.encode(searchAccess, StandardCharsets.UTF_8);
        String url = "https://www.linkedin.com/search/results/people/?keywords=" + encoded + "&page=" + pageNum;
        page.navigate(url);
        page.waitForLoadState();
    }

    public void search(String searchAccess) {
        String encoded = URLEncoder.encode(searchAccess, StandardCharsets.UTF_8);
        page.navigate("https://www.linkedin.com/search/results/people/?keywords=" + encoded);
        page.waitForLoadState();
    }

    public void filterByPeople() {
        page.waitForSelector("button.artdeco-pill--choice:has-text('People')",
                new Page.WaitForSelectorOptions().setTimeout(30000));
        page.click("button.artdeco-pill--choice:has-text('People')");
    }

    public void scrollToBottom() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        page.waitForTimeout(500);
    }

    public void goToPage(int pageNumber) {
        page.click("button[aria-label='Page " + pageNumber + "']");
    }

    private static final String CONNECT_SELECTOR = "a[aria-label^='Invite'][aria-label*='connect']";

    public boolean isConnectButtonVisible() {
        return page.querySelector(CONNECT_SELECTOR) != null;
    }

    public void hoverAndClickConnect() {
        page.locator(CONNECT_SELECTOR).first().scrollIntoViewIfNeeded();
        page.locator(CONNECT_SELECTOR).first().click();
        page.waitForTimeout(800);
    }

    public boolean isInvitationLimitReached() {
        return page.querySelector("div.artdeco-modal__header h2.ip-fuse-limit-alert__header") != null;
    }
    public void waitForSearchResults() {
        page.waitForSelector("main ul li", new Page.WaitForSelectorOptions().setTimeout(30000));
    }

    public boolean isSendButtonVisible() {
        return page.isVisible("button:has-text('Send')");
    }

    public void sendInvitation() {
        page.click("button:has-text('Send')");
        page.waitForTimeout(200);
    }

    public boolean isNextPageAvailable() {
        try {
            // Ждем появления кнопки "Next" до 10 секунд
            page.waitForSelector("button[aria-label='Next']", new Page.WaitForSelectorOptions().setTimeout(10000));
            // Проверяем, существует ли кнопка
            return page.querySelector("button[aria-label='Next']") != null;
        } catch (TimeoutError e) {
            // Если кнопка не появилась за 10 секунд, считаем, что следующей страницы нет
            return false;
        }
    }

    public void waitForTimeout(int millis) {
        page.waitForTimeout(millis);
    }
}