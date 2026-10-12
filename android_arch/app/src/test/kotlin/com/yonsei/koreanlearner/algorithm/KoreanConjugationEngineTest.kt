package com.yonsei.koreanlearner.algorithm.korean

import org.junit.Assert.*
import org.junit.Test

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语谓词变音与变形算法单元测试套件（覆盖 7 大不规则与全部特例）
 * ══════════════════════════════════════════════════════════════════
 */
class KoreanConjugationEngineTest {

    // ── 1. Hangul Unicode 底层拆解与组装测试 ────────────────────

    @Test
    fun testHangulDecompositionAndComposition() {
        val decGa = HangulJamo.decompose('가')
        assertEquals('ㄱ', decGa.cho)
        assertEquals('ㅏ', decGa.jung)
        assertEquals('\u0000', decGa.jong)
        assertFalse(decGa.hasJongseong)
        assertTrue(decGa.isYangVowel)
        assertEquals('가', HangulJamo.compose(decGa.cho, decGa.jung, decGa.jong))

        val decHang = HangulJamo.decompose('한')
        assertEquals('ㅎ', decHang.cho)
        assertEquals('ㅏ', decHang.jung)
        assertEquals('ㄴ', decHang.jong)
        assertTrue(decHang.hasJongseong)
        assertEquals('한', HangulJamo.compose(decHang.cho, decHang.jung, decHang.jong))

        val decDul = HangulJamo.decompose('둘')
        assertEquals('ㄷ', decDul.cho)
        assertEquals('ㅜ', decDul.jung)
        assertEquals('ㄹ', decDul.jong)
        assertFalse(decDul.isYangVowel) // 阴性元音
        assertEquals('둘', HangulJamo.compose(decDul.cho, decDul.jung, decDul.jong))
    }

    // ── 2. ㄷ 不规则测试（걷다, 듣다 vs 닫다, 받다, 믿다）───────

    @Test
    fun testDIrregular() {
        // 걷다 -> 걸어요, 걸었습니다, 걸으면, 걷습니다
        val r1 = KoreanConjugationEngine.conjugate("걷다", "-아/어요")
        assertEquals("걸어요", r1.conjugatedText)
        assertEquals(IrregularType.D_IRREGULAR, r1.appliedIrregular)

        val r2 = KoreanConjugationEngine.conjugate("걷다", "-았/었어요")
        assertEquals("걸었어요", r2.conjugatedText)

        val r3 = KoreanConjugationEngine.conjugate("걷다", "-(으)면")
        assertEquals("걸으면", r3.conjugatedText)

        val r4 = KoreanConjugationEngine.conjugate("걷다", "-(으)ㅂ니다")
        assertEquals("걷습니다", r4.conjugatedText) // 遇到辅音保持 ㄷ 收音接 습니다

        // 듣다 -> 들어요, 들으면
        val r5 = KoreanConjugationEngine.conjugate("듣다", "-아/어요")
        assertEquals("들어요", r5.conjugatedText)

        val r6 = KoreanConjugationEngine.conjugate("듣다", "-(으)면")
        assertEquals("들으면", r6.conjugatedText)

        // ㄷ 特例排除词：닫다, 받다, 믿다
        val r7 = KoreanConjugationEngine.conjugate("닫다", "-아/어요")
        assertEquals("닫아요", r7.conjugatedText) // 规则接 -아요

        val r8 = KoreanConjugationEngine.conjugate("받다", "-아/어요")
        assertEquals("받아요", r8.conjugatedText)

        val r9 = KoreanConjugationEngine.conjugate("믿다", "-아/어요")
        assertEquals("믿어요", r9.conjugatedText)
    }

    // ── 3. ㅂ 不规则测试（돕다, 눕다, 춥다 vs 입다, 잡다）────────

    @Test
    fun testBIrregular() {
        // 돕다 (特例转 와) -> 도와요, 도왔어요, 도우면, 돕습니다
        val r1 = KoreanConjugationEngine.conjugate("돕다", "-아/어요")
        assertEquals("도瓦요".replace("瓦", "와"), r1.conjugatedText) // "도와요"
        assertEquals("도와요", r1.conjugatedText)

        val r2 = KoreanConjugationEngine.conjugate("돕다", "-았/었어요")
        assertEquals("도왔어요", r2.conjugatedText)

        val r3 = KoreanConjugationEngine.conjugate("돕다", "-(으)면")
        assertEquals("도우면", r3.conjugatedText)

        val r4 = KoreanConjugationEngine.conjugate("돕다", "-(으)ㅂ니다")
        assertEquals("돕습니다", r4.conjugatedText)

        // 눕다 / 춥다 / 아름답다 (转 워)
        val r5 = KoreanConjugationEngine.conjugate("눕다", "-아/어요")
        assertEquals("누워요", r5.conjugatedText)

        val r6 = KoreanConjugationEngine.conjugate("춥다", "-아/어요")
        assertEquals("추워요", r6.conjugatedText)

        val r7 = KoreanConjugationEngine.conjugate("춥다", "-(으)면")
        assertEquals("추우면", r7.conjugatedText)

        val r8 = KoreanConjugationEngine.conjugate("아름답다", "-아/어요")
        assertEquals("아름다워요", r8.conjugatedText)

        // ㅂ 特例排除词：입다, 잡다, 좁다
        val r9 = KoreanConjugationEngine.conjugate("입다", "-아/어요")
        assertEquals("입어요", r9.conjugatedText)

        val r10 = KoreanConjugationEngine.conjugate("잡다", "-아/어요")
        assertEquals("잡아요", r10.conjugatedText)

        val r11 = KoreanConjugationEngine.conjugate("좁다", "-아/어요")
        assertEquals("좁아요", r11.conjugatedText)
    }

    // ── 4. ㅅ 不规则测试（낫다, 짓다 vs 벗다, 씻다, 웃다）───────

    @Test
    fun testSIrregular() {
        // 낫다 -> 나아요, 나았습니다, 나으면, 낫습니다
        val r1 = KoreanConjugationEngine.conjugate("낫다", "-아/어요")
        assertEquals("나아요", r1.conjugatedText)

        val r2 = KoreanConjugationEngine.conjugate("낫다", "-았/었어요")
        assertEquals("나았어요", r2.conjugatedText)

        val r3 = KoreanConjugationEngine.conjugate("낫다", "-(으)면")
        assertEquals("나으면", r3.conjugatedText)

        val r4 = KoreanConjugationEngine.conjugate("낫다", "-(으)ㅂ니다")
        assertEquals("낫습니다", r4.conjugatedText)

        // 짓다 -> 지어요, 지으면
        val r5 = KoreanConjugationEngine.conjugate("짓다", "-아/어요")
        assertEquals("지어요", r5.conjugatedText)

        val r6 = KoreanConjugationEngine.conjugate("짓다", "-(으)면")
        assertEquals("지으면", r6.conjugatedText)

        // ㅅ 特例排除词：벗다, 씻다, 웃다
        val r7 = KoreanConjugationEngine.conjugate("벗다", "-아/어요")
        assertEquals("벗어요", r7.conjugatedText)

        val r8 = KoreanConjugationEngine.conjugate("씻다", "-아/어요")
        assertEquals("씻어요", r8.conjugatedText)

        val r9 = KoreanConjugationEngine.conjugate("웃다", "-아/어요")
        assertEquals("웃어요", r9.conjugatedText)
    }

    // ── 5. 르 不规则测试（모르다, 빠르다, 부르다）───────────────

    @Test
    fun testReuIrregular() {
        // 모르다 (前字 ㅗ 阳性) -> 몰라요, 몰랐어요, 모르면
        val r1 = KoreanConjugationEngine.conjugate("모르다", "-아/어요")
        assertEquals("몰라요", r1.conjugatedText)
        assertEquals(IrregularType.REU_IRREGULAR, r1.appliedIrregular)

        val r2 = KoreanConjugationEngine.conjugate("모르다", "-았/었어요")
        assertEquals("몰랐어요", r2.conjugatedText)

        val r3 = KoreanConjugationEngine.conjugate("모르다", "-(으)면")
        assertEquals("모르면", r3.conjugatedText)

        // 빠르다 (前字 ㅏ 阳性) -> 빨라요, 빨랐어요
        val r4 = KoreanConjugationEngine.conjugate("빠르다", "-아/어요")
        assertEquals("빨라요", r4.conjugatedText)

        // 부르다 (前字 ㅜ 阴性) -> 불러요, 불렀어요
        val r5 = KoreanConjugationEngine.conjugate("부르다", "-아/어요")
        assertEquals("불러요", r5.conjugatedText)

        val r6 = KoreanConjugationEngine.conjugate("부르다", "-았/었어요")
        assertEquals("불렀어요", r6.conjugatedText)
    }

    // ── 6. 으 脱落测试（크다, 쓰다, 아프다, 예쁘다）─────────────

    @Test
    fun testEuDropping() {
        // 单音节：크다 -> 커요, 컸어요, 크면
        val r1 = KoreanConjugationEngine.conjugate("크다", "-아/어요")
        assertEquals("커요", r1.conjugatedText)

        val r2 = KoreanConjugationEngine.conjugate("크다", "-았/었어요")
        assertEquals("컸어요", r2.conjugatedText)

        val r3 = KoreanConjugationEngine.conjugate("크다", "-(으)면")
        assertEquals("크면", r3.conjugatedText)

        // 单音节：쓰다 -> 써요, 썼어요
        val r4 = KoreanConjugationEngine.conjugate("쓰다", "-아/어요")
        assertEquals("써요", r4.conjugatedText)

        // 多音节前字阳性：아프다 (前字 ㅏ) -> 아파요, 아팠어요
        val r5 = KoreanConjugationEngine.conjugate("아프다", "-아/어요")
        assertEquals("아파요", r5.conjugatedText)

        val r6 = KoreanConjugationEngine.conjugate("바쁘다", "-아/어요")
        assertEquals("바빠요", r6.conjugatedText)

        // 多音节前字阴性：예쁘다 (前字 ㅖ) -> 예뻐요, 예뻤어요
        val r7 = KoreanConjugationEngine.conjugate("예쁘다", "-아/어요")
        assertEquals("예뻐요", r7.conjugatedText)
    }

    // ── 7. ㅎ 不规则测试（그렇다, 까맣다, 하얗다 vs 좋다）────────

    @Test
    fun testHIrregular() {
        // 그렇다 -> 그래요, 그랬어요, 그러면
        val r1 = KoreanConjugationEngine.conjugate("그렇다", "-아/어요")
        assertEquals("그래요", r1.conjugatedText)

        val r2 = KoreanConjugationEngine.conjugate("그렇다", "-았/었어요")
        assertEquals("그랬어요", r2.conjugatedText)

        val r3 = KoreanConjugationEngine.conjugate("그렇다", "-(으)면")
        assertEquals("그러면", r3.conjugatedText)

        // 까맣다 (ㅏ+ㅣ->ㅐ) -> 까매요
        val r4 = KoreanConjugationEngine.conjugate("까맣다", "-아/어요")
        assertEquals("까매요", r4.conjugatedText)

        // 하얗다 (ㅑ+ㅣ->ㅒ) -> 하얘요
        val r5 = KoreanConjugationEngine.conjugate("하얗다", "-아/어요")
        assertEquals("하얘요", r5.conjugatedText)

        // ㅎ 规则特例：좋다 -> 좋아요, 좋으면, 좋습니다
        val r6 = KoreanConjugationEngine.conjugate("좋다", "-아/어요")
        assertEquals("좋아요", r6.conjugatedText)

        val r7 = KoreanConjugationEngine.conjugate("좋다", "-(으)면")
        assertEquals("좋으면", r7.conjugatedText)

        val r8 = KoreanConjugationEngine.conjugate("좋다", "-(으)ㅂ니다")
        assertEquals("좋습니다", r8.conjugatedText)
    }

    // ── 8. ㄹ 脱落测试（살다, 만들다）───────────────────────────

    @Test
    fun testLDropping() {
        // 살다 -> 살아요, 살면, 삽니다, 사세요, 사는, 사니
        val r1 = KoreanConjugationEngine.conjugate("살다", "-아/어요")
        assertEquals("살아요", r1.conjugatedText)

        val r2 = KoreanConjugationEngine.conjugate("살다", "-(으)면")
        assertEquals("살면", r2.conjugatedText) // 遇到 (으) 不加 으

        val r3 = KoreanConjugationEngine.conjugate("살다", "-(으)ㅂ니다")
        assertEquals("삽니다", r3.conjugatedText) // 遇到 ㅂ, ㄹ 脱落填 ㅂ

        val r4 = KoreanConjugationEngine.conjugate("살다", "-(으)세요")
        assertEquals("사세요", r4.conjugatedText) // 遇到 ㅅ, ㄹ 脱落

        val r5 = KoreanConjugationEngine.conjugate("살다", "-는")
        assertEquals("사는", r5.conjugatedText) // 遇到 ㄴ, ㄹ 脱落

        val r6 = KoreanConjugationEngine.conjugate("살다", "-니")
        assertEquals("사니", r6.conjugatedText)

        // 만들다 -> 만들어요, 만들면, 만듭니다, 만드세요, 만드는
        val r7 = KoreanConjugationEngine.conjugate("만들다", "-아/어요")
        assertEquals("만들어요", r7.conjugatedText)

        val r8 = KoreanConjugationEngine.conjugate("만들다", "-(으)ㅂ니다")
        assertEquals("만듭니다", r8.conjugatedText)

        val r9 = KoreanConjugationEngine.conjugate("만들다", "-(으)세요")
        assertEquals("만드세요", r9.conjugatedText)

        val r10 = KoreanConjugationEngine.conjugate("만들다", "-는")
        assertEquals("만드는", r10.conjugatedText)
    }

    // ── 9. 规则自然元音缩合与 하다 测试 ─────────────────────────

    @Test
    fun testRegularContractionAndHada() {
        // 가다 -> 가요, 갔어요, 가면, 갑니다
        assertEquals("가요", KoreanConjugationEngine.conjugate("가다", "-아/어요").conjugatedText)
        assertEquals("갔어요", KoreanConjugationEngine.conjugate("가다", "-았/었어요").conjugatedText)
        assertEquals("가면", KoreanConjugationEngine.conjugate("가다", "-(으)면").conjugatedText)
        assertEquals("갑니다", KoreanConjugationEngine.conjugate("가다", "-(으)ㅂ니다").conjugatedText)

        // 오다 -> 와요, 왔어요
        assertEquals("와요", KoreanConjugationEngine.conjugate("오다", "-아/어요").conjugatedText)
        assertEquals("왔어요", KoreanConjugationEngine.conjugate("오다", "-았/었어요").conjugatedText)

        // 보다 -> 봐요, 봤어요
        assertEquals("봐요", KoreanConjugationEngine.conjugate("보다", "-아/어요").conjugatedText)
        assertEquals("봤어요", KoreanConjugationEngine.conjugate("보다", "-았/었어요").conjugatedText)

        // 마시다 -> 마셔요, 마셨어요
        assertEquals("마셔요", KoreanConjugationEngine.conjugate("마시다", "-아/어요").conjugatedText)
        assertEquals("마셨어요", KoreanConjugationEngine.conjugate("마시다", "-았/었어요").conjugatedText)

        // 공부하다 -> 공부해요, 공부했습니다
        assertEquals("공부해요", KoreanConjugationEngine.conjugate("공부하다", "-아/어요").conjugatedText)
        assertEquals("공부했어요", KoreanConjugationEngine.conjugate("공부하다", "-았/었어요").conjugatedText)
    }

    // ── 10. 直接接续语法测试 (-고, -지만, -기 때문에) ────────────

    @Test
    fun testDirectEnding() {
        assertEquals("걷고", KoreanConjugationEngine.conjugate("걷다", "-고").conjugatedText)
        assertEquals("듣지만", KoreanConjugationEngine.conjugate("듣다", "-지만").conjugatedText)
        assertEquals("돕고", KoreanConjugationEngine.conjugate("돕다", "-고").conjugatedText)
        assertEquals("모르지만", KoreanConjugationEngine.conjugate("모르다", "-지만").conjugatedText)
        assertEquals("살기 때문에", KoreanConjugationEngine.conjugate("살다", "-기 때문에").conjugatedText)
    }
}
