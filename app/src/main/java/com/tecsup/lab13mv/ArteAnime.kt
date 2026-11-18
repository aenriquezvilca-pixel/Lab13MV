package com.tecsup.lab13mv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// MOTOR GRÁFICO 3D: PISTA AMPLIA DE 2 CARRILES, ÁRBOLES PROPORCIONADOS Y
// PERSONAJES DE BT21 HERMOSOS, TIERNOS Y FIELES A LAS FIGURAS ORIGINALES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LienzoCarreraBT21(
    motor: MotorCarreraBT21,
    modifier: Modifier = Modifier
) {
    val pista = motor.pistaActual
    val pj = motor.personajeActual

    Canvas(modifier = modifier.fillMaxSize()) {
        clipRect {
            val w = size.width
            val h = size.height
            val horizY = h * 0.36f
            val shake = sin(motor.tiempoCarreraSegundos * 45f) * motor.shakeCamara

            // 1. CIELO
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(pista.cielo1, pista.cielo2),
                    startY = 0f,
                    endY = horizY
                ),
                size = Size(w, horizY)
            )

            // Sol / Luna tierna de BT21
            val solX = (w * 0.8f - (motor.distanciaJugador * 0.04f) % (w * 1.5f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color.Transparent),
                    center = Offset(solX, horizY * 0.35f),
                    radius = 80f
                ),
                radius = 65f,
                center = Offset(solX, horizY * 0.35f)
            )

            // Nubes flotantes
            dibujarNubesBT21(w, horizY, motor.distanciaJugador * 0.10f)

            // Fondo de rascacielos o colinas
            dibujarFondoTematico(w, horizY, pista, motor.distanciaJugador * 0.30f)

            // 2. PASTO / SUELO LATERAL
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(pista.colorPasto, pista.colorPasto.copy(alpha = 0.85f)),
                    startY = horizY,
                    endY = h
                ),
                topLeft = Offset(0f, horizY),
                size = Size(w, h - horizY)
            )

            // 3. PISTA AMPLIA DE 2 CARRILES BIEN DEFINIDOS
            dibujarPista2CarrilesAmpios(w, h, horizY, pista, motor.distanciaJugador, motor.curvaActual)

            // 4. BANDAS DE ACELERACIÓN TURBO PADS (EN LOS 2 CARRILES)
            dibujarTurboPads(w, h, horizY, motor)

            // 5. MONEDAS DORADAS EN LOS 2 CARRILES
            dibujarMonedas(w, h, horizY, motor)

            // 6. OBSTÁCULOS: CAJAS DE MADERA (SALTAR) Y ÁRBOLES (TAMAÑO PROPORCIONAL Y REDUCIDO)
            dibujarObstaculos(w, h, horizY, motor)

            // 7. CAJAS DE ÍTEMS CTR
            dibujarCajasItems(w, h, horizY, motor)

            // 8. RIVALES BT21 COMPITIENDO EN LOS 2 CARRILES
            val rivalesOrdenados = motor.rivales.sortedByDescending { it.distanciaZ }
            rivalesOrdenados.forEach { rival ->
                val distRel = rival.distanciaZ - motor.distanciaJugador
                if (distRel in -80f..580f) {
                    val t = (1f - (distRel / 600f)).coerceIn(0.12f, 1f)
                    val yPos = horizY + (h - horizY) * (t * t)

                    val curvaturaOffset = (1f - t) * (1f - t) * (motor.curvaActual * 240f)
                    val anchoPistaEnY = (w * 0.28f) + (w * 0.84f) * (t * t)
                    val xPos = w * 0.5f + curvaturaOffset + (rival.carrilX * anchoPistaEnY * 0.40f)
                    val escalaRival = (t * 0.90f).coerceIn(0.35f, 1.20f)

                    dibujarKartToyBT21(
                        pj = rival.personaje,
                        colorChasis = rival.colorKart,
                        diseno = DisenoKart.Clasico21,
                        centro = Offset(xPos, yPos),
                        escala = escalaRival,
                        inclinacion = -motor.curvaActual * 10f,
                        aturdido = rival.aturdido > 0f,
                        tiempo = motor.tiempoCarreraSegundos,
                        mostrarNombre = true
                    )
                }
            }

            // 9. JUGADOR PRINCIPAL: KART GRANDE Y BELLO DENTRO DE LOS 2 CARRILES
            val saltoOffset = sin(motor.enSalto * PI.toFloat()) * 95f
            val anchoPistaPlayer = w * 0.92f
            val playerX = w * 0.5f + (motor.posXJugador * anchoPistaPlayer * 0.40f)
            val playerY = h * 0.81f - saltoOffset + shake

            val inclinacionJugador = (motor.direccionVolante * 16f) - (motor.curvaActual * 6f)

            dibujarKartToyBT21(
                pj = pj,
                colorChasis = motor.colorKartSeleccionado,
                diseno = motor.disenoKartSeleccionado,
                centro = Offset(playerX, playerY),
                escala = 1.35f,
                inclinacion = inclinacionJugador,
                aturdido = motor.tiempoAturdido > 0f,
                tiempo = motor.tiempoCarreraSegundos,
                nitroActivo = motor.nitroActivo || motor.tiempoTurboPad > 0f,
                escudoActivo = motor.escudoActivo,
                mostrarNombre = false
            )

            // 10. SPEED LINES EN NITRO
            if (motor.nitroActivo || motor.tiempoTurboPad > 0f) {
                dibujarLineasNitro(w, h, motor.tiempoCarreraSegundos)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PISTA AMPLIA DE 2 CARRILES (IZQUIERDO Y DERECHO CON LÍNEA CENTRAL DIVISORIA)
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.dibujarPista2CarrilesAmpios(
    w: Float, h: Float, horizY: Float, pista: PistaBT21, distPlayer: Float, curvaActual: Float
) {
    val segmentos = 26
    val centroX = w * 0.5f

    for (i in segmentos - 1 downTo 0) {
        val t1 = i / segmentos.toFloat()
        val t2 = (i + 1) / segmentos.toFloat()

        val y1 = horizY + (h - horizY) * (t1 * t1)
        val y2 = horizY + (h - horizY) * (t2 * t2)

        // PISTA MUCHO MÁS AMPLIA (2 CARRILES COMPLETOS)
        val w1 = w * 0.28f + (w * 0.84f) * (t1 * t1)
        val w2 = w * 0.28f + (w * 0.84f) * (t2 * t2)

        val curvaOffset1 = (1f - t1) * (1f - t1) * (curvaActual * 240f)
        val curvaOffset2 = (1f - t2) * (1f - t2) * (curvaActual * 240f)

        val cx1 = centroX + curvaOffset1
        val cx2 = centroX + curvaOffset2

        val esFranjaA = ((distPlayer * 0.05f).toInt() + i) % 2 == 0
        val colorAsfalto = if (esFranjaA) pista.colorPista else pista.colorPista.copy(alpha = 0.88f)

        // Asfalto amplio
        drawPath(
            Path().apply {
                moveTo(cx1 - w1 * 0.5f, y1)
                lineTo(cx1 + w1 * 0.5f, y1)
                lineTo(cx2 + w2 * 0.5f, y2)
                lineTo(cx2 - w2 * 0.5f, y2)
                close()
            },
            colorAsfalto
        )

        // Bordillo exterior izquierdo y derecho
        val colorBorde = if (esFranjaA) pista.colorBorde1 else pista.colorBorde2
        val anchoBorde1 = 18f * t1 + 4f
        val anchoBorde2 = 18f * t2 + 4f

        // Borde izquierdo
        drawPath(
            Path().apply {
                moveTo(cx1 - w1 * 0.5f, y1)
                lineTo(cx1 - w1 * 0.5f - anchoBorde1, y1)
                lineTo(cx2 - w2 * 0.5f - anchoBorde2, y2)
                lineTo(cx2 - w2 * 0.5f, y2)
                close()
            },
            colorBorde
        )

        // Borde derecho
        drawPath(
            Path().apply {
                moveTo(cx1 + w1 * 0.5f, y1)
                lineTo(cx1 + w1 * 0.5f + anchoBorde1, y1)
                lineTo(cx2 + w2 * 0.5f + anchoBorde2, y2)
                lineTo(cx2 + w2 * 0.5f, y2)
                close()
            },
            colorBorde
        )

        // LÍNEA DIVISORIA CENTRAL DE 2 CARRILES (Línea discontinua amarilla brillante)
        if (esFranjaA) {
            drawLine(
                color = Color(0xFFFFEB3B),
                start = Offset(cx1, y1),
                end = Offset(cx2, y2),
                strokeWidth = 6f * t1 + 1.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BANDAS DE ACELERACIÓN EN AMBOS CARRILES
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.dibujarTurboPads(w: Float, h: Float, horizY: Float, motor: MotorCarreraBT21) {
    motor.turboPads.forEach { pad ->
        val dist = pad.distanciaZ - motor.distanciaJugador
        if (dist in 10f..580f) {
            val t = (1f - (dist / 600f)).coerceIn(0.1f, 1f)
            val y = horizY + (h - horizY) * (t * t)

            val curvaOffset = (1f - t) * (1f - t) * (motor.curvaActual * 240f)
            val anchoPista = (w * 0.28f) + (w * 0.84f) * (t * t)
            val x = w * 0.5f + curvaOffset + (pad.carrilX * anchoPista * 0.40f)

            val anchoPad = 55f * t + 12f
            val altoPad = 32f * t + 8f

            drawRoundRect(
                color = Color(0xFFFF6D00),
                topLeft = Offset(x - anchoPad * 0.5f, y - altoPad * 0.5f),
                size = Size(anchoPad, altoPad),
                cornerRadius = CornerRadius(6f, 6f)
            )

            val pathFlecha = Path().apply {
                moveTo(x - anchoPad * 0.35f, y + altoPad * 0.25f)
                lineTo(x, y - altoPad * 0.35f)
                lineTo(x + anchoPad * 0.35f, y + altoPad * 0.25f)
            }
            drawPath(pathFlecha, Color(0xFFFFFF00), style = Stroke(5f * t + 1.5f, cap = StrokeCap.Round))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MONEDAS EN AMBOS CARRILES
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.dibujarMonedas(w: Float, h: Float, horizY: Float, motor: MotorCarreraBT21) {
    motor.monedas.forEach { moneda ->
        if (!moneda.recogida) {
            val dist = moneda.distanciaZ - motor.distanciaJugador
            if (dist in 10f..580f) {
                val t = (1f - (dist / 600f)).coerceIn(0.1f, 1f)
                val y = horizY + (h - horizY) * (t * t)

                val curvaOffset = (1f - t) * (1f - t) * (motor.curvaActual * 240f)
                val anchoPista = (w * 0.28f) + (w * 0.84f) * (t * t)
                val x = w * 0.5f + curvaOffset + (moneda.carrilX * anchoPista * 0.40f)

                val radio = 18f * t + 5f

                drawCircle(Color(0xFFFFD54F), radio, Offset(x, y))
                drawCircle(Color(0xFFFF8F00), radio, Offset(x, y), style = Stroke(2.5f * t + 1f))
                drawCircle(Color(0xFFFFF9C4), radio * 0.55f, Offset(x - 2f, y - 2f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OBSTÁCULOS: CAJAS Y ÁRBOLES DE TAMAÑO REDUCIDO Y PROPORCIONADO AL AUTO
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.dibujarObstaculos(w: Float, h: Float, horizY: Float, motor: MotorCarreraBT21) {
    motor.obstaculos.forEach { obs ->
        if (!obs.destruido) {
            val dist = obs.distanciaZ - motor.distanciaJugador
            if (dist in 10f..580f) {
                val t = (1f - (dist / 600f)).coerceIn(0.1f, 1f)
                val y = horizY + (h - horizY) * (t * t)

                val curvaOffset = (1f - t) * (1f - t) * (motor.curvaActual * 240f)
                val anchoPista = (w * 0.28f) + (w * 0.84f) * (t * t)
                val x = w * 0.5f + curvaOffset + (obs.carrilX * anchoPista * 0.40f)

                when (obs.tipo) {
                    TipoObstaculoPista.CajaMadera -> {
                        // CAJA DE MADERA COMPACTA (SE PUEDE SALTAR)
                        val tam = 40f * t + 10f
                        drawRoundRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(x - tam * 0.5f, y - tam * 0.5f),
                            size = Size(tam, tam),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = Color(0xFF4E342E),
                            topLeft = Offset(x - tam * 0.5f, y - tam * 0.5f),
                            size = Size(tam, tam),
                            style = Stroke(3f * t + 1f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawLine(Color(0xFF4E342E), Offset(x - tam * 0.4f, y - tam * 0.4f), Offset(x + tam * 0.4f, y + tam * 0.4f), 2.5f * t + 1f)
                        drawLine(Color(0xFF4E342E), Offset(x + tam * 0.4f, y - tam * 0.4f), Offset(x - tam * 0.4f, y + tam * 0.4f), 2.5f * t + 1f)
                    }

                    TipoObstaculoPista.Arbol -> {
                        // ÁRBOL REDUCIDO Y PROPORCIONAL AL AUTO (NO SALTABLE)
                        // Tamaño calibrado para que sea un obstáculo elegante dentro del carril
                        val anchoCopa = 38f * t + 10f
                        val altoTronco = 24f * t + 8f

                        // Tronco esbelto
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(x - 5f * t, y - altoTronco * 0.5f),
                            size = Size(10f * t + 3f, altoTronco)
                        )

                        // Copa de pino verde en niveles tiernos
                        val yCopa = y - altoTronco * 0.7f
                        drawCircle(Color(0xFF1B5E20), anchoCopa * 0.6f, Offset(x, yCopa))
                        drawCircle(Color(0xFF2E7D32), anchoCopa * 0.5f, Offset(x, yCopa - anchoCopa * 0.35f))
                        drawCircle(Color(0xFF43A047), anchoCopa * 0.35f, Offset(x, yCopa - anchoCopa * 0.68f))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CAJAS DE ÍTEMS CTR
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.dibujarCajasItems(w: Float, h: Float, horizY: Float, motor: MotorCarreraBT21) {
    motor.cajasItem.forEach { caja ->
        if (!caja.recogida) {
            val dist = caja.distanciaZ - motor.distanciaJugador
            if (dist in 10f..580f) {
                val t = (1f - (dist / 600f)).coerceIn(0.1f, 1f)
                val y = horizY + (h - horizY) * (t * t)

                val curvaOffset = (1f - t) * (1f - t) * (motor.curvaActual * 240f)
                val anchoPista = (w * 0.28f) + (w * 0.84f) * (t * t)
                val x = w * 0.5f + curvaOffset + (caja.carrilX * anchoPista * 0.40f)

                val tam = 44f * t + 10f
                drawRoundRect(
                    color = Color(0xFFFF9100),
                    topLeft = Offset(x - tam * 0.5f, y - tam * 0.5f),
                    size = Size(tam, tam),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(x - tam * 0.5f, y - tam * 0.5f),
                    size = Size(tam, tam),
                    style = Stroke(3f * t + 1f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawCircle(Color.White, tam * 0.22f, Offset(x, y))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KART CONVERTIBLE DE JUGUETE EXACTO (FOTO 1 DE MCDONALD'S)
// CON CARACTERÍSTICAS HERMOSAS, PROPORCIONADAS Y DETALLADAS
// ─────────────────────────────────────────────────────────────────────────────

fun DrawScope.dibujarKartToyBT21(
    pj: PersonajeBT21,
    colorChasis: Color,
    diseno: DisenoKart,
    centro: Offset,
    escala: Float,
    inclinacion: Float,
    aturdido: Boolean,
    tiempo: Float,
    nitroActivo: Boolean = false,
    escudoActivo: Boolean = false,
    mostrarNombre: Boolean = false
) {
    val x = centro.x
    val y = centro.y
    val r = 56f * escala

    if (mostrarNombre) {
        val yBadge = y - r * 1.8f
        drawRoundRect(
            color = Color(0xCC000000),
            topLeft = Offset(x - 38f * escala, yBadge - 12f * escala),
            size = Size(76f * escala, 24f * escala),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawCircle(pj.colorCabeza, 7f * escala, Offset(x - 24f * escala, yBadge))
    }

    drawOval(Color(0x55000000), Offset(x - r * 1.35f, y + r * 0.75f), Size(r * 2.7f, r * 0.75f))

    // 1. Ruedas estilo juguete redondeadas
    val anchoRueda = 24f * escala
    val altoRueda = 48f * escala
    drawRoundRect(Color(0xFF263238), Offset(x - r * 1.35f, y + r * 0.15f), Size(anchoRueda, altoRueda), CornerRadius(8f, 8f))
    drawCircle(Color.White, 7f * escala, Offset(x - r * 1.35f + anchoRueda * 0.5f, y + r * 0.15f + altoRueda * 0.5f))
    drawRoundRect(Color(0xFF263238), Offset(x + r * 1.35f - anchoRueda, y + r * 0.15f), Size(anchoRueda, altoRueda), CornerRadius(8f, 8f))
    drawCircle(Color.White, 7f * escala, Offset(x + r * 1.35f - anchoRueda * 0.5f, y + r * 0.15f + altoRueda * 0.5f))

    // 2. Fuego de escape en Nitro
    if (nitroActivo) {
        val flamaY = y + r * 1.05f
        val altoFlama = 60f + sin(tiempo * 50f) * 20f
        drawPath(
            Path().apply {
                moveTo(x - r * 0.5f, flamaY)
                lineTo(x - r * 0.7f, flamaY + altoFlama)
                lineTo(x - r * 0.3f, flamaY)
                close()
            },
            Color(0xFFFF3D00)
        )
        drawPath(
            Path().apply {
                moveTo(x + r * 0.3f, flamaY)
                lineTo(x + r * 0.7f, flamaY + altoFlama)
                lineTo(x + r * 0.5f, flamaY)
                close()
            },
            Color(0xFFFFD54F)
        )
    }

    // 3. Carrocería convertible retro (acabado pastel limpio con brillo)
    drawRoundRect(
        color = colorChasis,
        topLeft = Offset(x - r * 1.15f, y - r * 0.32f),
        size = Size(r * 2.3f, r * 1.30f),
        cornerRadius = CornerRadius(24f * escala, 24f * escala)
    )

    // Brillo superior en el chasis
    drawRoundRect(
        color = Color(0x33FFFFFF),
        topLeft = Offset(x - r * 1.05f, y - r * 0.28f),
        size = Size(r * 2.1f, r * 0.35f),
        cornerRadius = CornerRadius(14f * escala, 14f * escala)
    )

    // Parachoques blanco ranurado (como el juguete)
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x - r * 0.88f, y + r * 0.72f),
        size = Size(r * 1.76f, 18f * escala),
        cornerRadius = CornerRadius(7f, 7f)
    )
    for (i in 0..4) {
        val sx = x - r * 0.6f + i * (r * 0.3f)
        drawRect(Color(0xFF90A4AE), Offset(sx, y + r * 0.76f), Size(5f * escala, 11f * escala))
    }

    // Faros redondos blancos clásicos con borde
    drawCircle(Color.White, 14f * escala, Offset(x - r * 0.85f, y + r * 0.45f))
    drawCircle(Color(0xFFFFF9C4), 9f * escala, Offset(x - r * 0.85f, y + r * 0.45f))
    drawCircle(Color.White, 14f * escala, Offset(x + r * 0.85f, y + r * 0.45f))
    drawCircle(Color(0xFFFFF9C4), 9f * escala, Offset(x + r * 0.85f, y + r * 0.45f))

    // Círculo amarillo con el #21 en el capó
    drawCircle(Color(0xFFFFD54F), 20f * escala, Offset(x, y + r * 0.32f))
    drawCircle(Color.White, 20f * escala, Offset(x, y + r * 0.32f), style = Stroke(3f * escala))

    when (diseno) {
        DisenoKart.Clasico21 -> {
            drawLine(Color(0xFF1565C0), Offset(x - 9f * escala, y + r * 0.24f), Offset(x - 3f * escala, y + r * 0.24f), 3.5f * escala)
            drawLine(Color(0xFF1565C0), Offset(x - 3f * escala, y + r * 0.24f), Offset(x - 9f * escala, y + r * 0.40f), 3.5f * escala)
            drawLine(Color(0xFF1565C0), Offset(x - 9f * escala, y + r * 0.40f), Offset(x - 2f * escala, y + r * 0.40f), 3.5f * escala)
            drawLine(Color(0xFF1565C0), Offset(x + 5f * escala, y + r * 0.22f), Offset(x + 5f * escala, y + r * 0.40f), 3.5f * escala)
        }
        DisenoKart.Corazones -> {
            drawCircle(Color(0xFFE53935), 9f * escala, Offset(x - 4f * escala, y + r * 0.32f))
            drawCircle(Color(0xFFE53935), 9f * escala, Offset(x + 4f * escala, y + r * 0.32f))
        }
        DisenoKart.Estrellas -> drawCircle(Color(0xFFFF8F00), 10f * escala, Offset(x, y + r * 0.32f))
        DisenoKart.Rayos -> drawCircle(Color(0xFFFF3D00), 10f * escala, Offset(x, y + r * 0.32f))
        DisenoKart.Llamas -> drawCircle(Color(0xFFFF1744), 10f * escala, Offset(x, y + r * 0.32f))
    }

    // Parabrisas transparente
    drawRoundRect(
        color = Color(0x99B2EBF2),
        topLeft = Offset(x - r * 0.8f, y - r * 0.58f),
        size = Size(r * 1.6f, r * 0.48f),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Espejitos retrovisores blancos
    drawCircle(Color.White, 8f * escala, Offset(x - r * 1.08f, y - r * 0.35f))
    drawCircle(Color.White, 8f * escala, Offset(x + r * 1.08f, y - r * 0.35f))

    // Manitas del personaje en el volante
    drawCircle(Color.White, 7f * escala, Offset(x - r * 0.45f, y - r * 0.15f))
    drawCircle(Color.White, 7f * escala, Offset(x + r * 0.45f, y - r * 0.15f))

    // 4. CABEZA HERMOSA Y DETALLADA DEL PERSONAJE BT21
    val cabezaY = y - r * 1.05f
    dibujarCabezaBT21Hermosa(this, pj, Offset(x, cabezaY), escala)

    // Escudo
    if (escudoActivo) {
        val pulso = sin(tiempo * 18f) * 10f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x3340C4FF), Color(0xBB00E5FF), Color.Transparent),
                center = Offset(x, y),
                radius = r * 2.3f + pulso
            ),
            radius = r * 2.3f + pulso,
            center = Offset(x, y)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CABEZAS HERMOSAS DE BT21 CON TRAZOS LIMPIOS Y EXPRESIONES FIELES
// ─────────────────────────────────────────────────────────────────────────────

fun dibujarCabezaBT21Hermosa(drawScope: DrawScope, pj: PersonajeBT21, centro: Offset, escala: Float) {
    with(drawScope) {
        val x = centro.x
        val y = centro.y
        val s = escala * 1.25f

        when (pj.tipo) {
            // ── CHIMMY (Capucha amarilla brillante, cara redonda blanca, lengua afuera)
            TipoPersonajeBT21.CHIMMY -> {
                // Orejas de perrito negras colgantes suaves
                drawRoundRect(Color(0xFF212121), Offset(x - 52f * s, y - 18f * s), Size(22f * s, 48f * s), CornerRadius(11f, 11f))
                drawRoundRect(Color(0xFF212121), Offset(x + 30f * s, y - 18f * s), Size(22f * s, 48f * s), CornerRadius(11f, 11f))

                // Capucha amarilla redonda
                drawCircle(Color(0xFFFFD54F), 44f * s, Offset(x, y))
                drawCircle(Color(0xFFFFC107), 44f * s, Offset(x, y), style = Stroke(2f * s))

                // Cara blanca interior
                drawOval(Color.White, Offset(x - 30f * s, y - 26f * s), Size(60f * s, 54f * s))

                // Ojos negros redondos y tiernos con brillito
                drawCircle(Color.Black, 4.2f * s, Offset(x - 14f * s, y - 5f * s))
                drawCircle(Color.White, 1.4f * s, Offset(x - 15f * s, y - 6f * s))
                drawCircle(Color.Black, 4.2f * s, Offset(x + 14f * s, y - 5f * s))
                drawCircle(Color.White, 1.4f * s, Offset(x + 13f * s, y - 6f * s))

                // Nariz pequeña de botón negra
                drawCircle(Color.Black, 4f * s, Offset(x, y + 4f * s))

                // Lengüita rosa graciosa saliendo hacia un lado
                drawRoundRect(Color(0xFFFF4081), Offset(x + 2f * s, y + 10f * s), Size(13f * s, 14f * s), CornerRadius(6f, 6f))

                // Cordones de la capucha con bolitas naranjas
                drawLine(Color(0xFFFF6D00), Offset(x - 8f * s, y + 36f * s), Offset(x - 8f * s, y + 48f * s), 3f * s, StrokeCap.Round)
                drawLine(Color(0xFFFF6D00), Offset(x + 8f * s, y + 36f * s), Offset(x + 8f * s, y + 48f * s), 3f * s, StrokeCap.Round)
            }

            // ── TATA (Corazón perfecto rojo de labios amarillos)
            TipoPersonajeBT21.TATA -> {
                val rCorazon = 36f * s
                drawCircle(Color(0xFFE53935), rCorazon, Offset(x - 19f * s, y - 10f * s))
                drawCircle(Color(0xFFE53935), rCorazon, Offset(x + 19f * s, y - 10f * s))
                drawPath(
                    Path().apply {
                        moveTo(x - 50f * s, y - 6f * s)
                        lineTo(x + 50f * s, y - 6f * s)
                        lineTo(x, y + 46f * s)
                        close()
                    },
                    Color(0xFFE53935)
                )

                // Cejas
                drawLine(Color.Black, Offset(x - 20f * s, y - 6f * s), Offset(x - 8f * s, y - 6f * s), 3f * s, StrokeCap.Round)
                drawLine(Color.Black, Offset(x + 8f * s, y - 6f * s), Offset(x + 20f * s, y - 6f * s), 3f * s, StrokeCap.Round)

                // Ojos
                drawCircle(Color.Black, 4f * s, Offset(x - 14f * s, y + 2f * s))
                drawCircle(Color.Black, 4f * s, Offset(x + 14f * s, y + 2f * s))

                // Labios amarillos característicos de TATA
                drawOval(Color(0xFFFFD54F), Offset(x - 12f * s, y + 12f * s), Size(24f * s, 14f * s))
                drawCircle(Color(0xFFBF360C), 3f * s, Offset(x, y + 19f * s))
            }

            // ── COOKY (Conejito rosa pastel con oreja doblada y ceja desafiante)
            TipoPersonajeBT21.COOKY -> {
                // Oreja izquierda (recta)
                drawRoundRect(Color(0xFFF48FB1), Offset(x - 34f * s, y - 72f * s), Size(22f * s, 50f * s), CornerRadius(11f, 11f))
                drawRoundRect(Color(0xFFFCE4EC), Offset(x - 30f * s, y - 66f * s), Size(14f * s, 36f * s), CornerRadius(7f, 7f))

                // Oreja derecha doblada
                drawRoundRect(Color(0xFFF48FB1), Offset(x + 12f * s, y - 52f * s), Size(22f * s, 30f * s), CornerRadius(11f, 11f))
                drawRoundRect(Color(0xFFF48FB1), Offset(x + 18f * s, y - 36f * s), Size(20f * s, 26f * s), CornerRadius(10f, 10f))

                // Cabeza
                drawCircle(Color(0xFFF48FB1), 42f * s, Offset(x, y))

                // Ceja ruda y ceja recta
                drawLine(Color(0xFF4A148C), Offset(x - 22f * s, y - 12f * s), Offset(x - 8f * s, y - 18f * s), 4f * s, StrokeCap.Round)
                drawLine(Color(0xFF4A148C), Offset(x + 8f * s, y - 14f * s), Offset(x + 22f * s, y - 14f * s), 4f * s, StrokeCap.Round)

                drawCircle(Color.Black, 4f * s, Offset(x - 14f * s, y - 2f * s))
                drawCircle(Color.Black, 4f * s, Offset(x + 14f * s, y - 2f * s))
                drawCircle(Color(0xFF880E4F), 4f * s, Offset(x, y + 7f * s))
                drawArc(Color.Black, 20f, 140f, false, Offset(x - 9f * s, y + 9f * s), Size(18f * s, 9f * s), style = Stroke(3f * s))
            }

            // ── KOYA (Koala celeste con orejotas redondas y ojos dormilones)
            TipoPersonajeBT21.KOYA -> {
                drawCircle(Color(0xFF81D4FA), 22f * s, Offset(x - 36f * s, y - 22f * s))
                drawCircle(Color.White, 12f * s, Offset(x - 36f * s, y - 22f * s))
                drawCircle(Color(0xFF81D4FA), 22f * s, Offset(x + 36f * s, y - 22f * s))
                drawCircle(Color.White, 12f * s, Offset(x + 36f * s, y - 22f * s))

                drawCircle(Color(0xFF81D4FA), 40f * s, Offset(x, y))

                // Ojos dormilones tiernos
                drawArc(Color.Black, 20f, 140f, false, Offset(x - 22f * s, y - 9f * s), Size(15f * s, 10f * s), style = Stroke(3.5f * s, cap = StrokeCap.Round))
                drawArc(Color.Black, 20f, 140f, false, Offset(x + 7f * s, y - 9f * s), Size(15f * s, 10f * s), style = Stroke(3.5f * s, cap = StrokeCap.Round))
                // Nariz de koala moradita
                drawOval(Color(0xFF7E57C2), Offset(x - 10f * s, y - 1f * s), Size(20f * s, 16f * s))
            }

            // ── RJ (Alpaca blanca de lana esponjosa y pañuelito rojo)
            TipoPersonajeBT21.RJ -> {
                // Mechones esponjosos de lana
                drawCircle(Color(0xFFECEFF1), 20f * s, Offset(x - 32f * s, y - 26f * s))
                drawCircle(Color(0xFFECEFF1), 20f * s, Offset(x, y - 38f * s))
                drawCircle(Color(0xFFECEFF1), 20f * s, Offset(x + 32f * s, y - 26f * s))

                drawCircle(Color.White, 40f * s, Offset(x, y))
                drawCircle(Color.White, 11f * s, Offset(x - 28f * s, y - 36f * s))
                drawCircle(Color.White, 11f * s, Offset(x + 28f * s, y - 36f * s))

                drawCircle(Color.Black, 4f * s, Offset(x - 13f * s, y - 5f * s))
                drawCircle(Color.Black, 4f * s, Offset(x + 13f * s, y - 5f * s))
                drawCircle(Color(0xFFFFCDD2), 7f * s, Offset(x - 21f * s, y + 5f * s))
                drawCircle(Color(0xFFFFCDD2), 7f * s, Offset(x + 21f * s, y + 5f * s))
                drawCircle(Color.Black, 4f * s, Offset(x, y + 2f * s))

                // Pañuelo rojo anudado
                drawPath(
                    Path().apply {
                        moveTo(x - 25f * s, y + 36f * s)
                        lineTo(x + 25f * s, y + 36f * s)
                        lineTo(x, y + 54f * s)
                        close()
                    },
                    Color(0xFFE53935)
                )
            }

            // ── SHOOKY (Galletita dorada traviesa con dientecito)
            TipoPersonajeBT21.SHOOKY -> {
                drawRoundRect(Color(0xFF8D6E63), Offset(x - 36f * s, y - 22f * s), Size(72f * s, 54f * s), CornerRadius(24f, 24f))
                drawLine(Color(0xFF3E2723), Offset(x - 22f * s, y - 12f * s), Offset(x - 8f * s, y - 8f * s), 3.5f * s, StrokeCap.Round)
                drawLine(Color(0xFF3E2723), Offset(x + 8f * s, y - 8f * s), Offset(x + 22f * s, y - 12f * s), 3.5f * s, StrokeCap.Round)

                drawCircle(Color.Black, 4f * s, Offset(x - 14f * s, y - 1f * s))
                drawCircle(Color.Black, 4f * s, Offset(x + 14f * s, y - 1f * s))

                drawRoundRect(Color(0xFF3E2723), Offset(x - 18f * s, y + 9f * s), Size(36f * s, 15f * s), CornerRadius(8f, 8f))
                drawRect(Color.White, Offset(x - 8f * s, y + 9f * s), Size(16f * s, 6f * s))
            }

            // ── MANG (Pony bailarín con máscara lila y nariz de corazón)
            TipoPersonajeBT21.MANG -> {
                drawCircle(Color(0xFF4FC3F7), 15f * s, Offset(x - 18f * s, y - 36f * s))
                drawCircle(Color(0xFF4FC3F7), 18f * s, Offset(x, y - 38f * s))
                drawCircle(Color(0xFF4FC3F7), 15f * s, Offset(x + 18f * s, y - 36f * s))

                drawRoundRect(Color(0xFFCE93D8), Offset(x - 38f * s, y - 28f * s), Size(76f * s, 64f * s), CornerRadius(26f, 26f))
                drawCircle(Color(0xFFEC407A), 10f * s, Offset(x - 6f * s, y + 10f * s))
                drawCircle(Color(0xFFEC407A), 10f * s, Offset(x + 6f * s, y + 10f * s))

                drawCircle(Color.Black, 4.5f * s, Offset(x - 17f * s, y - 4f * s))
                drawCircle(Color.White, 1.8f * s, Offset(x - 18f * s, y - 5f * s))
                drawCircle(Color.Black, 4.5f * s, Offset(x + 17f * s, y - 4f * s))
                drawCircle(Color.White, 1.8f * s, Offset(x + 16f * s, y - 5f * s))
            }

            // ── VAN (Robot espacial bicolor con ojos X y O)
            TipoPersonajeBT21.VAN -> {
                val oreja = Path().apply {
                    moveTo(x - 28f * s, y - 30f * s)
                    lineTo(x, y - 56f * s)
                    lineTo(x + 28f * s, y - 30f * s)
                    close()
                }
                drawPath(oreja, Color(0xFF78909C))

                drawArc(Color(0xFF78909C), 90f, 180f, true, Offset(x - 40f * s, y - 32f * s), Size(80f * s, 72f * s))
                drawArc(Color.White, 270f, 180f, true, Offset(x - 40f * s, y - 32f * s), Size(80f * s, 72f * s))

                drawLine(Color.Black, Offset(x - 22f * s, y - 8f * s), Offset(x - 10f * s, y + 4f * s), 3.5f * s, StrokeCap.Round)
                drawLine(Color.Black, Offset(x - 10f * s, y - 8f * s), Offset(x - 22f * s, y + 4f * s), 3.5f * s, StrokeCap.Round)
                drawCircle(Color.Black, 4.5f * s, Offset(x + 16f * s, y - 2f * s))
                drawLine(Color.Black, Offset(x - 12f * s, y + 18f * s), Offset(x + 12f * s, y + 18f * s), 3.5f * s, StrokeCap.Round)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// POLAROID SELFIE DE BT21 PARA LA PORTADA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FotoPolaroidBT21Selfie(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(4f, 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = Color(0xFFE0E0E0),
            topLeft = Offset(4f, 4f),
            size = Size(w - 8f, h - 8f),
            style = Stroke(3f),
            cornerRadius = CornerRadius(16f, 16f)
        )

        val fotoW = w - 36f
        val fotoH = h - 74f
        val fotoX = 18f
        val fotoY = 18f

        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA))
            ),
            topLeft = Offset(fotoX, fotoY),
            size = Size(fotoW, fotoH),
            cornerRadius = CornerRadius(10f, 10f)
        )

        drawLine(Color(0xFFFFCC80), Offset(fotoX, fotoY + fotoH * 0.35f), Offset(fotoX + fotoW, fotoY + fotoH * 0.35f), 6f)
        drawLine(Color(0xFFFFCC80), Offset(fotoX + fotoW * 0.5f, fotoY), Offset(fotoX + fotoW * 0.5f, fotoY + fotoH * 0.35f), 6f)

        // RJ
        dibujarCabezaBT21Hermosa(this, listaBT21[4], Offset(fotoX + fotoW * 0.22f, fotoY + fotoH * 0.65f), 0.72f)
        // TATA
        dibujarCabezaBT21Hermosa(this, listaBT21[0], Offset(fotoX + fotoW * 0.68f, fotoY + fotoH * 0.50f), 0.70f)
        // COOKY
        dibujarCabezaBT21Hermosa(this, listaBT21[2], Offset(fotoX + fotoW * 0.44f, fotoY + fotoH * 0.42f), 0.66f)
        // KOYA
        dibujarCabezaBT21Hermosa(this, listaBT21[3], Offset(fotoX + fotoW * 0.82f, fotoY + fotoH * 0.52f), 0.66f)
        // SHOOKY
        dibujarCabezaBT21Hermosa(this, listaBT21[5], Offset(fotoX + fotoW * 0.82f, fotoY + fotoH * 0.84f), 0.70f)
        // CHIMMY EN PRIMER PLANO SOSTENIENDO EL SELFIE
        dibujarCabezaBT21Hermosa(this, listaBT21[1], Offset(fotoX + fotoW * 0.50f, fotoY + fotoH * 0.74f), 0.95f)

        drawCircle(Color.White, 22f, Offset(w * 0.5f, h - 8f))
        drawCircle(Color(0xFFE0E0E0), 22f, Offset(w * 0.5f, h - 8f), style = Stroke(2f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AUXILIARES
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.dibujarNubesBT21(w: Float, horizY: Float, offset: Float) {
    val nubeColor = Color(0x66FFFFFF)
    for (i in 0..5) {
        val cx = ((i * 240f - offset) % (w + 350f)) - 120f
        val cy = horizY * (0.22f + (i % 3) * 0.16f)
        drawCircle(nubeColor, 50f, Offset(cx, cy))
        drawCircle(nubeColor, 65f, Offset(cx + 40f, cy - 8f))
        drawCircle(nubeColor, 45f, Offset(cx + 80f, cy))
    }
}

private fun DrawScope.dibujarFondoTematico(w: Float, horizY: Float, pista: PistaBT21, offset: Float) {
    if (pista.id == "ciudad") {
        for (i in 0..8) {
            val bx = ((i * 140f - offset) % (w + 200f)) - 60f
            val alto = 85f + ((i * 37) % 70)
            drawRect(Color(0xFF1E1035), Offset(bx, horizY - alto), Size(80f, alto))
            drawCircle(Color(0xFFFF4081), 4f, Offset(bx + 20f, horizY - alto + 25f))
            drawCircle(Color(0xFF00E5FF), 4f, Offset(bx + 55f, horizY - alto + 45f))
        }
    } else {
        val path = Path().apply {
            moveTo(-100f, horizY)
            for (i in 0..6) {
                val px = (i * ((w + 300f) / 6) - offset % 150f) - 80f
                val py = horizY - (if (i % 2 == 0) 95f else 60f)
                lineTo(px, py)
            }
            lineTo(w + 100f, horizY)
            close()
        }
        drawPath(path, pista.colorPasto.copy(alpha = 0.55f))
    }
}

private fun DrawScope.dibujarLineasNitro(w: Float, h: Float, tiempo: Float) {
    val centro = Offset(w * 0.5f, h * 0.55f)
    for (i in 0..16) {
        val ang = (i * 22.5f + (tiempo * 200f) % 22.5f) * (PI.toFloat() / 180f)
        val r1 = w * 0.28f
        val r2 = w * 0.68f
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(centro.x + cos(ang) * r1, centro.y + sin(ang) * r1),
            end = Offset(centro.x + cos(ang) * r2, centro.y + sin(ang) * r2),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

fun DrawScope.dibujarKartGrandeBT21(
    pj: PersonajeBT21,
    colorChasis: Color,
    diseno: DisenoKart,
    centro: Offset,
    escala: Float,
    inclinacion: Float,
    aturdido: Boolean,
    tiempo: Float,
    nitroActivo: Boolean = false,
    escudoActivo: Boolean = false,
    mostrarNombre: Boolean = false
) {
    dibujarKartToyBT21(pj, colorChasis, diseno, centro, escala, inclinacion, aturdido, tiempo, nitroActivo, escudoActivo, mostrarNombre)
}

fun DrawScope.dibujarCabezaBT21Grande(pj: PersonajeBT21, centro: Offset, escala: Float) {
    dibujarCabezaBT21Hermosa(this, pj, centro, escala)
}
