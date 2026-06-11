package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ads.AdManager
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.game.GameViewModel
import com.example.game.GameViewModelFactory
import com.example.ui.WordScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database, Repository, and managers
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.userStatsDao())
        val adManager = AdManager(applicationContext)
        
        // Setup ViewModel
        val factory = GameViewModelFactory(repository, adManager)
        viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WordScreen(viewModel = viewModel)
            }
        }
    }
}
