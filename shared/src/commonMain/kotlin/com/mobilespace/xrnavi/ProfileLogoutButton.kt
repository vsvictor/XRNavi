package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileLogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("authenticated-logout"),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccountIcon(
                AccountIcon.Back, Modifier.size(20.dp).rotate(180f),
                tint = LocalContentColor.current,
            )
            Text("Вийти з акаунта", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
