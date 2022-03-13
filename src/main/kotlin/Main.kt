// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import ui.FileDialog
import utils.PDFContents
import utils.PdfUtils
import java.nio.file.Path

@Composable
@Preview
fun App(window: FrameWindowScope) {
    var filePath by remember { mutableStateOf("") }
    var pageIndex by remember { mutableStateOf("") }
    var isFileDialogOpen by remember { mutableStateOf(false) }
    var isContentsGenerator by remember { mutableStateOf(false) }
    var isGetContents by remember { mutableStateOf(false) }

    var context by remember { mutableStateOf<String>("") }
    MaterialTheme {
        Column {

            Row {
                TextField(value = filePath,
                    modifier = Modifier.width(400.dp),
                    onValueChange = { filePath = it },
                    label = { Text("PDF 文件地址") })
                TextField(value = pageIndex,
                    onValueChange = { pageIndex = it },
                    modifier = Modifier.padding(start = 8.dp).width(100.dp),
                    label = { Text("页面偏移") })
                Button(
                    onClick = { isFileDialogOpen = true },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Text("选择 PDF", style = TextStyle(fontSize = 20.sp))
                }
            }


            TextField(value = context,
                onValueChange = { context = it },
                maxLines = 10,
                textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                label = { Text("目录") })

            Row {

                Button(
                    onClick = { isGetContents = true },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Text("获取目录", style = TextStyle(fontSize = 20.sp))
                }

                Button(
                    onClick = {
                        isContentsGenerator = true
                    },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Text("生成 PDF", style = TextStyle(fontSize = 20.sp))
                }
            }
        }
        var path by remember { mutableStateOf<Path?>(null) }
        var isPDF by remember { mutableStateOf(false) }
        if (isFileDialogOpen) {
            window.FileDialog(
                title = "选择 PDF 文件",
                isLoad = true,
                onResult = {
                    isFileDialogOpen = false
                    path = it
                    if (!path?.fileName.toString().endsWith(".pdf")) {
                        isPDF = true
                    }
                }
            )
        }
        if (isPDF) {
            Dialog(title = "错误！不是pdf 文件", onCloseRequest = { isPDF = false }) {
                Text("请选择pdf 文件 ", color = Color.Red)
            }
        }
        if (path != null) {
            filePath = path.toString()
        }
        // 获取目录
        if (isGetContents) {
            if (context.isEmpty()) {
                var isEmpty by remember { mutableStateOf(false) }
                if (isEmpty) {
                    Dialog(title = "错误！目录不能为空", onCloseRequest = { isEmpty = false }) {
                        Text("请输入目录 or 地址  ", color = Color.Red)
                    }
                }
            }

            if (context.isNotEmpty() && context.startsWith("http")) {
                context = PDFContents.getContentsByUrl(context).toString()
            }

        }

        // 生成pdf 目录文件
        if (isContentsGenerator) {
            if (pageIndex.isNotEmpty() && !pageIndex.matches("[0-9]+".toRegex())) {
                isGetContents = true
                var isDialogOpen by remember { mutableStateOf(false) }
                if (isDialogOpen) {
                    Dialog(
                        title = "错误",
                        onCloseRequest = { isDialogOpen = false }
                    ) {
                        Text("偏移量设置错误,页码偏移量只能为整数", color = Color.Red)
                    }
                }
            }
            if (path == null) {
                var isPathNull by remember { mutableStateOf(true) }
                if (!isPathNull) {
                    Dialog(title = "错误！", onCloseRequest = { isPathNull = false }) {
                        Text("请选择pdf 文件 ", color = Color.Red)
                    }
                }
            }
            val srcFile = filePath
            val srcFileName = path?.fileName.toString()
            val ext = srcFileName.substring(srcFileName.lastIndexOf("."))
            val destFile = srcFile.substring(0, srcFile.lastIndexOf(srcFileName)) + srcFileName.substring(
                0,
                srcFileName.lastIndexOf(".")
            ) + "_含目录" + ext;
            if (pageIndex == "") {
                pageIndex = "0"
            }
            val offset = pageIndex.toInt()
            if (context.isNotEmpty()) {

                PdfUtils.addBookmark(
                    context,
                    srcFile,
                    destFile,
                    offset
                )
                var isSuccess by remember { mutableStateOf(true) }
                if (isSuccess) {
                    Dialog(title = "成功", onCloseRequest = { isSuccess = false }) {
                        Column {
                            Text("生成成功", color = Color.Green)
                            Text("文件路径：$destFile", color = Color.Green)
                        }
                    }
                }

            }

        }

    }
}


fun main() = application {
    val icon = painterResource("pdf.png")
    Tray(
        icon = icon,
        menu = {
            Item("Quit App", onClick = ::exitApplication)
        }
    )
    Window(onCloseRequest = ::exitApplication, title = "pdf bookmark", icon = icon) {
        App(this)

    }
}
