package com.remindcare.ui
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val colors=lightColorScheme(primary=Color(0xFF075E9D),onPrimary=Color.White,secondary=Color(0xFF176B50),surface=Color(0xFFF9FAFC),surfaceVariant=Color(0xFFE7F0F7))
@Composable fun RemindCareTheme(content:@Composable()->Unit){MaterialTheme(colorScheme=colors,typography=Typography(),content=content)}
