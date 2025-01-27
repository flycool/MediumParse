import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import md.messCodeRegex

data class Blog(
    var blogTitle: String = "",
    var date: String = "",
    var url: String = "",
    var title: String ="",
    var isLoading:  MutableState<Boolean> = mutableStateOf(false),
    var desPath: MutableState<String> = mutableStateOf(""),
) {
    fun title() = if (date.isNotEmpty() && blogTitle.isNotEmpty()) {
        val validTitle = blogTitle.replace(Regex(messCodeRegex), "")
        "$date $validTitle"
    } else ""
}

@Composable
fun BlogScreen(
    blogList: List<Blog>,
    modifier: Modifier = Modifier,
    onBlogClick: (Blog) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(blogList) { blog ->
            BlogItem(blog, onClick = onBlogClick)
        }
    }
}


@Composable
fun BlogItem(
    blog: Blog,
    onClick: (Blog) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
            .background(color = Color.LightGray),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.9f)) {
            Text(
                text = blog.blogTitle,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    uriHandler.openUri(blog.url)
                }
            )
            Text(text = blog.date)
            if (blog.isLoading.value) {
                LinearProgressIndicator()
            }
            if (blog.desPath.value.isNotEmpty()) {
                Text(text = blog.desPath.value)
            }
        }
        Button(
            onClick = {
                onClick(blog)
            },
        ) {
            Text(text = "Go")
        }
    }
}

@Preview
@Composable
fun BlogListPreview() {
    BlogScreen(blogList = sampleBlogList) {}
}

val sampleBlogList = listOf(
    Blog(
        "jlsdkfdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
        "2024-5-7"
    ),
    Blog("2", "2024-5-7"),
    Blog("3", "2024-5-7"),
    Blog("hello compose", "2024-5-7"),
)