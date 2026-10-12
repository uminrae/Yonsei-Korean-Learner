package com.yonsei.koreanlearner.ui.screen.loanword

import androidx.lifecycle.ViewModel
import com.yonsei.koreanlearner.data.local.database.LoanwordSeedData
import com.yonsei.koreanlearner.data.local.entity.LoanwordEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoanwordUiState(
    val selectedCategory  : String? = null,         // null 表示全部
    val loanwords         : List<LoanwordEntity> = LoanwordSeedData.PRESET_LOANWORDS,
    val searchQuery       : String = ""
)

@HiltViewModel
class LoanwordViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LoanwordUiState())
    val state: StateFlow<LoanwordUiState> = _state.asStateFlow()

    fun selectCategory(category: String?) {
        _state.update {
            val filtered = if (category == null) {
                LoanwordSeedData.PRESET_LOANWORDS
            } else {
                LoanwordSeedData.PRESET_LOANWORDS.filter { lw -> lw.category == category }
            }
            it.copy(selectedCategory = category, loanwords = filtered)
        }
    }

    fun searchLoanwords(query: String) {
        val q = query.trim()
        _state.update {
            val filtered = if (q.isBlank()) {
                if (it.selectedCategory == null) LoanwordSeedData.PRESET_LOANWORDS
                else LoanwordSeedData.PRESET_LOANWORDS.filter { lw -> lw.category == it.selectedCategory }
            } else {
                LoanwordSeedData.PRESET_LOANWORDS.filter { lw ->
                    lw.loanword.contains(q) || lw.foreignOrigin.contains(q, ignoreCase = true) ||
                    lw.nativeKoreanEquivalent.contains(q) || lw.chineseMeaning.contains(q)
                }
            }
            it.copy(searchQuery = query, loanwords = filtered)
        }
    }
}
