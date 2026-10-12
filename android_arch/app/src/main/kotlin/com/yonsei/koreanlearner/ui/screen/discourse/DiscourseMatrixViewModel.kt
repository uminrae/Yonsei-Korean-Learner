package com.yonsei.koreanlearner.ui.screen.discourse

import androidx.lifecycle.ViewModel
import com.yonsei.koreanlearner.algorithm.korean.DiscourseCategory
import com.yonsei.koreanlearner.algorithm.korean.DiscourseMarker
import com.yonsei.koreanlearner.algorithm.korean.DiscourseMarkerDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DiscourseMatrixUiState(
    val selectedCategory : DiscourseCategory? = null, // null 表示全部
    val markers          : List<DiscourseMarker> = DiscourseMarkerDatabase.MARKERS
)

@HiltViewModel
class DiscourseMatrixViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DiscourseMatrixUiState())
    val state: StateFlow<DiscourseMatrixUiState> = _state.asStateFlow()

    fun selectCategory(category: DiscourseCategory?) {
        _state.update {
            val list = if (category == null) {
                DiscourseMarkerDatabase.MARKERS
            } else {
                DiscourseMarkerDatabase.MARKERS.filter { m -> m.category == category }
            }
            it.copy(selectedCategory = category, markers = list)
        }
    }
}
