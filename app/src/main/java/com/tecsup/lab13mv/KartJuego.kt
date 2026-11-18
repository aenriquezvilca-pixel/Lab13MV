package com.tecsup.lab13mv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

const val FILAS = 64

class Rival(val corredor: Corredor, var z: Float, var x: Float, val vBase: Float, val fase: Float) {
    var v = 0f
    var giro = 0f
    var terminado = false
}

class Trampa(val z: Float, val x: Float)

class Misil(var z: Float, var x: Float, val objetivo: Rival)

// Lógica de una carrera con 2 carriles: el kart siempre queda centrado en un carril.
class CarreraKart(
    val pista: RegionPeru,
    val indicePista: Int,
    val corredor: Corredor,
    val vueltas: Int = 2
) {
    val largo = 1500f
    val total = largo * vueltas

    // Estado observable por la interfaz
    var fase by mutableStateOf(FaseCarrera.Cuenta)
    var cuenta by mutableIntStateOf(3)
    var posicion by mutableIntStateOf(4)
    var vuelta by mutableIntStateOf(1)
    var galletas by mutableIntStateOf(0)
    var monedas by mutableIntStateOf(0)
    var faltan by mutableIntStateOf(total.toInt())
    var item by mutableStateOf<ItemKart?>(null)
    var mensaje by mutableStateOf("")
    var turboActivo by mutableStateOf(false)
    var posFinal by mutableIntStateOf(4)
    var frame by mutableIntStateOf(0)

    // Entrada del jugador
    var izq = false
    var der = false
    var derrape = false

    // Física
    var z = 0f
    var x = 0.5f          // posición lateral: -0.5 = centro del carril izquierdo, +0.5 = carril derecho
    var carril = 1        // carril objetivo: -1 izquierdo, +1 derecho
    var v = 0f
    var turbo = 0f
    var escudo = 0f
    var girando = 0f
    var carga = 0f
    var derrapando = false
    var dirDerrape = 1
    var rumbo = 0f
    var tiempo = 0f
    private var mensajeT = 0f

    val recogidos = HashSet<Int>()
    val cajasTomadas = HashSet<Int>()
    val obstaculosGolpeados = HashSet<Int>()
    val padsUsados = HashSet<Int>()
    val trampas = mutableListOf<Trampa>()
    val misiles = mutableListOf<Misil>()

    // Arreglos que llena el dibujo de la pista (perspectiva) para ubicar objetos
    val centros = FloatArray(FILAS)
    val anchos = FloatArray(FILAS)

    val rivales: List<Rival> = corredores
        .filter { it != corredor }
        .shuffled(kotlin.random.Random(indicePista + 7))
        .take(3)
        .mapIndexed { i, c ->
            Rival(
                corredor = c,
                z = 14f + i * 10f,
                x = if (i % 2 == 0) -0.5f else 0.5f,
                vBase = 56f + indicePista * 3f + i * 1.5f,
                fase = i * 2.1f
            )
        }

    private var terminados = 0

    fun curvatura(zw: Float): Float {
        val s = indicePista * 1.7f
        val c = sin(zw * 0.0045f + s) * 0.85f + sin(zw * 0.013f + s * 2f) * 0.35f
        return c.coerceIn(-1f, 1f) * (0.6f + 0.15f * indicePista)
    }

    // ---- Elementos de la pista (se generan por índice, siempre en el centro de un carril) ----
    // Las monedas y galletas van en "rachas" de 240 m que alternan de carril
    fun carrilRacha(zw: Float): Float = if (floor(zw / 240f).toInt() % 2 == 0) -0.5f else 0.5f

    fun esGalleta(n: Int): Boolean = n % 4 == 0

    fun hayObstaculo(m: Int): Boolean = m >= 3 && (m * 7) % 3 != 0
    fun tipoObstaculo(m: Int): Int = m % 3                       // 0 caja, 1 cono, 2 roca
    fun xObstaculo(m: Int): Float = carrilRacha(m * 90f)         // están sobre la racha: hay que esquivarlos

    fun hayObstaculoCerca(zn: Float): Boolean {
        val m = Math.round(zn / 90f)
        return hayObstaculo(m) && abs(m * 90f - zn) < 25f
    }

    // Turbo pads: solo en UN carril (el contrario a la racha de monedas)
    fun xPad(p: Int): Float = -carrilRacha(p * 400f)

    fun xCaja(m: Int): Float = if (m % 2 == 0) -0.5f else 0.5f

    private fun avisar(texto: String) {
        mensaje = texto
        mensajeT = 1.6f
    }

    fun usarItem() {
        val obj = item ?: return
        if (fase != FaseCarrera.Corriendo) return
        item = null
        val pot = galletas >= 10
        when (obj) {
            ItemKart.Turbo -> { turbo = max(turbo, if (pot) 2.6f else 1.8f); avisar("¡TURBO!") }
            ItemKart.Escudo -> { escudo = if (pot) 6f else 4f; avisar("¡ESCUDO!") }
            ItemKart.Bomba -> { trampas.add(Trampa(z - 8f, x)); avisar("¡Bomba soltada!") }
            ItemKart.Misil -> {
                val objetivo = rivales.filter { it.z > z }.minByOrNull { it.z - z }
                if (objetivo == null) avisar("Sin objetivo...")
                else { misiles.add(Misil(z + 6f, x, objetivo)); avisar("¡MISIL!") }
            }
        }
    }

    fun update(dt: Float) {
        tiempo += dt
        if (cuenta == 0 && tiempo > 0.9f) cuenta = -1
        if (mensajeT > 0f) {
            mensajeT -= dt
            if (mensajeT <= 0f) mensaje = ""
        }

        // ---------------- Jugador ----------------
        val dir = (if (der) 1 else 0) - (if (izq) 1 else 0)
        if (dir != 0) carril = dir
        val vMaxBase = 60f + corredor.velocidad * 1.6f + galletas * 1.2f
        val vMax = if (turbo > 0f) vMaxBase * 1.4f else vMaxBase

        if (girando > 0f) {
            girando -= dt
            v += (20f - v) * min(1f, 3f * dt)
        } else {
            val acel = 1.2f + corredor.aceleracion * 0.25f
            v += (vMax - v) * min(1f, acel * dt)
            // el kart se desliza hasta quedar al MEDIO del carril elegido
            val paso = (2.4f + corredor.giro * 0.35f) * dt * (if (derrapando) 1.4f else 1f)
            val d = carril * 0.5f - x
            x += d.coerceIn(-paso, paso)
        }
        val curvAhora = curvatura(z + 10f)
        x -= curvAhora * dt * v * 0.004f
        x = x.coerceIn(-0.9f, 0.9f)
        rumbo += curvAhora * dt * v * 0.001f
        z += v * dt

        // Derrape (Power Slide): suelta en la zona verde para obtener turbo azul
        if (derrape && v > 30f && girando <= 0f) {
            if (!derrapando) {
                derrapando = true
                dirDerrape = carril
                carga = 0f
            }
            carga = min(1.2f, carga + dt * 0.75f)
        } else if (derrapando) {
            derrapando = false
            if (carga in 0.55f..0.9f) {
                turbo = max(turbo, if (galletas >= 10) 2.0f else 1.4f)
                avisar("¡TURBO AZUL!")
            } else if (carga > 0.9f) {
                avisar("¡Muy tarde!")
            }
            carga = 0f
        }

        turbo = max(0f, turbo - dt)
        escudo = max(0f, escudo - dt)
        val ta = turbo > 0f
        if (ta != turboActivo) turboActivo = ta

        // Galletas y monedas
        val n0 = floor(z / 40f).toInt()
        for (n in n0..n0 + 1) {
            if (n < 2 || n in recogidos) continue
            val zn = n * 40f
            if (abs(zn - z) < 6f && abs(carrilRacha(zn) - x) < 0.3f && !hayObstaculoCerca(zn)) {
                recogidos.add(n)
                if (esGalleta(n)) {
                    galletas = min(10, galletas + 1)
                    if (galletas == 10) avisar("¡POTENCIADO! 10 galletas")
                } else {
                    monedas++
                }
            }
        }

        // Obstáculos: cajas, conos y rocas
        val o0 = floor(z / 90f).toInt()
        for (m in o0..o0 + 1) {
            if (!hayObstaculo(m) || m in obstaculosGolpeados) continue
            if (abs(m * 90f - z) < 5f && abs(xObstaculo(m) - x) < 0.3f) {
                obstaculosGolpeados.add(m)
                if (escudo > 0f || turbo > 0f) {
                    avisar("¡Destruido!")
                } else {
                    girando = 0.7f
                    v *= 0.4f
                    avisar("¡Obstáculo!")
                }
            }
        }

        // Turbo pads (solo en un lado de la pista)
        val p0 = floor(z / 400f).toInt()
        for (p in p0..p0 + 1) {
            if (p < 1 || p in padsUsados) continue
            if (abs(p * 400f - z) < 8f && abs(xPad(p) - x) < 0.3f) {
                padsUsados.add(p)
                turbo = max(turbo, 1.3f)
                avisar("¡TURBO PAD!")
            }
        }

        // Cajas sorpresa
        val m0 = floor(z / 260f).toInt()
        for (m in m0..m0 + 1) {
            if (m < 1 || m in cajasTomadas) continue
            if (abs(m * 260f - z) < 6f && abs(xCaja(m) - x) < 0.3f) {
                cajasTomadas.add(m)
                if (item == null) item = ItemKart.values().random()
            }
        }

        // ---------------- Trampas ----------------
        for (t in trampas.toList()) {
            if (abs(t.z - z) < 4f && abs(t.x - x) < 0.28f && girando <= 0f) {
                trampas.remove(t)
                if (escudo <= 0f) { girando = 1.1f; avisar("¡BOOM!") }
                continue
            }
            for (r in rivales) {
                if (abs(t.z - r.z) < 4f && abs(t.x - r.x) < 0.28f) {
                    trampas.remove(t)
                    r.giro = 1.0f
                    break
                }
            }
        }

        // ---------------- Misiles ----------------
        for (m in misiles.toList()) {
            m.z += 150f * dt
            m.x += (m.objetivo.x - m.x) * min(1f, 4f * dt)
            if (m.z >= m.objetivo.z - 2f) {
                m.objetivo.giro = 1.4f
                misiles.remove(m)
            }
        }

        // ---------------- Rivales (también van por el medio de un carril) ----------------
        for (r in rivales) {
            if (r.giro > 0f) {
                r.giro -= dt
                r.v += (15f - r.v) * min(1f, 3f * dt)
            } else {
                val rubber = ((z - r.z) / 300f).coerceIn(-0.1f, 0.12f)
                val objetivo = r.vBase * (1f + rubber)
                r.v += (objetivo - r.v) * min(1f, 1.5f * dt)
            }
            if (!r.terminado) r.z += r.v * dt
            val carrilR = if (sin(tiempo * 0.5f + r.fase) > 0f) 0.5f else -0.5f
            r.x += (carrilR - r.x) * min(1f, 3f * dt)
            if (!r.terminado && r.z >= total) {
                r.terminado = true
                terminados++
            }
            if (abs(r.z - z) < 5f && abs(r.x - x) < 0.28f && girando <= 0f) {
                if (escudo > 0f) r.giro = 1.0f else v *= 0.7f
            }
        }

        posicion = 1 + rivales.count { it.z > z }
        vuelta = min(vueltas, (z / largo).toInt() + 1)
        faltan = (max(0f, total - z) / 10f).toInt() * 10

        if (z >= total && fase == FaseCarrera.Corriendo) {
            posFinal = 1 + terminados
            fase = FaseCarrera.Fin
        }
        frame++
    }
}

// Estado general: pantallas, corredor elegido, pistas desbloqueadas y trofeos
class JuegoKart {
    var pantalla by mutableStateOf(PantallaKart.Seleccion)
    var corredorIdx by mutableIntStateOf(0)
    var desbloqueadas by mutableIntStateOf(1)
    var trofeos by mutableStateOf(setOf<Int>())
    var mejorPos by mutableStateOf(mapOf<Int, Int>())
    var carrera by mutableStateOf<CarreraKart?>(null)

    val corredor: Corredor get() = corredores[corredorIdx]

    fun iniciar(indice: Int) {
        carrera = CarreraKart(regiones[indice], indice, corredor)
        pantalla = PantallaKart.Carrera
    }

    fun terminar(pos: Int) {
        val i = carrera?.indicePista ?: 0
        val anterior = mejorPos[i] ?: 99
        mejorPos = mejorPos + (i to min(anterior, pos))
        if (pos <= 2) desbloqueadas = max(desbloqueadas, min(regiones.size, i + 2))
        if (pos == 1) trofeos = trofeos + i
        pantalla = PantallaKart.Mapa
    }
}
