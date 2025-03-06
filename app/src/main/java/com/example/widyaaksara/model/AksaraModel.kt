package com.example.widyaaksara.model

data class AksaraModel(
    val nama: String,
    var titik: List<Titik>
)

data class Titik(
    val x: Int,
    val y: Int
)
