package com.github.numq.speechrecognition.silero.decoder

internal class SileroDecoder(private val labels: List<String>) : Decoder {
    private val blankIdx: Int = labels.indexOf("_")
    private val spaceIdx: Int = labels.indexOf(" ")
    private val token2Idx: Int = labels.indexOf("2")

    override fun process(probs: Array<FloatArray>) = runCatching {
        require(labels.size == probs[0].size) { "Labels size must match probability matrix second dimension" }

        val argm = probs.map { row -> row.withIndex().maxByOrNull { it.value }?.index ?: 0 }
        val str = mutableListOf<String>()
        val alignList = mutableListOf<MutableList<Int>>(mutableListOf())

        for ((j, i) in argm.withIndex()) {
            if (i == token2Idx) {
                if (str.isNotEmpty()) {
                    val prev = str.last()
                    str.add("$")
                    str.add(prev)
                    alignList.last().add(j)
                    continue
                } else {
                    str.add(" ")
                    alignList.add(mutableListOf())
                    continue
                }
            }
            if (i != blankIdx) {
                str.add(labels.getOrElse(i) { "" })
                if (i == spaceIdx) {
                    alignList.add(mutableListOf())
                } else {
                    alignList.last().add(j)
                }
            }
        }

        str.joinToString("").replace("$", "").replace(Regex("\\s+"), " ").trim()
    }
}
