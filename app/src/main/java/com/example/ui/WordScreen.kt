package com.example.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WordEntry
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.game.LetterTile
import kotlinx.coroutines.launch
import kotlin.math.sin
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val uiState by viewModel.gameUiState.collectAsStateWithLifecycle()
    
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val fName by viewModel.formName.collectAsStateWithLifecycle()
    val fFatherName by viewModel.formFatherName.collectAsStateWithLifecycle()
    val fEmail by viewModel.formEmail.collectAsStateWithLifecycle()
    val completedCount = stats.currentLevelIndex
    val isSending by viewModel.isSendingEmail.collectAsStateWithLifecycle()
    val sendStatus by viewModel.emailStatus.collectAsStateWithLifecycle()
    val certId by viewModel.certificateId.collectAsStateWithLifecycle()
    val paperSize by viewModel.selectedPaperSize.collectAsStateWithLifecycle()

    // Automatically trigger rewarded ad and navigate to Certificate Form after solving exactly 1000 words in page 0
    LaunchedEffect(completedCount) {
        if (completedCount >= 1000 && currentPage == 0) {
            delay(2200)
            activity?.let { viewModel.triggerCompletionAdAndNavigate(it, 1) }
        }
    }

    // Setup matching current level info
    val activeLevelIndex = stats.currentLevelIndex % WordEntry.ALL_WORDS.size
    val currentWord = WordEntry.ALL_WORDS[activeLevelIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (currentPage) {
                        0 -> "Word Scramble"
                        1 -> "Certificate Form"
                        2 -> "Achievement Certificate"
                        else -> "Privacy Policy"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = titleText,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (currentPage == 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.navigateToPage(3) },
                                    modifier = Modifier.size(36.dp).testTag("privacy_policy_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Privacy Policy",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Coins icon",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${stats.coins}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (currentPage > 0) {
                        IconButton(onClick = {
                            if (currentPage == 2) {
                                viewModel.navigateToPage(1)
                            } else {
                                viewModel.navigateToPage(0)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            when (currentPage) {
                0 -> {
                    GameWorkspaceLayout(
                        uiState = uiState,
                        score = stats.score,
                        streak = stats.streak,
                        highScore = stats.highScore,
                        completedCount = completedCount,
                        currentWord = currentWord,
                        onLetterSelected = { viewModel.selectLetter(it) },
                        onLetterDeselected = { viewModel.deselectLetter(it) },
                        onShufflePool = { viewModel.shufflePool() },
                        onClearSelection = { viewModel.clearSelection() },
                        onBuyHint = { viewModel.triggerHint() },
                        onNextLevel = { activity?.let { viewModel.playNextLevelWithAd(it) } },
                        onClaimCertificate = { activity?.let { viewModel.triggerCompletionAdAndNavigate(it, 1) } }
                    )

                    // FULLSCREEN SIMULATED AD OVERLAY
                    if (uiState.showAdSimulation) {
                        SimulatedAdOverlay(uiState = uiState)
                    }
                }
                1 -> {
                    CertificateFormScreen(
                        firstName = fName,
                        fatherName = fFatherName,
                        email = fEmail,
                        onSubmit = { name, father, email ->
                            viewModel.submitCertificateForm(context, name, father, email)
                        },
                        onBack = { viewModel.navigateToPage(0) }
                    )
                }
                2 -> {
                    CertificatePreviewScreen(
                        name = fName,
                        fatherName = fFatherName,
                        email = fEmail,
                        certId = certId,
                        isSending = isSending,
                        sendStatus = sendStatus,
                        selectedPaperSize = paperSize,
                        onPaperSizeChanged = { size -> viewModel.changePaperSizeAndRegenerate(context, size) },
                        onSharePdf = { viewModel.shareCertificatePdf(context) },
                        onEdit = { viewModel.navigateToPage(1) },
                        onPlayAgain = { viewModel.resetSessionAndPlayAgain() }
                    )
                }
                3 -> {
                    PrivacyPolicyScreen(
                        onBack = { viewModel.navigateToPage(0) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameWorkspaceLayout(
    uiState: GameUiState,
    score: Int,
    streak: Int,
    highScore: Int,
    completedCount: Int,
    currentWord: WordEntry,
    onLetterSelected: (LetterTile) -> Unit,
    onLetterDeselected: (LetterTile) -> Unit,
    onShufflePool: () -> Unit,
    onClearSelection: () -> Unit,
    onBuyHint: () -> Unit,
    onNextLevel: () -> Unit,
    onClaimCertificate: () -> Unit
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        val isTablet = maxWidth > 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Panel
            StatsBanner(
                score = score,
                streak = streak,
                highScore = highScore,
                isTablet = isTablet
            )

            // Progress tracking badge to visually assist 5-word target
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Check badge",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Level Challenge Progress:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$completedCount / 1000 Solved",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Categorized Card
            val rawHint = currentWord.hint.trim()
            val baseHint = if (rawHint.endsWith("?")) rawHint.substring(0, rawHint.length - 1).trim() else rawHint
            val easierPromptText = "$baseHint? (${currentWord.word})"

            WordCategoryCard(
                category = currentWord.category,
                promptText = easierPromptText,
                modifier = Modifier.fillMaxWidth()
            )

            // Selected Slots area
            SelectionTrack(
                selectedLetters = uiState.selectedLetters,
                targetLength = currentWord.word.length,
                isWrongGuess = uiState.isWrongGuess,
                onDeselected = onLetterDeselected,
                modifier = Modifier.fillMaxWidth()
            )

            // Game Instructions / Notification message
            Text(
                text = uiState.message,
                color = if (uiState.isWrongGuess) MaterialTheme.colorScheme.error 
                        else if (uiState.isLevelSolved) Color(0xFF2E7D32) 
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Letters Pool area
            PoolArea(
                scrambledPool = uiState.scrambledPool,
                isSolved = uiState.isLevelSolved,
                onSelected = onLetterSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Operational Action buttons
            ActionBar(
                isSolved = uiState.isLevelSolved,
                onShuffle = onShufflePool,
                onClear = onClearSelection,
                onHint = onBuyHint,
                modifier = Modifier.widthIn(max = 500.dp)
            )

            // Dynamic Custom styled Banner Ad
            AdmobBanner(modifier = Modifier.widthIn(max = 500.dp))

            Spacer(modifier = Modifier.weight(1f))

            // Gate transition level card if solved!
            if (uiState.isLevelSolved) {
                SuccessLevelCard(
                    word = currentWord.word,
                    pointsEarned = currentWord.word.length * 10,
                    completedCount = completedCount,
                    onNext = onNextLevel,
                    onClaimCertificate = onClaimCertificate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatsBanner(
    score: Int,
    streak: Int,
    highScore: Int,
    isTablet: Boolean
) {
    val horizontalSpacing = if (isTablet) 32.dp else 12.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatBox(label = "SCORE", value = score.toString(), highlight = true)
        Spacer(modifier = Modifier.width(horizontalSpacing))
        StatBox(label = "STREAK", value = "🔥 $streak", highlight = false)
        Spacer(modifier = Modifier.width(horizontalSpacing))
        StatBox(label = "HIGH SCORE", value = highScore.toString(), highlight = false)
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    highlight: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WordCategoryCard(
    category: String,
    promptText: String,
    modifier: Modifier = Modifier
) {
    // Generate color base according to category name to look extraordinarily custom!
    val accentColor = remember(category) {
        when (category) {
            "Animals" -> Color(0xFFF57F17) // Gold orange
            "Food" -> Color(0xFFD32F2F) // Deep Red
            "Technology" -> Color(0xFF0288D1) // Tech Blue
            "Nature" -> Color(0xFF388E3C) // Green forest
            "Science" -> Color(0xFF7B1FA2) // Purple
            "Sports" -> Color(0xFFE64A19) // Orange rust
            "Calendar" -> Color(0xFF00796B) // Teal
            else -> Color(0xFF1976D2) // General Blue
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = promptText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionTrack(
    selectedLetters: List<LetterTile>,
    targetLength: Int,
    isWrongGuess: Boolean,
    onDeselected: (LetterTile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp, 
                color = if (isWrongGuess) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.outlineVariant, 
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isWrongGuess) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) 
                        else MaterialTheme.colorScheme.surface, 
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display empty/full boxes
            for (i in 0 until targetLength) {
                val tile = selectedLetters.getOrNull(i)
                if (tile != null) {
                    LetterTileItem(
                        tile = tile,
                        isClickable = true,
                        isSecondary = false,
                        onClick = { onDeselected(tile) }
                    )
                } else {
                    EmptySlotBox()
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun EmptySlotBox() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoolArea(
    scrambledPool: List<LetterTile>,
    isSolved: Boolean,
    onSelected: (LetterTile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (scrambledPool.isEmpty() && !isSolved) {
            Text(
                text = "Tapped all letters! Clear or edit your selection.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scrambledPool.forEach { tile ->
                    LetterTileItem(
                        tile = tile,
                        isClickable = !isSolved,
                        isSecondary = true,
                        onClick = { onSelected(tile) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
fun LetterTileItem(
    tile: LetterTile,
    isClickable: Boolean,
    isSecondary: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSecondary) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (isSecondary) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val tileScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(46.dp)
            .offset { IntOffset(0, 0) }
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(enabled = isClickable) {
                scope.launch {
                    tileScale.animateTo(
                        targetValue = 0.88f,
                        animationSpec = spring(dampingRatio = StepRatio, stiffness = Spring.StiffnessHigh)
                    )
                    tileScale.animateTo(1f)
                }
                onClick()
            }
            .border(1.dp, contentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.char.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor
        )
    }
}

private const val StepRatio = 0.5f

@Composable
fun ActionBar(
    isSolved: Boolean,
    onShuffle: () -> Unit,
    onClear: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Pool
        IconButton(
            onClick = onShuffle,
            enabled = !isSolved,
            modifier = Modifier
                .shadow(1.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .size(48.dp)
                .testTag("shuffle_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Shuffle pool items",
                tint = if (isSolved) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
            )
        }

        // Clear Pool
        Button(
            onClick = onClear,
            enabled = !isSolved,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier
                .height(48.dp)
                .testTag("clear_button")
        ) {
            Text("Clear All", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Buy hint
        Button(
            onClick = onHint,
            enabled = !isSolved,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB300), // Gold
                contentColor = Color.Black
            ),
            modifier = Modifier
                .height(48.dp)
                .testTag("hint_button")
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Hint icon",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Hint (-20c)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun SuccessLevelCard(
    word: String,
    pointsEarned: Int,
    completedCount: Int,
    onNext: () -> Unit,
    onClaimCertificate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(top = 16.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9) // Success green background
        ),
        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Green checkmark success",
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GUESS IS CORRECT!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1B5E20),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target solved: $word",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "+$pointsEarned Points and +20 Hint Coins!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (completedCount >= 1000) {
                Button(
                    onClick = onClaimCertificate,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300), // Gold
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("claim_certificate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Claim certificate"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CLAIM YOUR CERTIFICATE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("You completed the 1000-word challenge!", fontSize = 10.sp, color = Color.Black.copy(alpha = 0.7f))
                    }
                }
            } else {
                val isAdRequired = completedCount > 0 && completedCount % 5 == 0
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdRequired) Color(0xFF1B5E20) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_word_button")
                ) {
                    Icon(
                        imageVector = if (isAdRequired) Icons.Default.PlayArrow else Icons.Default.ArrowForward,
                        contentDescription = if (isAdRequired) "Watch ad to continue" else "Continue to next word"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isAdRequired) {
                            Text("NEXT WORD GATED BY AD", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("Watch compensated ad & collect +50 Coins!", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        } else {
                            Text("CONTINUE TO NEXT WORD", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("Direct access to next level scramble!", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedAdOverlay(
    uiState: GameUiState
) {
    Dialog(
        onDismissRequest = { /* Forbid canceling ad midway */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ad Badge Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPONSOR ADVERTISING",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF333333))
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Reward at 0s",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Interactive Video Box drawing an dynamic audio wave effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111111))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AdWaveformAnimation()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Simulation playing",
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "OFFLINE TEST DEMO PLAYER",
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Funny random ad text
                Text(
                    text = uiState.activeAdMessage,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Unlock levels instantly without subscription costs",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // Interactive countdown action button
                if (uiState.isAdSuccessTriggered) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ad Reward Confirmed",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REWARD EARNED! Unlocking Level...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { uiState.adCountdownSeconds / 5f },
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            trackColor = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Closing in ${uiState.adCountdownSeconds}S",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AdWaveformAnimation() {
    val transitionPhase = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        transitionPhase.animateTo(
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = spring(dampingRatio = 1f, stiffness = 0.8f) // Simulate slow looping
        )
        // Keep moving
        while (true) {
            transitionPhase.snapTo(0f)
            transitionPhase.animateTo(
                targetValue = 2 * Math.PI.toFloat(),
                animationSpec = spring(dampingRatio = 1f, stiffness = 0.8f)
            )
            delay(10)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val waveColor = Color.White.copy(alpha = 0.05f)

        val path = Path()
        path.moveTo(0f, height / 2)

        for (x in 0..width.toInt() step 5) {
            val y = sin(x * 0.01 + transitionPhase.value) * 60 + (height / 2)
            path.lineTo(x.toFloat(), y.toFloat())
        }

        drawPath(
            path = path,
            color = waveColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateFormScreen(
    firstName: String,
    fatherName: String,
    email: String,
    onSubmit: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var nameInput by remember { mutableStateOf(firstName) }
    var fatherInput by remember { mutableStateOf(fatherName) }
    var emailInput by remember { mutableStateOf(email) }

    var nameError by remember { mutableStateOf(false) }
    var fatherError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Achievement Header
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Achievement Ribbon",
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "Congratulations!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "You have successfully answered all the questions!\n\nYou will be issued a certificate from Word Scramble App.\n\nIf you wish to obtain it, fill out this form.",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Name input
        OutlinedTextField(
            value = nameInput,
            onValueChange = {
                nameInput = it
                nameError = it.isBlank()
            },
            label = { Text("Your Full Name") },
            placeholder = { Text("Enter your name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User icon"
                )
            },
            isError = nameError,
            supportingText = {
                if (nameError) {
                    Text("Name is required", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("form_name_input")
        )

        // Father's Name input
        OutlinedTextField(
            value = fatherInput,
            onValueChange = {
                fatherInput = it
                fatherError = it.isBlank()
            },
            label = { Text("Father's Name") },
            placeholder = { Text("Enter father's name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Father icon"
                )
            },
            isError = fatherError,
            supportingText = {
                if (fatherError) {
                    Text("Father's name is required", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("form_father_input")
        )

        // Email input
        OutlinedTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it
                emailError = it.isBlank() || !it.contains("@") || !it.contains(".")
            },
            label = { Text("Email Address") },
            placeholder = { Text("yourname@domain.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email icon"
                )
            },
            isError = emailError,
            supportingText = {
                if (emailError) {
                    Text("Please enter a valid email address", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("form_email_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Button(
            onClick = {
                val hasNameError = nameInput.isBlank()
                val hasFatherError = fatherInput.isBlank()
                val hasEmailError = emailInput.isBlank() || !emailInput.contains("@") || !emailInput.contains(".")

                nameError = hasNameError
                fatherError = hasFatherError
                emailError = hasEmailError

                if (!hasNameError && !hasFatherError && !hasEmailError) {
                    onSubmit(nameInput.trim(), fatherInput.trim(), emailInput.trim())
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_form_button")
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Claim & Email Certificate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CertificatePreviewScreen(
    name: String,
    fatherName: String,
    email: String,
    certId: String,
    isSending: Boolean,
    sendStatus: String,
    selectedPaperSize: String,
    onPaperSizeChanged: (String) -> Unit,
    onSharePdf: () -> Unit,
    onEdit: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Dynamic Status Box during/after simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isSending) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                 else Color(0xFFE8F5E9)
            ),
            border = BorderStroke(1.dp, if (isSending) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else Color(0xFFC8E6C9))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success check icon",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSending) "EMAIL DISPATCH PROGRESS" else "TRANSMISSION DISPATCH SUCCESSFUL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isSending) MaterialTheme.colorScheme.primary else Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sendStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }

        // PAPER SIZE SELECTION CARD (A4/Letter Toggle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CERTIFICATE PRINT FORMAT PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // A4 Option
                    val isA4Selected = selectedPaperSize == "A4"
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPaperSizeChanged("A4") }
                            .testTag("paper_size_a4"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isA4Selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isA4Selected) 2.dp else 1.dp,
                            color = if (isA4Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "A4 Format",
                                tint = if (isA4Selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "A4 Portrait",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isA4Selected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                            )
                            Text(
                                text = "210 × 297 mm",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Letter Option
                    val isLetterSelected = selectedPaperSize == "LETTER"
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPaperSizeChanged("LETTER") }
                            .testTag("paper_size_letter"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLetterSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isLetterSelected) 2.dp else 1.dp,
                            color = if (isLetterSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "US Letter Format",
                                tint = if (isLetterSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "US Letter",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLetterSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                            )
                            Text(
                                text = "8.5 × 11.0 inches",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🖨️ High-Contrast Print-Ready Layout formatted for standard home inkjet/laser printers. Includes vector seals, signatures, and ample margins.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
        }

        // PHYSICAL CERTIFICATE BOARD DRAW-OUT (PORTRAIT ASPECT RATIO)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFAF8F5)) // Elegant traditional warm white certificate stock
                .border(8.dp, Color(0xFF0F1E3D), RoundedCornerShape(16.dp)) // Majestic Navy outer border
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFC5A059), RoundedCornerShape(12.dp)) // Traditional Muted Gold inline border
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Crest emblem / School Symbol (Top Center)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFC5A059))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F1E3D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Royal Academic Crest",
                                tint = Color(0xFFC5A059),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "WORD SCRAMBLE ACADEMIC BOARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F1E3D),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CERTIFICATE OF ACHIEVEMENT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF0F1E3D),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = "HONORING EXCELLENT SPELLING AND VOCABULARY",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC5A059),
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "This certificate is proudly presented to",
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF5A6B7C)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Recipient Name
                    Text(
                        text = name.uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF111111),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Line decoration
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .width(220.dp)
                            .height(1.5.dp)
                            .background(Color(0xFFC5A059))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "for outstanding spelling performance and academic excellence, having successfully completed the 5-word scrambling challenge with great dedication, mental focus, and strong vocabulary skills.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF445566),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // SEALS & SIGNATURE COLUMNS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Vector Ribbon Seal Representation
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Box(contentAlignment = Alignment.TopCenter) {
                                // Draw ribbons dangling behind
                                Canvas(modifier = Modifier.size(width = 44.dp, height = 54.dp)) {
                                    val rPath = Path().apply {
                                        moveTo(size.width * 0.2f, 0f)
                                        lineTo(size.width * 0.1f, size.height * 0.9f)
                                        lineTo(size.width * 0.35f, size.height * 0.8f)
                                        lineTo(size.width * 0.6f, size.height * 0.9f)
                                        lineTo(size.width * 0.5f, 0f)
                                        close()
                                    }
                                    val rPath2 = Path().apply {
                                        moveTo(size.width * 0.5f, 0f)
                                        lineTo(size.width * 0.45f, size.height * 0.95f)
                                        lineTo(size.width * 0.65f, size.height * 0.85f)
                                        lineTo(size.width * 0.9f, size.height * 0.95f)
                                        lineTo(size.width * 0.8f, 0f)
                                        close()
                                    }
                                    drawPath(rPath, Color(0xFFB28E46))
                                    drawPath(rPath2, Color(0xFFB28E46))
                                }
                                // Center Gold Circle
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFC5A059))
                                        .border(1.5.dp, Color(0xFF0F1E3D), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "OFFICIAL SEAL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Digital Signature Holder
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Word Scramble App",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Cursive,
                                color = Color(0xFF0F1E3D)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .width(130.dp)
                                    .height(1.dp)
                                    .background(Color.Gray.copy(alpha = 0.4f))
                            )
                            Text(
                                text = "GOVERNMENT BOARD",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Audit line
                    Text(
                        text = "CERTIFICATE ID: $certId  |  STATUS: SECURE DATABASE VALID  |  FORMAT: $selectedPaperSize",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // EMAIL / SHARE PDF ACTION BUTTON
        Button(
            onClick = onSharePdf,
            enabled = !isSending,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB300), // Elegant gold
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("share_pdf_button")
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Share PDF icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send / Share PDF Certificate", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // EDIT & REPLAY ACTION BUTTONS
        Button(
            onClick = onEdit,
            enabled = !isSending,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("edit_certificate_button")
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Edit details icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Certificate Details", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onPlayAgain,
            enabled = !isSending,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("play_again_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset and replay Scramble"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset & Play Again", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPONSOR ADVERTISEMENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-2241360064247668/9367025607"
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { adView ->
                    try {
                        adView.loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        Log.e("AdmobBanner", "Error updating banner ad", e)
                    }
                }
            )
        }
    }
}

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Privacy Policy",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Last Updated: June 2026",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Text(
                text = "Introduction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Welcome to World Scrambler app. Your privacy is important to us. This privacy policy document outlines the types of personal information received and collected by World Scrambler and how it is used.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Text(
                text = "1. Information We Collect & Use",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "• Personal Details: We collect user names, parents' or guardians' names, and email addresses solely when voluntarily submitted through our Certificate generation form to issue and email accomplishment certificates.\n\n" +
                        "• Technical Data: We utilize standard Google AdMob services which collect device identifiers, IP addresses, and other technical analytics to serve non-personalized and personalized advertisements.\n\n" +
                        "• Local Statistics: The application stores game progress parameters (puzzles solved, high scores, coins) locally on your device via an integrated private database.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Text(
                text = "2. How We Use Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "• To deliver certificate rewards: Your name, father's or guardian's name, and email address are used to instantiate your official PDF excellence certificate and deliver it direct to your inbox.\n\n" +
                        "• To display relevant advertisements: Google AdMob SDK uses cookies, mobile advertiser IDs, and device telemetry to provision personalized or contextual rewards/interstitial/banner advertisements.\n\n" +
                        "• To maintain scoring indices: Your gaming data tracks and displays achievement ranks natively in the application interface.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Text(
                text = "3. AdMob Integration & Advertising Identifier",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "This application integrates the Google Mobile Ads SDK (AdMob) to display Banner and Rewarded Ads. Google may use advertising identifiers to analyze users' gameplay, serve relevant ads, and prevent fraudulent visual activity. You can control or opt out of personalized tracking via your Android operating system settings.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Text(
                text = "4. Children's Privacy Integration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "We actively respect Children's Online Privacy Protection regulations. We do not knowingly collect or store personal parameters from children without parental information or consent. If a student uses the certificate system, we recommend parents/guardians supervise inputs and form credentials.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Text(
                text = "5. Safety & Security of Data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "All certificate transmissions utilize modern, secure network layers. We do not store or share your personal data on external centralized databases, and your local game statistics remain strictly off-grid in your device storage environment.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Text(
                text = "6. Contact Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "If you have any feedback or queries about this Privacy Policy or data safety compliance, please feel free to reach out to us at support@example.com.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("back_to_game_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = "Accept & Return to Game", fontWeight = FontWeight.Bold)
            }
        }
    }
}

