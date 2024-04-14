import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import md.getMediumMd

@Composable
@Preview
fun App() {
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
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = urlText,
                onValueChange = { urlText = it }
            )
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
                        desPath = getMediumMd(urlText) { errorMsg ->
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
        }
    }
}

fun main() = application {
    Window(title = "MediumParse",onCloseRequest = ::exitApplication) {
        App()
    }
}
