import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.*
import md.getMediumMd
import md.setUpWebDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class ParseBlogViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //private var _listFlow = MutableStateFlow<List<Blog>>(emptyList())
    //val listFlow = _listFlow.asStateFlow()

    val b = mutableStateListOf<Blog>()

    fun blogFlow(url: String): Job {
        return scope.launch {
            val list = parseBlog(url)
            //_listFlow.value = list
            b.clear()
            b.addAll(list)
        }
    }

    fun getMediumMdWithContext(
        title: String,
        url: String,
        errorBlock: (String?) -> Unit
    ) {
        scope.launch {
            b.add(Blog())
            b.map {
                if (it.url == url) {
                    it.isLoading = true
                }
                it
            }

            val desPath = getMediumMd(title, url, errorBlock)

            b.removeLast()
            b.map {
                if (it.url == url) {
                    it.desPath = desPath
                    it.isLoading = false
                }
                it
            }
        }
    }

    private suspend fun parseBlog(url: String): List<Blog> {
        return withContext(Dispatchers.IO) {
            var driver: WebDriver? = null
            try {
                driver = setUpWebDriver()
                driver.get(url)

                val blogList = ArrayList<Blog>()
                driver.findElement(By.tagName("section"))?.let { sectionElement ->
                    sectionElement.findElements(By.tagName("div"))?.let { divInfos ->
                        divInfos.forEach { div ->
                            div.getAttribute("data-index")?.let { _ ->
                                val blog = getBlog(div)
                                if (blog != null) {
                                    blogList.add(blog)
                                }
                            }
                        }
                    }
                }
                driver.quit()
                blogList
            } catch (e: Exception) {
                driver?.quit()
                emptyList()
            }
        }
    }

    private fun getBlog(divInfo: WebElement): Blog? {
        divInfo.findElements(By.tagName("a"))?.let { aes ->
            aes.forEach { ae ->
                val h3Element: WebElement?
                try {
                    h3Element = ae.findElement(By.tagName("h3"))
                } catch (e: Exception) {
                    return@forEach
                }

                val blog = Blog()
                val url = ae.getAttribute("href")
                blog.url = url
                blog.blogTitle = h3Element.text

                val d = divInfo.findElement(By.tagName("time")).getAttribute("datetime").split("T")[0]
                blog.date = d
                blog.title = blog.title()

                return blog
            }
        }
        return null
    }
}