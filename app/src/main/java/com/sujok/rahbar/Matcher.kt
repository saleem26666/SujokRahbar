package com.sujok.rahbar

object Matcher {
    fun match(query: String): LocalGuide? {
        val raw = query.trim()
        if (raw.length < 2) return null
        val tokens = tokens(raw)
        val scored = Knowledge.guides.map { g -> g to score(raw, tokens, g) }
        val best = scored.maxByOrNull { it.second } ?: return null
        val second = scored.map { it.second }.sortedDescending().getOrNull(1) ?: 0
        if (best.second < 8) return null
        if (best.second - second < 2 && second >= 8) {
            val a = best.first
            val b = scored.filter { it.second == second && it.first.id != a.id }.maxByOrNull { it.second }?.first
            if (b != null && a.id != b.id) {
                return listOf(a, b).minBy { g ->
                    g.aliases.minOf { al -> dist(norm(raw), norm(al)) }
                }
            }
        }
        return best.first
    }

    private fun score(raw: String, tokens: List<String>, g: LocalGuide): Int {
        val q = norm(raw)
        var s = 0
        val aliases = g.aliases.map { norm(it) }.filter { it.length >= 2 }
        if (aliases.any { it == q }) s += 30
        tokens.forEach { tok ->
            if (aliases.any { it == tok }) s += 20
            aliases.forEach { al ->
                if (tok.length >= 5 && al.length >= 4) {
                    val d = dist(tok, al)
                    if (d == 0) s += 18
                    else if (d == 1) s += 16
                    else if (d == 2 && tok.length >= 7 && al.length >= 7) s += 8
                }
                if (al.length >= 4 && tok.length >= 4 && (al.startsWith(tok) || tok.startsWith(al)) &&
                    kotlin.math.abs(al.length - tok.length) <= 1
                ) {
                    s += 10
                }
            }
        }
        val blob = " $q "
        g.aliases.forEach { al ->
            val a = norm(al)
            if (a.length >= 3 && " $a " in blob) s += 14
        }
        return s
    }

    fun tokens(raw: String): List<String> {
        return norm(raw).split(" ").filter { it.length >= 2 }
    }

    fun norm(s: String): String {
        return s.lowercase()
            .replace("أ", "ا").replace("آ", "ا").replace("إ", "ا")
            .replace("ی", "ي").replace("ے", "ي")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun dist(a: String, b: String): Int {
        if (a == b) return 0
        val m = a.length
        val n = b.length
        if (m == 0) return n
        if (n == 0) return m
        if (kotlin.math.abs(m - n) > 4) return 99
        val dp = IntArray(n + 1) { it }
        for (i in 1..m) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..n) {
                val cur = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + cost)
                prev = cur
            }
        }
        return dp[n]
    }

    fun sourceOk(src: WebSource, guide: LocalGuide?): Boolean {
        if (guide == null) return true
        val t = "${src.title} ${src.snippet} ${src.url}".lowercase()
        val hitTopic = guide.topic.any { it.lowercase() in t }
        val hitBan = guide.bans.any { it.lowercase() in t }
        if (hitBan && !hitTopic) return false
        return true
    }
}
