package com.tecsup.lab13mv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// ═════════════════════════════════════════════════════════════════════════════
// PUNTOS ADICIONALES — Codelab de animaciones en Jetpack Compose
//   3. animate*AsState        -> color de fondo según la pestaña
//   4. AnimatedVisibility     -> botón EDIT que se expande/contrae + mensaje que se desliza
//   5. animateContentSize     -> tarjetas de "Temas" que se expanden
//   6. updateTransition       -> indicador de pestañas (varios valores a la vez)
//   7. InfiniteTransition     -> indicador de carga con alfa repetido (keyframes)
//   8. Gestos (Animatable)    -> deslizar para descartar tareas
// ═════════════════════════════════════════════════════════════════════════════

private val Seashell = Color(0xFFFFF1EC)
private val GreenLight = Color(0xFFE3F4E1)
private val PaleDogwood = Color(0xFFFFB59E)
private val VerdeApp = Color(0xFF2E9E5B)
private val Tinta = Color(0xFF263238)

private enum class AdicTabPage { Home, Work }

private val temas = listOf(
    "Llegaron 2 paquetes nuevos" to "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
    "Recomendación de proyecto DIY" to "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    "Festival el próximo mes" to "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis.",
    "Semillas de flores disponibles" to "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt."
)

private val tareasIniciales = listOf(
    "Comprar leche", "Elegir cortinas", "Plantar romero", "Reparar el techo", "Regar el jardín"
)

@Composable
fun PuntosAdicionales() {
    var tabPage by remember { mutableStateOf(AdicTabPage.Home) }

    // PUNTO 3: animate*AsState — el cambio de color de fondo ahora es animado
    val backgroundColor by animateColorAsState(
        targetValue = if (tabPage == AdicTabPage.Home) Seashell else GreenLight,
        label = "background color"
    )

    val listState = rememberLazyListState()
    val extended = listState.isScrollingUp()
    var editMessageShown by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var temperatura by remember { mutableIntStateOf(23) }
    val tareas = remember { mutableStateListOf<String>().apply { addAll(tareasIniciales) } }

    // El mensaje de edición se oculta solo
    LaunchedEffect(editMessageShown) {
        if (editMessageShown) {
            delay(2000)
            editMessageShown = false
        }
    }
    // Simula la descarga del clima
    LaunchedEffect(cargando) {
        if (cargando) {
            delay(3000)
            temperatura = (18..30).random()
            cargando = false
        }
    }

    Box(Modifier.fillMaxSize().background(backgroundColor)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                "Puntos adicionales · Animaciones en Compose",
                color = Tinta, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 40.dp, end = 16.dp, bottom = 8.dp)
            )
            AdicTabBar(tabPage) { tabPage = it }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Encabezado("Clima", "Punto 7 · InfiniteTransition") }
                item { ClimaCard(temperatura, cargando) { cargando = true } }

                item { Encabezado("Temas", "Punto 5 · animateContentSize") }
                items(temas, key = { it.first }) { (titulo, cuerpo) -> TopicRow(titulo, cuerpo) }

                item { Encabezado("Tareas", "Punto 8 · gesto de deslizar para descartar") }
                items(tareas.toList(), key = { it }) { tarea ->
                    TareaRow(tarea) { tareas.remove(tarea) }
                }
                if (tareas.isEmpty()) {
                    item {
                        Surface(
                            Modifier.fillMaxWidth().clickable {
                                tareas.addAll(tareasIniciales)
                            },
                            shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp
                        ) {
                            Text(
                                "🎉 ¡Sin tareas! Toca para restaurarlas",
                                color = Tinta, modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // PUNTO 4: mensaje que se desliza desde arriba
        EditMessage(editMessageShown, Modifier.align(Alignment.TopCenter))

        // PUNTO 4: botón que se expande/contrae al hacer scroll
        EditFab(
            extended = extended,
            onClick = { editMessageShown = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun Encabezado(titulo: String, punto: String) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(titulo, color = Tinta, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(punto, color = VerdeApp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── PUNTO 6: Transition con varios valores animados a la vez ────────────────
@Composable
private fun AdicTabBar(tabPage: AdicTabPage, onTabSelected: (AdicTabPage) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        val anchoTab = maxWidth / 2

        val transition = updateTransition(tabPage, label = "Tab indicator")
        val indicatorLeft by transition.animateDp(
            transitionSpec = {
                if (AdicTabPage.Home isTransitioningTo AdicTabPage.Work) {
                    // Se mueve a la derecha: el borde izquierdo es más lento
                    spring(stiffness = Spring.StiffnessVeryLow)
                } else {
                    spring(stiffness = Spring.StiffnessMedium)
                }
            },
            label = "Indicator left"
        ) { page -> if (page == AdicTabPage.Home) 0.dp else anchoTab }

        val indicatorRight by transition.animateDp(
            transitionSpec = {
                if (AdicTabPage.Home isTransitioningTo AdicTabPage.Work) {
                    // Se mueve a la derecha: el borde derecho es más rápido
                    spring(stiffness = Spring.StiffnessMedium)
                } else {
                    spring(stiffness = Spring.StiffnessVeryLow)
                }
            },
            label = "Indicator right"
        ) { page -> if (page == AdicTabPage.Home) anchoTab else anchoTab * 2 }

        val color by transition.animateColor(label = "Border color") { page ->
            if (page == AdicTabPage.Home) PaleDogwood else VerdeApp
        }

        Row(Modifier.fillMaxWidth()) {
            AdicTab("🏠 Home", Modifier.weight(1f)) { onTabSelected(AdicTabPage.Home) }
            AdicTab("💼 Work", Modifier.weight(1f)) { onTabSelected(AdicTabPage.Work) }
        }
        Box(
            Modifier
                .offset(x = indicatorLeft)
                .width(indicatorRight - indicatorLeft)
                .height(48.dp)
                .border(BorderStroke(3.dp, color), RoundedCornerShape(10.dp))
        )
    }
}

@Composable
private fun AdicTab(texto: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = Tinta, fontWeight = FontWeight.SemiBold)
    }
}

// ─── PUNTO 7: InfiniteTransition con keyframes ───────────────────────────────
@Composable
private fun ClimaCard(temp: Int, cargando: Boolean, onRefresh: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("☀️", fontSize = 36.sp)
                Text(
                    "$temp°C", color = Tinta, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp).weight(1f)
                )
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .clickable(enabled = !cargando, onClick = onRefresh),
                    contentAlignment = Alignment.Center
                ) { Text("🔄", fontSize = 22.sp) }
            }
            AnimatedVisibility(cargando) { LoadingRow() }
        }
    }
}

@Composable
private fun LoadingRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "carga")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
                0.9f at 800
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Row(
        Modifier.heightIn(min = 64.dp).padding(top = 12.dp).alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray))
        Spacer(Modifier.width(16.dp))
        Box(Modifier.fillMaxWidth().height(32.dp).background(Color.LightGray))
    }
}

// ─── PUNTO 5: animateContentSize ─────────────────────────────────────────────
@Composable
private fun TopicRow(topic: String, body: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("●", color = Tinta, fontSize = 18.sp)
                Text(
                    topic, color = Tinta, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Text(body, color = Tinta, fontSize = 14.sp)
            }
        }
    }
}

// ─── PUNTO 8: gesto de deslizar para descartar ───────────────────────────────
@Composable
private fun TareaRow(tarea: String, onDismissed: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().swipeToDismiss(onDismissed),
        shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("✓", color = VerdeApp, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                tarea, color = Tinta,
                modifier = Modifier.padding(start = 16.dp).weight(1f)
            )
            Text("⟷", color = Color.Gray, fontSize = 16.sp)
        }
    }
}

private fun Modifier.swipeToDismiss(onDismissed: () -> Unit): Modifier = composed {
    // El Animatable guarda el desplazamiento horizontal del elemento
    val offsetX = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Para calcular dónde se detiene un fling
        val decay = splineBasedDecay<Float>(this)
        coroutineScope {
            while (true) {
                // Espera un toque y toma su pointerId
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                // Un toque nuevo interrumpe cualquier animación en curso
                offsetX.stop()
                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    horizontalDrag(pointerId) { change ->
                        // Sincroniza el valor de la animación con el dedo
                        val horizontalDragOffset = offsetX.value + change.positionChange().x
                        launch { offsetX.snapTo(horizontalDragOffset) }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        if (change.positionChange() != Offset.Zero) change.consume()
                    }
                }
                // Terminó el arrastre: calcula la velocidad y dónde terminaría el fling
                val velocity = velocityTracker.calculateVelocity().x
                val targetOffsetX = decay.calculateTargetValue(offsetX.value, velocity)
                offsetX.updateBounds(
                    lowerBound = -size.width.toFloat(),
                    upperBound = size.width.toFloat()
                )
                launch {
                    if (targetOffsetX.absoluteValue <= size.width) {
                        // No hubo suficiente velocidad: vuelve a su sitio
                        offsetX.animateTo(targetValue = 0f, initialVelocity = velocity)
                    } else {
                        // Suficiente velocidad: sale por el borde y se descarta
                        offsetX.animateDecay(velocity, decay)
                        onDismissed()
                    }
                }
            }
        }
    }
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
}

// ─── PUNTO 4: AnimatedVisibility ─────────────────────────────────────────────
@Composable
private fun EditMessage(shown: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = shown,
        modifier = modifier,
        enter = slideInVertically(
            // Entra deslizándose desde -fullHeight hasta 0
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            // Sale deslizándose de 0 a -fullHeight
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "La función de edición no está disponible",
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.statusBarsPadding().padding(16.dp)
            )
        }
    }
}

@Composable
private fun EditFab(extended: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.navigationBarsPadding().padding(16.dp),
        containerColor = VerdeApp,
        contentColor = Color.White
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✏️")
            // Antes era un simple if (extended); ahora es animado
            AnimatedVisibility(extended) {
                Text(
                    text = "EDIT",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, top = 3.dp)
                )
            }
        }
    }
}

// Detecta la dirección del scroll de la lista
@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
