package com.yonsei.koreanlearner.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.data.local.database.DatabaseInitStatus
import com.yonsei.koreanlearner.data.local.database.DatabaseInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {

    val initStatus: StateFlow<DatabaseInitStatus> = databaseInitializer.status

    init {
        startDatabaseInitialization()
    }

    fun startDatabaseInitialization() {
        viewModelScope.launch {
            databaseInitializer.initialize()
        }
    }
}
