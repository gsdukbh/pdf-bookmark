package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import java.awt.FileDialog
import java.io.File
import java.nio.file.Path

/**
 *
 *
 * @author leejiawei
 * @since on  2022/3/7
 * @version TODO
 */
@Composable
fun FrameWindowScope.FileDialog(
    title: String,
    isLoad: Boolean,
    onResult: (result: Path?) -> Unit
) = AwtWindow(
    create = {

        object : FileDialog(window, "Choose a file", if (isLoad) LOAD else SAVE) {
            override fun setFile(file: String?) {
                super.setFile("pdf;*.pdf;")
            }
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title
        }
    },
    dispose = FileDialog::dispose
)