import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.winterreisender.webviewko.WebviewKo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import md.getMediumMd

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
                    scope.launch(Dispatchers.IO) {
                        error = ""
                        desPath = ""
                        desPath = getMediumMd(title, urlText) { errorMsg ->
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

            RadioWebsite()
        }
    }
}

val websites = listOf("https://proandroiddev.com", "https://medium.com/androiddevelopers")

@Composable
fun RadioWebsite() {
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

    Button(
        onClick = {
            scope.launch(Dispatchers.IO) {
                WebviewKo().run {
                    url(selectedWebsite)
                    size(500, 1000)
                    show()
                }
            }
        }
    ) {
        Text("go")
    }
}

fun main() = application {
    Window(title = "MediumParse", onCloseRequest = ::exitApplication) {
        App()
    }
}
