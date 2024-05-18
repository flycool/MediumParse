package md

import kotlinx.coroutines.delay
import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

const val CHROME_APP_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"

fun setUpWebDriver(): ChromeDriver {
    val options = ChromeOptions().apply {
        setBinary(CHROME_APP_PATH)
        addArguments("--headless", "--disable-gpu");
    }
    System.setProperty("webdriver.chrome.driver", "lib/chromedriver.exe")

    val driver = ChromeDriver(options)
    return driver
}

suspend fun seleniumGetPageHtml(driver: ChromeDriver, url: String): String {
    //设置足够高，以完全加载网页
    driver.manage().window().size = Dimension(1000, 30000)

    driver.get(url)

//    (driver as JavascriptExecutor).executeScript("window.scrollTo(0, document.body.scrollHeight/3)")
//    Thread.sleep(1000);
//    (driver as JavascriptExecutor).executeScript("window.scrollTo(0, (document.body.scrollHeight)*2/3)")
//    Thread.sleep(1000);
//    (driver as JavascriptExecutor).executeScript("window.scrollTo(0, document.body.scrollHeight)")
//    Thread.sleep(1000);
    //(driver as JavascriptExecutor).executeScript("window.scrollTo(document.body.scrollHeight, 0)")

    delay(500)

    val html = driver.pageSource
    return trimWithArticleTag(html)
}

private fun trimWithArticleTag(html: String): String {
    require(html.isNotEmpty())
    val prefixIndex = html.indexOf("<article>")
    val suffixIndex = html.indexOf("</article>")
    val s = html.substring(prefixIndex, suffixIndex + "</article>".length)
    return s
}
