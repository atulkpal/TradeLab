package com.ashwathai.tradelab.ui.academy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashwathai.tradelab.R
import com.ashwathai.tradelab.data.LectureMedia
import com.ashwathai.tradelab.data.VideoManifestRepository
import com.ashwathai.tradelab.ui.ChapterModule
import com.ashwathai.tradelab.ui.AcademyScoring
import com.ashwathai.tradelab.ui.PortfolioStats
import com.ashwathai.tradelab.ui.common.formatCurrencyNoDecimals
import com.ashwathai.tradelab.ui.common.LevelPlayBanner
import com.ashwathai.tradelab.ui.common.VideoPlayerView
import com.ashwathai.tradelab.ui.theme.*

/** Launches the ACADEMY_DOUBLE rewarded ad, routing the legacy callback contract. */
typealias DoubleRewardAdLauncher = (
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    onUserEarnedReward: () -> Unit
) -> Unit

/**
 * Full-screen Academy lecture + knowledge-check destination.
 *
 * Replaces the former modal Dialog (MainActivity) with proper reading ergonomics:
 * immersive video slot, swipeable reading pane, and the complete multi-question
 * knowledge-check flow with claim / double-reward (rewarded ad) outcomes.
 *
 * Behavior-preserving migration of MainActivity's lecture dialog (Epic 25.3).
 * Theme-correct from birth: DynamicPrimary / TextOnAccent / theme-aware alphas.
 */
@Composable
fun LectureScreen(
    quiz: ChapterModule,
    isChapterLocked: Boolean,
    isAlreadyCompleted: Boolean,
    stats: PortfolioStats,
    academyLanguage: String = VideoManifestRepository.LANG_EN,
    manifestReady: Boolean = false,
    onToggleAcademyLanguage: () -> Unit = {},
    resolveLectureVideo: (String) -> LectureMedia = {
        LectureMedia(it, hasHindi = false)
    },
    onLaunchBonusAd: DoubleRewardAdLauncher = { _, onFailed, _ -> onFailed("Ad host unavailable") },
    onDismiss: () -> Unit,
    onCompleteStandard: () -> Unit,
    onCompleteDouble: () -> Unit,
    onFeedback: (String) -> Unit,
    onLaunchDoubleRewardAd: DoubleRewardAdLauncher
) {
    BackHandler(onBack = onDismiss)

    var activeLectureIndex by remember { mutableStateOf<Int?>(0) }
    var questionIndex by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var showResult by remember { mutableStateOf(false) }
    var quizComplete by remember { mutableStateOf(false) }
    var isAdLoading by remember { mutableStateOf(false) }

    // 2.0.2: post-video bonus rewarded (fail-closed, once per chapter, Pro skips)
    var bonusClaimed by remember { mutableStateOf(false) }
    var isBonusLoading by remember { mutableStateOf(false) }

    val questions = quiz.quizzes.ifEmpty {
        listOf(com.ashwathai.tradelab.ui.QuizQuestion(quiz.concept, listOf("True", "False"), 0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .testTag("lecture_screen")
    ) {
        // â”€â”€ Header â”€â”€
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = quiz.topic.uppercase(),
                color = DynamicPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Close", tint = TextMuted)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = quiz.title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )
            if (quiz.riskDisclosure.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = quiz.riskDisclosure,
                    color = AccentYellow,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (activeLectureIndex != null) {
                // â”€â”€ LECTURES MODE â”€â”€
                Text(
                    text = "COURSE LECTURES:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (quiz.lectures.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quiz.lectures.forEachIndexed { idx, lecture ->
                            val isLecSel = activeLectureIndex == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLecSel) DynamicPrimary.copy(alpha = 0.15f) else TextPrimary.copy(alpha = 0.04f))
                                    .border(1.dp, if (isLecSel) DynamicPrimary else TextPrimary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .clickable { activeLectureIndex = idx }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Lec ${idx + 1}",
                                    color = if (isLecSel) DynamicPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val selectedLecture = quiz.lectures.getOrNull(activeLectureIndex ?: 0)
                    if (selectedLecture != null) {
                        // ── VIDEO-FIRST: the video leads when present ──
                        // Epic 27: manifest-aware resolution + dynamic EN/HI toggle
                        val media = remember(
                            selectedLecture.videoUrl, academyLanguage, manifestReady
                        ) { resolveLectureVideo(selectedLecture.videoUrl) }
                        if (media.resolvedUrl.isNotBlank()) {
                            if (media.hasHindi) {
                                LanguageToggleRow(
                                    selected = academyLanguage,
                                    onToggle = onToggleAcademyLanguage
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            VideoPlayerView(
                                videoUrl = media.resolvedUrl,
                                videoHeight = 540.dp // portrait-immersive: NotebookLM videos are vertical
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = selectedLecture.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedLecture.content,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = selectedLecture.title,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedLecture.content,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TextPrimary.copy(alpha = 0.04f))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Video lecture coming soon for this lesson.",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // ── 2.0.2: post-video bonus rewarded (fail-closed, once per
                        // chapter, visible when 3+ lectures, active only on last lecture, Pro users skip) ──
                        if (quiz.lectures.size >= 3 &&
                            !bonusClaimed && !stats.isPremium
                        ) {
                            val isActive = activeLectureIndex == quiz.lectures.lastIndex
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentYellow.copy(alpha = if (isActive) 0.08f else 0.03f)
                                ),
                                border = BorderStroke(1.dp, AccentYellow.copy(alpha = if (isActive) 0.4f else 0.15f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎁 CHAPTER BONUS",
                                        color = AccentYellow.copy(alpha = if (isActive) 1f else 0.5f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isActive) "Watched every lecture? Claim 100 Brokerage Credits — on the house."
                                               else "Watch all lectures to unlock +100 Brokerage Credits.",
                                        color = TextSecondary.copy(alpha = if (isActive) 1f else 0.5f),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            isBonusLoading = true
                                            onLaunchBonusAd(
                                                { isBonusLoading = false },
                                                { err ->
                                                    isBonusLoading = false
                                                    onFeedback("No ad available right now — try again shortly")
                                                },
                                                {
                                                    isBonusLoading = false
                                                    bonusClaimed = true
                                                    onFeedback("Bonus claimed: +100 Brokerage Credits!")
                                                }
                                            )
                                        },
                                        enabled = isActive && !isBonusLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentYellow,
                                            disabledContainerColor = AccentYellow.copy(alpha = 0.2f),
                                            disabledContentColor = TextOnAccent.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isBonusLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = TextOnAccent,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("LOADING…", color = TextOnAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(
                                                if (isActive) "WATCH AD — CLAIM 100 BROKERAGE CREDITS" else "COMPLETE ALL LECTURES FIRST",
                                                color = TextOnAccent,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = quiz.concept,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isChapterLocked) {
                    val currentCourseOrder = quiz.courseId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("locked_assessment"),
                        colors = CardDefaults.cardColors(containerColor = TextPrimary.copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Assessment locked",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Assessment Locked",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Complete all chapters of the previous course to unlock this assessment & reward.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            activeLectureIndex = null
                            questionIndex = 0
                            quizComplete = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_knowledge_check")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_status_quiz),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Knowledge Check",
                                color = TextOnAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else if (!quizComplete) {
                // â”€â”€ MULTI-QUESTION KNOWLEDGE CHECK â”€â”€
                val currentQuestion = questions.getOrNull(questionIndex.coerceAtMost(questions.size - 1))
                val selectedAnswer = answers[questionIndex]

                if (currentQuestion == null) {
                    Text(
                        text = "This chapter has no questions yet. Review the lectures and try again.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "KNOWLEDGE CHECK:",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Question ${questionIndex + 1} of ${questions.size}",
                        color = DynamicPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { if (questions.isNotEmpty()) (questionIndex + 1) / questions.size.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = DynamicPrimary,
                        trackColor = TextPrimary.copy(alpha = 0.08f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.question,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSel = selectedAnswer == index
                        val itemBg = if (isSel) DynamicPrimary.copy(alpha = 0.08f) else TextPrimary.copy(alpha = 0.03f)
                        val itemBorder = if (isSel) DynamicPrimary.copy(alpha = 0.5f) else TextPrimary.copy(alpha = 0.08f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(itemBg)
                                .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = !showResult) { answers = answers + (questionIndex to index) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { if (!showResult) answers = answers + (questionIndex to index) },
                                colors = RadioButtonDefaults.colors(selectedColor = DynamicPrimary, unselectedColor = TextSubtle),
                                enabled = !showResult
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (showResult) {
                        val wasCorrect = AcademyScoring.isCorrect(currentQuestion, selectedAnswer ?: -1)
                        if (wasCorrect) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AccentGreenDark.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_status_celebrate),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "CORRECT!",
                                            color = AccentGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (currentQuestion.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentQuestion.explanation,
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AccentRoseDark.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_status_wrong),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "INCORRECT",
                                            color = AccentRose,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (currentQuestion.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentQuestion.explanation,
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val isLast = questionIndex >= questions.size - 1
                        Button(
                            onClick = {
                                if (isLast) {
                                    quizComplete = true
                                } else {
                                    questionIndex++
                                    showResult = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLast) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_status_target),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (isLast) "See My Results" else "Next Question â†’",
                                    color = TextOnAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { showResult = true },
                            enabled = selectedAnswer != null,
                            colors = ButtonDefaults.buttonColors(containerColor = DynamicPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Check Answer", color = TextOnAccent, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { activeLectureIndex = 0 },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("â† Back to Lectures", color = DynamicPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // â”€â”€ SCORE SUMMARY â”€â”€
                val (correctCount, totalCount) = AcademyScoring.score(questions, answers)
                val passed = AcademyScoring.passes(questions, answers)

                Text(
                    text = "KNOWLEDGE CHECK COMPLETE:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your Score: $correctCount / $totalCount",
                    color = if (passed) AccentGreen else AccentRose,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (passed) {
                        Image(
                            painter = painterResource(R.drawable.ic_status_celebrate),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = if (passed) {
                            "Chapter passed! You've mastered this topic."
                        } else {
                            "Keep going! Review the lectures and try again to unlock the reward."
                        },
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                questions.forEachIndexed { index, q ->
                    val answered = answers[index]
                    val wasCorrect = AcademyScoring.isCorrect(q, answered ?: -1)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (wasCorrect) "Correct" else "Incorrect",
                            tint = if (wasCorrect) AccentGreen else AccentRose,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Q${index + 1}",
                            color = if (wasCorrect) AccentGreen else AccentRose,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = q.question,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (passed) {
                    if (isAdLoading) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = DynamicPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Connecting to ad stream...", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (!isAlreadyCompleted) {
                                        onCompleteStandard()
                                    }
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAlreadyCompleted) TextPrimary.copy(alpha = 0.1f) else DynamicPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isAlreadyCompleted) "Close Quiz" else "Claim Standard ${formatCurrencyNoDecimals(quiz.rewardAmt, stats.currency)} Capital",
                                    color = TextOnAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!isAlreadyCompleted) {
                                Button(
                                    onClick = {
                                        if (stats.isPremium) {
                                            onCompleteDouble()
                                            onDismiss()
                                            onFeedback("Pro Advantage: Double Reward Claimed Instantly!")
                                        } else {
                                            isAdLoading = true
                                            onLaunchDoubleRewardAd(
                                                { isAdLoading = false },
                                                { err ->
                                                    isAdLoading = false
                                                    onFeedback("Ad failed: $err. Launching fallback.")
                                                                                                    },
                                                {
                                                    onCompleteDouble()
                                                    onDismiss()
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (stats.isPremium) AccentYellow.copy(alpha = 0.15f) else Color.Transparent
                                    ),
                                    border = BorderStroke(1.5.dp, if (stats.isPremium) AccentYellow else AccentGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (stats.isPremium) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                            contentDescription = "Double Reward",
                                            tint = if (stats.isPremium) AccentYellow else AccentGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (stats.isPremium) "PRO: Double Reward Claim Instantly" else "Double Reward (${formatCurrencyNoDecimals(quiz.rewardAmt * 2.0, stats.currency)})",
                                            color = if (stats.isPremium) AccentYellow else AccentGreen,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Image(
                                            painter = painterResource(if (stats.isPremium) R.drawable.ic_status_pro else R.drawable.ic_status_ad),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(if (stats.isPremium) AccentYellow else AccentGreen),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    activeLectureIndex = 0
                                    quizComplete = false
                                    questionIndex = 0
                                    answers = emptyMap()
                                    showResult = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Review Lectures", color = TextPrimary, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    quizComplete = false
                                    questionIndex = 0
                                    answers = emptyMap()
                                    showResult = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRoseMedium),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Try Again", color = TextOnAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // ── 2.0.2: bottom banner (scrolls with content, Pro users skip) ──
            if (!stats.isPremium) {
                LevelPlayBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

/**
 * Epic 27: dynamic EN/HI language selector - rendered ONLY when a Hindi variant
 * of the current lecture exists (remote manifest entry or bundled raw resource).
 */
@Composable
private fun LanguageToggleRow(
    selected: String,
    onToggle: () -> Unit
) {
    val isHindi = selected == VideoManifestRepository.LANG_HI
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("language_toggle")
            .clip(RoundedCornerShape(10.dp))
            .background(TextPrimary.copy(alpha = 0.04f))
            .border(1.dp, TextPrimary.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "LANGUAGE",
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "EN",
            color = if (!isHindi) DynamicPrimary else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (!isHindi) FontWeight.ExtraBold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isHindi) DynamicPrimary else TextPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(start = if (isHindi) 18.dp else 2.dp)
                    .size(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isHindi) TextOnAccent else TextSubtle)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "हिंदी",
            color = if (isHindi) DynamicPrimary else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isHindi) FontWeight.ExtraBold else FontWeight.Normal
        )
    }
}

