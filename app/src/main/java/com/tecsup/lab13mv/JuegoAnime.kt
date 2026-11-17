package com.tecsup.lab13mv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// Estado y lógica del juego (turnos, daño, recompensas y niveles)
class JuegoAnime {
    var pantalla by mutableStateOf(PantallaJuego.Mapa)
    var heroe by mutableStateOf(Heroe())
    var regionActual by mutableIntStateOf(0)
    var desbloqueadas by mutableIntStateOf(1)
    var kills by mutableIntStateOf(0)

    var monstruo by mutableStateOf<Monstruo?>(null)
    var monstruoVisible by mutableStateOf(false)
    var vidaMonstruo by mutableIntStateOf(0)

    var energia by mutableIntStateOf(0)
    var pociones by mutableIntStateOf(3)
    var mensaje by mutableStateOf("")
    var ocupado by mutableStateOf(false)

    // Banderas que disparan las animaciones
    var heroeAtacando by mutableStateOf(false)
    var heroeGolpeado by mutableStateOf(false)
    var monstruoAtacando by mutableStateOf(false)
    var monstruoGolpeado by mutableStateOf(false)
    var subioNivel by mutableStateOf(false)

    val region: RegionPeru get() = regiones[regionActual]

    suspend fun iniciarMision(indice: Int) {
        ocupado = true
        regionActual = indice
        kills = 0
        energia = 0
        pociones = 3
        heroe = heroe.copy(vida = heroe.vidaMax)
        monstruoVisible = false
        monstruo = null
        pantalla = PantallaJuego.Batalla
        mensaje = "Llegas a ${region.nombre}..."
        delay(900)
        aparecerMonstruo()
    }

    private fun aparecerMonstruo() {
        val m = region.monstruos[kills]
        monstruo = m
        vidaMonstruo = m.vidaMax
        monstruoVisible = true
        mensaje = "¡Aparece ${m.nombre}!"
        ocupado = false
    }

    suspend fun atacar(especial: Boolean) {
        val m = monstruo ?: return
        if (ocupado || !monstruoVisible) return
        if (especial && energia < 100) return
        ocupado = true

        heroeAtacando = true
        delay(180)
        val base = heroe.ataque + Random.nextInt(0, 5)
        val dano = if (especial) (base * 2.5f).toInt() else base
        vidaMonstruo = max(0, vidaMonstruo - dano)
        energia = if (especial) 0 else min(100, energia + 25)
        monstruoGolpeado = true
        mensaje = if (especial) "¡ATAQUE SOLAR! Inti causa $dano de daño"
        else "Inti ataca: $dano de daño"
        delay(250)
        heroeAtacando = false
        monstruoGolpeado = false

        if (vidaMonstruo == 0) derrotarMonstruo(m) else contraatacar(m)
    }

    suspend fun curar() {
        val m = monstruo ?: return
        if (ocupado || !monstruoVisible || pociones == 0 || heroe.vida == heroe.vidaMax) return
        ocupado = true
        pociones--
        val cura = (heroe.vidaMax * 0.4f).toInt()
        heroe = heroe.copy(vida = min(heroe.vidaMax, heroe.vida + cura))
        mensaje = "Inti bebe una poción: +$cura de vida"
        delay(500)
        contraatacar(m)
    }

    private suspend fun contraatacar(m: Monstruo) {
        delay(500)
        monstruoAtacando = true
        delay(200)
        val dano = m.ataque + Random.nextInt(0, 3)
        heroe = heroe.copy(vida = max(0, heroe.vida - dano))
        heroeGolpeado = true
        mensaje = "${m.nombre} te golpea: -$dano de vida"
        delay(250)
        monstruoAtacando = false
        heroeGolpeado = false
        if (heroe.vida == 0) {
            delay(500)
            pantalla = PantallaJuego.Derrota
        }
        ocupado = false
    }

    private suspend fun derrotarMonstruo(m: Monstruo) {
        monstruoVisible = false
        kills++
        mensaje = "¡Derrotaste a ${m.nombre}! +${m.xp} XP"

        // Recompensa por cada monstruo: más vida máxima, más fuerza y un poco de curación
        var h = heroe.copy(
            vidaMax = heroe.vidaMax + 5,
            ataque = heroe.ataque + 1,
            xp = heroe.xp + m.xp
        )
        h = h.copy(vida = min(h.vidaMax, h.vida + h.vidaMax / 4))

        // Si junta suficiente XP sube de nivel: gran aumento de vida y fuerza
        var subio = false
        while (h.xp >= h.xpSiguiente) {
            h = h.copy(
                xp = h.xp - h.xpSiguiente,
                nivel = h.nivel + 1,
                vidaMax = h.vidaMax + 15,
                ataque = h.ataque + 4
            )
            h = h.copy(vida = h.vidaMax)
            subio = true
        }
        heroe = h
        if (subio) subioNivel = true

        delay(1400)
        subioNivel = false

        if (kills >= region.monstruos.size) {
            desbloqueadas = max(desbloqueadas, min(regiones.size, regionActual + 2))
            pantalla = PantallaJuego.MisionCompleta
            ocupado = false
        } else {
            aparecerMonstruo()
        }
    }

    fun volverAlMapa() {
        pantalla = PantallaJuego.Mapa
    }
}
