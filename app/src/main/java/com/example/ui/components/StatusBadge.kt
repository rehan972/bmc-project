package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderColor, icon) = when (priority.uppercase()) {
        "CRITICAL" -> Quadruple(
            Color(0xFFFEE2E2),
            Color(0xFF991B1B),
            Color(0xFFF87171),
            Icons.Default.Dangerous
        )
        "HIGH" -> Quadruple(
            Color(0xFFFFEDD5),
            Color(0xFFC2410C),
            Color(0xFFFB923C),
            Icons.Default.Warning
        )
        "MEDIUM" -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            Color(0xFFFCD34D),
            Icons.Default.Info
        )
        else -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF166534),
            Color(0xFF86EFAC),
            Icons.Default.CheckCircle
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = priority.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderColor) = when (status.uppercase()) {
        "NEW" -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), Color(0xFF93C5FD))
        "ASSIGNED" -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), Color(0xFFC4B5FD))
        "IN PROGRESS" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFCD34D))
        "RESOLVED" -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), Color(0xFF86EFAC))
        "CLOSED" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFCBD5E1))
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF374151), Color(0xFFD1D5DB))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
