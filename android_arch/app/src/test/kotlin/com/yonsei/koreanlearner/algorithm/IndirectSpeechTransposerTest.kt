package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.Beneficiary
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.PartOfSpeech
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.SentenceType
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.Tense
import org.junit.Assert.*
import org.junit.Test

/**
 * 间接引语与口语缩略形推导引擎单元测试
 */
class IndirectSpeechTransposerTest {

    // ── 1. 陈述句测试 ───────────────────────────────────────────

    @Test
    fun testDeclarativeTransposition() {
        // 动词现在时有收音：먹다 -> 먹는대요
        val r1 = IndirectSpeechTransposer.transpose("먹다", PartOfSpeech.VERB, SentenceType.DECLARATIVE)
        assertEquals("먹는다고 해요", r1.fullForm)
        assertEquals("먹는대요", r1.contractedForm)

        // 动词现在时无收音：가다 -> 간대요
        val r2 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.DECLARATIVE)
        assertEquals("간다고 해요", r2.fullForm)
        assertEquals("간대요", r2.contractedForm)

        // 动词 ㄹ 收音脱落：살다 -> 산대요
        val r3 = IndirectSpeechTransposer.transpose("살다", PartOfSpeech.VERB, SentenceType.DECLARATIVE)
        assertEquals("산다고 해요", r3.fullForm)
        assertEquals("산대요", r3.contractedForm)

        // 形容词现在时：예쁘다 -> 예쁘대요 (不加 ㄴ)
        val r4 = IndirectSpeechTransposer.transpose("예쁘다", PartOfSpeech.ADJECTIVE, SentenceType.DECLARATIVE)
        assertEquals("예쁘다고 해요", r4.fullForm)
        assertEquals("예쁘대요", r4.contractedForm)

        // 名词有收音：학생 -> 학생이래요
        val r5 = IndirectSpeechTransposer.transpose("학생", PartOfSpeech.NOUN, SentenceType.DECLARATIVE)
        assertEquals("학생이라고 해요", r5.fullForm)
        assertEquals("학생이래요", r5.contractedForm)

        // 名词无收音：의사 -> 의사래요
        val r6 = IndirectSpeechTransposer.transpose("의사", PartOfSpeech.NOUN, SentenceType.DECLARATIVE)
        assertEquals("의사라고 해요", r6.fullForm)
        assertEquals("의사래요", r6.contractedForm)

        // 过去时：가다 -> 갔대요
        val r7 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.DECLARATIVE, Tense.PAST)
        assertEquals("갔대요", r7.contractedForm)

        // 将来时：가다 -> 갈 거래요
        val r8 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.DECLARATIVE, Tense.FUTURE)
        assertEquals("갈 거래요", r8.contractedForm)
    }

    // ── 2. 疑问句测试 ───────────────────────────────────────────

    @Test
    fun testInterrogativeTransposition() {
        // 动词：가다 -> 가냬요
        val r1 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.INTERROGATIVE)
        assertEquals("가냐고 해요", r1.fullForm)
        assertEquals("가냬요", r1.contractedForm)

        // 形容词：좋다 -> 좋냬요
        val r2 = IndirectSpeechTransposer.transpose("좋다", PartOfSpeech.ADJECTIVE, SentenceType.INTERROGATIVE)
        assertEquals("좋냐고 해요", r2.fullForm)
        assertEquals("좋냬요", r2.contractedForm)

        // 名词：학생 -> 학생이냬요
        val r3 = IndirectSpeechTransposer.transpose("학생", PartOfSpeech.NOUN, SentenceType.INTERROGATIVE)
        assertEquals("학생이냐고 해요", r3.fullForm)
        assertEquals("학생이냬요", r3.contractedForm)
    }

    // ── 3. 命令句测试 ───────────────────────────────────────────

    @Test
    fun testImperativeTransposition() {
        // 有收音：먹다 -> 먹으래요
        val r1 = IndirectSpeechTransposer.transpose("먹다", PartOfSpeech.VERB, SentenceType.IMPERATIVE)
        assertEquals("먹으라고 해요", r1.fullForm)
        assertEquals("먹으래요", r1.contractedForm)

        // 无收音：가다 -> 가래요
        val r2 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.IMPERATIVE)
        assertEquals("가라고 해요", r2.fullForm)
        assertEquals("가래요", r2.contractedForm)

        // 否定命令：가다 -> 가지 말래요
        val r3 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.IMPERATIVE, isNegative = true)
        assertEquals("가지 말라고 해요", r3.fullForm)
        assertEquals("가지 말래요", r3.contractedForm)

        // 委托特例（帮说话人做）：도와주다 -> 도와 달래요
        val r4 = IndirectSpeechTransposer.transpose("도와주다", PartOfSpeech.VERB, SentenceType.IMPERATIVE, beneficiary = Beneficiary.SPEAKER_SELF)
        assertEquals("도와달라고 해요".replace("도와달", "도와 달"), r4.fullForm)
        assertEquals("도와달래요".replace("도와달", "도와 달"), r4.contractedForm)

        // 委托特例（帮第三方做）：도와주다 -> 도와 주래요
        val r5 = IndirectSpeechTransposer.transpose("도와주다", PartOfSpeech.VERB, SentenceType.IMPERATIVE, beneficiary = Beneficiary.THIRD_PARTY)
        assertEquals("도와주라고 해요".replace("도와주", "도와 주"), r5.fullForm)
        assertEquals("도와주래요".replace("도와주", "도와 주"), r5.contractedForm)
    }

    // ── 4. 共动句测试 ───────────────────────────────────────────

    @Test
    fun testPropositiveTransposition() {
        // 肯定共动：가다 -> 가재요
        val r1 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.PROPOSITIVE)
        assertEquals("가자고 해요", r1.fullForm)
        assertEquals("가재요", r1.contractedForm)

        // 肯定共动：먹다 -> 먹재요
        val r2 = IndirectSpeechTransposer.transpose("먹다", PartOfSpeech.VERB, SentenceType.PROPOSITIVE)
        assertEquals("먹자고 해요", r2.fullForm)
        assertEquals("먹재요", r2.contractedForm)

        // 否定共动：가다 -> 가지 말재요
        val r3 = IndirectSpeechTransposer.transpose("가다", PartOfSpeech.VERB, SentenceType.PROPOSITIVE, isNegative = true)
        assertEquals("가지 말자고 해요", r3.fullForm)
        assertEquals("가지 말재요", r3.contractedForm)
    }
}
