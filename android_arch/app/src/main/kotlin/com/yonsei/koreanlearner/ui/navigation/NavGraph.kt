package com.yonsei.koreanlearner.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yonsei.koreanlearner.core.state.AppStateManager
import com.yonsei.koreanlearner.ui.components.AppNavDestinations
import com.yonsei.koreanlearner.ui.components.KoreanBottomNavBar
import com.yonsei.koreanlearner.ui.screen.explore.ExploreHubScreen
import com.yonsei.koreanlearner.ui.screen.grammar.GrammarTreeScreen
import com.yonsei.koreanlearner.ui.screen.learn.LearnScreen
import com.yonsei.koreanlearner.ui.screen.review.ReviewScreen
import com.yonsei.koreanlearner.ui.screen.wordcard.WordCardScreen

/**
 * ══════════════════════════════════════════════════════════════════
 * 路由常量定义（语法第一 · 单词第二 · 纯类型安全）
 * ══════════════════════════════════════════════════════════════════
 */
sealed class Screen(val route: String) {

    /** 冷启动预置数据校验引导界面 */
    data object Splash : Screen("splash")

    // ── 主 Tab 目的地（对应底部导航栏 4 项）────────────────────────
    /** 语法树核心信息流模块（第一核心） */
    data object GrammarTree : Screen("grammar_tree")

    /** 单词记忆卡片模块（第二核心） */
    data object WordCard : Screen("wordcard") {
        fun createRoute(book: Int = 1, lesson: Int = 1) = "wordcard"
    }

    /** 复习界面（延世 7 轮记忆漏斗 + 智能排程） */
    data object Review : Screen("review")

    /** 拓展研习沙盒枢纽（收纳所有辅助实验室与实战工具） */
    data object Explore : Screen("explore")

    // ── 子页面与专项沙盒 ──────────────────────────────────────────
    data object Learn : Screen("learn")
    data object ErrorList : Screen("error_list")
    data object Profile : Screen("profile")
    data object Numbers : Screen("numbers")
    data object Dictation : Screen("dictation")
    data object HanjaGraph : Screen("hanja_graph")
    data object IdiomModule : Screen("idiom_module")
    data object SyntaxLego : Screen("syntax_lego")
    data object DialogueSandbox : Screen("dialogue_sandbox")
    data object LoanwordModule : Screen("loanword_module")
    data object TopikExam : Screen("topik_exam")
    data object DiscourseMatrix : Screen("discourse_matrix")
    data object SpacingCorrection : Screen("spacing_correction")
    data object IntonationTrainer : Screen("intonation_trainer")
    data object HangulStroke : Screen("hangul_stroke")
    data object PhonologyLab : Screen("phonology_lab")
    data object SpeechLevelSandbox : Screen("speech_level_sandbox")
    data object TopikEssayGrader : Screen("topik_essay_grader")
    data object SlangCorpus : Screen("slang_corpus")
    data object KMediaCorpus : Screen("kmedia_corpus")
    data object TypingMaster : Screen("typing_master")
    data object GradedReader : Screen("graded_reader")
    data object KoreanLens : Screen("korean_lens")
    data object WordChain : Screen("word_chain")
    data object BackgroundRadio : Screen("background_radio")
    data object ShadowingTrainer : Screen("shadowing_trainer")
    data object UniversalSearch : Screen("universal_search")
    data object LearningAnalytics : Screen("learning_analytics")
}

// ── 底部导航栏常驻主 Tab 集合 ──────────────────────────────────────
private val bottomNavRoutes = setOf(
    Screen.GrammarTree.route,
    Screen.WordCard.route,
    Screen.Review.route,
    Screen.Explore.route
)

/**
 * 应用全局 Compose 导航图（语法第一 · 单词第二 · 全局状态记忆恢复）
 */
@Composable
fun KoreanLearnerNavGraph(
    appStateManager: AppStateManager,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    // 判断当前页面是否显示底部导航栏
    val showBottomBar = currentRoute?.let { route ->
        bottomNavRoutes.any { route.startsWith(it) }
    } ?: false

    // 记忆保存：每当主 Tab 切换时，实时同步至本地持久化存储
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            val cleanRoute = route.substringBefore("?")
            if (bottomNavRoutes.contains(cleanRoute)) {
                appStateManager.setLastRoute(cleanRoute)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        bottomBar = {
            if (showBottomBar) {
                KoreanBottomNavBar(
                    destinations  = AppNavDestinations,
                    selectedRoute = when {
                        currentRoute?.startsWith("grammar_tree") == true -> "grammar_tree"
                        currentRoute?.startsWith("wordcard")     == true -> "wordcard"
                        currentRoute?.startsWith("review")       == true -> "review"
                        currentRoute?.startsWith("explore")      == true -> "explore"
                        else -> "grammar_tree"
                    },
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            appStateManager.setLastRoute(route)
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->

        // 默认恢复上次离开的页面（默认首选语法，0延迟直接启动）
        val startDest = remember {
            val saved = appStateManager.getLastRoute()
            if (bottomNavRoutes.contains(saved)) saved else Screen.GrammarTree.route
        }

        NavHost(
            navController    = navController,
            startDestination = startDest,
            enterTransition  = { fadeIn(tween(100)) },
            exitTransition   = { fadeOut(tween(100)) },
            popEnterTransition = { fadeIn(tween(100)) },
            popExitTransition  = { fadeOut(tween(100)) },
            modifier         = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(padding)
        ) {

            // ── 1. 语法核心树（第一核心主 Tab）─────────────────────
            composable(Screen.GrammarTree.route) {
                GrammarTreeScreen()
            }

            // ── 2. 单词记忆流（第二核心主 Tab）─────────────────────
            composable(Screen.WordCard.route) {
                WordCardScreen(
                    book   = appStateManager.getLastWordBook(),
                    lesson = appStateManager.getLastWordLesson(),
                    onBack = { navController.popBackStack() }
                )
            }

            // ── 3. 复习界面（第三核心主 Tab）───────────────────────
            composable(Screen.Review.route) {
                ReviewScreen(
                    onSessionComplete = {
                        navController.navigate(Screen.GrammarTree.route) {
                            popUpTo(Screen.Review.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── 4. 拓展沙盒枢纽（第四主 Tab）───────────────────────
            composable(Screen.Explore.route) {
                ExploreHubScreen(
                    appStateManager = appStateManager,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    }
                )
            }

            // ── 今日新词学习流程 ─────────────────────────────────
            composable(Screen.Learn.route) {
                LearnScreen(
                    onNavigateToReview = {
                        navController.navigate(Screen.Review.route) {
                            popUpTo(Screen.Learn.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── 各专项沙盒与拓展路由 ─────────────────────────────
            composable(Screen.KoreanLens.route) {
                com.yonsei.koreanlearner.ui.screen.lens.KoreanLensScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UniversalSearch.route) {
                com.yonsei.koreanlearner.ui.screen.search.UniversalSearchScreen(
                    onNavigateToRoute = { route ->
                        navController.navigate(route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LearningAnalytics.route) {
                com.yonsei.koreanlearner.ui.screen.analytics.LearningAnalyticsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Dictation.route) {
                com.yonsei.koreanlearner.ui.screen.dictation.LyricalDictationScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BackgroundRadio.route) {
                com.yonsei.koreanlearner.ui.screen.radio.BackgroundRadioScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.WordChain.route) {
                com.yonsei.koreanlearner.ui.screen.game.WordChainGameScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PhonologyLab.route) {
                com.yonsei.koreanlearner.ui.screen.phonology.PhonologyLabScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SpeechLevelSandbox.route) {
                com.yonsei.koreanlearner.ui.screen.honorific.SpeechLevelSandboxScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SpacingCorrection.route) {
                com.yonsei.koreanlearner.ui.screen.spacing.SpacingCorrectionScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.HangulStroke.route) {
                com.yonsei.koreanlearner.ui.screen.stroke.HangulStrokeScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ShadowingTrainer.route) {
                com.yonsei.koreanlearner.ui.screen.pitch.ShadowingTrainerScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TopikEssayGrader.route) {
                com.yonsei.koreanlearner.ui.screen.essay.TopikEssayGraderScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TopikExam.route) {
                com.yonsei.koreanlearner.ui.screen.topik.TopikExamScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DialogueSandbox.route) {
                com.yonsei.koreanlearner.ui.screen.dialogue.SituationalDialogueScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LoanwordModule.route) {
                com.yonsei.koreanlearner.ui.screen.loanword.LoanwordComparisonScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.IdiomModule.route) {
                com.yonsei.koreanlearner.ui.screen.idiom.IdiomModuleScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GradedReader.route) {
                com.yonsei.koreanlearner.ui.screen.reading.GradedReaderScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
