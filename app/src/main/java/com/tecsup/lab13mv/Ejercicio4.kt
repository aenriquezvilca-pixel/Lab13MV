package com.tecsup.lab13mv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Los tres estados posibles de la pantalla
enum class EstadoPantalla {
    Cargando,
    Contenido,
    Error
}

@Composable
fun Ejercicio4(modifier: Modifier = Modifier) {
    var estado by remember { mutableStateOf(EstadoPantalla.Cargando) }

    // Simula una carga real: despues de 3 segundos pasa solo a Contenido
    LaunchedEffect(estado) {
        if (estado == EstadoPantalla.Cargando) {
            delay(3000)
            estado = EstadoPantalla.Contenido
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Botón que avanza al siguiente estado: Cargando -> Contenido -> Error -> Cargando
        Button(onClick = {
            estado = when (estado) {
                EstadoPantalla.Cargando -> EstadoPantalla.Contenido
                EstadoPantalla.Contenido -> EstadoPantalla.Error
                EstadoPantalla.Error -> EstadoPantalla.Cargando
            }
        }) {
            Text("Cambiar estado")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transición entre estados con fadeIn y fadeOut de tiempos personalizados
        AnimatedContent(
            targetState = estado,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 700, delayMillis = 300)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 300))
            },
            label = "estadoPantalla"
        ) { estadoActual ->
            when (estadoActual) {
                EstadoPantalla.Cargando -> TarjetaEstado(
                    color = Color(0xFF1E88E5),
                    mensaje = "Cargando..."
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 5.dp
                    )
                }

                EstadoPantalla.Contenido -> TarjetaEstado(
                    color = Color(0xFF43A047),
                    mensaje = "Contenido cargado correctamente"
                )

                EstadoPantalla.Error -> TarjetaEstado(
                    color = Color(0xFFE53935),
                    mensaje = "Ocurrió un error"
                )
            }
        }
    }
}

@Composable
private fun TarjetaEstado(
    color: Color,
    mensaje: String,
    icono: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icono != null) {
                icono()
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = mensaje,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
