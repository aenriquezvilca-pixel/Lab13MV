package com.tecsup.lab13mv

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

// ---------------------------------------------------------------------------
// MASCOTA KAWAII (dibujo propio con Canvas)
// ---------------------------------------------------------------------------
fun DrawScope.dibujarMascota(c: Corredor, centro: Offset, r: Float) {
    val negro = Color(0xFF212121)
    val cx = centro.x
    val cy = centro.y

    // orejas / accesorios detrás de la cabeza
    when (c.forma) {
        Forma.Conejo -> {
            for (sx in listOf(-1f, 1f)) {
                drawOval(c.cuerpo, Offset(cx + sx * r * 0.45f - r * 0.22f, cy - r * 2.0f), Size(r * 0.44f, r * 1.3f))
                drawOval(c.detalle, Offset(cx + sx * r * 0.45f - r * 0.11f, cy - r * 1.8f), Size(r * 0.22f, r * 0.85f))
            }
        }
        Forma.Koala -> {
            for (sx in listOf(-1f, 1f)) {
                drawCircle(c.cuerpo, r * 0.45f, Offset(cx + sx * r * 0.85f, cy - r * 0.7f))
                drawCircle(c.detalle, r * 0.25f, Offset(cx + sx * r * 0.85f, cy - r * 0.7f))
            }
        }
        Forma.Osito, Forma.Galleta -> {
            for (sx in listOf(-1f, 1f)) {
                drawCircle(c.cuerpo, r * 0.32f, Offset(cx + sx * r * 0.75f, cy - r * 0.8f))
                drawCircle(c.detalle, r * 0.16f, Offset(cx + sx * r * 0.75f, cy - r * 0.8f))
            }
        }
        Forma.Gato -> {
            for (sx in listOf(-1f, 1f)) {
                val oreja = Path().apply {
                    moveTo(cx + sx * r * 0.95f, cy - r * 0.1f)
                    lineTo(cx + sx * r * 0.8f, cy - r * 1.35f)
                    lineTo(cx + sx * r * 0.1f, cy - r * 0.8f)
                    close()
                }
                drawPath(oreja, c.detalle)
            }
        }
        Forma.Capucha -> {
            drawCircle(c.detalle, r * 1.22f, centro)
            for (sx in listOf(-1f, 1f)) drawCircle(negro, r * 0.34f, Offset(cx + sx * r * 0.95f, cy - r * 0.95f))
        }
        Forma.Corazon -> {}
    }

    // cabeza
    if (c.forma == Forma.Corazon) {
        drawCircle(c.cuerpo, r * 0.68f, Offset(cx - r * 0.5f, cy - r * 0.25f))
        drawCircle(c.cuerpo, r * 0.68f, Offset(cx + r * 0.5f, cy - r * 0.25f))
        val punta = Path().apply {
            moveTo(cx - r * 1.14f, cy - r * 0.05f)
            lineTo(cx + r * 1.14f, cy - r * 0.05f)
            lineTo(cx, cy + r * 1.2f)
            close()
        }
        drawPath(punta, c.cuerpo)
    } else {
        drawCircle(c.cuerpo, if (c.forma == Forma.Capucha) r * 0.88f else r, centro)
    }

    // cara
    val ojoY = cy + r * 0.05f
    for (sx in listOf(-1f, 1f)) {
        drawCircle(negro, r * 0.11f, Offset(cx + sx * r * 0.38f, ojoY))
        drawCircle(Color.White, r * 0.04f, Offset(cx + sx * r * 0.35f, ojoY - r * 0.04f))
        drawCircle(Color(0x55FF5252), r * 0.16f, Offset(cx + sx * r * 0.62f, ojoY + r * 0.3f))
    }
    drawArc(negro, 20f, 140f, false, Offset(cx - r * 0.14f, ojoY + r * 0.12f), Size(r * 0.28f, r * 0.2f), style = Stroke(r * 0.05f))
}

@Composable
fun MascotaVista(c: Corredor, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        dibujarMascota(c, Offset(size.width / 2f, size.height * 0.62f), size.minDimension * 0.27f)
    }
}

// ---------------------------------------------------------------------------
// KART (visto desde atrás) con la mascota al volante
// ---------------------------------------------------------------------------
fun DrawScope.dibujarKart(c: Corredor, cx: Float, base: Float, s: Float, inclina: Float = 0f, turbo: Boolean = false) {
    drawOval(Color(0x55000000), Offset(cx - 52f * s, base - 6f * s), Size(104f * s, 14f * s))
    rotate(inclina, Offset(cx, base)) {
        val llanta = Color(0xFF212121)
        drawRoundRect(llanta, Offset(cx - 50f * s, base - 28f * s), Size(20f * s, 28f * s), CornerRadius(6f * s, 6f * s))
        drawRoundRect(llanta, Offset(cx + 30f * s, base - 28f * s), Size(20f * s, 28f * s), CornerRadius(6f * s, 6f * s))
        drawRoundRect(Color(0xFF9E9E9E), Offset(cx - 44f * s, base - 20f * s), Size(8f * s, 12f * s), CornerRadius(3f * s, 3f * s))
        drawRoundRect(Color(0xFF9E9E9E), Offset(cx + 36f * s, base - 20f * s), Size(8f * s, 12f * s), CornerRadius(3f * s, 3f * s))
        // alerón
        drawRect(c.detalle, Offset(cx - 30f * s, base - 46f * s), Size(6f * s, 14f * s))
        drawRect(c.detalle, Offset(cx + 24f * s, base - 46f * s), Size(6f * s, 14f * s))
        drawRoundRect(c.detalle, Offset(cx - 46f * s, base - 52f * s), Size(92f * s, 9f * s), CornerRadius(4f * s, 4f * s))
        // carrocería
        drawRoundRect(c.kart, Offset(cx - 40f * s, base - 38f * s), Size(80f * s, 28f * s), CornerRadius(12f * s, 12f * s))
        drawRect(Color.White, Offset(cx - 5f * s, base - 38f * s), Size(10f * s, 28f * s))
        // mascota
        dibujarMascota(c, Offset(cx, base - 66f * s), 24f * s)
        // escapes y llamas azules
        for (sx in listOf(-1f, 1f)) {
            val ex = cx + sx * 22f * s
            drawCircle(Color(0xFF424242), 6f * s, Offset(ex, base - 12f * s))
            if (turbo) {
                drawOval(Color(0xFF40C4FF), Offset(ex - 8f * s, base - 8f * s), Size(16f * s, 30f * s))
                drawOval(Color.White, Offset(ex - 4f * s, base - 8f * s), Size(8f * s, 16f * s))
            }
        }
    }
}

@Composable
fun KartVista(c: Corredor, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "kartvista")
    val salto by inf.animateFloat(
        0f, -10f, infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "salto"
    )
    Canvas(modifier.graphicsLayer { translationY = salto }) {
        dibujarKart(c, size.width / 2f, size.height * 0.92f, size.width / 150f)
    }
}

private fun DrawScope.dibujarEstrella(cx: Float, cy: Float, r: Float, color: Color) {
    val p = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.45f
        val ang = (-90f + i * 36f) * (PI.toFloat() / 180f)
        val px = cx + cos(ang) * rad
        val py = cy + sin(ang) * rad
        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
    }
    p.close()
    drawPath(p, color)
}

private fun DrawScope.colinas(color: Color, w: Float, y: Float, desfase: Float, amp: Float, frec: Float) {
    val p = Path()
    p.moveTo(0f, y)
    var px = 0
    while (px <= w.toInt() + 20) {
        val yy = y - amp * (0.6f + 0.4f * sin((px + desfase) * frec * 0.01f))
        p.lineTo(px.toFloat(), yy)
        px += 20
    }
    p.lineTo(w, y)
    p.close()
    drawPath(p, color)
}

private class ObjetoEscena(val dz: Float, val tipo: Int, val x: Float, val rival: Rival?)

// ---------------------------------------------------------------------------
// ESCENA DE CARRERA: pista en perspectiva (pseudo 3D), objetos, rivales y kart
// ---------------------------------------------------------------------------
fun DrawScope.dibujarCarrera(c: CarreraKart) {
    val region = c.pista
    val w = size.width
    val h = size.height
    val hor = h * 0.40f
    val alto = h - hor
    val u0 = w / 400f

    // cielo
    drawRect(
        Brush.verticalGradient(
            listOf(region.colorCielo, lerp(region.colorCielo, Color.White, 0.6f)),
            startY = 0f, endY = hor
        ),
        size = Size(w, hor + 1f)
    )
    drawCircle(Color(0x88FFFFFF), 34f * u0, Offset(w * 0.75f - (c.rumbo * w * 0.3f) % w, hor * 0.35f))
    colinas(lerp(region.colorCielo, Color.Black, 0.3f), w, hor, c.rumbo * w * 0.5f, 46f * u0, 0.9f)
    colinas(lerp(region.colorCielo, Color.Black, 0.5f), w, hor + 2f, c.rumbo * w * 0.9f, 26f * u0, 1.7f)

    // suelo y carretera por franjas
    var dx = 0f
    var acum = 0f
    val paso = alto / FILAS
    for (k in 0 until FILAS) {
        val u = (FILAS - k).toFloat() / FILAS
        val profundidad = 7f / u
        val zw = c.z + profundidad
        dx += c.curvatura(zw) * 0.00022f * w
        acum += dx
        val mitad = w * (0.02f + 0.46f * u)
        val centro = w / 2f - c.x * mitad + acum
        c.centros[k] = centro
        c.anchos[k] = mitad
        val yTop = hor + (u - 1f / FILAS) * alto
        val franja = ((zw / 14f).toInt() % 2 == 0)

        drawRect(
            lerp(region.colorSuelo, Color.Black, if (franja) 0.08f else 0.22f),
            Offset(0f, yTop), Size(w, paso + 1f)
        )
        drawRect(
            if (franja) Color(0xFF5E5E6E) else Color(0xFF52526A),
            Offset(centro - mitad, yTop), Size(mitad * 2f, paso + 1f)
        )
        val borde = mitad * 0.12f
        val rumble = if (franja) Color.White else Color(0xFFE53935)
        drawRect(rumble, Offset(centro - mitad - borde, yTop), Size(borde, paso + 1f))
        drawRect(rumble, Offset(centro + mitad, yTop), Size(borde, paso + 1f))
        if (franja) {
            drawRect(Color(0xCCFFFFFF), Offset(centro - mitad * 0.02f, yTop), Size(mitad * 0.04f, paso + 1f))
        }
        if (zw % c.largo < 14f && zw > 100f) {
            for (j in 0 until 8) {
                drawRect(
                    if (j % 2 == 0) Color.White else Color.Black,
                    Offset(centro - mitad + j * mitad * 2f / 8f, yTop),
                    Size(mitad * 2f / 8f + 1f, paso + 1f)
                )
            }
        }
    }

    // objetos de la pista ordenados de lejos a cerca
    val objs = ArrayList<ObjetoEscena>()
    val dMax = 230f
    val n0 = floor((c.z + 8f) / 40f).toInt()
    for (n in n0..n0 + 6) {
        if (n < 2 || n in c.recogidos) continue
        val zn = n * 40f
        if (c.hayObstaculoCerca(zn)) continue
        val dz = zn - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, if (c.esGalleta(n)) 0 else 6, c.carrilRacha(zn), null))
    }
    val o0 = floor((c.z + 8f) / 90f).toInt()
    for (m in o0..o0 + 3) {
        if (!c.hayObstaculo(m) || m in c.obstaculosGolpeados) continue
        val dz = m * 90f - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, 7 + c.tipoObstaculo(m), c.xObstaculo(m), null))
    }
    val p0 = floor((c.z + 8f) / 400f).toInt()
    for (p in p0..p0 + 1) {
        if (p < 1) continue
        val dz = p * 400f - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, 10, c.xPad(p), null))
    }
    val m0 = floor((c.z + 8f) / 260f).toInt()
    for (m in m0..m0 + 1) {
        if (m < 1 || m in c.cajasTomadas) continue
        val dz = m * 260f - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, 1, c.xCaja(m), null))
    }
    for (t in c.trampas) {
        val dz = t.z - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, 2, t.x, null))
    }
    for (r in c.rivales) {
        val dz = r.z - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, 3, r.x, r))
    }
    for (m in c.misiles) {
        val dz = m.z - c.z
        if (dz in 8f..dMax) objs.add(ObjetoEscena(dz, 4, m.x, null))
    }
    objs.sortByDescending { it.dz }

    for (o in objs) {
        val u = 7f / o.dz
        val k = ((1f - u) * FILAS).toInt().coerceIn(0, FILAS - 1)
        val px = c.centros[k] + o.x * c.anchos[k]
        val py = hor + u * alto
        val s = u * 1.25f * u0
        val bote = sin(c.tiempo * 5f + o.dz) * 4f * s
        when (o.tipo) {
            0 -> dibujarGalleta(px, py - 22f * s + bote, 15f * s)
            6 -> dibujarMoneda(px, py - 22f * s + bote, 13f * s, c.tiempo)
            7 -> dibujarObstaculo(0, px, py, s)
            8 -> dibujarObstaculo(1, px, py, s)
            9 -> dibujarObstaculo(2, px, py, s)
            10 -> dibujarPad(px, py, c.anchos[k] * 0.42f, s)
            1 -> {
                val cy = py - 26f * s + bote
                val rombo = Path().apply {
                    moveTo(px, cy - 18f * s); lineTo(px + 16f * s, cy); lineTo(px, cy + 18f * s); lineTo(px - 16f * s, cy); close()
                }
                drawPath(rombo, Color(0xFFE040FB))
                drawCircle(Color.White, 5f * s, Offset(px, cy))
            }
            2 -> {
                drawCircle(Color(0xFF212121), 11f * s, Offset(px, py - 11f * s))
                drawCircle(Color(0xFFFF1744), 4f * s, Offset(px, py - 24f * s))
            }
            3 -> {
                val r = o.rival!!
                dibujarKart(r.corredor, px, py, s, if (r.giro > 0f) r.giro * 720f else 0f, false)
            }
            4 -> drawOval(Color(0xFFFF5252), Offset(px - 5f * s, py - 30f * s), Size(10f * s, 22f * s))
            else -> {}
        }
    }

    // kart del jugador
    val sp = 1.25f * u0
    val basePy = h * 0.88f
    val dir = (if (c.der) 1 else 0) - (if (c.izq) 1 else 0)
    val inclina = dir * 6f +
        (if (c.derrapando) c.dirDerrape * 16f else 0f) +
        (if (c.girando > 0f) c.girando * 720f else 0f)
    if (c.derrapando) {
        val enZona = c.carga in 0.55f..0.9f
        for (i in 0 until 6) {
            val a = c.tiempo * 13f + i * 1.7f
            val sx = w / 2f + (if (i % 2 == 0) -40f else 40f) * sp + sin(a) * 8f * sp
            val sy = basePy - (a % 1f) * 30f * sp
            drawCircle(if (enZona) Color(0xFF40C4FF) else Color(0xFFFFB74D), 4f * sp, Offset(sx, sy))
        }
    }
    dibujarKart(c.corredor, w / 2f, basePy, sp, inclina, c.turbo > 0f)
    if (c.escudo > 0f) {
        drawCircle(Color(0x4400E5FF), 75f * sp, Offset(w / 2f, basePy - 42f * sp))
        drawCircle(Color(0xFF00E5FF), 75f * sp, Offset(w / 2f, basePy - 42f * sp), style = Stroke(3f))
    }

    // medidor de derrape: suelta en la zona verde
    if (c.derrapando) {
        val bx = w * 0.3f
        val by = h * 0.66f
        val bw = w * 0.4f
        val bh = 14f * u0
        drawRoundRect(Color(0xAA000000), Offset(bx, by), Size(bw, bh), CornerRadius(7f, 7f))
        drawRect(Color(0xFF66BB6A), Offset(bx + bw * 0.55f / 1.2f, by), Size(bw * 0.35f / 1.2f, bh))
        val enZona = c.carga in 0.55f..0.9f
        drawRoundRect(
            if (enZona) Color(0xFF40C4FF) else Color(0xFFFFB74D),
            Offset(bx, by), Size(bw * c.carga / 1.2f, bh), CornerRadius(7f, 7f)
        )
        drawRoundRect(Color.White, Offset(bx, by), Size(bw, bh), CornerRadius(7f, 7f), style = Stroke(2f))
    }
}
