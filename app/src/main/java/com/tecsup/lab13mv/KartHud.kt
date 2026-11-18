package com.tecsup.lab13mv

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.cos

// ---------------------------------------------------------------------------
// Elementos de la pista: galleta, moneda, obstáculos y turbo pad
// ---------------------------------------------------------------------------
fun DrawScope.dibujarGalleta(cx: Float, cy: Float, r: Float) {
    drawCircle(Color(0xFFD7A86E), r, Offset(cx, cy))
    drawCircle(Color(0xFF8D5524), r, Offset(cx, cy), style = Stroke(r * 0.15f))
    drawCircle(Color(0xFF4E342E), r * 0.18f, Offset(cx - r * 0.35f, cy - r * 0.25f))
    drawCircle(Color(0xFF4E342E), r * 0.18f, Offset(cx + r * 0.3f, cy - r * 0.1f))
    drawCircle(Color(0xFF4E342E), r * 0.18f, Offset(cx - r * 0.05f, cy + r * 0.35f))
}

fun DrawScope.dibujarMoneda(cx: Float, cy: Float, r: Float, tiempo: Float) {
    // la moneda gira: su ancho cambia con el tiempo
    val ancho = r * (0.25f + 0.75f * abs(cos(tiempo * 4f)))
    drawOval(Color(0xFFFFC107), Offset(cx - ancho, cy - r), Size(ancho * 2f, r * 2f))
    drawOval(Color(0xFFFF8F00), Offset(cx - ancho, cy - r), Size(ancho * 2f, r * 2f), style = Stroke(r * 0.15f))
    drawOval(Color(0xFFFFF59D), Offset(cx - ancho * 0.45f, cy - r * 0.6f), Size(ancho * 0.5f, r * 0.7f))
}

// tipo: 0 caja de madera, 1 cono, 2 roca
fun DrawScope.dibujarObstaculo(tipo: Int, px: Float, py: Float, s: Float) {
    when (tipo) {
        0 -> {
            val t = 38f * s
            drawRoundRect(Color(0xFF8D6E63), Offset(px - t / 2f, py - t), Size(t, t), CornerRadius(4f * s, 4f * s))
            drawRoundRect(Color(0xFF4E342E), Offset(px - t / 2f, py - t), Size(t, t), CornerRadius(4f * s, 4f * s), style = Stroke(3f * s))
            drawLine(Color(0xFF4E342E), Offset(px - t * 0.42f, py - t * 0.92f), Offset(px + t * 0.42f, py - t * 0.08f), 3f * s)
            drawLine(Color(0xFF4E342E), Offset(px + t * 0.42f, py - t * 0.92f), Offset(px - t * 0.42f, py - t * 0.08f), 3f * s)
        }
        1 -> {
            val cono = Path().apply {
                moveTo(px - 16f * s, py); lineTo(px + 16f * s, py); lineTo(px, py - 44f * s); close()
            }
            drawPath(cono, Color(0xFFFF6D00))
            drawRect(Color.White, Offset(px - 9f * s, py - 24f * s), Size(18f * s, 7f * s))
            drawRect(Color(0xFF424242), Offset(px - 20f * s, py - 3f * s), Size(40f * s, 5f * s))
        }
        else -> {
            drawOval(Color(0xFF607D8B), Offset(px - 24f * s, py - 30f * s), Size(48f * s, 32f * s))
            drawOval(Color(0xFF90A4AE), Offset(px - 14f * s, py - 27f * s), Size(20f * s, 12f * s))
        }
    }
}

// Turbo pad plano en el suelo, solo ocupa un carril
fun DrawScope.dibujarPad(px: Float, py: Float, ancho: Float, s: Float) {
    val alto = maxOf(4f, 16f * s)
    drawRoundRect(Color(0xFFFF6D00), Offset(px - ancho / 2f, py - alto), Size(ancho, alto), CornerRadius(4f, 4f))
    val flecha = Path().apply {
        moveTo(px - ancho * 0.22f, py - alto * 0.2f)
        lineTo(px, py - alto * 0.85f)
        lineTo(px + ancho * 0.22f, py - alto * 0.2f)
    }
    drawPath(flecha, Color(0xFFFFFF00), style = Stroke(maxOf(2f, 4f * s)))
}

// ---------------------------------------------------------------------------
// Barra lateral de avance hacia la meta (jugador y rivales)
// ---------------------------------------------------------------------------
fun DrawScope.dibujarAvance(c: CarreraKart) {
    val cx = size.width / 2f
    val top = 16f
    val bottom = size.height - 16f
    val largoBarra = bottom - top

    fun yDe(zz: Float): Float = bottom - (zz / c.total).coerceIn(0f, 1f) * largoBarra

    // riel
    drawRoundRect(Color(0x99000000), Offset(cx - 7f, top - 8f), Size(14f, largoBarra + 16f), CornerRadius(7f, 7f))
    // tramo recorrido
    val yJ = yDe(c.z)
    drawRoundRect(Color(0xFFFFD54F), Offset(cx - 7f, yJ), Size(14f, bottom - yJ + 8f), CornerRadius(7f, 7f))
    // marcas de vuelta
    for (i in 1 until c.vueltas) {
        val y = yDe(i * c.largo)
        drawLine(Color.White, Offset(cx - 12f, y), Offset(cx + 12f, y), 3f)
    }
    // meta a cuadros
    for (j in 0 until 4) {
        drawRect(if (j % 2 == 0) Color.White else Color.Black, Offset(cx - 12f + j * 6f, top - 14f), Size(6f, 6f))
    }
    // rivales
    for (r in c.rivales) {
        val y = yDe(r.z)
        drawCircle(Color.White, 9f, Offset(cx, y))
        drawCircle(r.corredor.kart, 7f, Offset(cx, y))
    }
    // jugador con la carita de su mascota
    drawCircle(Color.White, 16f, Offset(cx, yJ))
    drawCircle(c.corredor.kart, 14f, Offset(cx, yJ))
    dibujarMascota(c.corredor, Offset(cx, yJ + 2f), 9f)
}
