package com.tecsup.lab13mv

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Pistas del Modo Aventura (kart) con su clima. Archivo propio del juego de karts.

enum class Clima { Sol, Viento, Nieve, Lluvia }

data class RegionPeru(
    val nombre: String,
    val clima: Clima,
    val descripcionClima: String,
    val emojiClima: String,
    val colorCielo: Color,
    val colorSuelo: Color
)

val regiones = listOf(
    RegionPeru("Costa · Huacachina", Clima.Sol, "Sol ardiente y desierto", "☀️", Color(0xFFFFB74D), Color(0xFFD7A86E)),
    RegionPeru("Altiplano · Titicaca", Clima.Viento, "Viento fuerte sobre el lago", "🌬️", Color(0xFF4FC3F7), Color(0xFF0277BD)),
    RegionPeru("Sierra · Cusco", Clima.Nieve, "Frío extremo y nevada", "❄️", Color(0xFF90A4AE), Color(0xFFECEFF1)),
    RegionPeru("Selva · Amazonas", Clima.Lluvia, "Lluvia tropical y humedad", "🌧️", Color(0xFF2E7D32), Color(0xFF1B3D1F))
)

// Clima animado sobre la carrera (lluvia, nieve, viento y resplandor de sol)
@Composable
fun ClimaAnimado(clima: Clima, modifier: Modifier = Modifier) {
    val duracion = when (clima) {
        Clima.Lluvia -> 700
        Clima.Nieve -> 5000
        Clima.Viento -> 1600
        Clima.Sol -> 3000
    }
    val inf = rememberInfiniteTransition(label = "clima")
    val t by inf.animateFloat(
        0f, 1f, infiniteRepeatable(tween(duracion, easing = LinearEasing)), label = "t"
    )
    Canvas(modifier) { dibujarClima(clima, t) }
}

private fun DrawScope.dibujarClima(clima: Clima, t: Float) {
    val w = size.width
    val h = size.height
    when (clima) {
        Clima.Lluvia -> {
            for (i in 0 until 60) {
                val x = ((i * 37) % 100) / 100f * w
                val y = ((t + i * 0.137f) % 1f) * h
                drawLine(Color(0x99B3E5FC), Offset(x, y), Offset(x - 8f, y + 26f), 2.5f)
            }
        }
        Clima.Nieve -> {
            for (i in 0 until 50) {
                val x = ((i * 53) % 100) / 100f * w + sin((t * 2 * PI + i).toFloat()) * 14f
                val y = ((t + i * 0.071f) % 1f) * h
                drawCircle(Color(0xCCFFFFFF), 3f + (i % 3), Offset(x, y))
            }
        }
        Clima.Viento -> {
            for (i in 0 until 14) {
                val y = ((i * 29) % 100) / 100f * h
                val x = ((t + i * 0.11f) % 1f) * (w + 200f) - 200f
                drawLine(Color(0x66FFFFFF), Offset(x, y), Offset(x + 120f, y), 3f, StrokeCap.Round)
            }
        }
        Clima.Sol -> {
            val radio = w * (0.6f + 0.08f * sin(t * 2 * PI).toFloat())
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0x55FFF59D), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.1f), radius = radio
                ),
                radius = w
            )
        }
    }
}

// Líneas de velocidad al usar turbo
@Composable
fun LineasVelocidad(activa: Boolean, modifier: Modifier = Modifier, color: Color = Color.White) {
    val alpha by animateFloatAsState(if (activa) 0.75f else 0f, tween(120), label = "velocidad")
    if (alpha > 0.01f) {
        Canvas(modifier) {
            val c = center
            for (i in 0 until 28) {
                val ang = (i * 360f / 28f) * (PI.toFloat() / 180f)
                val r0 = size.minDimension * (0.16f + 0.08f * (i % 3))
                val r1 = size.maxDimension
                drawLine(
                    color.copy(alpha = alpha),
                    c + Offset(cos(ang) * r0, sin(ang) * r0),
                    c + Offset(cos(ang) * r1, sin(ang) * r1),
                    strokeWidth = if (i % 2 == 0) 4f else 2f
                )
            }
        }
    }
}
