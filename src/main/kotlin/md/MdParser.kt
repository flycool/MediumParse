package md

fun h1(text: String) = "# $text"
fun h2(text: String) = "## $text"
fun h3(text: String) = "### $text"

fun code(text: String) = "`$text`"

fun bold(text: String) = "**${text}**"

fun italic(text: String) = "_${text}_"

fun a(linkName: String, href: String) = "[$linkName]($href)"

fun img(imgName: String, src: String) = "![$imgName]($src)"

fun li(text: String) = "- $text"

fun formatCode(code: String, format: String = "kotlin") = """
```$format
$code
```
""".trimIndent()

fun separator() = "---"

fun StringBuilder.br() = append("\r\n")

fun blockquote(text:String) = "> $text"

fun ol(index:Int, text: String) = "$index. $text"

fun StringBuilder.clearAndAppend(text:String) = clear().append(text)