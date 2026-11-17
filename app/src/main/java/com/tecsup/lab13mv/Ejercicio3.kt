package com.tecsup.lab13mv

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Ejercicio3(modifier: Modifier = Modifier) {
    // Estado que alterna entre la posición inicial y la final
    var movido by remember { mutableStateOf(false) }

    // Animación del tamaño del cuadro
    val tamano by animateDpAsState(
        targetValue = if (movido) 150.dp else 60.dp,
        animationSpec = tween(durationMillis = 800),
        label = "tamano"
    )

    // Animación de la posición horizontal
    val desplazamientoX by animateDpAsState(
        targetValue = if (movido) 150.dp else 0.dp,
        animationSpec = tween(durationMillis = 800),
        label = "desplazamientoX"
    )

    // Animación de la posición vertical
    val desplazamientoY by animateDpAsState(
        targetValue = if (movido) 200.dp else 0.dp,
        animationSpec = tween(durationMillis = 800),
        label = "desplazamientoY"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Botón que mueve y cambia el tamaño del cuadro
        Button(onClick = { movido = !movido }) {
            Text(if (movido) "Regresar cuadro" else "Mover y agrandar cuadro")
        }

        // Zona donde se mueve el cuadro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // El orden importa: offset va primero para mover el cuadro completo
            // y luego size y background se aplican sobre la nueva posición
            Box(
                modifier = Modifier
                    .offset(x = desplazamientoX, y = desplazamientoY)
                    .size(tamano)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE53935))
            )
        }
    }
}
