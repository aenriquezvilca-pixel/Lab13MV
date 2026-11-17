package com.tecsup.lab13mv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// ---------------------------------------------------------------------------
// DATOS DEL JUEGO: "Leyendas del Perú" (batallas anime contra monstruos)
// ---------------------------------------------------------------------------

enum class Clima { Sol, Viento, Nieve, Lluvia }

enum class PantallaJuego { Mapa, Batalla, MisionCompleta, Derrota }

data class Monstruo(
    val nombre: String,
    val emoji: String,
    val vidaMax: Int,
    val ataque: Int,
    val xp: Int
)

data class RegionPeru(
    val nombre: String,
    val clima: Clima,
    val descripcionClima: String,
    val emojiClima: String,
    val colorCielo: Color,
    val colorSuelo: Color,
    val mision: String,
    val monstruos: List<Monstruo>
)

data class Heroe(
    val nivel: Int = 1,
    val vidaMax: Int = 100,
    val vida: Int = 100,
    val ataque: Int = 12,
    val xp: Int = 0
) {
    val xpSiguiente: Int get() = 50 * nivel
}

// Cada región es una misión con su propio clima y sus monstruos (el último es el jefe)
val regiones = listOf(
    RegionPeru(
        nombre = "Costa · Huacachina",
        clima = Clima.Sol,
        descripcionClima = "Sol ardiente y desierto",
        emojiClima = "☀️",
        colorCielo = Color(0xFFFFB74D),
        colorSuelo = Color(0xFFD7A86E),
        mision = "Derrota a los monstruos de las dunas",
        monstruos = listOf(
            Monstruo("Sirena Oscura", "🧜", 40, 6, 30),
            Monstruo("Alacrán Gigante", "🦂", 50, 7, 35),
            Monstruo("Cóndor del Eclipse (Jefe)", "🦅", 90, 10, 80)
        )
    ),
    RegionPeru(
        nombre = "Altiplano · Titicaca",
        clima = Clima.Viento,
        descripcionClima = "Viento fuerte sobre el lago",
        emojiClima = "🌬️",
        colorCielo = Color(0xFF4FC3F7),
        colorSuelo = Color(0xFF0277BD),
        mision = "Calma las aguas del lago sagrado",
        monstruos = listOf(
            Monstruo("Sapo Gigante", "🐸", 60, 8, 40),
            Monstruo("Cisne Espectral", "🦢", 70, 9, 45),
            Monstruo("Espíritu del Lago (Jefe)", "🌊", 120, 12, 100)
        )
    ),
    RegionPeru(
        nombre = "Sierra · Cusco",
        clima = Clima.Nieve,
        descripcionClima = "Frío extremo y nevada",
        emojiClima = "❄️",
        colorCielo = Color(0xFF90A4AE),
        colorSuelo = Color(0xFFECEFF1),
        mision = "Sube a la montaña y vence al Apu helado",
        monstruos = listOf(
            Monstruo("Ukuku Salvaje", "🐻", 80, 10, 50),
            Monstruo("Llama Oni", "🦙", 90, 11, 55),
            Monstruo("Apu Helado (Jefe)", "🏔️", 150, 14, 130)
        )
    ),
    RegionPeru(
        nombre = "Selva · Amazonas",
        clima = Clima.Lluvia,
        descripcionClima = "Lluvia tropical y humedad",
        emojiClima = "🌧️",
        colorCielo = Color(0xFF2E7D32),
        colorSuelo = Color(0xFF1B3D1F),
        mision = "Protege la selva del gran Amaru",
        monstruos = listOf(
            Monstruo("Otorongo Sombra", "🐆", 100, 13, 60),
            Monstruo("Yacumama", "🐍", 120, 14, 70),
            Monstruo("Amaru Esmeralda (Jefe)", "🐉", 220, 17, 200)
        )
    )
)

// ---------------------------------------------------------------------------
// ESTADO Y LÓGICA DEL JUEGO
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// PANTALLA PRINCIPAL: AnimatedContent cambia entre mapa, batalla y resultados
// ---------------------------------------------------------------------------

@Composable
fun EjercicioFinal(modifier: Modifier = Modifier) {
    val juego = remember { JuegoAnime() }
    val scope = rememberCoroutineScope()

    AnimatedContent(
        targetState = juego.pantalla,
        modifier = modifier.fillMaxSize(),
        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) },
        label = "pantallasJuego"
    ) { pantallaActual ->
        when (pantallaActual) {
            PantallaJuego.Mapa -> PantallaMapa(juego) { indice ->
                scope.launch { juego.iniciarMision(indice) }
            }

            PantallaJuego.Batalla -> PantallaBatalla(juego, scope)

            PantallaJuego.MisionCompleta -> {
                val ultima = juego.regionActual == regiones.size - 1
                PantallaResultado(
                    emoji = "🏆",
                    titulo = if (ultima) "¡Salvaste todo el Perú!" else "¡Misión cumplida!",
                    detalle = "Liberaste ${juego.region.nombre}.\n" +
                            "Inti es nivel ${juego.heroe.nivel} · Vida ${juego.heroe.vidaMax} · Fuerza ${juego.heroe.ataque}",
                    fondo = Color(0xFF1B5E20)
                ) {
                    Button(onClick = { juego.volverAlMapa() }) { Text("Volver al mapa") }
                }
            }

            PantallaJuego.Derrota -> PantallaResultado(
                emoji = "💀",
                titulo = "Has caído...",
                detalle = "Pero tu poder se conserva: nivel ${juego.heroe.nivel}, " +
                        "vida ${juego.heroe.vidaMax}, fuerza ${juego.heroe.ataque}.",
                fondo = Color(0xFF7F0000)
            ) {
                Button(onClick = { scope.launch { juego.iniciarMision(juego.regionActual) } }) {
                    Text("Reintentar misión")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { juego.volverAlMapa() }) { Text("Volver al mapa") }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// MAPA DE MISIONES
// ---------------------------------------------------------------------------

@Composable
private fun PantallaMapa(juego: JuegoAnime, onMision: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D1B2A))))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Text("🇵🇪 Leyendas del Perú", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Elige tu misión, guerrero Inti", color = Color(0xFFB0BEC5), fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))

        PanelHeroe(juego.heroe, mostrarEnergia = false, juego = juego)

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(regiones) { indice, region ->
                TarjetaRegion(region, indice < juego.desbloqueadas) { onMision(indice) }
            }
        }
    }
}

@Composable
private fun TarjetaRegion(region: RegionPeru, desbloqueada: Boolean, onClick: () -> Unit) {
    val fondo by animateColorAsState(
        targetValue = if (desbloqueada) region.colorCielo else Color(0xFF455A64),
        animationSpec = tween(800),
        label = "fondoRegion"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(fondo)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (desbloqueada) region.emojiClima else "🔒", fontSize = 40.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(region.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(region.descripcionClima, color = Color(0xEEFFFFFF), fontSize = 13.sp)
            Text(region.mision, color = Color(0xCCFFFFFF), fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onClick,
            enabled = desbloqueada,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text(if (desbloqueada) "Ir" else "Bloqueada", fontSize = 12.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// BATALLA
// ---------------------------------------------------------------------------

@Composable
private fun PantallaBatalla(juego: JuegoAnime, scope: CoroutineScope) {
    val region = juego.region
    val cielo by animateColorAsState(region.colorCielo, tween(1200), label = "cielo")
    val suelo by animateColorAsState(region.colorSuelo, tween(1200), label = "suelo")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cielo, suelo)))
    ) {
        // Efecto de clima animado según la región
        EfectoClima(region.clima)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(12.dp)
        ) {
            Text(
                "${region.emojiClima} ${region.nombre} · ${region.descripcionClima}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                "Misión: ${region.mision} (${juego.kills}/${region.monstruos.size})",
                color = Color(0xEEFFFFFF),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            BarraProgreso(
                progreso = juego.kills / region.monstruos.size.toFloat(),
                color = Color(0xFFFFD54F),
                modifier = Modifier.fillMaxWidth(),
                alto = 8.dp
            )

            // Arena de combate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroeEnArena(juego)
                MonstruoEnArena(juego)
            }

            // Mensaje de combate con transición suave
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = juego.mensaje,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                    label = "mensajeCombate"
                ) { texto ->
                    Text(
                        texto,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            PanelHeroe(juego.heroe, mostrarEnergia = true, juego = juego)
            Spacer(Modifier.height(8.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val puedeActuar = !juego.ocupado && juego.monstruoVisible
                Button(
                    onClick = { scope.launch { juego.atacar(false) } },
                    enabled = puedeActuar,
                    modifier = Modifier.weight(1f)
                ) { Text("⚔️ Atacar", fontSize = 13.sp) }

                Button(
                    onClick = { scope.launch { juego.atacar(true) } },
                    enabled = puedeActuar && juego.energia >= 100,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
                ) { Text("🔥 Solar", fontSize = 13.sp) }

                Button(
                    onClick = { scope.launch { juego.curar() } },
                    enabled = puedeActuar && juego.pociones > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) { Text("🧪 x${juego.pociones}", fontSize = 13.sp) }
            }
        }

        // Aviso de subida de nivel
        AnimatedVisibility(
            visible = juego.subioNivel,
            modifier = Modifier.align(Alignment.Center),
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF2FFD54F))
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⭐ ¡NIVEL ${juego.heroe.nivel}! ⭐", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
                Text("+ Vida  + Fuerza", fontSize = 16.sp, color = Color(0xFF4E342E))
            }
        }
    }
}

@Composable
private fun HeroeEnArena(juego: JuegoAnime) {
    // Embestida hacia el monstruo al atacar y retroceso al ser golpeado
    val avance by animateDpAsState(
        targetValue = if (juego.heroeAtacando) 70.dp else 0.dp,
        animationSpec = tween(150),
        label = "avanceHeroe"
    )
    val retroceso by animateDpAsState(
        targetValue = if (juego.heroeGolpeado) (-14).dp else 0.dp,
        animationSpec = tween(80),
        label = "retrocesoHeroe"
    )
    val fondo by animateColorAsState(
        targetValue = if (juego.heroeGolpeado) Color(0xAAFF1744) else Color(0x33000000),
        animationSpec = tween(120),
        label = "flashHeroe"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .offset(x = avance + retroceso)
                .size(100.dp)
                .clip(CircleShape)
                .background(fondo),
            contentAlignment = Alignment.Center
        ) {
            Text("🥷", fontSize = 56.sp)
        }
        Text("Inti", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MonstruoEnArena(juego: JuegoAnime) {
    val avance by animateDpAsState(
        targetValue = if (juego.monstruoAtacando) (-70).dp else 0.dp,
        animationSpec = tween(150),
        label = "avanceMonstruo"
    )
    val retroceso by animateDpAsState(
        targetValue = if (juego.monstruoGolpeado) 14.dp else 0.dp,
        animationSpec = tween(80),
        label = "retrocesoMonstruo"
    )
    val fondo by animateColorAsState(
        targetValue = if (juego.monstruoGolpeado) Color(0xAAFF1744) else Color(0x33000000),
        animationSpec = tween(120),
        label = "flashMonstruo"
    )

    // El espacio queda reservado para que el diseño no salte cuando el monstruo desaparece
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = juego.monstruoVisible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(tween(400)),
            exit = scaleOut(tween(500)) + fadeOut(tween(500))
        ) {
            val m = juego.monstruo
            Column(
                modifier = Modifier.width(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    m?.nombre ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                BarraProgreso(
                    progreso = juego.vidaMonstruo / (m?.vidaMax ?: 1).toFloat(),
                    color = Color(0xFFEF5350),
                    modifier = Modifier.fillMaxWidth(),
                    alto = 12.dp
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .offset(x = avance + retroceso)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(fondo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(m?.emoji ?: "", fontSize = 64.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CLIMA ANIMADO (sol, viento, nieve y lluvia dibujados en un Canvas)
// ---------------------------------------------------------------------------

@Composable
private fun EfectoClima(clima: Clima) {
    val transicion = rememberInfiniteTransition(label = "clima")
    val t by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tiempoClima"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        when (clima) {
            Clima.Lluvia -> repeat(70) { i ->
                val x = ((i * 53) % 100) / 100f * w
                val y = ((((i * 37) % 100) / 100f) + t * 2f) % 1f * h
                drawLine(Color(0x99E1F5FE), Offset(x, y), Offset(x - 8f, y + 40f), strokeWidth = 3f)
            }

            Clima.Nieve -> repeat(60) { i ->
                val x = ((i * 47) % 100) / 100f * w + sin(t * 2f * PI.toFloat() + i) * 15f
                val y = ((((i * 29) % 100) / 100f) + t) % 1f * h
                drawCircle(Color(0xCCFFFFFF), radius = 4f + (i % 3) * 2f, center = Offset(x, y))
            }

            Clima.Viento -> repeat(25) { i ->
                val x = ((((i * 41) % 100) / 100f) + t * 1.5f) % 1f * w
                val y = ((i * 17) % 100) / 100f * h
                drawLine(Color(0x88FFFFFF), Offset(x, y), Offset(x + 90f, y), strokeWidth = 3f)
            }

            Clima.Sol -> {
                val pulso = 1f + 0.12f * sin(t * 2f * PI.toFloat())
                val centro = Offset(w * 0.85f, h * 0.08f)
                drawCircle(Color(0x55FFF176), radius = 150f * pulso, center = centro)
                drawCircle(Color(0xAAFFEB3B), radius = 65f * pulso, center = centro)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// COMPONENTES REUTILIZABLES
// ---------------------------------------------------------------------------

@Composable
private fun BarraProgreso(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier,
    alto: Dp = 12.dp
) {
    val animado by animateFloatAsState(
        targetValue = progreso.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "barra"
    )
    Box(
        modifier = modifier
            .height(alto)
            .clip(RoundedCornerShape(50))
            .background(Color(0x55000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animado)
                .background(color)
        )
    }
}

@Composable
private fun BarraVida(vida: Int, vidaMax: Int, modifier: Modifier = Modifier) {
    val p = vida / vidaMax.toFloat()
    // La barra cambia de color (verde, amarillo, rojo) según la vida que queda
    val color by animateColorAsState(
        targetValue = when {
            p > 0.5f -> Color(0xFF66BB6A)
            p > 0.25f -> Color(0xFFFFCA28)
            else -> Color(0xFFEF5350)
        },
        animationSpec = tween(400),
        label = "colorVida"
    )
    BarraProgreso(progreso = p, color = color, modifier = modifier, alto = 14.dp)
}

@Composable
private fun PanelHeroe(heroe: Heroe, mostrarEnergia: Boolean, juego: JuegoAnime) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x77000000))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🥷 Inti · Nivel ${heroe.nivel}", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("⚔️ Fuerza ${heroe.ataque}", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text("❤️ Vida ${heroe.vida}/${heroe.vidaMax}", color = Color.White, fontSize = 12.sp)
        BarraVida(heroe.vida, heroe.vidaMax, Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        Text("✨ XP ${heroe.xp}/${heroe.xpSiguiente}", color = Color.White, fontSize = 12.sp)
        BarraProgreso(
            progreso = heroe.xp / heroe.xpSiguiente.toFloat(),
            color = Color(0xFF42A5F5),
            modifier = Modifier.fillMaxWidth(),
            alto = 8.dp
        )
        if (mostrarEnergia) {
            Spacer(Modifier.height(4.dp))
            Text(
                if (juego.energia >= 100) "🔥 ¡Ataque Solar listo!" else "🔥 Energía ${juego.energia}/100",
                color = Color.White,
                fontSize = 12.sp
            )
            BarraProgreso(
                progreso = juego.energia / 100f,
                color = Color(0xFFFF9800),
                modifier = Modifier.fillMaxWidth(),
                alto = 8.dp
            )
        }
    }
}

@Composable
private fun PantallaResultado(
    emoji: String,
    titulo: String,
    detalle: String,
    fondo: Color,
    botones: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(fondo, Color(0xFF000000)))),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(tween(600))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 90.sp)
                Spacer(Modifier.height(8.dp))
                Text(titulo, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(detalle, color = Color(0xFFE0E0E0), fontSize = 15.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                botones()
            }
        }
    }
}
