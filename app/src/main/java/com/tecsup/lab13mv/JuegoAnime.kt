package com.tecsup.lab13mv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// Rival en la carrera
data class CorredorBT21(
    val personaje: PersonajeBT21,
    val colorKart: Color,
    var distanciaZ: Float,
    var carrilX: Float, // -0.55f (carril izquierdo) o +0.55f (carril derecho)
    var velocidad: Float,
    var aturdido: Float = 0f
)

class MotorCarreraBT21 {
    var pantalla by mutableStateOf(PantallaJuego.PortadaPolaroid)

    var personajeIdx by mutableIntStateOf(0)
    val personajeActual: PersonajeBT21 get() = listaBT21[personajeIdx]

    // Garage
    var colorKartSeleccionado by mutableStateOf(Color(0xFF43A047))
    var disenoKartSeleccionado by mutableStateOf(DisenoKart.Clasico21)

    // Pista
    var pistaIdx by mutableIntStateOf(0)
    val pistaActual: PistaBT21 get() = pistasBT21[pistaIdx]

    // ─── FÍSICAS CTR CON 2 CARRILES AMPLIOS Y LÍMITES ESTRICTOS ───────────────
    val metaDistancia = 3600f
    var distanciaJugador by mutableFloatStateOf(0f)
    var velocidadJugador by mutableFloatStateOf(0f)

    // Posición del jugador: Limitado estrictamente para que NUNCA se salga de la pista
    // -0.75f = carril izquierdo, 0f = centro, +0.75f = carril derecho (límite estricto ±0.80f)
    var posXJugador by mutableFloatStateOf(0f)
    var direccionVolante by mutableFloatStateOf(0f)

    var curvaActual by mutableFloatStateOf(0f)

    // 5 Vidas
    var vidasRestantes by mutableIntStateOf(5)
    var choquesTotales by mutableIntStateOf(0)

    // Monedas
    var monedasRecogidas by mutableIntStateOf(0)

    // Turbo Pad
    var tiempoTurboPad by mutableFloatStateOf(0f)

    // Ítems CTR
    var itemAlmacenado by mutableStateOf<TipoItemCTR?>(null)
    var escudoActivo by mutableStateOf(false)
    var tiempoEscudo by mutableFloatStateOf(0f)
    var mensajeCTR by mutableStateOf("")

    // Nitro y Salto
    var nitro by mutableFloatStateOf(85f)
    var nitroActivo by mutableStateOf(false)
    var enSalto by mutableFloatStateOf(0f)
    var tiempoAturdido by mutableFloatStateOf(0f)

    // Elementos de pista
    val rivales = mutableStateListOf<CorredorBT21>()
    val monedas = mutableStateListOf<MonedaPista>()
    val obstaculos = mutableStateListOf<ObstaculoInteractivo>()
    val turboPads = mutableStateListOf<TurboPadPista>()
    val cajasItem = mutableStateListOf<CajaItemCTR>()

    // Cámara y posición
    var shakeCamara by mutableFloatStateOf(0f)
    var posicionJugador by mutableIntStateOf(1)
    var tiempoCarreraSegundos by mutableFloatStateOf(0f)
    var carreraTerminada by mutableStateOf(false)

    fun elegirPersonaje(idx: Int) {
        personajeIdx = idx
        colorKartSeleccionado = listaBT21[idx].colorKartPredeterminado
    }

    fun prepararCarrera() {
        distanciaJugador = 0f
        velocidadJugador = 0f
        posXJugador = 0f
        direccionVolante = 0f
        vidasRestantes = 5
        choquesTotales = 0
        monedasRecogidas = 0
        tiempoTurboPad = 0f
        itemAlmacenado = TipoItemCTR.Turbo
        escudoActivo = false
        tiempoEscudo = 0f
        mensajeCTR = "¡Pista amplia de 2 carriles! ¡Salta cajas 📦 y esquiva árboles 🌲!"
        nitro = 90f
        nitroActivo = false
        enSalto = 0f
        tiempoAturdido = 0f
        shakeCamara = 0f
        posicionJugador = 1
        tiempoCarreraSegundos = 0f
        carreraTerminada = false

        // 1. Rivales en los 2 carriles amplios (-0.5f o +0.5f)
        rivales.clear()
        val otros = listaBT21.filterIndexed { index, _ -> index != personajeIdx }
        val carriles = listOf(-0.50f, 0.50f, -0.45f, 0.45f, 0f)
        val distancias = listOf(35f, 55f, 80f, 110f, 140f)

        otros.take(5).forEachIndexed { i, p ->
            rivales.add(
                CorredorBT21(
                    personaje = p,
                    colorKart = p.colorKartPredeterminado,
                    distanciaZ = distancias[i],
                    carrilX = carriles[i],
                    velocidad = 22f + p.velocidadBase * 8f
                )
            )
        }

        // 2. Monedas en ambos carriles
        monedas.clear()
        var posM = 40f
        while (posM < metaDistancia - 80f) {
            val carril = listOf(-0.50f, 0.50f).random()
            monedas.add(MonedaPista(posM, carril))
            monedas.add(MonedaPista(posM + 12f, carril))
            monedas.add(MonedaPista(posM + 24f, carril))
            posM += Random.nextInt(70, 115)
        }

        // 3. Bandas de aceleración Turbo Pads en los 2 carriles
        turboPads.clear()
        var posPad = 130f
        while (posPad < metaDistancia - 100f) {
            turboPads.add(TurboPadPista(posPad, listOf(-0.50f, 0.50f).random()))
            posPad += Random.nextInt(280, 390)
        }

        // 4. Obstáculos: Cajas de madera (saltables) y Árboles (esquivables)
        obstaculos.clear()
        var posObs = 85f
        while (posObs < metaDistancia - 80f) {
            val tipo = if (Random.nextBoolean()) TipoObstaculoPista.Arbol else TipoObstaculoPista.CajaMadera
            val carril = listOf(-0.50f, 0.50f).random()
            obstaculos.add(ObstaculoInteractivo(posObs, carril, tipo))
            posObs += Random.nextInt(85, 135)
        }

        // 5. Cajas de Ítems CTR
        cajasItem.clear()
        var posC = 160f
        while (posC < metaDistancia - 100f) {
            cajasItem.add(CajaItemCTR(posC, -0.50f, TipoItemCTR.values().random()))
            cajasItem.add(CajaItemCTR(posC, 0.50f, TipoItemCTR.values().random()))
            posC += Random.nextInt(190, 270)
        }

        pantalla = PantallaJuego.Carrera
    }

    fun iniciarGiro(dir: Float) {
        direccionVolante = dir
    }

    fun detenerGiro() {
        direccionVolante = 0f
    }

    fun saltar() {
        if (enSalto == 0f) {
            enSalto = 1f
        }
    }

    fun activarNitro() {
        if (nitro > 10f && !nitroActivo) {
            nitroActivo = true
        }
    }

    fun soltarNitro() {
        nitroActivo = false
    }

    fun usarItem() {
        val item = itemAlmacenado ?: return
        itemAlmacenado = null

        when (item) {
            TipoItemCTR.Misil -> {
                mensajeCTR = "🚀 ¡MISIL GUIADO LANZADO!"
                shakeCamara = 8f
                val rivalCercano = rivales.filter { it.distanciaZ > distanciaJugador }.minByOrNull { it.distanciaZ }
                if (rivalCercano != null) {
                    rivalCercano.aturdido = 3f
                    mensajeCTR = "💥 ¡IMPACTASTE A ${rivalCercano.personaje.nombre}!"
                }
            }
            TipoItemCTR.Bomba -> {
                mensajeCTR = "💣 ¡BOMBA EXPLOTADA!"
                shakeCamara = 14f
                rivales.forEach { r ->
                    if (abs(r.distanciaZ - distanciaJugador) < 220f) {
                        r.aturdido = 2.8f
                    }
                }
            }
            TipoItemCTR.Escudo -> {
                mensajeCTR = "🛡️ ¡ESCUDO MÁGICO ACTIVADO!"
                escudoActivo = true
                tiempoEscudo = 6f
            }
            TipoItemCTR.Turbo -> {
                mensajeCTR = "⚡ ¡TURBO CTR AL MÁXIMO!"
                velocidadJugador += 26f
                shakeCamara = 9f
            }
            TipoItemCTR.Pocion -> {
                mensajeCTR = "🧪 ¡POCIÓN EN LA PISTA!"
            }
        }
    }

    fun registrarChoque(mensaje: String) {
        if (escudoActivo) {
            mensajeCTR = "🛡️ ¡ESCUDO PROTEGIÓ EL GOLPE!"
            return
        }
        vidasRestantes--
        choquesTotales++
        tiempoAturdido = 1.3f
        shakeCamara = 15f
        velocidadJugador = max(6f, velocidadJugador - 28f)
        mensajeCTR = mensaje

        if (vidasRestantes <= 0) {
            pantalla = PantallaJuego.GameOver
        }
    }

    fun actualizar(dt: Float) {
        if (pantalla != PantallaJuego.Carrera || carreraTerminada) return

        tiempoCarreraSegundos += dt

        if (shakeCamara > 0f) {
            shakeCamara = max(0f, shakeCamara - dt * 20f)
        }

        if (tiempoTurboPad > 0f) {
            tiempoTurboPad -= dt
        }

        if (escudoActivo) {
            tiempoEscudo -= dt
            if (tiempoEscudo <= 0f) escudoActivo = false
        }

        // Curvatura de la pista
        curvaActual = sin(distanciaJugador * 0.0035f) * pistaActual.curvaturaPista

        // Aturdido
        if (tiempoAturdido > 0f) {
            tiempoAturdido -= dt
            velocidadJugador = max(8f, velocidadJugador - dt * 45f)
        } else {
            var velMax = 32f + (personajeActual.velocidadBase * 9f) + (monedasRecogidas * 0.4f)
            if (tiempoTurboPad > 0f) velMax += 28f

            if (nitroActivo && nitro > 0f) {
                velMax += 22f
                nitro = max(0f, nitro - dt * 30f)
                if (nitro == 0f) nitroActivo = false
            } else {
                nitro = min(100f, nitro + dt * 8f)
            }

            velocidadJugador = min(velMax, velocidadJugador + dt * 24f)
        }

        // Salto
        if (enSalto > 0f) {
            enSalto = max(0f, enSalto - dt * 2.3f)
        }

        // GIRO FLUIDO: LÍMITE ESTRICTO DE PISTA (-0.78f a +0.78f)
        // El auto NUNCA se sale de las líneas laterales de los 2 carriles
        if (direccionVolante != 0f) {
            posXJugador = (posXJugador + direccionVolante * dt * 2.2f).coerceIn(-0.78f, 0.78f)
        }
        // Suave influencia de curva pero sin salirse del límite
        posXJugador = (posXJugador - curvaActual * dt * 0.20f).coerceIn(-0.78f, 0.78f)

        // Avanzar
        distanciaJugador += velocidadJugador * dt * 10f

        // Rivales en los 2 carriles
        rivales.forEachIndexed { i, rival ->
            if (rival.aturdido > 0f) {
                rival.aturdido -= dt
            } else {
                rival.distanciaZ += rival.velocidad * dt * 10f
                // Se mantienen en su carril preferido (-0.5f o +0.5f)
                val carrilBase = if (i % 2 == 0) -0.50f else 0.50f
                rival.carrilX += (carrilBase - rival.carrilX) * dt * 2f
            }
        }

        // Posición
        var pos = 1
        rivales.forEach { r ->
            if (r.distanciaZ > distanciaJugador) pos++
        }
        posicionJugador = pos

        // 1. Monedas
        monedas.forEach { moneda ->
            if (!moneda.recogida && abs(moneda.distanciaZ - distanciaJugador) < 25f) {
                if (abs(moneda.carrilX - posXJugador) < 0.40f) {
                    moneda.recogida = true
                    monedasRecogidas++
                    mensajeCTR = "💰 ¡Moneda! ($monedasRecogidas monedas)"
                }
            }
        }

        // 2. Turbo Pads
        turboPads.forEach { pad ->
            if (abs(pad.distanciaZ - distanciaJugador) < 22f) {
                if (abs(pad.carrilX - posXJugador) < 0.45f) {
                    tiempoTurboPad = 2.2f
                    velocidadJugador += 28f
                    shakeCamara = 9f
                    mensajeCTR = "⚡ ¡TURBO PAD EN EL CARRIL! ¡ACELERACIÓN!"
                }
            }
        }

        // 3. Obstáculos: Cajas (saltables) vs Árboles (reducidos, no saltables)
        obstaculos.forEach { obs ->
            if (!obs.destruido && abs(obs.distanciaZ - distanciaJugador) < 20f) {
                if (abs(obs.carrilX - posXJugador) < 0.42f) {
                    when (obs.tipo) {
                        TipoObstaculoPista.CajaMadera -> {
                            if (enSalto > 0.22f) {
                                obs.destruido = true
                                monedasRecogidas += 3
                                mensajeCTR = "🦘 ¡SALTASTE LA CAJA DE MADERA! +3 Monedas"
                            } else {
                                obs.destruido = true
                                registrarChoque("💥 ¡CHOCASTE CON LA CAJA! (Debes presionar SALTO 🦘)")
                            }
                        }
                        TipoObstaculoPista.Arbol -> {
                            obs.destruido = true
                            registrarChoque("🌲 ¡CHOCASTE CON UN ÁRBOL! (¡Cámbiese de carril para esquivar!)")
                        }
                    }
                }
            }
        }

        // 4. Cajas de Ítems
        cajasItem.forEach { caja ->
            if (!caja.recogida && abs(caja.distanciaZ - distanciaJugador) < 25f) {
                if (abs(caja.carrilX - posXJugador) < 0.45f) {
                    caja.recogida = true
                    itemAlmacenado = caja.item
                    mensajeCTR = "📦 ¡Conseguiste ${caja.item.name}! Toca ÍTEM"
                }
            }
        }

        // Meta
        if (distanciaJugador >= metaDistancia && vidasRestantes > 0) {
            carreraTerminada = true
            pantalla = PantallaJuego.Podio
        }
    }
}
