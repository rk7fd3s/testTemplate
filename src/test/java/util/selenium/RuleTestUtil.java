/**
 *
 */
package util.selenium;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.RuleResource;

/**
 * seleniumテスト共通ユーティリティ
 *
 * @author r.kinoshita
 *
 */
public class RuleTestUtil extends TestWatcher {
    protected static final Logger log = LoggerFactory.getLogger(RuleTestUtil.class);

    /** 実行中テストメソッド名 */
    protected String testName;

    /**
     * テストメソッド開始時の処理
     */
    @Override
    protected void starting(Description d) {
        testName = d.getMethodName();
        log.debug("[START]  " + testName + " ===================");
    }

    /**
     * テストメソッド終了時の処理
     */
    @Override
    protected void finished(Description d) {
        log.debug("[FINISH] " + testName + " ===================");
    }

    /**
     * @return 現在実施中のテストメソッド名
     */
    public String getMethodName() {
        return testName;
    }

    /**
     * 最初のアクセス<br>
     * 設定ファイルの"firstAccess.*"に準じてアクセスを行います
     *
     * @param driver WebDriverオブジェクト
     * @throws InterruptedException
     */
    public void firstAccess(WebDriver driver) throws InterruptedException {
        Properties configuration = RuleResource.getConfiguration();

        // アクセス
        driver.get(configuration.getProperty("firstAccess.url"));

        // ログイン処理
        // 設定ファイルにfirstAccess.useridがあれば、上記URLがログイン画面だとし、ログイン処理を行う
        if (configuration.getProperty("firstAccess.userid") != null
                && !configuration.getProperty("firstAccess.userid").isEmpty()) {

            Thread.sleep(3000);

            // TODO システムにより要書換
            driver.findElement(By.id("user_id")).clear();
            driver.findElement(By.id("user_id")).sendKeys(configuration.getProperty("firstAccess.userid"));
            driver.findElement(By.id("user_pw")).clear();
            driver.findElement(By.id("user_pw")).sendKeys(configuration.getProperty("firstAccess.userpw"));
            driver.findElement(By.linkText("LOGIN")).click();
        }

        // クッキーの追加
        // 設定ファイルにfirstAccess.cookies("項目=値"で複数の場合","区切り)があれば、cookieを食わす
        String strCookies = configuration.getProperty("firstAccess.cookies");
        if (strCookies != null && !strCookies.isEmpty()) {
            for (String strCookie : strCookies.split(", *")) {
                String name = strCookie.substring(0, strCookie.indexOf("="));
                String value = strCookie.substring(strCookie.indexOf("=") + 1);

                driver.manage().addCookie(new Cookie(name, value));
            }
        }
    }

    /**
     * キャプチャの取得<br>
     * 各テストデータディレクトリに、「テストメソッド名_fileName.png」でスクリーンキャプチャが保存される
     *
     * @param driver WebDriverオブジェクト
     * @param fileName キャプチャファイルの名前
     */
    public void capture(WebDriver driver, String fileName) {
        if (RuleResource.isCapture()) {
            File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File out = new File(RuleResource.getCapturePath(), testName + "_" + fileName + ".png");
            if (!out.getParentFile().exists()) {
                out.getParentFile().mkdirs();
            }
            try {
                FileUtils.copyFile(file, out);
                log.debug("Captured : " + out.getPath());
            } catch (IOException e) {
                log.warn("Cannot captured : " + out.getPath());
            }
        }
    }

    /**
     * 画面上に指定文字列が現れるまで待機します
     *
     * @param driver
     * @param text
     * @throws InterruptedException
     */
    public void waitForTextPresent(WebDriver driver, String text) throws InterruptedException {
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*" + text + "[\\s\\S]*$")) {
                    Thread.sleep(1000);
                    break;
                }
            } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }

    /**
     * 要素が現れるまで待機します
     *
     * @param driver
     * @param by
     * @throws InterruptedException
     */
    public void waitForElementPresent(WebDriver driver, By by) throws InterruptedException {
        waitForElementPresent(driver.findElement(By.cssSelector("BODY")), by);
    }

    /**
     * 要素が現れるまで待機
     *
     * @param element
     * @param by
     * @throws InterruptedException
     */
    public void waitForElementPresent(WebElement element, By by) throws InterruptedException {
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            try { if (isElementPresent(element, by)) break; } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }

    /**
     * 要素の存在確認
     *
     * @param element
     * @param by
     * @return
     */
    protected boolean isElementPresent(WebElement element, By by) {
        try {
            element.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
