package utils

import utils.ContentsProvider
import utils.PDFContents
import java.util.HashMap
import utils.ChinaPubContentProvider
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author leejiawei
 * @version TODO
 * @since on  2022/3/7
 */
class PDFContents {
    fun addContentsProvider(site: String, provider: ContentsProvider) {
        providers[site] = provider
    }

    fun removeContentsProvider(site: String) {
        providers.remove(site)
    }

    companion object {
        private val providers: MutableMap<String, ContentsProvider> = HashMap()

        init {
            providers["china-pub.com"] = ChinaPubContentProvider()
        }

        fun getContentsByUrl(url: String): String? {
            var provider: ContentsProvider? = null
            for ((key, value) in providers) {
                if (url.contains(key)) {
                    provider = value
                    break
                }
            }
            if (provider != null) {
                var contents = provider.getContentsByUrl(url)
                if (contents != null) {
                    contents = Stream.of(*contents.split("\n").toTypedArray()).map { obj: String -> obj.trim { it <= ' ' } }
                        .collect(Collectors.joining("\n"))
                }
                return contents
            }
            return null
        }
    }
}