package com.yonsei.koreanlearner.ui.screen.hanja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.algorithm.MemoryScheduler
import com.yonsei.koreanlearner.algorithm.korean.DerivedHanjaWord
import com.yonsei.koreanlearner.algorithm.korean.HanjaNetworkEngine
import com.yonsei.koreanlearner.algorithm.korean.HanjaRadialNetwork
import com.yonsei.koreanlearner.algorithm.korean.HanjaRoot
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HanjaGraphUiState(
    val searchQuery          : String = "",
    val availableRoots       : List<HanjaRoot> = HanjaNetworkEngine.ROOTS_DATABASE,
    val selectedRoot         : HanjaRoot = HanjaNetworkEngine.ROOTS_DATABASE[0], // 默认 '학(學)'
    val currentNetwork       : HanjaRadialNetwork = HanjaNetworkEngine.getNetworkByRoot('학'),
    val selectedDerivedWord  : DerivedHanjaWord? = null,
    val isAddedToVocabulary  : Boolean = false
)

@HiltViewModel
class HanjaGraphViewModel @Inject constructor(
    private val reviewRecordDao: ReviewRecordDao
) : ViewModel() {

    private val _state = MutableStateFlow(HanjaGraphUiState())
    val state: StateFlow<HanjaGraphUiState> = _state.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        val filtered = HanjaNetworkEngine.searchHanja(query)
        _state.update {
            it.copy(
                searchQuery = query,
                availableRoots = filtered
            )
        }
    }

    fun selectRoot(root: HanjaRoot) {
        val network = HanjaNetworkEngine.getNetworkByRoot(root.koreanChar)
        _state.update {
            it.copy(
                selectedRoot = root,
                currentNetwork = network,
                selectedDerivedWord = network.derivedWords.firstOrNull(),
                isAddedToVocabulary = false
            )
        }
    }

    fun selectDerivedWord(word: DerivedHanjaWord) {
        _state.update {
            it.copy(
                selectedDerivedWord = word,
                isAddedToVocabulary = false
            )
        }
    }

    fun addToVocabulary(word: DerivedHanjaWord) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = MemoryScheduler.createInitialRecord(word.id, "WORD")
            reviewRecordDao.upsert(record)
            _state.update { it.copy(isAddedToVocabulary = true) }
        }
    }
}
