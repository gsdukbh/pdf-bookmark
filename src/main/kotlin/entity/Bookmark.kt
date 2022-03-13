package entity

import entity.Bookmark
import java.util.HashMap
import java.lang.StringBuffer
import java.util.ArrayList

/**
 * @author leejiawei
 * @version TODO
 * @since on  2022/3/7
 */
class Bookmark {
    var seq: String? = null
    var pageIndex = -1
    var title: String
    private val subBookMarks: MutableList<Bookmark>? = ArrayList()

    constructor(title: String, pageIndex: Int) {
        this.pageIndex = pageIndex
        this.title = title
    }

    constructor(seq: String?, title: String, pageIndex: Int) {
        this.pageIndex = pageIndex
        this.title = title
        this.seq = seq
    }

    constructor(title: String) {
        this.title = title
    }

    fun getSubBookMarks(): List<Bookmark>? {
        return subBookMarks
    }

    fun addSubBookMark(kid: Bookmark) {
        subBookMarks!!.add(kid)
    }

    fun addSubBookMarkBySeq(kid: Bookmark) {
        for (bookmark in subBookMarks!!) {
            if (kid.seq!!.startsWith(bookmark.seq + ".")) {
                bookmark.addSubBookMarkBySeq(kid)
                return
            }
        }
        subBookMarks.add(kid)
    }

    fun outlines(): HashMap<String, Any> {
        val root = HashMap<String, Any>()
        root["Title"] = (if (seq != null) "$seq " else "") + title
        root["Action"] = "GoTo"
        if (pageIndex >= 0) root["Page"] = String.format("%d Fit", pageIndex)
        val kids = ArrayList<HashMap<String, Any>>()
        if (subBookMarks != null && !subBookMarks.isEmpty()) {
            for (bookmark in subBookMarks) {
                kids.add(bookmark.outlines())
            }
            root["Kids"] = kids
        }
        return root
    }

    override fun toString(): String {
        val indent = "- "
        val sb = StringBuffer()
        if (seq != null) {
            sb.append(seq)
            sb.append(" ")
        }
        sb.append(title)
        if (getSubBookMarks() != null && !getSubBookMarks()!!.isEmpty()) {
            for (bookmark in getSubBookMarks()!!) {
                sb.append("\n")
                sb.append(indent)
                sb.append(bookmark.toString().replace(indent.toRegex(), indent + indent))
            }
        }
        return sb.toString()
    }
}