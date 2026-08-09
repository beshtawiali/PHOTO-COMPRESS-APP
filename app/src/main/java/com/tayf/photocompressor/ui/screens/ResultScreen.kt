package com.tayf.photocompressor.ui.screens

import androidx.activity.compose.BackHandler
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tayf.photocompressor.ui.components.BeforeAfterSlider
import com.tayf.photocompressor.ui.theme.GridBlueBg
import com.tayf.photocompressor.ui.theme.GridBlueIcon
import com.tayf.photocompressor.ui.theme.GridGreenBg
import com.tayf.photocompressor.ui.theme.GridGreenIcon
import com.tayf.photocompressor.ui.theme.GridPurpleBg
import com.tayf.photocompressor.ui.theme.GridPurpleIcon
import com.tayf.photocompressor.ui.theme.GridYellowBg
import com.tayf.photocompressor.ui.theme.GridYellowIcon
import com.tayf.photocompressor.ui.theme.RoyalPurplePrimary
import com.tayf.photocompressor.util.ImageProcessor
import com.tayf.photocompressor.util.ProcessResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: ProcessResult?,
    beforeUri: Uri?,
    onSaveToGallery: (ProcessResult) -> Unit,
    onShare: (Uri) -> Unit,
    onProcessAnother: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compression Results",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No result available")
            }
            return@Scaffold
        }

        val savedPct = if (result.originalSize > 0) {
            (((result.originalSize - result.resultSize).toDouble() / result.originalSize) * 100).toInt().coerceAtLeast(0)
        } else 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Success Header
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RoyalPurplePrimary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = RoyalPurplePrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Optimization Complete!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPurplePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your photo was compressed successfully on device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Before/After Image Slider
            if (beforeUri != null) {
                BeforeAfterSlider(
                    beforeUri = beforeUri,
                    afterUri = result.outputUri,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2x2 Metric Cards Grid
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MetricCard(
                        title = "Original Size",
                        value = ImageProcessor.formatFileSize(result.originalSize),
                        icon = Icons.Filled.PhotoLibrary,
                        containerBg = GridYellowBg,
                        iconColor = GridYellowIcon,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Compressed Size",
                        value = ImageProcessor.formatFileSize(result.resultSize),
                        icon = Icons.Filled.Compress,
                        containerBg = GridGreenBg,
                        iconColor = GridGreenIcon,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MetricCard(
                        title = "Space Saved",
                        value = "$savedPct%",
                        icon = Icons.Filled.PieChart,
                        containerBg = GridPurpleBg,
                        iconColor = GridPurpleIcon,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Resolution",
                        value = "${result.resultWidth} x ${result.resultHeight}",
                        icon = Icons.Filled.AspectRatio,
                        containerBg = GridBlueBg,
                        iconColor = GridBlueIcon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Primary Action Pill Buttons
            Button(
                onClick = {
                    onSaveToGallery(result)
                    Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurplePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Filled.Download, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Save to Gallery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onShare(result.outputUri) },
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Image", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onProcessAnother,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Process Another", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerBg: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
