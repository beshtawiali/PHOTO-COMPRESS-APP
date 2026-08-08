package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.RoyalPurplePrimary
import com.example.ui.theme.RoyalPurpleSecondary
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String
)

val onboardingPages = listOf(
    OnboardingPageData(
        title = "Welcome to\nPhoto Compressor",
        subtitle = "Compress and resize images quickly without losing visual quality."
    ),
    OnboardingPageData(
        title = "Save Phone\nStorage",
        subtitle = "Reduce image sizes up to 90% and save space for your memories."
    ),
    OnboardingPageData(
        title = "100% Private\nOn-Device",
        subtitle = "Your photos are processed entirely on your device and never uploaded."
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RoyalPurplePrimary,
                        RoyalPurpleSecondary
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header Row with Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = pagerState.currentPage < 2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(
                    onClick = onFinish,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center Illustration Container
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(220.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_launcher_icon_1786194452760),
                        contentDescription = "Photo Compress Official App Icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                    )
                }
            }

            // Bottom White Sheet Card (matching Left Layout in reference UI)
            Card(
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val data = onboardingPages[page]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = data.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        repeat(onboardingPages.size) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val color = if (isSelected) RoyalPurplePrimary else MaterialTheme.colorScheme.outlineVariant
                            val dotWidth = if (isSelected) 28.dp else 8.dp
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(8.dp)
                                    .width(dotWidth)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }

                    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

                    // Pill-shaped Primary Action Button
                    Button(
                        onClick = {
                            if (isLastPage) {
                                onFinish()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalPurplePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLastPage) "Get Started" else "Continue",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
