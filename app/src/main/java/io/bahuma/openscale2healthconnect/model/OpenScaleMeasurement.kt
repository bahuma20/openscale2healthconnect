package io.bahuma.openscale2healthconnect.model

import java.math.BigDecimal
import java.util.Date

data class OpenScaleMeasurement(
    val id: Int,
    val dateTime: Date,
    val weight: BigDecimal,
    val fat: BigDecimal,
    val water: BigDecimal,
    val muscle: BigDecimal
)
