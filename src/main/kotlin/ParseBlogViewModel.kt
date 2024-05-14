import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import md.setUpWebDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class ParseBlogViewModel {

    private var _listFlow = MutableStateFlow<List<Blog>>(emptyList())
    val listFlow = _listFlow.asStateFlow()

    suspend fun blogFlow(url: String) {
        _listFlow.value = parseBlog(url)
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
                emptyList<Blog>()
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