package com.tecsup.lab13mv

import androidx.compose.ui.graphics.Color

// ─── ENUMS BT21 RACING ────────────────────────────────────────────────────────
enum class TipoPersonajeBT21 { TATA, COOKY, CHIMMY, KOYA, RJ, SHOOKY, MANG, VAN }
enum class PantallaJuego { PortadaPolaroid, GarageCircular, Pistas, Carrera, Podio, GameOver }
enum class TipoItemCTR { Misil, Bomba, Escudo, Turbo, Pocion }
enum class DisenoKart { Clasico21, Estrellas, Corazones, Rayos, Llamas }
enum class TipoObstaculoPista { CajaMadera, Arbol } // Caja madera se puede saltar, árbol NO

// ─── MODELO PERSONAJE BT21 ───────────────────────────────────────────────────
data class PersonajeBT21(
    val tipo: TipoPersonajeBT21,
    val nombre: String,
    val apodo: String,
    val colorCabeza: Color,
    val colorDetalle: Color,
    val colorRopa: Color,
    val velocidadBase: Float,
    val colorKartPredeterminado: Color,
    val emoji: String
)

// ─── COLOR Y DISEÑO KART ──────────────────────────────────────────────────────
data class OpcionColorKart(
    val nombre: String,
    val color: Color
)

// ─── PISTA TEMÁTICA ───────────────────────────────────────────────────────────
data class PistaBT21(
    val id: String,
    val nombre: String,
    val subti: String,
    val cielo1: Color,
    val cielo2: Color,
    val colorPista: Color,
    val colorBorde1: Color,
    val colorBorde2: Color,
    val colorPasto: Color,
    val emoji: String,
    val curvaturaPista: Float
)

// ─── ELEMENTOS EN PISTA ───────────────────────────────────────────────────────
data class MonedaPista(
    val distanciaZ: Float,
    var carrilX: Float,
    var recogida: Boolean = false
)

data class TurboPadPista(
    val distanciaZ: Float,
    var carrilX: Float,
    var activado: Boolean = false
)

data class ObstaculoInteractivo(
    val distanciaZ: Float,
    var carrilX: Float,
    val tipo: TipoObstaculoPista, // CajaMadera o Arbol
    var destruido: Boolean = false
)

data class CajaItemCTR(
    val distanciaZ: Float,
    var carrilX: Float,
    val item: TipoItemCTR,
    var recogida: Boolean = false
)

// ─── LISTA OFICIAL BT21 ───────────────────────────────────────────────────────
val listaBT21 = listOf(
    PersonajeBT21(
        tipo = TipoPersonajeBT21.TATA,
        nombre = "TATA",
        apodo = "Príncipe Alien Corazón",
        colorCabeza = Color(0xFFE53935),
        colorDetalle = Color(0xFFFFD54F),
        colorRopa = Color(0xFF1E88E5),
        velocidadBase = 1.35f,
        colorKartPredeterminado = Color(0xFF1565C0),
        emoji = "❤️"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.CHIMMY,
        nombre = "CHIMMY",
        apodo = "Perrito Capucha Amarilla",
        colorCabeza = Color(0xFFFFD54F),
        colorDetalle = Color(0xFF212121),
        colorRopa = Color(0xFFFFFFFF),
        velocidadBase = 1.3f,
        colorKartPredeterminado = Color(0xFF43A047),
        emoji = "🐶"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.COOKY,
        nombre = "COOKY",
        apodo = "Conejito Rosa Fuerte",
        colorCabeza = Color(0xFFF48FB1),
        colorDetalle = Color(0xFF880E4F),
        colorRopa = Color(0xFFE91E63),
        velocidadBase = 1.45f,
        colorKartPredeterminado = Color(0xFFEC407A),
        emoji = "🐰"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.KOYA,
        nombre = "KOYA",
        apodo = "Koala Soñador Celeste",
        colorCabeza = Color(0xFF81D4FA),
        colorDetalle = Color(0xFF7E57C2),
        colorRopa = Color(0xFFE1F5FE),
        velocidadBase = 1.35f,
        colorKartPredeterminado = Color(0xFF29B6F6),
        emoji = "🐨"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.RJ,
        nombre = "RJ",
        apodo = "Alpaca Cariñosa",
        colorCabeza = Color(0xFFFFFFFF),
        colorDetalle = Color(0xFFE53935),
        colorRopa = Color(0xFFECEFF1),
        velocidadBase = 1.25f,
        colorKartPredeterminado = Color(0xFFFB8C00),
        emoji = "🦙"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.SHOOKY,
        nombre = "SHOOKY",
        apodo = "Galletita Traviesa",
        colorCabeza = Color(0xFF8D6E63),
        colorDetalle = Color(0xFF3E2723),
        colorRopa = Color(0xFF6D4C41),
        velocidadBase = 1.5f,
        colorKartPredeterminado = Color(0xFFFDD835),
        emoji = "🍪"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.MANG,
        nombre = "MANG",
        apodo = "Pony Bailarín Lila",
        colorCabeza = Color(0xFFCE93D8),
        colorDetalle = Color(0xFFEC407A),
        colorRopa = Color(0xFF80DEEA),
        velocidadBase = 1.4f,
        colorKartPredeterminado = Color(0xFFBA68C8),
        emoji = "🦄"
    ),
    PersonajeBT21(
        tipo = TipoPersonajeBT21.VAN,
        nombre = "VAN",
        apodo = "Robot Guardián",
        colorCabeza = Color(0xFFB0BEC5),
        colorDetalle = Color(0xFF263238),
        colorRopa = Color(0xFFECEFF1),
        velocidadBase = 1.35f,
        colorKartPredeterminado = Color(0xFFCFD8DC),
        emoji = "🤖"
    )
)

// ─── COLORES DE KART PARA EL GARAGE ───────────────────────────────────────────
val coloresDisponiblesKart = listOf(
    OpcionColorKart("Verde Chimmy", Color(0xFF43A047)),
    OpcionColorKart("Azul Tata", Color(0xFF1565C0)),
    OpcionColorKart("Rosa Cooky", Color(0xFFEC407A)),
    OpcionColorKart("Naranja RJ", Color(0xFFFB8C00)),
    OpcionColorKart("Amarillo Shooky", Color(0xFFFDD835)),
    OpcionColorKart("Celeste Koya", Color(0xFF29B6F6)),
    OpcionColorKart("Lila Mang", Color(0xFFAB47BC)),
    OpcionColorKart("Rojo Fuego", Color(0xFFD50000)),
    OpcionColorKart("Negro Neón", Color(0xFF212121))
)

// ─── CIRCUITOS CON CURVAS ─────────────────────────────────────────────────────
val pistasBT21 = listOf(
    PistaBT21(
        id = "arcoiris",
        nombre = "CIRCUITO ARCOÍRIS",
        subti = "Curvas suaves entre nubes de algodón",
        cielo1 = Color(0xFF81D4FA),
        cielo2 = Color(0xFFF48FB1),
        colorPista = Color(0xFF37474F),
        colorBorde1 = Color(0xFFFF4081),
        colorBorde2 = Color(0xFF00E5FF),
        colorPasto = Color(0xFFA7FFEB),
        emoji = "🌈",
        curvaturaPista = 1.3f
    ),
    PistaBT21(
        id = "ciudad",
        nombre = "NEÓN SPEEDWAY",
        subti = "Rascacielos y curvas veloces",
        cielo1 = Color(0xFF0F0C20),
        cielo2 = Color(0xFF311B92),
        colorPista = Color(0xFF1E1E28),
        colorBorde1 = Color(0xFFFF007F),
        colorBorde2 = Color(0xFF76FF03),
        colorPasto = Color(0xFF120E2E),
        emoji = "🌃",
        curvaturaPista = 1.9f
    ),
    PistaBT21(
        id = "galletas",
        nombre = "VALLE SHOOKY 3D",
        subti = "Curvas dulces de chocolate",
        cielo1 = Color(0xFFFFE082),
        cielo2 = Color(0xFFFFAB91),
        colorPista = Color(0xFF4E342E),
        colorBorde1 = Color(0xFFFFB300),
        colorBorde2 = Color(0xFFFFFFFF),
        colorPasto = Color(0xFF8D6E63),
        emoji = "🍪",
        curvaturaPista = 1.5f
    )
)
