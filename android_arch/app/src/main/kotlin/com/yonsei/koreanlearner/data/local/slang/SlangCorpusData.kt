package com.yonsei.koreanlearner.data.local.slang

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩国现代流行新造词与职场暗语语料库 (SlangCorpusData)
 * ══════════════════════════════════════════════════════════════════
 */

data class SlangItem(
    val termKr: String,
    val originKr: String,
    val chineseTitle: String,
    val explanation: String,
    val category: String, // "MZ流行新造词" or "职场黑话与礼仪"
    val sampleChat: List<Pair<String, String>> // Speaker to Message
)

object SlangCorpusData {

    val SLANG_LIST = listOf(
        SlangItem(
            termKr = "갓생",
            originKr = "God(갓) + 生(생)",
            chineseTitle = "充实向上的自律生活",
            explanation = "形容每天早起学习、健身、自律规划的极高质量生活方式，源于年轻人对高自律人生的向往。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "민수" to "오늘부터 6시에 일어나서 운동하고 한국어 공부할 거야.",
                "지수" to "대박, 너 완전 갓생 사는구나! 파이팅!"
            )
        ),
        SlangItem(
            termKr = "중꺾마",
            originKr = "중요한 것은 꺾이지 않는 마음",
            chineseTitle = "百折不挠的心 / 不放弃的执念",
            explanation = "出自 2022 年英雄联盟世界赛 Deft 选手名言，广泛用于形容即使身处逆境也绝不轻言放弃的坚定意志。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "태형" to "이번 시험 너무 어려울 것 같아서 포기하고 싶어.",
                "윤기" to "야, 중꺾마 몰라? 끝까지 하면 할 수 있어!"
            )
        ),
        SlangItem(
            termKr = "알잘딱깔센",
            originKr = "알아서 잘 딱 깔끔하고 센스있게",
            chineseTitle = "有眼力见、利落得体、干脆敏锐",
            explanation = "形容无需多余叮嘱，自己就能把事情处理得既漂亮又有分寸的高情商办事风格。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "팀장" to "이번 발표 자료 준비는 어떻게 됐어?",
                "대리" to "제가 알잘딱깔센하게 다 정리해 두었습니다!"
            )
        ),
        SlangItem(
            termKr = "억까",
            originKr = "억지로 까기 (강제 비난)",
            chineseTitle = "无端遭黑 / 强行挑刺",
            explanation = "指完全没有正当理由、恶意且毫无根据地挑刺或针对某人，也可指运气差到离谱的境遇。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "지민" to "출근길에 비 오고 버스 놓치고 지갑까지 두고 왔어...",
                "정국" to "와, 오늘의 날씨와 상황이 너를 완전 억까하네."
            )
        ),
        SlangItem(
            termKr = "완내스",
            originKr = "완전 내 스타일",
            chineseTitle = "完全是我的菜 / 深得我心",
            explanation = "形容某个人、某种穿搭、或者食物装潢极其符合自己的审美与偏好。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "수빈" to "여기 새로 생긴 인스타 카페 어때?",
                "연준" to "화이트 톤 인테리어 대박이다, 완전 완내스야!"
            )
        ),
        SlangItem(
            termKr = "오운완",
            originKr = "오늘 운동 완료",
            chineseTitle = "今日运动打卡完毕",
            explanation = "社交平台（Instagram 等）健身房自拍或打卡时最常用的流行标签。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "인스타그램" to "헬스 2시간 + 유산소 30분 달성! #오운완 💪"
            )
        ),
        SlangItem(
            termKr = "분조카",
            originKr = "분위기 좋은 카페",
            chineseTitle = "氛围感超绝的咖啡馆",
            explanation = "年轻人约会或打卡探店时常用的缩写词汇。",
            category = "MZ流行新造词",
            sampleChat = listOf(
                "하린" to "주말에 홍대 가서 분조카 찾아서 사진 찍자!"
            )
        ),
        SlangItem(
            termKr = "칼퇴",
            originKr = "칼 + 퇴근 (정시 퇴근)",
            chineseTitle = "踩点准时下班",
            explanation = "韩国职场中到点秒关电脑准时离开公司的行为（不加班）。",
            category = "职场黑话与礼仪",
            sampleChat = listOf(
                "동료" to "오늘 저녁 약속 있어? 퇴근 시간 다 됐네.",
                "나" to "네! 오늘 일 다 끝내서 칼퇴할 예정입니다!"
            )
        ),
        SlangItem(
            termKr = "품의서",
            originKr = "稟議書 (회사 내부 결재 문서)",
            chineseTitle = "立项报批请示公文",
            explanation = "职场中为了申请预算、开展新业务或采购物资而撰写向上级审批的正规报告文书。",
            category = "职场黑话与礼仪",
            sampleChat = listOf(
                "과장" to "이번 프로젝트 소프트웨어 구매 품의서 결재 올려주세요."
            )
        ),
        SlangItem(
            termKr = "팔로업",
            originKr = "Follow-up (진행 상황 지속 추적)",
            chineseTitle = "跟进落实 / 追踪后续",
            explanation = "职场外来语，指对已经安排或沟通的事项进行后续跟进确认，确保落地执行。",
            category = "职场黑话与礼仪",
            sampleChat = listOf(
                "팀장" to "거래처에서 아직 회신 없나요? 오늘 중으로 팔로업 부탁해요."
            )
        )
    )
}
