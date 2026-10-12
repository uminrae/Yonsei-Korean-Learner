package com.yonsei.koreanlearner.data.local.database

import com.yonsei.koreanlearner.data.local.entity.IdiomEntity
import com.yonsei.koreanlearner.data.local.entity.IdiomType

object IdiomSeedData {

    val PRESET_IDIOMS = listOf(
        // ── 1. 韩国经典俗语 (속담) ─────────────────────────────
        IdiomEntity(
            id = 1,
            korean = "쇠귀에 경 읽기",
            hanja = null,
            type = IdiomType.IDIOM,
            literalMeaning = "对着牛耳朵念佛经",
            metaphorMeaning = "对牛弹琴，白费口舌，任凭怎么劝说都毫无效果",
            culturalBackground = "源自韩国传统农耕文化中老黄牛虽通人性但不解经文之意，常用于形容教育顽固不听劝之人。",
            dialoguesJson = """[{"speakerA":"아무리 말해도 안 들어요.","speakerB":"정말 쇠귀에 경 읽기네요.","chineseA":"无论怎么说他都不听。","chineseB":"真是对牛弹琴啊。"}]""",
            level = "TOPIK 3",
            category = "生活态度",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 2,
            korean = "갈수록 태산",
            hanja = "泰山",
            type = IdiomType.IDIOM,
            literalMeaning = "越走越遇到泰山",
            metaphorMeaning = "雪上加霜，一波未平一波又起，情况越来越棘手",
            culturalBackground = "古时翻山越岭，刚翻过一座小山又迎面耸立大山，比喻困境层层递进。",
            dialoguesJson = """[{"speakerA":"시험에 떨어졌는데 지갑까지 잃어버렸어.","speakerB":"갈수록 태산이네, 힘내!","chineseA":"考试没过，居然连钱包也丢了。","chineseB":"真是雪上加霜啊，加油！"}]""",
            level = "TOPIK 3",
            category = "困境挑战",
            iconSymbol = "⛰️"
        ),
        IdiomEntity(
            id = 3,
            korean = "누워서 떡 먹기",
            hanja = null,
            type = IdiomType.IDIOM,
            literalMeaning = "躺着吃年糕",
            metaphorMeaning = "易如反掌，轻而易举，小菜一碟",
            culturalBackground = "传统打糕美味且无需费力走动即可享用，形容事情极度轻松容易。",
            dialoguesJson = """[{"speakerA":"이 문제 풀 수 있어?","speakerB":"이 정도는 누워서 떡 먹기지!","chineseA":"你能解开这道题吗？","chineseB":"这种程度简直易如反掌！"}]""",
            level = "TOPIK 2",
            category = "能力掌控",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 4,
            korean = "티끌 모아 태산",
            hanja = "泰山",
            type = IdiomType.IDIOM,
            literalMeaning = "尘土微粒汇聚成泰山",
            metaphorMeaning = "积少成多，集腋成裘",
            culturalBackground = "激励年轻人坚持储蓄与每天积累学习单词的经典家训。",
            dialoguesJson = """[{"speakerA":"매일 단어 10개씩 외워서 언제 다 해?","speakerB":"티끌 모아 태산이잖아, 꾸준히 해봐!","chineseA":"每天只背10个单词什么时候才能背完？","chineseB":"积少成多嘛，坚持下去试试！"}]""",
            level = "TOPIK 2",
            category = "勤奋积累",
            iconSymbol = "⏳"
        ),

        // ── 2. 四字成语 (사자성어) ────────────────────────────
        IdiomEntity(
            id = 5,
            korean = "고진감래",
            hanja = "苦盡甘來",
            type = IdiomType.FOUR_CHAR,
            literalMeaning = "苦尽甜来",
            metaphorMeaning = "苦尽甘来，艰难困苦的日子过去，美好的成果终将到来",
            culturalBackground = "源自汉字文化圈，韩国职场人与备考生最常用来互相鼓励的座右铭。",
            dialoguesJson = """[{"speakerA":"수험 생활이 너무 힘들어요.","speakerB":"고진감래라고 하니까 조금만 더 버텨요.","chineseA":"备考生活太辛苦了。","chineseB":"俗话说苦尽甘来，再坚持一下吧。"}]""",
            level = "TOPIK 4",
            category = "人生哲理",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 6,
            korean = "십시일반",
            hanja = "十匙一飯",
            type = IdiomType.FOUR_CHAR,
            literalMeaning = "十汤匙凑成一碗米饭",
            metaphorMeaning = "众人拾柴火焰高，大家各出一份微薄之力汇聚成大帮助",
            culturalBackground = "源自传统乡约邻里互助文化，常用于慈善募捐或集体集资场景。",
            dialoguesJson = """[{"speakerA":"어려운 이웃을 위해 십시일반으로 모았습니다.","speakerB":"정말 따뜻한 마음이네요.","chineseA":"为了困难邻居大家众人拾柴凑集了爱心款。","chineseB":"真是温暖的心意。"}]""",
            level = "TOPIK 4",
            category = "互助团结",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 7,
            korean = "일석이조",
            hanja = "一石二鳥",
            type = IdiomType.FOUR_CHAR,
            literalMeaning = "扔一块石头击中两只鸟",
            metaphorMeaning = "一举两得，一箭双雕",
            culturalBackground = "形容做一件事同时收获两份好处的高效行为。",
            dialoguesJson = """[{"speakerA":"자전거로 출퇴근하면 건강도 챙기고 돈도 아끼잖아.","speakerB":"완전 일석이조네!","chineseA":"骑车上下班既能锻炼身体又能省钱。","chineseB":"完全是一举两得啊！"}]""",
            level = "TOPIK 2",
            category = "智慧策略",
            iconSymbol = ""
        ),

        // ── 3. 拟声拟态词 (의성·의태어) ───────────────────────
        IdiomEntity(
            id = 8,
            korean = "차곡차곡",
            hanja = null,
            type = IdiomType.ONOMATOPOEIA,
            literalMeaning = "整齐有序地层层叠放",
            metaphorMeaning = "有条不紊地整理、循序渐进地积攒（知识/财富）",
            culturalBackground = "拟态词，形容将叠好的衣物或书本整整齐齐码放的状态。",
            dialoguesJson = """[{"speakerA":"적금을 차곡차곡 모았어요.","speakerB":"정말 알뜰하시네요!","chineseA":"我把定期存款一笔笔整齐地攒起来了。","chineseB":"您真会过日子！"}]""",
            level = "TOPIK 2",
            category = "状态描写",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 9,
            korean = "따르릉",
            hanja = null,
            type = IdiomType.ONOMATOPOEIA,
            literalMeaning = "叮铃铃",
            metaphorMeaning = "电话铃声或自行车清脆的铃铛声",
            culturalBackground = "经典拟声词，韩国儿歌《따르릉따르릉 비켜나세요》家喻户晓。",
            dialoguesJson = """[{"speakerA":"전화벨이 따르릉 울려요.","speakerB":"얼른 받아봐요.","chineseA":"电话铃叮铃铃响了。","chineseB":"快接电话吧。"}]""",
            level = "TOPIK 1",
            category = "声音模拟",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 10,
            korean = "두근두근",
            hanja = null,
            type = IdiomType.ONOMATOPOEIA,
            literalMeaning = "扑通扑通",
            metaphorMeaning = "心跳加速、紧张、激动或小鹿乱撞的心理状态",
            culturalBackground = "极高频日常拟态词，用于初次表白、重要面试或考前心理描写。",
            dialoguesJson = """[{"speakerA":"면접 전에 가슴이 두근두근 뛰었어요.","speakerB":"긴장 많이 하셨겠네요.","chineseA":"面试前我的心扑通扑通直跳。","chineseB":"当时一定很紧张吧。"}]""",
            level = "TOPIK 1",
            category = "心理情绪",
            iconSymbol = ""
        ),
        IdiomEntity(
            id = 11,
            korean = "다닥다닥",
            hanja = null,
            type = IdiomType.ONOMATOPOEIA,
            literalMeaning = "密密麻麻地附着",
            metaphorMeaning = "许多小物件紧挨着密集粘连排列的状态",
            culturalBackground = "形容胡同两旁紧凑的小房屋，或树枝上密集结出的果实。",
            dialoguesJson = """[{"speakerA":"집들이 다닥다닥 붙어 있어요.","speakerB":"골목길이 좁아 보이네요.","chineseA":"房子密密麻麻地紧挨着。","chineseB":"小胡同看起来好窄啊。"}]""",
            level = "TOPIK 3",
            category = "形态密集",
            iconSymbol = "️"
        )
    )
}
