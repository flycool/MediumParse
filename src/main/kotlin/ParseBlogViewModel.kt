import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import md.ParseBlog
import md.ParseHtml

class ParseBlogViewModel(
    private val parseBlogClass: ParseBlog,
    private val parseHtmlClass: ParseHtml
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val loadStatus = mutableStateOf(false)
    val blogList = mutableStateListOf<Blog>()

    fun blogFlow(url: String) {
        scope.launch {
            loadStatus.value = true
            val list = parseBlogClass.parseBlog(url)

            blogList.clear()
            blogList.addAll(list)
            loadStatus.value = false
        }
    }

    fun getMediumBlog(
        title: String,
        url: String,
        errorBlock: (String?) -> Unit
    ) {
        scope.launch {
            blogList.add(Blog())
            blogList.map {
                if (it.url == url) {
                    it.isLoading = true
                }
                it
            }
            val desPath = parseHtmlClass.getMediumMd(title, url, errorBlock)

            blogList.removeLast()
            blogList.map {
                if (it.url == url) {
                    it.desPath = desPath
                    it.isLoading = false
                }
                it
            }
        }
    }


}