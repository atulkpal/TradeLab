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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
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
import com.ashwathai.tradelab.R
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.TradingViewModel
import com.ashwathai.tradelab.ui.QuizModule
import com.ashwathai.tradelab.ui.AcademyCourse
import com.ashwathai.tradelab.ui.ChapterModule
import com.ashwathai.tradelab.ui.Lecture
import com.ashwathai.tradelab.ui.Mission
import com.ashwathai.tradelab.ui.AcademyScoring
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
    nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null,
    onOpenQuiz: (Int) -> Unit
) {
    var activeSubTab by rememberSaveable { mutableStateOf("Lessons") }
    val quizModules by viewModel.quizModules.collectAsStateWithLifecycle()
    val academyCourses by viewModel.academyCourses.collectAsStateWithLifecycle()
    val missionsList by viewModel.missionsList.collectAsStateWithLifecycle()
    val claimedMissions by viewModel.claimedMissions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // High-contrast neon scrolling sub-tab switcher
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(DarkSurfaceElevated, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val subTabs = listOf("Lessons", "Missions", "Leaderboard", "AI Coach")
            items(subTabs) { tab ->
                val isSelected = activeSubTab == tab
                val highlightColor = DynamicPrimary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) highlightColor.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) highlightColor.copy(alpha = 0.3f) else Color.Transparent,
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
                        color = if (isSelected) highlightColor else TextMuted,
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
                    border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.3f))
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
                                    color = DynamicPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Learn-to-Earn Virtual Capital",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Academy",
                                tint = DynamicPrimary,
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
                            color = DynamicPrimary,
                            trackColor = TextPrimary.copy(alpha = 0.08f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Gamification surface (Epic 25.4): streak + XP, previously invisible in Academy
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val streak = userProfile?.dailyStreak ?: 0
                            val academyXp = completedSet.size * 1000 + 1500
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TextPrimary.copy(alpha = 0.06f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔥", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak day${if (streak == 1) "" else "s"} streak",
                                    color = AccentYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TextPrimary.copy(alpha = 0.06f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "%,d XP".format(academyXp),
                                    color = DynamicPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Complete bite-sized risk management lessons and pass quizzes to claim free virtual capital. Expand your portfolio safely!",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                if (nativeAd != null && !stats.isPremium) {
                    Spacer(modifier = Modifier.height(12.dp))
                    NativeAdRow(nativeAd = nativeAd)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of Academy modules grouped by course
                val displayCourses = if (academyCourses.isNotEmpty()) {
                    academyCourses
                } else if (quizModules.isNotEmpty()) {
                    listOf(
                        AcademyCourse(
                            id = 1,
                            title = "Stock Market Basics",
                            tagline = "Foundational lessons for the Indian markets.",
                            iconEmoji = "ðŸ“ˆ",
                            tier = "BEGINNER",
                            order = 1,
                            chapters = quizModules
                        )
                    )
                } else {
                    emptyList()
                }

                CourseDeck(
                    courses = displayCourses,
                    completedSet = completedSet,
                    currency = stats.currency,
                    onOpenChapter = { onOpenQuiz(it) }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        } else if (activeSubTab == "Missions") {
            val completedSet = remember(stats.completedLevels) {
                stats.completedLevels.split(",").filter { it.isNotBlank() }.toSet()
            }
            val unlockedIds = remember(completedSet, academyCourses) {
                AcademyScoring.unlockedCourseIds(academyCourses, completedSet)
            }
            var showCertificateDialog by remember { mutableStateOf(false) }
            val totalCount = if (academyCourses.isNotEmpty()) academyCourses.sumOf { it.chapters.size } else quizModules.size

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
                    border = BorderStroke(1.dp, if (totalCount > 0 && completedSet.size >= totalCount) DynamicPrimary else DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Certificate",
                            tint = if (totalCount > 0 && completedSet.size >= totalCount) DynamicPrimary else TextSubtle,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TradeLab Certified Risk Manager",
                            color = TextPrimary,
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
                                containerColor = DynamicPrimary,
                                disabledContainerColor = TextPrimary.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val unlockedCertificate = totalCount > 0 && completedSet.size >= totalCount
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (unlockedCertificate) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_status_certificate),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (unlockedCertificate) "View Certificate" else "Locked (${completedSet.size}/$totalCount)",
                                    color = if (unlockedCertificate) TextOnAccent else TextSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Missions Section
                Text(
                    text = "ACTIVE MISSIONS",
                    color = DynamicPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Complete missions to grow your virtual wallet. Claim each reward once â€” it's yours forever.",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Dynamic Missions Loading
                missionsList.forEach { mission ->
                    val evaluation = AcademyScoring.evaluateMission(
                        mission = mission,
                        completedSet = completedSet,
                        academyCourses = academyCourses,
                        unlockedIds = unlockedIds,
                        stats = stats
                    )
                    MissionRow(
                        title = mission.title,
                        desc = mission.desc,
                        reward = mission.reward,
                        rewardAmt = mission.rewardAmt,
                        isCompleted = evaluation.isCompleted,
                        isClaimed = claimedMissions.contains(mission.id.toString()),
                        progress = evaluation.progress,
                        target = evaluation.target,
                        currency = stats.currency,
                        onClaim = { viewModel.claimMission(mission) }
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
                        border = BorderStroke(2.dp, DynamicPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("GRADUATION DIPLOMA", color = DynamicPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "This certifies that",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TradeLab Scholar",
                                color = TextPrimary,
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
                                color = DynamicPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showCertificateDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Great, Thank you!", color = TextOnAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Leaderboard") {
            val globalLeaders by viewModel.globalLeaderboard.collectAsStateWithLifecycle()
            val sortMode by viewModel.leaderboardSortMode.collectAsStateWithLifecycle()
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
                    border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Leaderboard",
                            tint = DynamicPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "TRADING LEADERBOARD",
                                color = DynamicPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TradeLab Arena",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sorted by $sortMode",
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Sort Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("XP", "Discipline").forEach { mode ->
                        val isSelected = sortMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DynamicPrimary else Color.Transparent)
                                .clickable { viewModel.setLeaderboardSort(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mode == "XP") "Wealth (XP)" else "Maturity (Score)",
                                color = if (isSelected) TextOnAccent else TextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Invite Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { viewModel.shareAppInvite() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DynamicPrimary.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DynamicPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = "Invite", tint = TextOnAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Challenge a Friend", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Invite fellow traders to the arena.", color = TextSubtle, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = DynamicPrimary)
                    }
                }

                if (nativeAd != null && !stats.isPremium) {
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        NativeAdRow(nativeAd = nativeAd)
                    }
                }

                // If global leaders are empty, show local placeholder for better UX
                val displayLeaders = if (globalLeaders.isEmpty()) {
                    listOf(
                        LeaderboardEntry("bot", "TradeLab Bot", 50000, 1000000.0, 95, "Rank #1"),
                        LeaderboardEntry("you", "You (Trader)", userScore, stats.totalValue, stats.disciplineScore, "Rank #2")
                    )
                } else {
                    globalLeaders
                }

                displayLeaders.forEachIndexed { index, leader ->
                    val isUser = leader.userId == (stats.completedLevels /* Using this as a proxy for UID if email is empty */) || leader.userName.contains("You") || leader.userId == hashUserId(userProfile?.userEmail.orEmpty())
                    val isKing = leader.userName.contains("TradeLab") && !isUser
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) DynamicPrimary.copy(alpha = 0.1f) else DarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isUser) DynamicPrimary else if (isKing) AccentYellow.copy(alpha = 0.4f) else DarkBorder
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
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isKing) {
                                            Image(
                                                painter = painterResource(R.drawable.ic_status_crown),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = leader.userName,
                                            color = if (isUser) DynamicPrimary else if (isKing) AccentYellow else TextOnAccent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (leader.disciplineScore > 0) {
                                        Text(
                                            text = "Discipline: ${leader.disciplineScore}/100",
                                            color = if (leader.disciplineScore >= 80) AccentGreen else TextSubtle,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%,d", leader.xp)} XP",
                                    color = TextPrimary,
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

/**
 * Single-open accordion course deck with progressive course unlocking.
 * Course N unlocks only after every chapter of course N-1 is completed.
 */

@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 5.dp,
    label: String? = null
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progressRing"
    )
    Box(modifier = modifier.size(44.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(strokePx)
            )
            if (animated > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f, sweepAngle = 360f * animated, useCenter = false,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = label.orEmpty(),
            color = TextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
// Hero scene art per course (vector-only per design discipline — crisp at any size)
private fun heroResFor(courseId: Int): Int = when (courseId) {
    1 -> R.drawable.hero_course_markets
    2 -> R.drawable.hero_course_technical
    3 -> R.drawable.hero_course_fundamental
    4 -> R.drawable.hero_course_derivatives
    5 -> R.drawable.hero_course_psychology
    else -> R.drawable.hero_course_taxation
}

@Composable
fun CourseDeck(
    courses: List<AcademyCourse>,
    completedSet: Set<String>,
    currency: String,
    onOpenChapter: (Int) -> Unit
) {
    val ordered = remember(courses) { courses.sortedBy { it.order } }
    val unlockedIds = remember(courses, completedSet) {
        AcademyScoring.unlockedCourseIds(courses, completedSet)
    }
    var expandedCourseId by rememberSaveable { mutableStateOf<Int?>(null) }

    ordered.forEachIndexed { index, course ->
        val isUnlocked = course.id in unlockedIds
        val isExpanded = expandedCourseId == course.id
        val chapters = course.chapters
        val completedCount = chapters.count { completedSet.contains(it.id.toString()) }
        val isCourseComplete = chapters.isNotEmpty() && completedCount == chapters.size
        val tierColor = when (course.tier.uppercase()) {
            "ADVANCED" -> AccentRose
            "INTERMEDIATE" -> AccentYellow
            else -> BrandViolet // BEGINNER — semantic tier identity (not a theme accent)
        }

        val borderModifier = if (isExpanded && isUnlocked) {
            Modifier.neonGlowPulse(tierColor)
        } else {
            Modifier.border(
                1.dp,
                if (isUnlocked) tierColor.copy(alpha = 0.35f) else DarkBorder,
                RoundedCornerShape(20.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .premiumEntrance(index)
                .testTag("academy_course_${course.id}")
                .then(borderModifier)
                .clickable {
                    if (isExpanded) expandedCourseId = null else expandedCourseId = course.id
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column {
                // ── HERO IMAGE with scrim + overlays ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Image(
                        painter = painterResource(heroResFor(course.id)),
                        contentDescription = course.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Bottom scrim: guarantees overlaid text/ring legibility
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.45f to Color.Transparent,
                                    1f to DarkSurface
                                )
                            )
                    )
                    if (!isUnlocked) {
                        // Locked: dim the art + lock badge
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(DarkBg.copy(alpha = 0.62f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextMuted,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Tier chip — top-start
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tierColor.copy(alpha = 0.22f))
                            .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = course.tier.uppercase(),
                            color = tierColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }

                    // State — top-end
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCourseComplete -> {
                                SparkleBurst(trigger = true, color = tierColor)
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Course complete",
                                    tint = tierColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            isUnlocked -> RotatingChevron(
                                expanded = isExpanded,
                                modifier = Modifier.size(22.dp),
                                tint = TextPrimary
                            )
                            else -> Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Title + progress ring — bottom row over scrim
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = course.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        ProgressRing(
                            progress = if (chapters.isNotEmpty()) completedCount / chapters.size.toFloat() else 0f,
                            color = tierColor,
                            label = "$completedCount/${chapters.size}"
                        )
                    }
                }

                // ── BODY ──
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = course.tagline.ifBlank { "Master this module to level up." },
                        color = if (isUnlocked) TextSubtle else TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            isCourseComplete -> "Course mastered — reward claimed!"
                            !isUnlocked -> "Locked — finish the previous course to earn rewards"
                            completedCount > 0 -> "$completedCount of ${chapters.size} knowledge checks passed"
                            else -> "${
                                if (isUnlocked) "Start" else "Preview"
                            } • ${chapters.size} chapters • reward up to ${
                                formatCurrencyNoDecimals(
                                    chapters.maxOfOrNull { it.rewardAmt } ?: 0.0,
                                    currency
                                )
                            }"
                        },
                        color = when {
                            isCourseComplete -> AccentGreen
                            isUnlocked -> DynamicPrimary
                            else -> TextMuted
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(260)) + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                chapters.forEachIndexed { chapterIndex, module ->
                    val isChapterCompleted = completedSet.contains(module.id.toString())
                    Column(
                        modifier = Modifier.premiumEntrance(chapterIndex, staggerMs = 40L, offsetY = 10.dp)
                    ) {
                        ChapterCard(
                            module = module,
                            isCompleted = isChapterCompleted,
                            isLocked = !isUnlocked,
                            currency = currency,
                            onOpen = { onOpenChapter(module.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterCard(
    module: ChapterModule,
    isCompleted: Boolean,
    isLocked: Boolean = false,
    currency: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onOpen() }
            .testTag("academy_module_${module.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLocked) DarkSurface.copy(alpha = 0.6f) else DarkSurface),
        border = BorderStroke(1.dp, if (isCompleted) TextPrimary.copy(alpha = 0.05f) else if (isLocked) TextPrimary.copy(alpha = 0.06f) else DynamicPrimary.copy(alpha = 0.2f))
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
                        color = if (isLocked) TextMuted else DynamicPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                when {
                    isCompleted -> Box(
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("academy_chapter_complete"),
                        contentAlignment = Alignment.Center
                    ) {
                        SparkleBurst(trigger = isCompleted, color = DynamicPrimary)
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Chapter complete",
                            tint = DynamicPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    isLocked -> Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TextPrimary.copy(alpha = 0.06f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Preview",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DynamicPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+${formatCurrencyNoDecimals(module.rewardAmt, currency)}",
                            color = DynamicPrimary,
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
            if (module.quizzes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_status_quiz),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Knowledge Check â€¢ ${module.quizzes.size} Questions",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun MissionRow(
    title: String,
    desc: String,
    reward: String,
    rewardAmt: Double,
    isCompleted: Boolean,
    isClaimed: Boolean,
    progress: Int,
    target: Int,
    currency: String,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isCompleted) DynamicPrimary.copy(alpha = 0.3f) else DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isClaimed) Icons.Default.CheckCircle else if (isCompleted) Icons.Default.Verified else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Status",
                    tint = if (isClaimed) AccentGreen else if (isCompleted) DynamicPrimary else TextSubtle,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = if (isClaimed) TextMuted else TextOnAccent,
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
                    Spacer(modifier = Modifier.height(6.dp))
                    if (target > 1) {
                        LinearProgressIndicator(
                            progress = { (progress.toFloat() / target).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isClaimed) AccentGreen else DynamicPrimary,
                            trackColor = TextPrimary.copy(alpha = 0.08f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$progress/$target complete",
                            color = if (isClaimed) AccentGreen else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isClaimed) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reward: $reward",
                            color = DynamicPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isCompleted && !isClaimed) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mission_claim_${title}"),
                    colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_status_cash),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Claim +${formatCurrencyNoDecimals(rewardAmt, currency)}",
                            color = TextOnAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isClaimed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Claimed âœ“",
                        color = AccentGreen,
                        fontSize = 11.sp,
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
            border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DynamicPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = "AI Coach", tint = DynamicPrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI PORTFOLIO COACH",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by Google Gemini â€¢ Offline Diagnostic Advisor",
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
                        color = TextPrimary,
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
                            border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = preset,
                                    color = DynamicPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.PlayArrow, contentDescription = "Send", tint = DynamicPrimary, modifier = Modifier.size(12.dp))
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
                                        .background(DynamicPrimary.copy(alpha = 0.1f))
                                        .align(Alignment.Top),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = "AI", tint = DynamicPrimary, modifier = Modifier.size(14.dp))
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
                        containerColor = if (isUser) DynamicPrimary.copy(alpha = 0.15f) else DarkSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isUser) DynamicPrimary.copy(alpha = 0.3f) else DarkBorder
                    )
                ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = text,
                                        color = TextPrimary,
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
                                    color = DynamicPrimary,
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
                        text = if (hasApiKey) "Pro Plan Active â€¢ Real Live Gemini API Enabled" else "Pro Plan Active â€¢ Unlimited Offline Simulation Mode",
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
                colors = CardDefaults.cardColors(containerColor = DynamicPrimary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.2f))
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
                            tint = DynamicPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credits Remaining: ${stats.aiAuditCredits} (1 / audit)",
                            color = TextOnAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DynamicPrimary.copy(alpha = 0.15f))
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
                            tint = TextOnAccent,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Watch Ad (+3 Credits)",
                            color = TextOnAccent,
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
                    border = BorderStroke(1.dp, DynamicPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isAdLoadingLocal) {
                            CircularProgressIndicator(color = DynamicPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading Sponsored stream...",
                                color = TextPrimary,
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
                                color = TextPrimary,
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
                                color = DynamicPrimary,
                                trackColor = TextPrimary.copy(alpha = 0.08f),
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
                    focusedTextColor = TextOnAccent,
                    unfocusedTextColor = TextOnAccent,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = DynamicPrimary,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = DynamicPrimary
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
                    .background(DynamicPrimary)
                    .testTag("ai_send_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Send Message", tint = TextOnAccent)
            }
        }
    }
}


