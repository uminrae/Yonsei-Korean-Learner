package com.yonsei.koreanlearner.data.local.database

import com.yonsei.koreanlearner.data.local.entity.LoanwordEntity

object LoanwordSeedData {

    val PRESET_LOANWORDS = listOf(
        LoanwordEntity(
            id = 1,
            loanword = "아르바이트",
            foreignOrigin = "Arbeit (德语：工作/劳动)",
            nativeKoreanEquivalent = "시간제 일 / 부업 (副业)",
            chineseMeaning = "打工 / 兼职 (常缩写为 알바)",
            category = "生活日常",
            exampleKorean = "방학 동안 카페에서 아르바이트를 해요.",
            exampleChinese = "放假期间在咖啡厅做兼职打工。",
            level = "TOPIK 1"
        ),
        LoanwordEntity(
            id = 2,
            loanword = "스마트폰",
            foreignOrigin = "Smartphone (英语)",
            nativeKoreanEquivalent = "휴대전화 (携带有线电话) / 지능형 단말기",
            chineseMeaning = "智能手机",
            category = "科技数码",
            exampleKorean = "스마트폰으로 한국어 단어를 검색해요.",
            exampleChinese = "用智能手机检索韩语单词。",
            level = "TOPIK 1"
        ),
        LoanwordEntity(
            id = 3,
            loanword = "다이어트",
            foreignOrigin = "Diet (英语：饮食/节食)",
            nativeKoreanEquivalent = "체중 감량 (体重减量) / 식단 조절",
            chineseMeaning = "减肥 / 节食瘦身",
            category = "饮食健康",
            exampleKorean = "건강을 위해 매일 운동하며 다이어트를 하고 있어요.",
            exampleChinese = "为了健康每天运动并进行身材管理减肥。",
            level = "TOPIK 2"
        ),
        LoanwordEntity(
            id = 4,
            loanword = "서비스",
            foreignOrigin = "Service (英语)",
            nativeKoreanEquivalent = "무료 제공 (免费提供) / 덤 (额外赠品)",
            chineseMeaning = "赠品 / 免费额外赠送",
            category = "生活日常",
            exampleKorean = "사장님이 떡볶이를 서비스로 주셨어요.",
            exampleChinese = "老板把炒年糕当作免费赠品送给我们了。",
            level = "TOPIK 2"
        ),
        LoanwordEntity(
            id = 5,
            loanword = "원룸",
            foreignOrigin = "One-room (韩式英语 Konglish)",
            nativeKoreanEquivalent = "단칸방 (单间房) / 일인용 주택",
            chineseMeaning = "单间公寓 (独立厨卫一居室)",
            category = "韩式英语Konglish",
            exampleKorean = "학교 근처에 깨끗한 원룸을 구했습니다.",
            exampleChinese = "在学校附近租到了一间干净的单间公寓。",
            level = "TOPIK 2"
        ),
        LoanwordEntity(
            id = 6,
            loanword = "리모컨",
            foreignOrigin = "Remote control (英语缩略)",
            nativeKoreanEquivalent = "원격조정기 (远距离调整器)",
            chineseMeaning = "遥控器",
            category = "科技数码",
            exampleKorean = "텔레비전 리모컨이 어디 갔지?",
            exampleChinese = "电视遥控器去哪儿了？",
            level = "TOPIK 2"
        ),
        LoanwordEntity(
            id = 7,
            loanword = "스킨십",
            foreignOrigin = "Skinship (韩式英语 Konglish)",
            nativeKoreanEquivalent = "신체 접촉 (身体接触) / 교감",
            chineseMeaning = "肢体接触 / 亲昵互动",
            category = "韩式英语Konglish",
            exampleKorean = "아이들과 자연스러운 스킨십이 중요해요.",
            exampleChinese = "与孩子们进行自然的肢体亲昵接触非常重要。",
            level = "TOPIK 3"
        ),
        LoanwordEntity(
            id = 8,
            loanword = "콘센트",
            foreignOrigin = "Concentric plug (韩式英语 Konglish)",
            nativeKoreanEquivalent = "전원 플러그 / 꽂개",
            chineseMeaning = "电源插座",
            category = "韩式英语Konglish",
            exampleKorean = "카페 벽 쪽에 콘센트가 있어요.",
            exampleChinese = "咖啡厅靠墙一侧有电源插座。",
            level = "TOPIK 2"
        )
    )
}
