package com.tecsup.lab13mv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Dorado = Color(0xFFFFD54F)
private val Fondo = Color(0xFF120B1F)

// Punto de entrada del Ejercicio Final: Kawaii Kart Racing
@Composable
fun EjercicioFinal() {
    val juego = remember { JuegoKart() }
    Box(Modifier.fillMaxSize().background(Fondo)) {
        AnimatedContent(
            targetState = juego.pantalla,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
            label = "pantallas"
        ) { pantalla ->
            when (pantalla) {
                PantallaKart.Seleccion -> PantallaSeleccionKart(juego)
                PantallaKart.Mapa -> PantallaMapaKart(juego)
                PantallaKart.Carrera -> juego.carrera?.let { PantallaCarrera(juego, it) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SELECCIÓN DE CORREDOR
// ---------------------------------------------------------------------------
@Composable
private fun PantallaSeleccionKart(juego: JuegoKart) {
    val c = juego.corredor
    val brillo by animateColorAsState(c.kart, tween(500), label = "brillo")
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Fondo, brillo.copy(alpha = 0.55f), Fondo)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("KAWAII KART RACING", color = Dorado, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Text("Elige a tu corredor", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = juego.corredorIdx,
            transitionSpec = { (fadeIn(tween(250)) + scaleIn(initialScale = 0.8f)) togetherWith fadeOut(tween(150)) },
            label = "preview",
            modifier = Modifier.weight(1f)
        ) { idx ->
            KartVista(corredores[idx], Modifier.fillMaxSize())
        }

        Text(c.nombre, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        BarraStat("VEL", c.velocidad, Color(0xFFFF5252))
        BarraStat("ACE", c.aceleracion, Color(0xFFFFC107))
        BarraStat("GIRO", c.giro, Color(0xFF40C4FF))
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(corredores) { i, cor ->
                val sel = i == juego.corredorIdx
                val borde by animateColorAsState(if (sel) Dorado else Color(0x33FFFFFF), label = "borde")
                MascotaVista(
                    cor,
                    Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x44000000))
                        .border(3.dp, borde, RoundedCornerShape(16.dp))
                        .clickable { juego.corredorIdx = i }
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { juego.pantalla = PantallaKart.Mapa },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("🏁 ¡A CORRER!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun BarraStat(nombre: String, valor: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(nombre, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
        for (i in 1..5) {
            val lleno by animateColorAsState(if (i <= valor) color else Color(0x33FFFFFF), label = "stat")
            Box(
                Modifier
                    .padding(end = 4.dp)
                    .size(width = 28.dp, height = 10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(lleno)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// MODO AVENTURA: mapa de pistas (cada una con su clima)
// ---------------------------------------------------------------------------
@Composable
private fun PantallaMapaKart(juego: JuegoKart) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1B1035), Color(0xFF3E1F47))))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MascotaVista(juego.corredor, Modifier.size(60.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text("MODO AVENTURA", color = Dorado, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("${juego.corredor.nombre} · 🏆 ${juego.trofeos.size}/${regiones.size}", color = Color.White, fontSize = 13.sp)
            }
            Button(
                onClick = { juego.pantalla = PantallaKart.Seleccion },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x55FFFFFF))
            ) { Text("Cambiar") }
        }
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            regiones.forEachIndexed { i, r ->
                TarjetaPista(
                    indice = i,
                    region = r,
                    abierta = i < juego.desbloqueadas,
                    trofeo = i in juego.trofeos,
                    mejor = juego.mejorPos[i],
                    siguiente = i == juego.desbloqueadas - 1 && i !in juego.trofeos
                ) { juego.iniciar(i) }
            }
        }
        Text(
            "Termina 1º o 2º para desbloquear la siguiente pista",
            color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
}

@Composable
private fun TarjetaPista(
    indice: Int,
    region: RegionPeru,
    abierta: Boolean,
    trofeo: Boolean,
    mejor: Int?,
    siguiente: Boolean,
    onClick: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "pista")
    val pulso by inf.animateFloat(1f, 1.03f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulso")
    val escala = if (siguiente) pulso else 1f
    val fondo = if (abierta) Brush.horizontalGradient(listOf(region.colorCielo, region.colorSuelo))
    else Brush.horizontalGradient(listOf(Color(0xFF444444), Color(0xFF2A2A2A)))
    Row(
        Modifier
            .fillMaxWidth()
            .scale(escala)
            .clip(RoundedCornerShape(20.dp))
            .background(fondo)
            .border(if (siguiente) 3.dp else 1.dp, if (siguiente) Dorado else Color(0x55FFFFFF), RoundedCornerShape(20.dp))
            .clickable(enabled = abierta, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(58.dp).clip(CircleShape).background(Color(0x55000000)),
            contentAlignment = Alignment.Center
        ) { Text(if (abierta) region.emojiClima else "🔒", fontSize = 28.sp) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text("Pista ${indice + 1}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            Text(region.nombre, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            Text(if (abierta) region.descripcionClima else "Bloqueada", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(if (trofeo) "🏆" else if (abierta) "🏁" else "", fontSize = 26.sp)
            if (mejor != null) Text("Mejor: ${mejor}º", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// CARRERA
// ---------------------------------------------------------------------------
@Composable
private fun PantallaCarrera(juego: JuegoKart, c: CarreraKart) {
    LaunchedEffect(c) {
        for (n in 3 downTo 1) {
            c.cuenta = n
            delay(900)
        }
        c.cuenta = 0
        c.fase = FaseCarrera.Corriendo
        var ultimo = withFrameNanos { it }
        while (c.fase == FaseCarrera.Corriendo) {
            val ahora = withFrameNanos { it }
            c.update(((ahora - ultimo) / 1_000_000_000f).coerceAtMost(0.05f))
            ultimo = ahora
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(Modifier.fillMaxSize()) {
            c.frame // leer el estado para redibujar en cada cuadro
            dibujarCarrera(c)
        }
        ClimaAnimado(clima = c.pista.clima, modifier = Modifier.fillMaxSize())
        LineasVelocidad(activa = c.turboActivo, modifier = Modifier.fillMaxSize(), color = Color(0xFF80D8FF))

        // ---- HUD superior ----
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedContent(
                    targetState = c.posicion,
                    transitionSpec = { (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith fadeOut() },
                    label = "posicion"
                ) { p ->
                    Text(
                        "${p}º", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold,
                        color = when (p) { 1 -> Dorado; 2 -> Color(0xFFCFD8DC); 3 -> Color(0xFFFFAB91); else -> Color.White }
                    )
                }
                Column(Modifier.padding(start = 8.dp)) {
                    Text("Vuelta ${c.vuelta}/${c.vueltas}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(c.pista.nombre, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    Modifier.size(58.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x88000000))
                        .border(2.dp, Color.White, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = c.item,
                        transitionSpec = { (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith scaleOut() + fadeOut() },
                        label = "item"
                    ) { it0 -> Text(it0?.emoji ?: "❔", fontSize = 30.sp) }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "🍪 ${c.galletas}/10" + (if (c.galletas >= 10) " ⚡" else "") + "\n🪙 ${c.monedas}",
                    color = Dorado, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, textAlign = TextAlign.End
                )
            }
        }

        // ---- Mensajes y cuenta regresiva ----
        AnimatedVisibility(
            visible = c.mensaje.isNotEmpty(),
            modifier = Modifier.align(Alignment.Center).padding(bottom = 140.dp),
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut()
        ) {
            Text(c.mensaje, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        }
        AnimatedContent(
            targetState = c.cuenta,
            transitionSpec = { (scaleIn(initialScale = 2f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.5f) + fadeOut()) },
            modifier = Modifier.align(Alignment.Center),
            label = "cuenta"
        ) { n ->
            if (n >= 0) {
                Text(
                    if (n > 0) "$n" else "¡YA!",
                    color = if (n > 0) Color.White else Dorado,
                    fontSize = 96.sp, fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // ---- Barra de avance hacia la meta ----
        Column(
            Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏁", fontSize = 20.sp)
            Text("${c.faltan} m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Canvas(Modifier.size(width = 36.dp, height = 240.dp)) {
                c.frame
                dibujarAvance(c)
            }
        }

        // ---- Controles táctiles ----
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonControl("◀", Color(0xFF1E88E5), { c.izq = true }, { c.izq = false })
                BotonControl("▶", Color(0xFF1E88E5), { c.der = true }, { c.der = false })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonControl("🌀", Color(0xFFFF6D00), { c.derrape = true }, { c.derrape = false })
                BotonControl("🎁", Color(0xFFD81B60), { c.usarItem() }, { })
            }
        }

        // ---- Resultado ----
        AnimatedVisibility(
            visible = c.fase == FaseCarrera.Fin,
            modifier = Modifier.align(Alignment.Center),
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xEE120B1F))
                    .border(3.dp, Dorado, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when (c.posFinal) { 1 -> "🏆"; 2 -> "🥈"; 3 -> "🥉"; else -> "🏁" },
                    fontSize = 56.sp
                )
                Text("${c.posFinal}º lugar", color = Dorado, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    when {
                        c.posFinal == 1 -> "¡Trofeo conseguido!"
                        c.posFinal == 2 -> "¡Pasas a la siguiente pista!"
                        else -> "Inténtalo otra vez: necesitas 1º o 2º"
                    },
                    color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { juego.terminar(c.posFinal) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                ) { Text("Continuar", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun BotonControl(texto: String, color: Color, onDown: () -> Unit, onUp: () -> Unit) {
    var presionado by remember { mutableStateOf(false) }
    val escala by animateFloatAsState(if (presionado) 0.86f else 1f, label = "boton")
    Box(
        Modifier
            .size(62.dp)
            .scale(escala)
            .clip(CircleShape)
            .background(color.copy(alpha = if (presionado) 1f else 0.75f))
            .border(2.dp, Color.White, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    presionado = true
                    onDown()
                    tryAwaitRelease()
                    presionado = false
                    onUp()
                })
            },
        contentAlignment = Alignment.Center
    ) { Text(texto, fontSize = 24.sp, color = Color.White) }
}
