package com.sujok.rahbar

object SearchEngine {
    fun find(query: String): SearchResult {
        val q = query.trim()
        val guide = Matcher.match(q)
        val webQ = guide?.searchEn ?: q
        val web = runCatching { Net.searchWeb(webQ, guide) }.getOrDefault(emptyList())
            .filter { Matcher.sourceOk(it, guide) }
        val images = runCatching {
            Net.searchImages(guide?.imageQuery ?: "sujok $webQ correspondence")
        }.getOrDefault(emptyList())
        val translated = web.take(4).map { src ->
            val ur = Net.translateUr(src.snippet.ifBlank { src.title })
            src.copy(snippetUr = ur)
        }
        val sources = (translated + web.drop(4)).sortedByDescending { it.score }.take(8)
        return SearchResult(
            query = q,
            guide = guide,
            consensusUr = consensus(guide, sources),
            methodUr = method(guide, sources),
            images = images,
            sources = sources,
        )
    }

    private fun consensus(guide: LocalGuide?, sources: List<WebSource>): String {
        val point = guide?.pointUr ?: "تلاش سے واضح پوائنٹ نہیں ملا"
        return if (guide != null) {
            "یہ ${guide.titleUr} (${guide.titleEn}) کا پوائنٹ ہے — ${guide.pointUr}۔ زیادہ سوجوک رہنمائی اسی نقشے پر آتی ہے۔"
        } else if (sources.isNotEmpty()) {
            "آن لائن نتائج ملے مگر تکلیف واضح نہیں۔ درست لفظ لکھیں مثلاً sinus یا سائنس، sciatica نہیں۔"
        } else {
            "پوائنٹ: $point"
        }
    }

    private fun method(guide: LocalGuide?, sources: List<WebSource>): String {
        val base = guide?.methodUr
            ?: "پہلے تکلیف کا درست نام لکھیں تاکہ الگ پوائنٹ آئے۔ عام قاعدہ: نقشے پر کچے نکتے کو دو سے پانچ منٹ نرم دبائیں۔"
        val fromWeb = sources.firstOrNull {
            it.snippetUr.length > 24 && (guide == null || Matcher.sourceOk(it, guide))
        }?.snippetUr.orEmpty()
        return if (fromWeb.isNotBlank() && guide != null) {
            "$base\n\nنیٹ سے اسی موضوع کا ترجمہ: $fromWeb"
        } else {
            base
        }
    }
}
