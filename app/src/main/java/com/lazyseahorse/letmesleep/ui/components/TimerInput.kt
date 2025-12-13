package com.lazyseahorse.letmesleep.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TimerInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .padding(20.dp)
            .size(width = 200.dp, height = 60.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFF2D2D2D),
            focusedContainerColor = Color(0xFF2D2D2D),
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        label = { Text("Enter seconds", color = Color.Gray) },
        singleLine = true,
        shape = RoundedCornerShape(20.dp)
    )
}
