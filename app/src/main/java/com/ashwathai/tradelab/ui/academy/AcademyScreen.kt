package com.ashwathai.tradelab.ui.academy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ashwathai.tradelab.data.*
import kotlinx.coroutines.launch
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.QuizModule
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.Mission
import com.ashwathai.tradelab.ui.theme.*
import com.ashwathai.tradelab.ui.AuthScreen
import com.ashwathai.tradelab.BuildConfig
import com.ashwathai.tradelab.ui.common.*
import com.ashwathai.tradelab.ui.charts.*
import com.ashwathai.tradelab.ui.portfolio.*
import com.ashwathai.tradelab.ui.watchlist.*
import com.ashwathai.tradelab.ui.academy.*
import com.ashwathai.tradelab.ui.derivatives.*
import com.ashwathai.tradelab.ui.commodities.*
import com.ashwathai.tradelab.ui.profile.*

@Composable
fun AcademyScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats,
    onOpenQuiz: (Int) -> Unit
) {
    var activeSubTab by remember { mutableStateOf("Lessons") }
    val quizModules by viewModel.quizModules.collectAsStateWithLifecycle()
    val missionsList by viewModel.missionsList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // High-contrast neon scrolling sub-tab switcher
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(Color(0xFF141414), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val subTabs = listOf("Lessons", "Missions", "Leaderboard", "AI Coach")
            items(subTabs) { tab ->
                val isSelected = activeSubTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BrandViolet.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) BrandViolet.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { activeSubTab = tab }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            "Lessons" -> "LEARN-TO-EARN"
                            "Missions" -> "MISSIONS"
                            "Leaderboard" -> "LEADERBOARD"
                            else -> "AI PORTFOLIO COACH"
                        },
                        color = if (isSelected) BrandViolet else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (activeSubTab == "Lessons") {
            val completedSet = remember(stats.completedLevels) {
                stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet()
            }
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("academy_header_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LEARNING ACADEMY",
                                    color = BrandViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Learn-to-Earn Virtual Capital",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Academy",
                                tint = BrandViolet,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Indicator
                        val completedCount = completedSet.size
                        val totalCount = quizModules.size
                        Text(
                            text = "Completed Modules: $completedCount of $totalCount",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) completedCount / totalCount.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BrandViolet,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Complete bite-sized risk management lessons and pass quizzes to claim free virtual capital. Expand your portfolio safely!",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of Academy modules
                quizModules.forEach { module ->
                    val isCompleted = completedSet.contains(module.id.toString())

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onOpenQuiz(module.id) }
                            .testTag("academy_module_${module.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, if (isCompleted) Color.White.copy(alpha = 0.05f) else BrandViolet.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = module.topic.uppercase(),
                                        color = BrandViolet,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = module.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                if (isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandVioletDark)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Earned!",
                                            color = BrandViolet,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandViolet.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+${formatCurrencyNoDecimals(module.rewardAmt, stats.currency)}",
                                            color = BrandViolet,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = module.concept,
                                color = TextSubtle,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        } else if (activeSubTab == "Missions") {
            val completedSet = remember(stats.completedLevels) {
                stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet()
            }
            var showCertificateDialog by remember { mutableStateOf(false) }
            val totalCount = quizModules.size

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Digital Certificate Showcase Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (totalCount > 0 && completedSet.size >= totalCount) BrandViolet else DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Certificate",
                            tint = if (totalCount > 0 && completedSet.size >= totalCount) BrandViolet else TextSubtle,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TradeLab Certified Risk Manager",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete all lessons to unlock your official graduation digital certificate.",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCertificateDialog = true },
                            enabled = totalCount > 0 && completedSet.size >= totalCount,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandViolet,
                                disabledContainerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (totalCount > 0 && completedSet.size >= totalCount) "View Certificate 🎓" else "Locked (${completedSet.size}/$totalCount)",
                                color = if (totalCount > 0 && completedSet.size >= totalCount) Color.White else TextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Missions Section
                Text(
                    text = "ACTIVE MISSIONS",
                    color = BrandViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Dynamic Missions Loading
                missionsList.forEach { mission ->
                    val isCompleted = when (mission.identifier) {
                        "has_traded" -> stats.startingCash != stats.cash || stats.holdingsValue > 0
                        "completed_3_modules" -> completedSet.size >= 3
                        "has_calibrated" -> stats.riskLevel != "Moderate" || stats.startingCash != 25000.0
                        else -> false
                    }
                    MissionRow(
                        title = mission.title,
                        desc = mission.desc,
                        isCompleted = isCompleted,
                        reward = mission.reward
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            if (showCertificateDialog) {
                Dialog(onDismissRequest = { showCertificateDialog = false }) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(2.dp, BrandViolet)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("GRADUATION DIPLOMA", color = BrandViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "This certifies that",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TradeLab Scholar",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "has successfully completed all advanced risk calibration models & Learn-to-Earn modules.",
                                color = TextSubtle,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Awarded with pride from",
                                color = TextSubtle,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "TradeLab Academy Engine",
                                color = BrandViolet,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showCertificateDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandViolet),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Great, Thank you!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Leaderboard") {
            val globalLeaders by viewModel.globalLeaderboard.collectAsStateWithLifecycle()
            val completedSet = remember(stats.completedLevels) {
                stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet()
            }
            val userScore = completedSet.size * 1000 + 1500

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Leaderboard",
                            tint = BrandViolet,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "TRADING LEADERBOARD",
                                color = BrandViolet,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TradeLab Arena",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Compete with other realistic practitioners.",
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // If global leaders are empty, show local placeholder for better UX
                val displayLeaders = if (globalLeaders.isEmpty()) {
                    listOf(
                        LeaderboardEntry("bot", "👑 TradeLab Bot", 50000, 1000000.0, "Rank #1"),
                        LeaderboardEntry("you", "You (Trader)", userScore, stats.totalValue, "Rank #2")
                    )
                } else {
                    globalLeaders
                }

                displayLeaders.forEachIndexed { index, leader ->
                    val isUser = leader.userId == (stats.completedLevels /* Using this as a proxy for UID if email is empty */) || leader.userName.contains("You") || leader.userId == viewModel.userProfile.value?.userEmail
                    val isKing = leader.userName.contains("👑 TradeLab") && !isUser
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) BrandViolet.copy(alpha = 0.1f) else DarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isUser) BrandViolet else if (isKing) AccentYellow.copy(alpha = 0.4f) else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${index + 1}",
                                    color = if (isKing) AccentYellow else TextSubtle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = leader.userName,
                                    color = if (isUser) BrandViolet else if (isKing) AccentYellow else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%,d", leader.xp)} XP",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = leader.rank.ifBlank { "Rank #${index + 1}" },
                                    color = TextSubtle,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        } else {
            AiCoachScreen(viewModel = viewModel, stats = stats)
        }
    }
}


@Composable
fun MissionRow(
    title: String,
    desc: String,
    isCompleted: Boolean,
    reward: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isCompleted) BrandViolet.copy(alpha = 0.3f) else DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Status",
                tint = if (isCompleted) BrandViolet else TextSubtle,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isCompleted) TextMuted else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = TextSubtle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reward: $reward",
                        color = BrandViolet,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun AiCoachScreen(
    viewModel: TradingViewModel,
    stats: PortfolioStats
) {
    val aiChatLog by viewModel.aiChatLog.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    
    var showAdPlayer by remember { mutableStateOf(false) }
    var isAdLoadingLocal by remember { mutableStateOf(false) }
    var adTimerSecLocal by remember { mutableStateOf(0) }
    
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Automatically scroll to bottom when new messages arrive
    LaunchedEffect(aiChatLog.size, isAiLoading) {
        if (aiChatLog.isNotEmpty()) {
            listState.animateScrollToItem(aiChatLog.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // AI Header card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("ai_coach_header"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandViolet.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = "AI Coach", tint = BrandViolet, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI PORTFOLIO COACH",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by Google Gemini • Offline Diagnostic Advisor",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (aiChatLog.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat logs or preset suggestions
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (aiChatLog.isEmpty()) {
                // Preset onboarding recommendations
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Coach Prompt Suggestions",
                        tint = TextSubtle,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ask your AI Strategic Advisor",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Consult Gemini to audit your position sizes, analyze your risk tolerance, or explain complex retail order types.",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "SUGGESTED CONSULTATIONS",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val presets = listOf(
                        "Audit my current portfolio risk limits",
                        "How do I set stop-loss or GTT orders?",
                        "What are the benefits of 12% position sizing?",
                        "Can you explain SMA & RSI charts?"
                    )

                    presets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.sendMessageToAi(preset)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = preset,
                                    color = BrandViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.PlayArrow, contentDescription = "Send", tint = BrandViolet, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            } else {
                // Scrollable Chat Message History
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(aiChatLog) { (sender, text) ->
                        val isUser = sender == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BrandViolet.copy(alpha = 0.1f))
                                        .align(Alignment.Top),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = "AI", tint = BrandViolet, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Card(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .testTag(if (isUser) "user_message" else "ai_message"),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) BrandVioletDark.copy(alpha = 0.4f) else DarkSurface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isUser) BrandViolet.copy(alpha = 0.3f) else DarkBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isAiLoading) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = BrandViolet,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Gemini is auditing portfolio...", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- MONETIZATION & CREDIT DESK BAR ---
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasApiKey = apiKey != "MY_GEMINI_API_KEY" && apiKey.isNotBlank()

        if (stats.isPremium) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AccentYellow.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pro active",
                        tint = AccentYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasApiKey) "Pro Plan Active • Real Live Gemini API Enabled" else "Pro Plan Active • Unlimited Offline Simulation Mode",
                        color = AccentYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandViolet.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Credits",
                            tint = BrandViolet,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credits Remaining: ${stats.aiAuditCredits} (1 / audit)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BrandViolet.copy(alpha = 0.15f))
                            .clickable {
                                showAdPlayer = true
                                isAdLoadingLocal = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch Ad",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Watch Ad (+3 Credits)",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- LOCAL AD PLAYER DIALOG ---
        if (showAdPlayer) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, BrandViolet.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isAdLoadingLocal) {
                            CircularProgressIndicator(color = BrandViolet, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading Sponsored stream...",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Matching relevant investor ads dynamically",
                                color = TextSubtle,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Playing Ad",
                                tint = AccentYellow,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Streaming Sponsored Message...",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please do not close. Your +3 AI Credits unlock in ${adTimerSecLocal}s.",
                                color = TextSubtle,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = (3 - adTimerSecLocal) / 3f,
                                color = BrandViolet,
                                trackColor = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
            
            LaunchedEffect(showAdPlayer) {
                kotlinx.coroutines.delay(1200)
                isAdLoadingLocal = false
                adTimerSecLocal = 3
                while (adTimerSecLocal > 0) {
                    kotlinx.coroutines.delay(1000)
                    adTimerSecLocal--
                }
                viewModel.earnAiAuditCredits(3)
                showAdPlayer = false
            }
        }

        // Input chat box bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                placeholder = { Text("Consult AI coach (e.g. Audit my trades)", color = TextSubtle, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = BrandViolet,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = BrandViolet
                ),
                shape = RoundedCornerShape(16.dp)
            )

            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        viewModel.sendMessageToAi(inputMessage)
                        inputMessage = ""
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandViolet)
                    .testTag("ai_send_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Send Message", tint = Color.White)
            }
        }
    }
}


