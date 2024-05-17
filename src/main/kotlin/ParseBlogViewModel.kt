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

    // this is work, cause have two BlogContent
    private var parseBlogJob : Job? = null

    fun blogFlow(url: String) {
        parseBlogJob?.cancel()
        parseBlogJob = scope.launch {
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
    ) {
        scope.launch {
            blogList.map {
                if (it.url == url) {
                    it.isLoading.value = true
                }
                it
            }

            var result = ""
            val desPath = parseHtmlClass.getMediumMd(title, url) { error ->
                result = error.toString()
            }

            blogList.map {
                if (it.url == url) {
                    it.desPath.value = desPath.ifEmpty { result }
                    it.isLoading.value = false
                }
                it
            }
        }
    }

}