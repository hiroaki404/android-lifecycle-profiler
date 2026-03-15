package com.hiroaki404.androidlifecycleprofiler.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiroaki404.androidlifecycleprofiler.model.LifecycleSpan
import kotlinx.coroutines.delay
import org.jetbrains.jewel.ui.component.Text

private const val ROW_HEIGHT = 48f
private const val LABEL_WIDTH = 160
private const val PADDING = 4f
private const val WINDOW_MS = 10_000L  // 表示する時間幅 (10秒)

@Composable
fun TimelineChart(spans: List<LifecycleSpan>, modifier: Modifier = Modifier) {
    val components = remember(spans) { spans.map { it.component }.distinct() }

    // 100msごとにnowを更新することで、アクティブなスパンがリアルタイムに伸びる
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(200)
            now = System.currentTimeMillis()
        }
    }

    LaunchedEffect(spans.size) {
        spans.forEachIndexed { index, span ->
            println("${now} [${span.component}]")
        }
    }

    val windowStart = now - WINDOW_MS

    if (components.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ライフサイクルイベント待機中...")
        }
        return
    }

    val scrollState = rememberScrollState(Int.MAX_VALUE)

    Row(modifier.fillMaxSize()) {
        // コンポーネント名ラベル列
        Column(
            Modifier
                .width(LABEL_WIDTH.dp)
                .fillMaxHeight()
                .background(Color(0xFF2B2B2B))
                .padding(top = 24.dp),
        ) {
            components.forEach { component ->
                Box(
                    Modifier
                        .height(ROW_HEIGHT.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = component,
                        style = TextStyle(fontSize = 12.sp, color = Color.White),
                    )
                }
            }
        }

        // タイムライン描画エリア
        val textMeasurer = rememberTextMeasurer()
        Canvas(
            Modifier
                .fillMaxSize(),
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 時間軸目盛り（2秒ごと）
            val tickIntervalMs = 10_000L
            var tickTime = (windowStart / tickIntervalMs + 1) * tickIntervalMs
            while (tickTime <= now) {
                val x = ((tickTime - windowStart).toFloat() / WINDOW_MS * canvasWidth)
                drawLine(
                    color = Color(0xFF555555),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f,
                )
                val label = "${(now - tickTime) / 1000}s ago"
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(x + 2f, 2f),
                    style = TextStyle(fontSize = 10.sp, color = Color(0xFF888888)),
                )
                tickTime += tickIntervalMs
            }

            // スパン描画
            components.forEachIndexed { index, component ->
                val rowTop = 24f + index * ROW_HEIGHT + PADDING

                spans
                    .filter { it.component == component && (it.endTime ?: now) >= windowStart }
                    .forEach { span ->
                        val xStart =
                            ((span.startTime - windowStart).coerceAtLeast(0).toFloat() / WINDOW_MS * canvasWidth)
                        val xEnd = (((span.endTime ?: now) - windowStart).coerceAtMost(WINDOW_MS)
                            .toFloat() / WINDOW_MS * canvasWidth)
                        val width = (xEnd - xStart).coerceAtLeast(4f)

                        drawRoundRect(
                            color = span.state.color,
                            topLeft = Offset(xStart, rowTop),
                            size = Size(width, ROW_HEIGHT - PADDING * 2),
                            cornerRadius = CornerRadius(4f),
                        )

                        if (width > 40f) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = span.state.label,
                                topLeft = Offset(xStart + 4f, rowTop + 4f),
                                style = TextStyle(fontSize = 10.sp, color = Color.White),
                            )
                        }
                    }
            }
        }
    }
}
