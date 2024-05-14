import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.InspectableModifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Blog(
    var blogTitle: String = "",
    var date: String = "",
    var url: String = "",
    var title: String = "",
) {
    fun title() = if (date.isNotEmpty() && blogTitle.isNotEmpty()) {
        "$date $blogTitle"
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
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
            .background(color = Color.LightGray),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = blog.blogTitle,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = blog.date)
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
    BlogScreen(blogList = blogList) {}
}

val blogList = listOf(
    Blog(
        "jlsdkfdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
        "2024-5-7"
    ),
    Blog("2", "2024-5-7"),
    Blog("3", "2024-5-7"),
    Blog("hello compose", "2024-5-7"),
)