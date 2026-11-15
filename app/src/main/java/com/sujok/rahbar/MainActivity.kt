package com.sujok.rahbar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Cream = Color(0xFFF6F1E8)
private val Teal = Color(0xFF0F4C5C)
private val Coral = Color(0xFFE07A5F)
private val Card = Color(0xFFFFFBF4)
private val Ink = Color(0xFF1D2B2E)
private val Soft = Color(0xFF5C6F73)

private val Urdu = TextStyle(
    fontSize = 16.sp,
    lineHeight = 28.sp,
    textDirection = TextDirection.Rtl,
    color = Ink,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Teal,
                    background = Cream,
                    surface = Card,
                ),
            ) {
                Surface(Modifier.fillMaxSize(), color = Cream) {
                    SujokApp()
                }
            }
        }
    }
}

@Composable
private fun SujokApp() {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<SearchResult?>(null) }

    fun runSearch(raw: String) {
        val q = raw.trim()
        if (q.isEmpty() || busy) return
        focus.clearFocus()
        busy = true
        error = null
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching { SearchEngine.find(q) }
            }
            busy = false
            r.onSuccess { result = it }
                .onFailure { error = "انٹرنیٹ چیک کریں، پھر دوبارہ کوشش کریں۔" }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
        ) {
            Text("سوجوک رہبر", color = Teal, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(
                "جو تکلیف لکھیں، نیٹ سے مستند پوائنٹ، تصویر اور آسان طریقہ آ جائے",
                style = Urdu.copy(color = Soft, fontSize = 14.sp, lineHeight = 22.sp),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("مثلاً lower backache یا کمر درد", color = Soft) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { runSearch(query) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = Color(0xFFD7CFC2),
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        focusedContainerColor = Card,
                        unfocusedContainerColor = Card,
                    ),
                    shape = RoundedCornerShape(18.dp),
                    textStyle = TextStyle(fontSize = 16.sp),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { runSearch(query) },
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.White)
                }
            }
            Row(
                Modifier
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Knowledge.chips.forEach { (ur, en) ->
                    Text(
                        ur,
                        color = Teal,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDCE8EA))
                            .clickable {
                                query = ur
                                runSearch(en)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
            if (busy) {
                Column(
                    Modifier.fillMaxWidth().padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Coral)
                    Text("ذرائع ڈھونڈ رہے ہیں…", style = Urdu.copy(color = Soft), modifier = Modifier.padding(top = 12.dp))
                }
            }
            error?.let {
                Text(it, style = Urdu.copy(color = Coral), modifier = Modifier.padding(top = 16.dp))
            }
            result?.let { ResultBlock(it) }
            Text(
                "یہ روایتی سوجوک معلومات ہے۔ دوا یا ڈاکٹر کا متبادل نہیں۔ شدید درد، کمزوری، حمل یا زخم ہو تو ہاتھ نہ لگائیں، معالج سے رجوع کریں۔",
                style = Urdu.copy(fontSize = 12.sp, lineHeight = 20.sp, color = Soft),
                modifier = Modifier.padding(top = 28.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun ResultBlock(result: SearchResult) {
    val ctx = LocalContext.current
    Spacer(Modifier.height(18.dp))
    CardBox {
        Text(result.guide?.titleUr ?: result.query, color = Teal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        result.guide?.let {
            Text(it.titleEn, color = Soft, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Text(
            result.guide?.pointUr ?: "سب سے زیادہ ذکر ہونے والا پوائنٹ نیچے ہے",
            style = Urdu.copy(fontWeight = FontWeight.SemiBold, color = Coral),
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(result.consensusUr, style = Urdu, modifier = Modifier.padding(top = 8.dp))
    }
    Spacer(Modifier.height(12.dp))
    CardBox {
        Text("ہاتھ پر جگہ", color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        HandMap(result.guide?.zone ?: HandZone.PALM_LOW, Modifier.padding(top = 8.dp))
        Text(
            "نارنجی دائرہ وہی علاقہ ہے جسے زیادہ گائیڈز دباتے ہیں۔ دونوں ہاتھ اور پاؤں چیک کریں۔",
            style = Urdu.copy(fontSize = 13.sp, color = Soft),
            modifier = Modifier.padding(top = 6.dp),
        )
    }
    if (result.images.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        CardBox {
            Text("تصاویر مختلف ذرائع سے", color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(
                Modifier
                    .padding(top = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                result.images.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Sujok picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(168.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE8E0D4))
                            .clickable {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    CardBox {
        Text("کرنے کا طریقہ", color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(result.methodUr, style = Urdu, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Justify)
    }
    if (result.sources.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        CardBox {
            Text("ذرائع — جو سائٹس زیادہ معتبر لگیں", color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            result.sources.forEach { src ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3EEE4))
                        .clickable {
                            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(src.url))) }
                        }
                        .padding(10.dp),
                ) {
                    Text(src.title, color = Teal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (src.snippetUr.isNotBlank()) {
                        Text(src.snippetUr, style = Urdu.copy(fontSize = 13.sp, lineHeight = 22.sp))
                    } else if (src.snippet.isNotBlank()) {
                        Text(src.snippet, color = Soft, fontSize = 12.sp)
                    }
                    Text(src.url.substringAfter("://").take(42), color = Coral, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Card)
            .padding(16.dp),
    ) { content() }
}
