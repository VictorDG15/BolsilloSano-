package org.bolsillosano.project.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
data class ShareBarItem(
    val name: String,
    val amount: String,
    val percentLabel: String,
    val fraction: Float,
)

@Composable
fun ShareBars(
    shares: List<ShareBarItem>,
    modifier: Modifier = Modifier,
) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        shares.forEach { share ->
            Column(
                Modifier.semantics {
                    contentDescription = "${share.name}, ${share.amount}, ${share.percentLabel} del gasto"
                },
            ) {
                Text("${share.name} · ${share.percentLabel}", style = MaterialTheme.typography.bodyMedium)
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .padding(top = 4.dp),
                ) {
                    drawRoundRect(color = trackColor, cornerRadius = CornerRadius(8.dp.toPx()))
                    drawRoundRect(
                        color = barColor,
                        size = Size(size.width * share.fraction, size.height),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                    )
                }
                Text(share.amount, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
