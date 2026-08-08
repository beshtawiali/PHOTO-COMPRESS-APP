package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeUri: Uri,
    afterUri: Uri,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        val containerWidth = maxWidth
        val widthPx = with(density) { containerWidth.toPx() }
        val handleOffsetPx = with(density) { 20.dp.toPx() }
        val maxHeightPx = constraints.maxHeight

        // After Image (Full background)
        AsyncImage(
            model = afterUri,
            contentDescription = "After compressed photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Before Image (Clipped to slider position)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(containerWidth * sliderPosition)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        ) {
            AsyncImage(
                model = beforeUri,
                contentDescription = "Before original photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(containerWidth)
            )
        }

        // Overlay Badges
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "Original",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Text(
                text = "Compressed",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Slider Handle Line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset { IntOffset((widthPx * sliderPosition).roundToInt(), 0) }
                .background(Color.White)
        )

        // Slider Touch Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset {
                    val x = ((widthPx * sliderPosition) - handleOffsetPx).roundToInt()
                    val y = (maxHeightPx / 2) - handleOffsetPx.toInt()
                    IntOffset(x, y)
                }
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        val newPos = (sliderPosition + dragAmount / widthPx).coerceIn(0.05f, 0.95f)
                        sliderPosition = newPos
                    }
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Drag to compare",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
