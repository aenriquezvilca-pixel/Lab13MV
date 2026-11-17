package com.tecsup.lab13mv

import androidx.compose.ui.graphics.Color

// Modelos de datos del juego "Leyendas del Perú"

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
