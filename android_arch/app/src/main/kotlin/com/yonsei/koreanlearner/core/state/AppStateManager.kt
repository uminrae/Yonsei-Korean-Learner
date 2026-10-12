package com.yonsei.koreanlearner.core.state

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ══════════════════════════════════════════════════════════════════
 * 应用全局状态与上次学习进度持久化管理器 (AppStateManager)
 * 记录并恢复用户最后停留的：
 * 1. 主页面路由 (grammar_tree, wordcard, review, explore)
 * 2. 语法树：册数、课次、每课折叠/展开状态、当前打开的语法详情弹窗、详情页Tab、列表滚动位置
 * 3. 单词卡片：册数、课次、当前卡片索引
 * 4. 拓展沙盒：分类索引
 * ══════════════════════════════════════════════════════════════════
 */
@Singleton
class AppStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("yonsei_app_state", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_ROUTE = "key_last_route"
        private const val KEY_GRAMMAR_BOOK = "key_grammar_book"
        private const val KEY_GRAMMAR_LESSON = "key_grammar_lesson"
        private const val KEY_GRAMMAR_SELECTED_PATTERN = "key_grammar_selected_pattern"
        private const val KEY_GRAMMAR_DETAIL_TAB = "key_grammar_detail_tab"
        private const val KEY_GRAMMAR_SCROLL_INDEX = "key_grammar_scroll_index"
        private const val KEY_GRAMMAR_SCROLL_OFFSET = "key_grammar_scroll_offset"

        private const val KEY_WORD_BOOK = "key_word_book"
        private const val KEY_WORD_LESSON = "key_word_lesson"
        private const val KEY_WORD_INDEX = "key_word_index"

        private const val KEY_EXPLORE_CATEGORY = "key_explore_category"

        const val DEFAULT_ROUTE = "grammar_tree" // 默认首屏为语法
    }

    /** 1. 主页面路由记忆 */
    fun getLastRoute(): String {
        return prefs.getString(KEY_LAST_ROUTE, DEFAULT_ROUTE) ?: DEFAULT_ROUTE
    }

    fun setLastRoute(route: String) {
        prefs.edit().putString(KEY_LAST_ROUTE, route).apply()
    }

    /** 2. 语法模块记忆 */
    fun getLastGrammarBook(): Int {
        return prefs.getInt(KEY_GRAMMAR_BOOK, 1)
    }

    fun setLastGrammarBook(book: Int) {
        prefs.edit().putInt(KEY_GRAMMAR_BOOK, book).apply()
    }

    fun getLastGrammarLesson(): Int {
        return prefs.getInt(KEY_GRAMMAR_LESSON, 1)
    }

    fun setLastGrammarLesson(lesson: Int) {
        prefs.edit().putInt(KEY_GRAMMAR_LESSON, lesson).apply()
    }

    /** 每课折叠/展开状态记忆 (默认为第1课展开，其他默认可按用户操作持久化) */
    fun isLessonExpanded(book: Int, lesson: Int): Boolean {
        val key = "key_grammar_expanded_${book}_${lesson}"
        return if (prefs.contains(key)) {
            prefs.getBoolean(key, true)
        } else {
            // 首次默认：第1课展开，后续课可折叠或展开
            true
        }
    }

    fun setLessonExpanded(book: Int, lesson: Int, expanded: Boolean) {
        prefs.edit().putBoolean("key_grammar_expanded_${book}_${lesson}", expanded).apply()
    }

    /** 语法详情弹窗记忆 */
    fun getLastSelectedGrammarPattern(): String {
        return prefs.getString(KEY_GRAMMAR_SELECTED_PATTERN, "") ?: ""
    }

    fun setLastSelectedGrammarPattern(pattern: String) {
        prefs.edit().putString(KEY_GRAMMAR_SELECTED_PATTERN, pattern).apply()
    }

    fun clearLastSelectedGrammarPattern() {
        prefs.edit().remove(KEY_GRAMMAR_SELECTED_PATTERN).apply()
    }

    fun getLastGrammarDetailTab(): Int {
        return prefs.getInt(KEY_GRAMMAR_DETAIL_TAB, 0)
    }

    fun setLastGrammarDetailTab(tabIndex: Int) {
        prefs.edit().putInt(KEY_GRAMMAR_DETAIL_TAB, tabIndex).apply()
    }

    /** 语法列表滚动位置 */
    fun getGrammarScrollIndex(): Int = prefs.getInt(KEY_GRAMMAR_SCROLL_INDEX, 0)
    fun getGrammarScrollOffset(): Int = prefs.getInt(KEY_GRAMMAR_SCROLL_OFFSET, 0)
    fun setGrammarScroll(index: Int, offset: Int) {
        prefs.edit()
            .putInt(KEY_GRAMMAR_SCROLL_INDEX, index)
            .putInt(KEY_GRAMMAR_SCROLL_OFFSET, offset)
            .apply()
    }

    /** 语法星标收藏夹 */
    fun getStarredGrammars(): Set<String> {
        return prefs.getStringSet("key_grammar_starred_set", emptySet()) ?: emptySet()
    }

    fun isGrammarStarred(pattern: String): Boolean {
        return getStarredGrammars().contains(pattern)
    }

    fun toggleStarredGrammar(pattern: String): Boolean {
        val current = getStarredGrammars().toMutableSet()
        val isNowStarred = if (current.contains(pattern)) {
            current.remove(pattern)
            false
        } else {
            current.add(pattern)
            true
        }
        prefs.edit().putStringSet("key_grammar_starred_set", current).apply()
        return isNowStarred
    }

    /** 语法是否只查看星标收藏 */
    fun isStarredOnlyFilter(): Boolean = prefs.getBoolean("key_grammar_filter_starred_only", false)
    fun setStarredOnlyFilter(enabled: Boolean) {
        prefs.edit().putBoolean("key_grammar_filter_starred_only", enabled).apply()
    }

    /** 语法韩文字母首字母索引 (如 "ㄱ", "ㄴ", 或 "" 代表全部) */
    fun getLastHangulInitial(): String = prefs.getString("key_grammar_hangul_initial", "") ?: ""
    fun setLastHangulInitial(initial: String) {
        prefs.edit().putString("key_grammar_hangul_initial", initial).apply()
    }

    /** 3. 单词模块记忆 */
    fun getLastWordBook(): Int {
        return prefs.getInt(KEY_WORD_BOOK, 1)
    }

    fun setLastWordBook(book: Int) {
        prefs.edit().putInt(KEY_WORD_BOOK, book).apply()
    }

    fun getLastWordLesson(): Int {
        return prefs.getInt(KEY_WORD_LESSON, 1)
    }

    fun setLastWordLesson(lesson: Int) {
        prefs.edit().putInt(KEY_WORD_LESSON, lesson).apply()
    }

    fun getLastWordIndex(): Int {
        return prefs.getInt(KEY_WORD_INDEX, 0)
    }

    fun setLastWordIndex(index: Int) {
        prefs.edit().putInt(KEY_WORD_INDEX, index).apply()
    }

    /** 4. 拓展沙盒记忆 */
    fun getLastExploreCategory(): Int {
        return prefs.getInt(KEY_EXPLORE_CATEGORY, 0)
    }

    fun setLastExploreCategory(index: Int) {
        prefs.edit().putInt(KEY_EXPLORE_CATEGORY, index).apply()
    }
}
