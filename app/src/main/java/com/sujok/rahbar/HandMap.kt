package com.sujok.rahbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun HandMap(zone: HandZone, modifier: Modifier = Modifier) {
    val hot = Color(0xFFE07A5F)
    val skin = Color(0xFFF3D7B6)
    val line = Color(0xFF0F4C5C)
    val dim = Color(0x66E07A5F)
    Canvas(modifier.fillMaxWidth().aspectRatio(0.85f)) {
        val w = size.width
        val h = size.height
        fun finger(x: Float, top: Float, fw: Float, fh: Float, on: Boolean) {
            drawRoundRect(
                color = if (on) hot else skin,
                topLeft = Offset(x, top),
                size = Size(fw, fh),
                cornerRadius = CornerRadius(fw / 2, fw / 2),
            )
            drawRoundRect(
                color = line,
                topLeft = Offset(x, top),
                size = Size(fw, fh),
                cornerRadius = CornerRadius(fw / 2, fw / 2),
                style = Stroke(width = 3f),
            )
        }
        val palmL = w * 0.22f
        val palmT = h * 0.38f
        val palmW = w * 0.52f
        val palmH = h * 0.42f
        val palmOn = zone == HandZone.PALM_CENTER || zone == HandZone.PALM_LOW || zone == HandZone.WRIST
        drawRoundRect(
            color = if (palmOn && zone == HandZone.PALM_CENTER) hot else skin,
            topLeft = Offset(palmL, palmT),
            size = Size(palmW, palmH),
            cornerRadius = CornerRadius(48f, 48f),
        )
        if (zone == HandZone.PALM_LOW || zone == HandZone.WRIST) {
            drawRoundRect(
                color = hot,
                topLeft = Offset(palmL + 8f, palmT + palmH * 0.52f),
                size = Size(palmW - 16f, palmH * 0.42f),
                cornerRadius = CornerRadius(36f, 36f),
            )
        }
        drawRoundRect(
            color = line,
            topLeft = Offset(palmL, palmT),
            size = Size(palmW, palmH),
            cornerRadius = CornerRadius(48f, 48f),
            style = Stroke(width = 4f),
        )
        val fw = w * 0.10f
        val fingersOn = zone == HandZone.FINGERS
        finger(w * 0.26f, h * 0.14f, fw, h * 0.28f, fingersOn)
        finger(w * 0.38f, h * 0.08f, fw, h * 0.34f, fingersOn)
        finger(w * 0.50f, h * 0.10f, fw, h * 0.32f, fingersOn)
        finger(w * 0.62f, h * 0.16f, fw, h * 0.26f, fingersOn)
        // thumb
        val thumbOn = zone == HandZone.THUMB
        drawRoundRect(
            color = if (thumbOn) hot else skin,
            topLeft = Offset(w * 0.08f, h * 0.42f),
            size = Size(w * 0.20f, h * 0.18f),
            cornerRadius = CornerRadius(40f, 40f),
        )
        drawRoundRect(
            color = line,
            topLeft = Offset(w * 0.08f, h * 0.42f),
            size = Size(w * 0.20f, h * 0.18f),
            cornerRadius = CornerRadius(40f, 40f),
            style = Stroke(width = 3f),
        )
        if (zone == HandZone.FINGERS) {
            drawLine(hot, Offset(w * 0.31f, h * 0.16f), Offset(w * 0.31f, h * 0.40f), 6f, StrokeCap.Round)
            drawLine(hot, Offset(w * 0.43f, h * 0.10f), Offset(w * 0.43f, h * 0.40f), 6f, StrokeCap.Round)
            drawLine(hot, Offset(w * 0.55f, h * 0.12f), Offset(w * 0.55f, h * 0.40f), 6f, StrokeCap.Round)
            drawLine(hot, Offset(w * 0.67f, h * 0.18f), Offset(w * 0.67f, h * 0.40f), 6f, StrokeCap.Round)
        }
        val cx = when (zone) {
            HandZone.THUMB -> Offset(w * 0.18f, h * 0.50f)
            HandZone.PALM_CENTER -> Offset(w * 0.48f, h * 0.55f)
            HandZone.PALM_LOW, HandZone.WRIST, HandZone.FOOT -> Offset(w * 0.48f, h * 0.70f)
            HandZone.FINGERS -> Offset(w * 0.43f, h * 0.28f)
        }
        drawCircle(dim, radius = w * 0.08f, center = cx)
        drawCircle(hot, radius = w * 0.035f, center = cx)
        drawCircle(Color.White, radius = w * 0.012f, center = cx)
    }
}
