package com.tecsup.lab13mv

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
fun Ejercicio2(modifier: Modifier = Modifier) {
    // Estado que alterna entre los dos colores
    var esAzul by remember { mutableStateOf(true) }

    // Animación con tween: transición lineal de 1000 ms
    val colorTween by animateColorAsState(
        targetValue = if (esAzul) Color(0xFF1E88E5) else Color(0xFF43A047),
        animationSpec = tween(durationMillis = 1000),
        label = "colorTween"
    )

    // Animación con spring: transición con efecto de rebote
    val colorSpring by animateColorAsState(
        targetValue = if (esAzul) Color(0xFF1E88E5) else Color(0xFF43A047),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "colorSpring"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Botón que cambia el color de los cuadros
        Button(onClick = { esAzul = !esAzul }) {
            Text("Cambiar color")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Cuadro con animación tween
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorTween)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("tween")
            }

            // Cuadro con animación spring
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorSpring)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("spring")
            }
        }
    }
}
