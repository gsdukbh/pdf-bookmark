package utils

/**
 * @author leejiawei
 * @version TODO
 * @since on  2022/3/7
 */
interface ContentsProvider {
    fun getContentsByUrl(url: String?): String?
}