package md

import Blog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver

class ParseBlog {
    suspend fun parseBlog(url: String): List<Blog> {
        return withContext(Dispatchers.IO) {
            var driver: ChromeDriver? = null
            try {
                driver = setUpWebDriver()
                driver.get(url)

                val blogList = ArrayList<Blog>()
                val divList = ArrayList<WebElement>()
                driver.findElement(By.tagName("section"))?.let { sectionElement ->
                    sectionElement.findElements(By.tagName("div"))?.let { divInfos ->

                        var addIndexZero = false
                        var nextAgain = false
                        divInfos.forEach { div ->
                            if (addIndexZero) {
                                addIndexZero = false
                                nextAgain = true
                                return@forEach
                            }
                            if (nextAgain) {
                                nextAgain = false
                                divList.add(div)
                                return@forEach // continue for get the next div 总共两个 next
                            }
                            div.getAttribute("data-index")?.let { dataIndex ->
                                if (dataIndex == "0") {
                                    addIndexZero = true
                                    return@forEach // continue for get the next div
                                } else {
                                    divList.add(div)
                                }
                            }
                        }
                    }
                }

                divList.forEach { div ->
                    val blog = getBlog(div)
                    if (blog != null) {
                        blogList.add(blog)
                    }
                }

                driver.quit()
                blogList
            } catch (e: Exception) {
                println("parseBlog error: ${e.message}")
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

                divInfo.findElement(By.tagName("time"))?.let {
                    val d = it.getAttribute("datetime").split("T")[0]
                    blog.date = d
                    blog.title = blog.title()

                    return blog
                }
            }
        }
        return null
    }
}