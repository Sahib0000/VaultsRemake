package net.carnagepvp.vaultsremake.core.util.chat

import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import java.util.*

object CC {

    @JvmStatic
    fun formatSpawnerName(name: String): String {
        return WordUtils.capitalize(name.replace("_", " ").lowercase(Locale.ROOT))
    }

    @JvmStatic
    fun translate(toTranslate: String): String = ChatColor.translateAlternateColorCodes('&', toTranslate)

    @JvmStatic
    fun strip(toStrip: String): String = ChatColor.stripColor(toStrip)

    fun translate(toTranslate: List<String>): List<String> = toTranslate.map { translate(it) }

    fun List<String>.translated(): List<String> = map { translate(it) }

    fun String.translated(): String = translate(this)

    fun String.stripped(): String = strip(this)

    @JvmStatic
    fun format(string: String, vararg arguments: Any?): String {
        var result = string
        if (arguments.isNotEmpty()) {
            arguments.forEachIndexed { index, arg ->
                result = result.replace("{$index}", arg?.toString() ?: "")
            }
        }
        return translate(result)
    }

    fun repeat(string: String, amount: Int): String = string.repeat(amount)

    @JvmStatic
    fun center(text: String): String {
        val colored = translate(text)
        var messagePxSize = 0
        var previousCode = false
        var isBold = false

        for (c in colored) {
            when {
                c == '§' -> previousCode = true
                previousCode -> {
                    previousCode = false
                    isBold = c == 'l' || c == 'L'
                }

                else -> {
                    val dFI = DefaultFontInfo.getDefaultFontInfo(c)
                    messagePxSize += if (isBold) dFI.boldLength else dFI.length
                    messagePxSize++
                }
            }
        }

        val halvedMessageSize = messagePxSize / 2
        val toCompensate = 154 - halvedMessageSize
        val spaceLength = DefaultFontInfo.SPACE.length + 1
        val compensated = (toCompensate / spaceLength).coerceAtLeast(0)
        return "${" ".repeat(compensated)}$colored"
    }

    @JvmStatic
    fun center(text: List<String>): List<String> = text.map { center(it) }

    enum class DefaultFontInfo(val character: Char, val length: Int) {
        A('A', 5), a('a', 5), B('B', 5), b('b', 5), C('C', 5), c('c', 5), D('D', 5), d('d', 5),
        E('E', 5), e('e', 5), F('F', 5), f('f', 4), G('G', 5), g('g', 5), H('H', 5), h('h', 5),
        I('I', 3), i('i', 1), J('J', 5), j('j', 5), K('K', 5), k('k', 4), L('L', 5), l('l', 1),
        M('M', 5), m('m', 5), N('N', 5), n('n', 5), O('O', 5), o('o', 5), P('P', 5), p('p', 5),
        Q('Q', 5), q('q', 5), R('R', 5), r('r', 5), S('S', 5), s('s', 5), T('T', 5), t('t', 4),
        U('U', 5), u('u', 5), V('V', 5), v('v', 5), W('W', 5), w('w', 5), X('X', 5), x('x', 5),
        Y('Y', 5), y('y', 5), Z('Z', 5), z('z', 5), SPACE(' ', 3), DEFAULT('a', 4);

        val boldLength: Int
            get() = if (this == SPACE) length else length + 1

        companion object {
            fun getDefaultFontInfo(c: Char): DefaultFontInfo =
                entries.find { it.character == c } ?: DEFAULT
        }
    }
}
