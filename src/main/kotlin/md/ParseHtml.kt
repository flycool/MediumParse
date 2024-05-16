package md

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.openqa.selenium.By
import org.openqa.selenium.By.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.time.Duration
import java.util.HashSet
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ParseHtml {

    suspend fun getMediumMd(title: String, url: String, errorBlock: (String?) -> Unit): String {
        //var chromeDriverService: ChromeDriverService? = null
        var driver: ChromeDriver? = null
        var desPath = ""
        try {
            // 频繁的启动关闭，会增加一个比较明显的延时导致浏览器进程不被关闭的情况发生，
            // 为了避免这一状况我们可以通过ChromeDriverService来控制ChromeDriver进程的生死
//        chromeDriverService =
//            ChromeDriverService.Builder().usingDriverExecutable(File(WEB_DRIVER_PATH)).usingAnyFreePort().build()
            driver = setUpWebDriver()

            val html = seleniumGetPageHtml(driver,url)
            val content = parseMedium(driver, html)

            driver.quit()

            var domain = ""
            var excepted = true
            if (url.contains("proandroiddev")) {
                domain = "proandroiddev"
            } else if (url.contains("medium")) {
                domain = "medium"
            } else {
                excepted = false
            }
            desPath = if (excepted) (BASE_DES_PATH + domain + makeUpPath(title)) else BACK_PATH

            return writeToFile(content, desPath)
        } catch (e: Exception) {
            errorBlock(e.message + desPath)
            return ""
        } finally {
            driver?.quit()
            //chromeDriverService?.stop()
        }
    }

    private fun makeUpPath(title: String): String {
        require(title.isNotEmpty())
        val split = title.split("-")
        val year = split[0]
        val month = split[1]
        return "\\$year\\$month\\$title.md"
    }

    private suspend fun writeToFile(content: String, path: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) {
                val index = path.lastIndexOf("\\")
                val dirPath = path.substring(0, index)
                File(dirPath).mkdirs()

                file.createNewFile()
            }
            OutputStreamWriter(FileOutputStream(file), "utf-8").use {
                it.write(content)
            }
            path
        }
    }

    suspend fun getGistCodeBlock(url: String): String {
        return withContext(Dispatchers.IO) {
            val connect = URL(url).openConnection() as HttpsURLConnection
            val content = connect.inputStream.bufferedReader().use {
                it.readText()
            }
            content
        }
    }

    suspend fun parseMedium(driver: ChromeDriver, html: String): String {

        val doc = Jsoup.parse(html)

        val hSet = HashSet<String>()
        hSet.add("Follow")
        hSet.add("ProAndroidDev")

        val sb = StringBuilder()

        val allElements = doc.allElements
        for (e in allElements) {
            val tagName = e.tagName()
            when (tagName) {
                "button" -> {
                    hSet.add(e.text())
                }

                "div" -> {
                    val attr: String? = e.attr("role")
                    if (attr != null && attr.equals("separator")) {
                        sb.append(separator()).br().br()
                        continue
                    }
                    val aChild: Element? = e.firstElementChild()
                    if (aChild != null && aChild.tagName() == "a") {
                        val atext: String? = aChild.text()
                        if (aChild.attr("data-testid") == "publicationName" || atext == null || atext.isEmpty()) {
                            continue
                        }
                        val h2e: Elements? = e.getElementsByTag("h2")
                        val h2Text = h2e?.text()

                        var link = aChild.attr("href")
                        if (!link.startsWith("https://")) {
                            link = "https://proandroiddev.com" + link
                        }
                        val a = a(h2Text ?: link, link)
                        sb.append(a).br().br()

                        hSet.add(h2Text ?: "")

                        val h3e = e.getElementsByTag("h3")
                        hSet.add(h3e.text())

                        val pe = e.getElementsByTag("p")
                        hSet.add(pe.text())
                    }
                }

                "h1" -> {
                    val h1 = h1(e.text())
                    sb.append(h1).br().br()
                }

                "h2" -> {
                    if (hSet.contains(e.text())) {
                        continue
                    }
                    val h2 = h2(e.text())
                    sb.append(h2).br().br()
                }

                "h3" -> {
                    if (hSet.contains(e.text())) {
                        continue
                    }
                    val h3 = h3(e.text())
                    sb.append(h3).br().br()
                }

                "p" -> {
                    val pe: Element? = e.firstElementChild()
                    if (pe != null && pe.tagName() == "button") {
                        continue
                    }
                    if (e.parent()?.tagName().equals("blockquote") ||
                        hSet.contains(e.text())
                    ) {
                        continue
                    }
                    parseParagraph(sb, e, e.text()).br().br()
                }

                "span" -> {
                    val hasAttr = e.hasAttr("data-selectable-paragraph")
                    if (hasAttr) {
                        val result = parseSpanCode(e.html())
                        sb.append(formatCode(result)).br().br()
                    }
                }

                "blockquote" -> {
                    val quoteText = e.text()
                    sb.append(blockquote(quoteText)).br().br()
                }

                "ol" -> {
                    e.children().forEachIndexed { index, li ->
                        val litext = li.text()
                        val mdliString = ol(index + 1, litext)
                        parseParagraph(sb, li, mdliString).br().br()
                    }
                }

                "ul" -> {
                    e.children().forEachIndexed { _, li ->
                        val litext = li.text()
                        val mdliString = li(litext)
                        parseParagraph(sb, li, mdliString).br().br()
                    }
                }

                "figure" -> {
                    val imgElement = e.getElementsByTag("img")
                    val imgUrl = imgElement.attr("src")
                    if (imgUrl.isNotEmpty()) {
                        val imgText = img("", imgUrl)
                        sb.append(imgText).br()

                        sb.append(e.text()).br().br()
                    }
                }

                "iframe" -> {
                    val frameSrc = e.attr("src")
                    if (frameSrc.isNotEmpty() /*&&
                    (frameSrc.contains("proandroiddev.com") || frameSrc.contains("medium.com"))*/
                    ) {
                        val codes = getGistFormatCode(driver, frameSrc)
                        codes.forEach { code ->
                            val formatCode = formatCode(code)
                            sb.append(formatCode).br().br()
                        }
                    }
                }
            }
        }
        //println(sb.toString())
        return sb.toString()
    }

    fun parseParagraph(sb: StringBuilder, e: Element, content: String): StringBuilder {
        var content2 = content
        val codeSb = StringBuilder()

        val children = e.children()
        for (child in children) {
            val tName = child.tagName()
            val originalText = child.text()
            when (tName) {
                // [**'text'**](link) code->strong->link
                // ['text'](link) code->link
                "code" -> {
                    val codeText = code(originalText)

                    content2 = composeString(codeSb, content2, originalText, codeText)

                    val strongTag: Elements? = child.getElementsByTag("strong")
                    var strongText: String? = null
                    if (strongTag != null) {
                        val text = strongTag.text()
                        if (text.isNotEmpty()) {
                            strongText = bold(codeText)
                            val s = codeSb.toString().replace(codeText, strongText)
                            codeSb.clearAndAppend(s)
                        }
                    }

                    val atag: Elements? = child.getElementsByTag("a")
                    if (atag != null) {
                        val text = atag.text()
                        if (text.isNotEmpty()) {
                            val link = atag.attr("href")
                            val innerText = strongText ?: codeText
                            val alink = a(innerText, link)
                            val s = codeSb.toString().replace(innerText, alink)
                            codeSb.clearAndAppend(s)
                        }
                    }
                }
                // [**text**](link) strong->link
                "strong" -> {
                    val strongText = bold(originalText)
                    content2 = composeString(codeSb, content2, originalText, strongText)

                    val atag: Elements? = child.getElementsByTag("a")
                    if (atag != null) {
                        val text = atag.text()
                        if (text.isNotEmpty()) {
                            val link = atag.attr("href")
                            val alink = a(strongText, link)
                            val s = codeSb.toString().replace(strongText, alink)
                            codeSb.clearAndAppend(s)
                        }
                    }
                }
                // [text](link)
                "a" -> {
                    val link = child.attr("href")
                    val a = a(originalText, link)
                    content2 = composeString(codeSb, content2, originalText, a)
                }
            }
        }
        sb.append(codeSb)
        return sb.append(content2)
    }

    fun composeString(codeSb: StringBuilder, content: String, originalText: String, formatText: String): String {
        val index = content.indexOf(originalText)
        val preString = content.substring(0, index)
        codeSb.append(preString).append(formatText)

        return content.substring(index + originalText.length)
    }

    suspend fun ChromeDriver.waitElement(url: String, timeOut: Long, elementName: By): List<String> =
        suspendCoroutine { cont ->
            this.get(url)
            val wait = WebDriverWait(this, Duration.ofSeconds(timeOut))
            wait.until {
                val gistMetas = this.findElements(elementName)
                gistMetas?.let {
                    val gistCodeList = arrayListOf<String>()
                    gistMetas.forEach { gistMeta ->
                        gistCodeList.add(gistMeta.text)
                    }
                    cont.resume(gistCodeList)
                }
            }
        }

    suspend fun getGistFormatCode(driver: ChromeDriver, frameSrc: String): List<String> {
        val codes = driver.waitElement(frameSrc, 10, className("gist-data"))
        return codes
    }

    fun parseSpanCode(html: String): String {
        val spanList = html.split("<br>")
        val sb = StringBuilder()
        spanList.forEachIndexed { index, s ->
            s.split("</span>").forEach { ss ->
                val code = StringEscapeUtils.unescapeHtml4(removeSpan(ss))
                sb.append(code)
            }
            if (index != (spanList.size - 1)) {
                sb.br()
            }
        }
        return sb.toString()
    }

    private fun removeSpan(s: String): String {
        val arrayIndex = ArrayList<Array<Int>>()
        var indexArray = Array(2) { -1 }
        s.forEachIndexed { index, c ->
            if (c == '<') {
                indexArray = Array(2) { -1 }
                indexArray[0] = index
            } else if (c == '>') {
                indexArray[1] = index
                arrayIndex.add(indexArray)
            }
        }
        var result = s
        arrayIndex.forEach { intArray ->
            val slice = s.slice(IntRange(intArray[0], intArray[1]))
            result = result.replace(slice, "")
        }
        return result
    }

}