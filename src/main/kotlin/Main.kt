import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.github.winterreisender.webviewko.WebviewKo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import md.MEDIUM
import md.PROANDROIDDEV
import md.getMediumMdWithContext

@Composable
@Preview
fun App() {
    var title by remember { mutableStateOf("") }
    var urlText by remember { mutableStateOf("") }
    var desPath by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var buttonEnable by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "title", modifier = Modifier.width(40.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = { title = it },
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "url", modifier = Modifier.width(40.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = urlText,
                    onValueChange = { urlText = it }
                )
            }
            Button(
                modifier = Modifier.align(alignment = Alignment.End),
                onClick = {
                    if (urlText.isEmpty()) {
                        error = "url must no empty"
                        return@Button
                    }
                    buttonEnable = false
                    isLoading = true
                    scope.launch {
                        error = ""
                        desPath = ""
                        desPath = getMediumMdWithContext(title, urlText) { errorMsg ->
                            error = errorMsg ?: ""
                        }
                        isLoading = false
                        buttonEnable = true
                    }
                },
                enabled = buttonEnable
            ) {
                Text("get")
            }
            if (isLoading) {
                CircularProgressIndicator()
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = desPath,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = error,
                color = Color.Red
            )

            RadioWebsite { blogList ->
                BlogScreen(
                    blogList = blogList,
                    modifier = Modifier.fillMaxWidth(),
                    onBlogClick = { blog ->
                        isLoading = true
                        scope.launch {
                            error = ""
                            desPath = ""
                            desPath = getMediumMdWithContext(blog.title, blog.url) { errorMsg ->
                                error = errorMsg ?: ""
                            }
                            isLoading = false
                            buttonEnable = true
                        }
                    }
                )
            }
        }
    }
}

val websites = listOf(PROANDROIDDEV, MEDIUM)

@Composable
fun RadioWebsite(
    onBlogList: @Composable (List<Blog>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedWebsite by remember { mutableStateOf(websites[0]) }

    websites.forEach { website ->
        Row(
            modifier = Modifier.wrapContentWidth().clickable {
                selectedWebsite = website
            },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = website == selectedWebsite,
                onClick = {
                    selectedWebsite = website
                },
            )
            Text(website)
        }
    }
    var isLoading by remember { mutableStateOf(false) }
    val parseBlogViewModel = remember { ParseBlogViewModel() }
    val blogs by parseBlogViewModel.listFlow.collectAsState()
    var job: Job? = null
    LaunchedEffect(selectedWebsite) {
        job?.cancel()
        isLoading = true
        job = scope.launch {
            parseBlogViewModel.blogFlow(selectedWebsite)
        }
    }
    if (blogs.isNotEmpty()) {
        isLoading = false
    }
    onBlogList(blogs)

    if (isLoading) {
        CircularProgressIndicator()
    }
}

private fun runWebViewKo(url: String) {
    WebviewKo().run {
        url(url)
        size(500, 1000)
        show()
    }
}

fun main() = application {
    val state = WindowState(size = DpSize(800.dp, 900.dp))
    Window(title = "MediumParse", onCloseRequest = ::exitApplication, state = state) {
        App()
    }
}
