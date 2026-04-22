package com.example;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class LinkedInTest {
    private static final Path USER_DATA_DIR = Paths.get(".playwright-profile");

    private static Playwright playwright;
    private static BrowserContext context;
    private static Page page;
    private static LinkedInPage linkedInPage;
    private int coin = 0;

    @BeforeAll
    public static void setup() {
        playwright = Playwright.create();
        context = playwright.chromium().launchPersistentContext(USER_DATA_DIR,
                new BrowserType.LaunchPersistentContextOptions()
                        .setHeadless(false)
                        .setSlowMo(100)
                        .setViewportSize(1280, 800));
        page = context.pages().isEmpty() ? context.newPage() : context.pages().get(0);
        linkedInPage = new LinkedInPage(page);
    }

    @AfterAll
    public static void teardown() {
        if (context != null) {
            context.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void diagnose() {
        Properties props = loadProperties();
        String login = props.getProperty("linkedin.login");
        String password = props.getProperty("linkedin.password");
        String searchAccess = props.getProperty("linkedin.searchAccess");

        if (!ensureLoggedIn(login, password)) {
            throw new RuntimeException("Login failed");
        }
        linkedInPage.search(searchAccess);
        page.waitForSelector("main ul li", new Page.WaitForSelectorOptions().setTimeout(30_000));
        linkedInPage.scrollToBottom();
        page.waitForTimeout(2000);

        Object deep = page.evaluate("() => {\n"
            + "  const q = sel => Array.from(document.querySelectorAll(sel));\n"
            + "  const allLinks = q('main a, main button').map(el => ({\n"
            + "    tag: el.tagName,\n"
            + "    text: (el.innerText || '').trim().slice(0,60),\n"
            + "    aria: el.getAttribute('aria-label'),\n"
            + "    href: el.getAttribute('href')\n"
            + "  })).filter(x => x.text || x.aria);\n"
            + "  const uniqueTexts = {};\n"
            + "  allLinks.forEach(x => { const k = (x.text||'')+'|'+(x.aria||''); uniqueTexts[k] = (uniqueTexts[k]||0)+1; });\n"
            + "  const sampleCards = q('main ul li').slice(0, 3).map(li => ({\n"
            + "    htmlSnippet: li.outerHTML.slice(0, 800),\n"
            + "    buttons: Array.from(li.querySelectorAll('button,a')).map(b => ({tag: b.tagName, text: (b.innerText||'').trim().slice(0,40), aria: b.getAttribute('aria-label')}))\n"
            + "  }));\n"
            + "  const paginationHtml = (q('.artdeco-pagination, nav[aria-label*=Pagin], [class*=pagination]')[0] || {outerHTML: 'NONE'}).outerHTML;\n"
            + "  return {\n"
            + "    url: location.href,\n"
            + "    totalMainButtonsLinks: allLinks.length,\n"
            + "    uniqueTextCounts: uniqueTexts,\n"
            + "    sampleCards,\n"
            + "    paginationHtml: paginationHtml.slice(0, 500)\n"
            + "  };\n"
            + "}");
        System.out.println("========== DEEP DIAG ==========");
        System.out.println(deep);
        System.out.println("========== END DIAG ==========");
    }

    @Test
    public void addPeople() {
        Properties props = loadProperties();
        String login = props.getProperty("linkedin.login");
        String password = props.getProperty("linkedin.password");
        String searchAccess = props.getProperty("linkedin.searchAccess");

        if (!ensureLoggedIn(login, password)) {
            throw new RuntimeException("Login failed — manual 2FA not completed in time");
        }

        int maxPages = 10;
        for (int pageNum = 1; pageNum <= maxPages; pageNum++) {
            System.out.println("--- Processing page " + pageNum + " ---");
            linkedInPage.gotoSearchPage(searchAccess, pageNum);
            try {
                page.waitForSelector("main ul li", new Page.WaitForSelectorOptions().setTimeout(30_000));
            } catch (TimeoutError e) {
                System.out.println("No more results on page " + pageNum);
                break;
            }
            linkedInPage.scrollToBottom();
            try {
                processConnectsOnCurrentPage();
            } catch (RuntimeException e) {
                if ("__LIMIT__".equals(e.getMessage())) return;
                throw e;
            }
        }
        System.out.println("Total added: " + coin + " members in this session");
        coin = 0;
    }

    private boolean ensureLoggedIn(String login, String password) {
        page.navigate("https://www.linkedin.com/feed/");
        page.waitForLoadState();
        if (page.url().contains("/feed")) {
            System.out.println("=== Session reused: already logged in ===");
            return true;
        }
        System.out.println("=== No saved session, logging in... ===");
        linkedInPage.navigateToLogin();
        linkedInPage.login(login, password);
        page.waitForTimeout(3000);
        linkedInPage.chooseAuthenticatorMethodIfPrompted();
        System.out.println("=== Enter 2FA code (and pass captcha if shown) in the browser. Waiting up to 5 minutes... ===");
        try {
            page.waitForURL("**/feed/**", new Page.WaitForURLOptions().setTimeout(300_000));
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    private void processConnectsOnCurrentPage() {
        int safetyCap = 50;
        while (safetyCap-- > 0 && linkedInPage.isConnectButtonVisible()) {
            try {
                linkedInPage.hoverAndClickConnect();
                if (linkedInPage.isInvitationLimitReached()) {
                    System.out.println("Invitation limit reached");
                    throw new RuntimeException("__LIMIT__");
                }
                if (linkedInPage.isSendButtonVisible()) {
                    linkedInPage.sendInvitation();
                }
                coin++;
                System.out.println("Added: " + coin + " members");
            } catch (RuntimeException e) {
                if ("__LIMIT__".equals(e.getMessage())) throw e;
                System.out.println("Error while adding: " + e.getMessage());
                linkedInPage.waitForTimeout(1000);
            } catch (Exception e) {
                System.out.println("Error while adding: " + e.getMessage());
                linkedInPage.waitForTimeout(1000);
            }
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Configuration file 'config.properties' not found in classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration: " + e.getMessage(), e);
        }
        return props;
    }
}
