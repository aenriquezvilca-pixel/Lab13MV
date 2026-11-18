package com.tecsup.lab13mv

import androidx.compose.ui.graphics.Color

// Modelos del juego de carreras "Kawaii Kart Racing" (estilo Crash Team Racing)

enum class Forma { Capucha, Conejo, Osito, Corazon, Koala, Gato, Galleta }

enum class ItemKart(val emoji: String, val nombre: String) {
    Turbo("🔥", "Turbo"),
    Escudo("🛡️", "Escudo"),
    Misil("🚀", "Misil"),
    Bomba("💣", "Bomba")
}

enum class FaseCarrera { Cuenta, Corriendo, Fin }

enum class PantallaKart { Seleccion, Mapa, Carrera }

data class Corredor(
    val nombre: String,
    val cuerpo: Color,
    val detalle: Color,
    val kart: Color,
    val forma: Forma,
    val velocidad: Int,    // 1..5
    val aceleracion: Int,  // 1..5
    val giro: Int          // 1..5
)

val corredores = listOf(
    Corredor("Chimmy", Color(0xFFFFFDE7), Color(0xFFFFD600), Color(0xFFFFD600), Forma.Capucha, 3, 4, 5),
    Corredor("Cooky", Color(0xFFF8BBD0), Color(0xFFEC407A), Color(0xFFEC407A), Forma.Conejo, 4, 3, 4),
    Corredor("RJ", Color(0xFFFFFFFF), Color(0xFFE53935), Color(0xFFEF5350), Forma.Osito, 5, 2, 3),
    Corredor("Tata", Color(0xFFE53935), Color(0xFFB71C1C), Color(0xFFFF5252), Forma.Corazon, 4, 4, 2),
    Corredor("Koya", Color(0xFF81D4FA), Color(0xFF0288D1), Color(0xFF29B6F6), Forma.Koala, 2, 5, 4),
    Corredor("Mang", Color(0xFFB39DDB), Color(0xFF5E35B1), Color(0xFF7E57C2), Forma.Gato, 3, 3, 5),
    Corredor("Shooky", Color(0xFF8D6E63), Color(0xFF4E342E), Color(0xFF6D4C41), Forma.Galleta, 5, 3, 2)
)
