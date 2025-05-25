


package com.mirackalyoncu.orvion.model

data class SurecWithProgress(
    val surecId: Int,
    val surecAdi: String,
    val toplamGorev: Int,
    val tamamlananGorev: Int
) {
    val tamamlanmaOrani: Int
        get() = if (toplamGorev == 0) 0 else (tamamlananGorev * 100) / toplamGorev
}
