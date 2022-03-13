package utils

import utils.ContentsProvider
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.RuntimeException

/**
 * @author leejiawei
 * @version TODO
 * @since on  2022/3/7
 */
class ChinaPubContentProvider : ContentsProvider {
    override fun getContentsByUrl(url: String?): String? {
        var contents: String? = null
        contents = try {
            val doc = Jsoup.connect(url).get()
            val contentsHtml = doc.select("#ml + div").first().html().replace("<br>".toRegex(), "###")
            Jsoup.parse(contentsHtml).text().replace("###".toRegex(), "\n")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return contents
    }
}