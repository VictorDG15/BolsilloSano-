package org.bolsillosano.project.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "salary" -> Icons.Outlined.Payments
    "freelance" -> Icons.Outlined.Work
    "investment" -> Icons.AutoMirrored.Outlined.TrendingUp
    "other_income" -> Icons.Outlined.AccountBalance
    "food" -> Icons.Outlined.Restaurant
    "transport" -> Icons.Outlined.DirectionsBus
    "housing" -> Icons.Outlined.Home
    "health" -> Icons.Outlined.LocalHospital
    "education" -> Icons.Outlined.School
    "leisure" -> Icons.Outlined.SportsEsports
    "services" -> Icons.AutoMirrored.Outlined.ReceiptLong
    else -> Icons.Outlined.Category
}
