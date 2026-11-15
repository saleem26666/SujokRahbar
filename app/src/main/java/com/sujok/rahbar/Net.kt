package com.sujok.rahbar

import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

object Net {
    private const val UA = "SujokRahbar/1.0 (educational Android app; sujok correspondence guide)"

    fun searchWeb(query: String, guide: LocalGuide? = null): List<WebSource> {
        val q = buildString {
            append("sujok ")
            append(query)
            append(" correspondence pressure point")
            if (guide != null) {
                append(" ")
                append(guide.topic.take(3).joinToString(" "))
            }
        }
        val out = ArrayList<WebSource>()
        out += duckDuckGo(q)
        out += wikipedia("sujok ${guide?.searchEn ?: query}")
        return out.distinctBy { it.url }
            .map { src ->
                val extra = topicBonus(src, guide)
                src.copy(score = src.score + extra)
            }
            .sortedByDescending { it.score }
    }

    fun searchImages(query: String): List<String> {
        val urls = LinkedHashSet<String>()
        urls += openverse(query)
        urls += wikimedia(query)
        if (urls.size < 3) {
            urls += wikimedia("$query hand")
        }
        return urls.filter { it.startsWith("http") }.take(10)
    }

    fun translateUr(text: String): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.length < 8) return ""
        val chunk = clean.take(400)
        return try {
            val url =
                "https://api.mymemory.translated.net/get?q=${enc(chunk)}&langpair=en|ur"
            val json = JSONObject(httpGet(url))
            json.optJSONObject("responseData")?.optString("translatedText").orEmpty()
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun duckDuckGo(q: String): List<WebSource> {
        return try {
            val doc = Jsoup.connect("https://html.duckduckgo.com/html/")
                .userAgent(UA)
                .data("q", q)
                .timeout(15000)
                .followRedirects(true)
                .get()
            doc.select("div.result, div.web-result").mapNotNull { el ->
                val a = el.selectFirst("a.result__a") ?: return@mapNotNull null
                val title = a.text().ifBlank { return@mapNotNull null }
                var href = a.absUrl("href").ifBlank { a.attr("href") }
                if (href.contains("uddg=")) {
                    href = java.net.URLDecoder.decode(
                        href.substringAfter("uddg=").substringBefore("&"),
                        "UTF-8",
                    )
                }
                val snip = el.selectFirst(".result__snippet, a.result__snippet")?.text().orEmpty()
                WebSource(title, href, snip, "", scoreOf(href, title, snip))
            }.take(8)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun wikipedia(q: String): List<WebSource> {
        return try {
            val url =
                "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=${enc(q)}&utf8=1&format=json"
            val json = JSONObject(httpGet(url, mapOf("Accept" to "application/json")))
            val arr = json.optJSONObject("query")?.optJSONArray("search") ?: return emptyList()
            (0 until arr.length()).map { i ->
                val it = arr.getJSONObject(i)
                val title = it.optString("title")
                val snip = Jsoup.parse(it.optString("snippet")).text()
                val page = "https://en.wikipedia.org/wiki/${enc(title).replace("+", "_")}"
                WebSource(title, page, snip, "", scoreOf(page, title, snip) + 2)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun openverse(q: String): List<String> {
        return try {
            val url = "https://api.openverse.org/v1/images/?q=${enc(q)}&page_size=8"
            val json = JSONObject(httpGet(url, mapOf("Accept" to "application/json")))
            val arr = json.optJSONArray("results") ?: return emptyList()
            (0 until arr.length()).mapNotNull { i ->
                arr.getJSONObject(i).optString("url").takeIf { it.startsWith("http") }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun wikimedia(q: String): List<String> {
        return try {
            val url =
                "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=${enc(q)}&gsrlimit=8&prop=imageinfo&iiprop=url&iiurlwidth=900&format=json"
            val json = JSONObject(httpGet(url, mapOf("Accept" to "application/json")))
            val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return emptyList()
            val keys = pages.keys()
            val out = ArrayList<String>()
            while (keys.hasNext()) {
                val p = pages.getJSONObject(keys.next())
                val info = p.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                val u = info.optString("thumburl").ifBlank { info.optString("url") }
                if (u.startsWith("http")) out += u
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun scoreOf(url: String, title: String, snippet: String): Int {
        val t = "$url $title $snippet".lowercase()
        var s = 1
        listOf("sujok", "su jok", "su-jok", "acupressure", "correspondence", "onnuri", "park jae").forEach {
            if (it in t) s += 3
        }
        listOf("wikipedia", "wikimedia", "nih.gov", "who.int", "pubmed", "edu").forEach {
            if (it in t) s += 2
        }
        if ("hand" in t || "palm" in t || "finger" in t || "foot" in t) s += 1
        return s
    }

    private fun topicBonus(src: WebSource, guide: LocalGuide?): Int {
        if (guide == null) return 0
        val t = "${src.title} ${src.snippet} ${src.url}".lowercase()
        var n = 0
        guide.topic.forEach { if (it.lowercase() in t) n += 6 }
        guide.bans.forEach { if (it.lowercase() in t) n -= 12 }
        return n
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    private fun httpGet(url: String, extra: Map<String, String> = emptyMap()): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 15000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", UA)
            extra.forEach { (k, v) -> setRequestProperty(k, v) }
        }
        conn.inputStream.use { ins ->
            return ins.readBytes().toString(Charset.forName("UTF-8"))
        }
    }
}
