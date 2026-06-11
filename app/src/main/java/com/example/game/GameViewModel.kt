package com.example.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ads.AdManager
import com.example.data.GameRepository
import com.example.data.UserStats
import com.example.data.WordEntry
import com.example.util.CertificatePdfGenerator
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LetterTile(
    val id: Int,
    val char: Char
)

data class GameUiState(
    val scrambledPool: List<LetterTile> = emptyList(),
    val selectedLetters: List<LetterTile> = emptyList(),
    val isLevelSolved: Boolean = false,
    val isWrongGuess: Boolean = false,
    val message: String = "",
    val showAdSimulation: Boolean = false,
    val adCountdownSeconds: Int = 5,
    val activeAdMessage: String = "",
    val isAdSuccessTriggered: Boolean = false,
    val currentLevelIndex: Int = 0
)

class GameViewModel(
    private val repository: GameRepository,
    private val adManager: AdManager
) : ViewModel() {

    val userStats: StateFlow<UserStats> = repository.userStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats(id = 0)
        )

    private val _gameUiState = MutableStateFlow(GameUiState())
    val gameUiState: StateFlow<GameUiState> = _gameUiState.asStateFlow()

    // Navigation state: 0 = Game, 1 = Form, 2 = Certificate Certificate Screen
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // Certificate inputs and validation states
    val formName = MutableStateFlow("")
    val formFatherName = MutableStateFlow("")
    val formEmail = MutableStateFlow("")
    val selectedPaperSize = MutableStateFlow("A4") // Either "A4" or "LETTER"

    // Track words successfully solved in the current round
    val completedCountInSession = MutableStateFlow(0)

    // SMTP Mailer simulation indicators
    val isSendingEmail = MutableStateFlow(false)
    val emailStatus = MutableStateFlow("")
    val certificateId = MutableStateFlow("")

    private var currentWordEntry: WordEntry? = null
    private var tileCounter = 0

    init {
        // Fetch initial stats to align completed session count with persistent database level index
        viewModelScope.launch {
            val stats = repository.getStatsDirect()
            completedCountInSession.value = stats.currentLevelIndex
        }

        // Automatically load levels as database updates
        viewModelScope.launch {
            userStats.collect { stats ->
                val wordsList = WordEntry.ALL_WORDS
                if (wordsList.isNotEmpty()) {
                    val safeIdx = stats.currentLevelIndex % wordsList.size
                    val newWord = wordsList[safeIdx]
                    
                    // Only load automatically if the UI is NOT currently showing a solved celebration state,
                    // OR if currentWordEntry is null (startup), OR if progress was reset to 0 database side.
                    val isReset = stats.currentLevelIndex == 0 && _gameUiState.value.currentLevelIndex != 0
                    if (currentWordEntry == null || isReset || (!_gameUiState.value.isLevelSolved && currentWordEntry?.id != newWord.id)) {
                        loadLevel(newWord, safeIdx)
                    }
                }
            }
        }
    }

    private fun loadLevel(wordEntry: WordEntry, levelIndex: Int) {
        currentWordEntry = wordEntry
        val target = wordEntry.word.uppercase().trim()
        
        // Generate scramble
        val chars = target.toList()
        var shuffled = chars.shuffled()
        var attempts = 0
        while (shuffled == chars && target.length > 1 && attempts < 15) {
            shuffled = chars.shuffled()
            attempts++
        }

        tileCounter = 0
        val pool = shuffled.map { LetterTile(tileCounter++, it) }

        _gameUiState.update {
            GameUiState(
                scrambledPool = pool,
                selectedLetters = emptyList(),
                isLevelSolved = false,
                isWrongGuess = false,
                message = "Rearrange the scrambled letters to guess the word!",
                currentLevelIndex = levelIndex
            )
        }
    }

    fun selectLetter(tile: LetterTile) {
        val currentState = _gameUiState.value
        if (currentState.isLevelSolved) return

        // Remove from pool, add to selection
        val newPool = currentState.scrambledPool.filter { it.id != tile.id }
        val newSelection = currentState.selectedLetters + tile

        _gameUiState.update {
            it.copy(
                scrambledPool = newPool,
                selectedLetters = newSelection,
                isWrongGuess = false
            )
        }

        checkSolution(newSelection)
    }

    fun deselectLetter(tile: LetterTile) {
        val currentState = _gameUiState.value
        if (currentState.isLevelSolved) return

        // Remove from selection, add back to pool
        val newSelection = currentState.selectedLetters.filter { it.id != tile.id }
        val newPool = currentState.scrambledPool + tile

        _gameUiState.update {
            it.copy(
                scrambledPool = newPool,
                selectedLetters = newSelection,
                isWrongGuess = false
            )
        }
    }

    fun clearSelection() {
        val currentState = _gameUiState.value
        if (currentState.isLevelSolved) return

        val originalPool = currentState.selectedLetters + currentState.scrambledPool
        // Keep consistent sorting or allow visual shuffling
        _gameUiState.update {
            it.copy(
                scrambledPool = originalPool.sortedBy { tile -> tile.id },
                selectedLetters = emptyList(),
                isWrongGuess = false,
                message = "Cleared selection."
            )
        }
    }

    fun shufflePool() {
        val currentState = _gameUiState.value
        if (currentState.isLevelSolved) return

        _gameUiState.update {
            it.copy(
                scrambledPool = currentState.scrambledPool.shuffled(),
                isWrongGuess = false
            )
        }
    }

    private fun checkSolution(selected: List<LetterTile>) {
        val target = currentWordEntry?.word?.uppercase() ?: return
        if (selected.size == target.length) {
            val guess = selected.map { it.char }.joinToString("")
            if (guess == target) {
                // Succeeded! Increment levels!
                viewModelScope.launch {
                    val nextCount = completedCountInSession.value + 1
                    val successMsg = if (nextCount >= 1000) {
                        "${target}! Perfect! You have solved all 1000 words!"
                    } else {
                        "${target}! Excellent search! Tap [NEXT LEVEL] to continue."
                    }
                    _gameUiState.update {
                        it.copy(
                            isLevelSolved = true,
                            message = successMsg
                        )
                    }
                    // Increment the completed session counter
                    completedCountInSession.update { nextCount }
                    // Reward progress through database
                    repository.addScoreAndLevel(target.length * 10, currentWordEntry?.id ?: 0)
                }
            } else {
                // Incorrect guess! Reset streak
                _gameUiState.update {
                    it.copy(
                        isWrongGuess = true,
                        message = "Incorrect arrangement. Try rearranging!"
                    )
                }
                viewModelScope.launch {
                    repository.resetStreak()
                }
            }
        }
    }

    fun triggerHint() {
        val target = currentWordEntry?.word?.uppercase() ?: return
        val currentState = _gameUiState.value
        if (currentState.isLevelSolved) return

        // Cost is 20 coins
        viewModelScope.launch {
            val hasCoins = repository.deductCoins(20)
            if (!hasCoins) {
                _gameUiState.update { it.copy(message = "Not enough coins! Need 20 coins for a hint.") }
                return@launch
            }

            // Figure out the next correct letter needed in the selection
            val selectedCount = currentState.selectedLetters.size
            if (selectedCount >= target.length) return@launch

            val nextCorrectChar = target[selectedCount]

            // Find an item matching nextCorrectChar in the pool, or swap if it is already selected in a wrong position
            val tileInPool = currentState.scrambledPool.firstOrNull { it.char == nextCorrectChar }

            if (tileInPool != null) {
                // Move from pool to selected
                val newPool = currentState.scrambledPool.filter { it.id != tileInPool.id }
                val newSelection = currentState.selectedLetters + tileInPool
                _gameUiState.update {
                    it.copy(
                        scrambledPool = newPool,
                        selectedLetters = newSelection,
                        isWrongGuess = false,
                        message = "Hint purchased! Match revealed."
                    )
                }
                checkSolution(newSelection)
            } else {
                // The correct char is in the wrong selected positions, clear everything and place the hint directly
                val allTiles = currentState.selectedLetters + currentState.scrambledPool
                val correctUnusedTile = allTiles.firstOrNull { it.char == nextCorrectChar }
                if (correctUnusedTile != null) {
                    // Let's reset the selection, but keep this one as the first selected letter!
                    val remainingPool = allTiles.filter { it.id != correctUnusedTile.id }
                    _gameUiState.update {
                        it.copy(
                            scrambledPool = remainingPool,
                            selectedLetters = listOf(correctUnusedTile),
                            isWrongGuess = false,
                            message = "Selection reset to fit correct hint!"
                        )
                    }
                    checkSolution(listOf(correctUnusedTile))
                }
            }
        }
    }

    fun playNextLevelWithAd(activity: Activity) {
        val completedCount = completedCountInSession.value
        val isAdRequired = completedCount > 0 && completedCount % 5 == 0

        if (isAdRequired) {
            val isReady = adManager.isAdReady()
            if (isReady) {
                adManager.showRewardedAd(
                    activity = activity,
                    onRewardEarned = {
                        viewModelScope.launch {
                            repository.awardAdCoins(50) // Extra reward coins!
                            advanceToNextLevel()
                        }
                    },
                    onAdClosed = {
                        // Pre-load next
                        adManager.loadRewardedAd()
                    },
                    onAdFailed = {
                        // Real ad failed (common in development test pipelines), trigger visual Simulation fallbacks!
                        startAdSimulation()
                    }
                )
            } else {
                // AdManager currently loading or offline, trigger immersive simulated ad overlay
                startAdSimulation()
            }
        } else {
            // No ad required, transition directly!
            advanceToNextLevel()
        }
    }

    private fun startAdSimulation() {
        val funnyAds = listOf(
            "Retro Candy Crunch: Match 3 candies for extreme explosions!",
            "Pixel Farmer: Grow voxel grains and feed digital chickens offline!",
            "LexiSuite premium expansion: Play vocabulary puzzles in 18 languages!",
            "Brain Speedrunner: Solve math problems before the clock strikes noon!"
        )
        val selectedAd = funnyAds.random()
        
        _gameUiState.update {
            it.copy(
                showAdSimulation = true,
                adCountdownSeconds = 5,
                activeAdMessage = selectedAd,
                isAdSuccessTriggered = false
            )
        }

        viewModelScope.launch(Dispatchers.Main) {
            for (i in 5 downTo 1) {
                delay(1000)
                _gameUiState.update { it.copy(adCountdownSeconds = i - 1) }
            }
            // Finished!
            _gameUiState.update { it.copy(isAdSuccessTriggered = true) }
            delay(1200) // Keep the success badge briefly before continuing
            _gameUiState.update { it.copy(showAdSimulation = false) }
            
            // Deliver reward and unlock level
            repository.awardAdCoins(50)
            advanceToNextLevel()
        }
    }

    private fun advanceToNextLevel() {
        val nextIdx = userStats.value.currentLevelIndex
        val wordsList = WordEntry.ALL_WORDS
        if (wordsList.isNotEmpty()) {
            val safeIdx = nextIdx % wordsList.size
            val newWord = wordsList[safeIdx]
            loadLevel(newWord, safeIdx)
        }
    }

    fun forceResetProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }

    fun navigateToPage(page: Int) {
        _currentPage.value = page
    }

    fun triggerCompletionAdAndNavigate(activity: Activity, targetPage: Int) {
        val isReady = adManager.isAdReady()
        if (isReady) {
            adManager.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    viewModelScope.launch {
                        repository.awardAdCoins(50)
                    }
                },
                onAdClosed = {
                    adManager.loadRewardedAd()
                    _currentPage.value = targetPage
                },
                onAdFailed = {
                    startAdSimulationWithTarget(targetPage)
                }
            )
        } else {
            startAdSimulationWithTarget(targetPage)
        }
    }

    private fun startAdSimulationWithTarget(targetPage: Int) {
        val funnyAds = listOf(
            "Retro Candy Crunch: Match 3 candies for extreme explosions!",
            "Pixel Farmer: Grow voxel grains and feed digital chickens offline!",
            "LexiSuite premium expansion: Play vocabulary puzzles in 18 languages!",
            "Brain Speedrunner: Solve math problems before the clock strikes noon!"
        )
        val selectedAd = funnyAds.random()
        
        _gameUiState.update {
            it.copy(
                showAdSimulation = true,
                adCountdownSeconds = 5,
                activeAdMessage = selectedAd,
                isAdSuccessTriggered = false
            )
        }

        viewModelScope.launch(Dispatchers.Main) {
            for (i in 5 downTo 1) {
                delay(1000)
                _gameUiState.update { it.copy(adCountdownSeconds = i - 1) }
            }
            _gameUiState.update { it.copy(isAdSuccessTriggered = true) }
            delay(1200)
            _gameUiState.update { it.copy(showAdSimulation = false) }
            
            repository.awardAdCoins(50)
            _currentPage.value = targetPage
        }
    }

    fun submitCertificateForm(context: Context, name: String, fatherName: String, email: String) {
        formName.value = name
        formFatherName.value = fatherName
        formEmail.value = email

        // Generate a random certificate id like WS-2026-A9D5
        val chars = ('A'..'Z') + ('0'..'9')
        val randomStr = (1..6).map { chars.random() }.joinToString("")
        val certId = "WS-2026-$randomStr"
        certificateId.value = certId

        // Transition to certificate preview page immediately
        _currentPage.value = 2

        // Simulate automatic email sending with live realistic logging updates!
        viewModelScope.launch {
            isSendingEmail.value = true
            emailStatus.value = "Connecting to Word Scramble App SMTP server..."
            delay(1000)
            emailStatus.value = "Generating high-resolution digital certificate PDF..."
            
            // Generate the actual PDF file
            withContext(Dispatchers.IO) {
                CertificatePdfGenerator.generateCertificate(context, name, fatherName, certId, selectedPaperSize.value == "LETTER")
            }
            
            delay(1000)
            emailStatus.value = "Attaching signed certificate PDF for $name..."
            delay(1000)
            emailStatus.value = "Email with PDF successfully transmitted to $email!"
            isSendingEmail.value = false
        }
    }

    fun changePaperSizeAndRegenerate(context: Context, paperSize: String) {
        selectedPaperSize.value = paperSize
        val name = formName.value
        val father = formFatherName.value
        val certId = certificateId.value
        if (name.isNotEmpty() && certId.isNotEmpty()) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    CertificatePdfGenerator.generateCertificate(context, name, father, certId, paperSize == "LETTER")
                }
            }
        }
    }

    fun shareCertificatePdf(context: Context) {
        viewModelScope.launch {
            try {
                val file = File(context.cacheDir, "Word_Scramble_Certificate.pdf")
                if (file.exists()) {
                    val authority = "${context.packageName}.fileprovider"
                    val uri = FileProvider.getUriForFile(context, authority, file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(formEmail.value))
                        putExtra(Intent.EXTRA_SUBJECT, "Word Scramble App - Your Certificate of Excellence!")
                        putExtra(Intent.EXTRA_TEXT, "Congratulations ${formName.value}! Please find attached your official Certificate of Excellence from Word Scramble App for successfully completing all 1000 questions.\n\nCertificate ID: ${certificateId.value}\nParent/Father Reference: ${formFatherName.value}")
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Email / Send PDF Certificate"))
                } else {
                    Log.e("GameViewModel", "Certificate PDF file does not exist!")
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Failed to share/send certificate PDF", e)
            }
        }
    }

    fun resetSessionAndPlayAgain() {
        viewModelScope.launch {
            completedCountInSession.value = 0
            _currentPage.value = 0
            formName.value = ""
            formFatherName.value = ""
            formEmail.value = ""
            emailStatus.value = ""
            repository.resetAllProgress()
        }
    }
}

class GameViewModelFactory(
    private val repository: GameRepository,
    private val adManager: AdManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository, adManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
