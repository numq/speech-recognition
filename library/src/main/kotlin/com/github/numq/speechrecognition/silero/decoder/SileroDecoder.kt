package com.github.numq.speechrecognition.silero.decoder

internal class SileroDecoder(private val labels: List<String>) : Decoder {
    private val blankIdx: Int = labels.indexOf("_")
    private val spaceIdx: Int = labels.indexOf(" ")
    private val token2Idx: Int = labels.indexOf("2")

    override fun process(probs: Array<FloatArray>) = runCatching {
        require(labels.size == probs[0].size) { "Labels size must match probability matrix second dimension" }

        val argm = probs.map { row -> row.withIndex().maxByOrNull { it.value }?.index ?: 0 }

        val forString = mutableListOf<String>()
        val alignList = mutableListOf<MutableList<Int>>(mutableListOf())

        for ((j, i) in argm.withIndex()) {
            if (i == token2Idx) {
                if (forString.isNotEmpty()) {
                    val prev = forString.last()
                    forString.add("$")
                    forString.add(prev)
                    alignList.last().add(j)
                } else {
                    forString.add(" ")
                    alignList.add(mutableListOf())
                }
                continue
            }

            if (i != blankIdx) {
                forString.add(labels.getOrElse(i) { "" })

                if (i == spaceIdx) {
                    alignList.add(mutableListOf())
                } else {
                    alignList.last().add(j)
                }
            }
        }

        forString.fold(mutableListOf<String>()) { acc, char ->
            if (acc.isEmpty() || acc.last() != char) {
                acc.add(char)
            }
            acc
        }.joinToString("").replace("$", "").replace(Regex("\\s+"), " ").trim()
    }
}
