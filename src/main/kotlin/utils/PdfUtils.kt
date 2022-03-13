package utils


import utils.PdfUtils
import utils.PDFContents
import java.util.Arrays
import entity.Bookmark
import kotlin.jvm.JvmOverloads
import java.util.HashMap
import java.util.stream.Collectors
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.util.ArrayList
import java.util.regex.Pattern

/**
 * @author leejiawei
 * @version TODO
 * @since on  2022/3/7
 */
object PdfUtils {
    private val bookmarkPattern = Pattern.compile("^[\t\\s　]*?([0-9.]+)?(.*?)/?[\t\\s　]*([0-9]+)[\t\\s　]*?$")
    private const val blankRegex = "[\t\\s　]+"
    fun replaceBlank(str: String): String {
        return str.replace(blankRegex.toRegex(), " ").trim { it <= ' ' }
    }

    fun addBookmark(bookmarks: String?, srcFile: String?, destFile: String?, pageIndexOffset: Int) {
        if (bookmarks != null && !bookmarks.isEmpty()) {
            if (bookmarks.trim { it <= ' ' }.startsWith("http")) {
                addBookmark(PDFContents.getContentsByUrl(bookmarks), srcFile, destFile, pageIndexOffset)
            } else {
                addBookmark(Arrays.asList(*bookmarks.split("\n").toTypedArray()), srcFile, destFile, pageIndexOffset)
            }
        }
    }

    fun generateBookmark(bookmarks: String, pageIndexOffset: Int, minLens: Int, maxLnes: Int): List<Bookmark> {
        return generateBookmark(Arrays.asList(*bookmarks.split("\n").toTypedArray()), pageIndexOffset, minLens, maxLnes)
    }

    fun generateBookmark(bookmarks: String, pageIndexOffset: Int): List<Bookmark> {
        return generateBookmark(
            Arrays.asList(*bookmarks.split("\n").toTypedArray()),
            pageIndexOffset,
            Int.MIN_VALUE,
            Int.MAX_VALUE
        )
    }

    /**
     * Add a directory to the pdf file
     *
     * @param bookmarks       Directory content, each list element is a directory content, such as：“1.1 Functional vs. Imperative Data Structures 1”
     * @param pageIndexOffset The pdf file is really the offset between the page number and the directory page number.
     * @param minLens         Legal directory entry minimum length
     * @param maxLnes         Legal directory entry maximum length
     * @return Returns a list of bookmarked content
     */
    fun generateBookmark(bookmarks: List<String>, pageIndexOffset: Int, minLens: Int, maxLnes: Int): List<Bookmark> {
        val bookmarkList: MutableList<Bookmark> = ArrayList()
        for ( ln in bookmarks) {
           var ln1 = replaceBlank(ln)
            if (ln1.length < minLens || ln1.length > maxLnes) continue
            val matcher = bookmarkPattern.matcher(ln1)
            if (matcher.find()) {
                val seq = matcher.group(1)
                val title = replaceBlank(matcher.group(2))
                val pageIndex = matcher.group(3).toInt()
                if (seq != null && bookmarkList.size > 0) {
                    val pre = bookmarkList[bookmarkList.size - 1]
                    if (pre.seq == null || seq.startsWith(pre.seq!!)) {
                        pre.addSubBookMarkBySeq(Bookmark(seq, title, pageIndex + pageIndexOffset))
                    } else {
                        bookmarkList.add(Bookmark(seq, title, pageIndex + pageIndexOffset))
                    }
                } else {
                    bookmarkList.add(Bookmark(seq, title, pageIndex + pageIndexOffset))
                }
            } else {
                bookmarkList.add(Bookmark(replaceBlank(ln1)))
            }
        }
        return bookmarkList
    }

    @JvmOverloads
    fun addBookmark(
        bookmarks: List<String>,
        srcFile: String?,
        destFile: String?,
        pageIndexOffset: Int,
        minLens: Int = Int.MIN_VALUE,
        maxLnes: Int = Int.MAX_VALUE
    ) {
        addBookmark(generateBookmark(bookmarks, pageIndexOffset, minLens, maxLnes), srcFile, destFile)
    }

    fun addBookmark(bookmark: Bookmark, srcFile: String?, destFile: String?) {
        addOutlines(Arrays.asList(bookmark.outlines()), srcFile, destFile)
    }

    fun addBookmark(bookmarks: List<Bookmark>, srcFile: String?, destFile: String?) {
        addOutlines(
            bookmarks.stream().map { obj: Bookmark -> obj.outlines() }.collect(Collectors.toList()),
            srcFile,
            destFile
        )
    }

    private fun addOutlines(outlines: List<HashMap<String, Any>>, srcFile: String?, destFile: String?) {
        try {
            class MyPdfReader(fileName: String?) : PdfReader(fileName) {
                init {
                    unethicalreading = true
                    encrypted = false
                }
            }

            val reader: PdfReader = MyPdfReader(srcFile)
            val stamper = PdfStamper(reader, FileOutputStream(destFile))
            stamper.setOutlines(outlines)
            stamper.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}