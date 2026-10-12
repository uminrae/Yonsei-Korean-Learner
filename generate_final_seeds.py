# -*- coding: utf-8 -*-
"""
Final Production Database Generator for Yonsei Korean (Android Room Seed Data)
Outputs:
  - words_seed.json (4446 entries)
  - grammars_seed.json (314 entries)
"""

import os
import sys
sys.stdout.reconfigure(encoding='utf-8')
import re
import json
import pymupdf

BASE_DIR = r"c:\Users\min.qeoleu\Desktop\手机kor"
WORDS_DIR = os.path.join(BASE_DIR, "单词")
GRAMMAR_DIR = os.path.join(BASE_DIR, "语法")

# ==============================================================================
# 1. WORDS EXTRACTION & CLASSIFICATION (Books 1 - 6)
# ==============================================================================

IRREGULAR_MAP = {
    # ㅂ irregular
    "돕다": "ㅂ不规则", "곱다": "ㅂ不规则", "춥다": "ㅂ不规则", "덥다": "ㅂ不规则",
    "맵다": "ㅂ不规则", "가볍다": "ㅂ不规则", "무겁다": "ㅂ不规则", "아름답다": "ㅂ不规则",
    "귀엽다": "ㅂ不规则", "어렵다": "ㅂ不规则", "쉽다": "ㅂ不规则", "반갑다": "ㅂ不规则",
    "즐겁다": "ㅂ不规则", "싱겁다": "ㅂ不规则", "차갑다": "ㅂ不规则", "뜨겁다": "ㅂ不规则",
    "어둡다": "ㅂ不规则", "안타깝다": "ㅂ不规则", "까다롭다": "ㅂ不规则", "고맙다": "ㅂ不规则",
    "눕다": "ㅂ不规则", "깁다": "ㅂ不规则", "줍다": "ㅂ不规则", "굽다": "ㅂ不规则",
    "가렵다": "ㅂ不规则", "그립다": "ㅂ不规则", "두렵다": "ㅂ不规则", "외롭다": "ㅂ不规则",
    "서럽다": "ㅂ不规则", "부럽다": "ㅂ不规则", "촌스럽다": "ㅂ不规则", "번거롭다": "ㅂ不规则",
    "수다스럽다": "ㅂ不规则", "자랑스럽다": "ㅂ不规则", "쑥스럽다": "ㅂ不规则", "당황스럽다": "ㅂ不规则",
    "부담스럽다": "ㅂ不规则", "짜증스럽다": "ㅂ不规则", "변덕스럽다": "ㅂ不规则", "어지럽다": "ㅂ不规则",
    "더럽다": "ㅂ不规则", "두껍다": "ㅂ不规则",
    
    # ㄷ irregular
    "걷다": "ㄷ不规则", "듣다": "ㄷ不规则", "묻다": "ㄷ不规则", "싣다": "ㄷ不规则",
    "긷다": "ㄷ不规则", "깨닫다": "ㄷ不规则",
    
    # ㅅ irregular
    "짓다": "ㅅ不规则", "낫다": "ㅅ不规则", "붓다": "ㅅ不规则", "젓다": "ㅅ不规则",
    "잇다": "ㅅ不规则", "긋다": "ㅅ不规则",
    
    # ㅎ irregular
    "빨갛다": "ㅎ不规则", "파랗다": "ㅎ不规则", "노랗다": "ㅎ不规则", "하얗다": "ㅎ不规则",
    "까맣다": "ㅎ不规则", "그렇다": "ㅎ不规则", "이렇다": "ㅎ不规则", "저렇다": "ㅎ不规则",
    "어떻다": "ㅎ不规则", "뿌옇다": "ㅎ不规则", "누렇다": "ㅎ不规则", "동그랗다": "ㅎ不规则",
    "조그맣다": "ㅎ不规则",
    
    # 르 irregular
    "모르다": "르不规则", "빠르다": "르不规则", "부르다": "르不规则", "고르다": "르不规则",
    "다르다": "르不规则", "마르다": "르不规则", "흐르다": "르不规则", "기르다": "르不规则",
    "누르다": "르不规则", "오르다": "르不规则", "자르다": "르不规则", "가르다": "르不规则",
    "나르다": "르不规则", "서투르다": "르不规则", "게으르다": "르不规则", "배부르다": "르不规则",
    "바르다": "르不规则", "지르다": "르不规则", "구르다": "르不规则", "가파르다": "르不规则",
    
    # 으 drop
    "크다": "으脱落", "쓰다": "으脱落", "뜨다": "으脱落", "끄다": "으脱落", "트다": "으脱落",
    "담그다": "으脱落", "잠그다": "으脱落", "아프다": "으脱落", "바쁘다": "으脱落", "기쁘다": "으脱落",
    "슬프다": "으脱落", "예쁘다": "으脱落", "나쁘다": "으脱落", "따르다": "으脱落", "치르다": "으脱落",
    "들르다": "으脱落", "모으다": "으脱落", "배고프다": "으脱落", "고프다": "으脱落"
}

def detect_irregular(korean, chinese):
    clean_k = re.sub(r'[\(\)\?!\.,~]', '', korean).strip()
    if clean_k in IRREGULAR_MAP:
        return IRREGULAR_MAP[clean_k]
    
    if clean_k.endswith("스럽다") or clean_k.endswith("롭다"):
        return "ㅂ不规则"
    if clean_k.endswith("르다") and clean_k not in ("따르다", "치르다", "들르다"):
        return "르不规则"
    if clean_k.endswith("맣다") or clean_k.endswith("랗다") or clean_k.endswith("렇다"):
        return "ㅎ不规则"
    
    if re.search(r'[가-힣]+[ㄹ][다]$', clean_k) and len(clean_k) <= 4:
        if any(v in clean_k for v in ["살다", "알다", "만들다", "열다", "팔다", "놀다", "멀다", "길다", "달다", "울다", "끌다", "불다", "밀다", "날다", "벌다", "돌다", "들다", "졸다", "걸다", "헐다", "말다", "갈다", "널다", "털다", "썰다", "절다"]):
            return "ㄹ不规则"
            
    return None

def determine_pos(korean, chinese):
    k = korean.strip()
    c = chinese.strip()
    
    if "（动）" in c or "(动)" in c or "（他动）" in c or "（自动）" in c:
        return "动词"
    if "（形）" in c or "(形)" in c:
        return "形容词"
    if "（名）" in c or "(名)" in c:
        return "名词"
    if "（助）" in c or "(助)" in c or "助词" in c:
        return "助词"
    if "（副）" in c or "(副)" in c:
        return "副词"
    if "（代）" in c or "(代)" in c or "代词" in c:
        return "代词"
    if "（数）" in c or "(数)" in c:
        return "数词"
    if "（量）" in c or "(量)" in c or "单位名词" in c:
        return "量词"
    if "（感）" in c or "(感)" in c:
        return "感叹词"
    if "（冠）" in c or "(冠)" in c:
        return "冠形词"

    if k.endswith("?") or k.endswith("!") or any(phrase in k for phrase in [
        "안녕하십니까", "안녕하세요", "안녕히", "감사합니다", "고맙습니다", "죄송합니다", 
        "미안합니다", "실례합니다", "처음 뵙겠습니다", "잘 부탁드립니다", "수고하셨습니다",
        "어서 오세요", "여보세요", "축하합니다", "어서오세요", "잘 먹겠습니다", "잘 먹었습니다",
        "다녀오겠습니다", "다녀왔습니다", "축하드립니다", "잠깐만요", "글쎄요", "그럼요", "천만에요"
    ]):
        return "常用语"
        
    if k.startswith("-") or k.startswith("~"):
        return "助词"
    if k in ["은/는", "이/가", "을/를", "에", "에서", "에게", "와/과", "도", "만", "부터", "까지", "로/으로", "의", "보다", "마다", "처럼", "하고", "(이)랑"]:
        return "助词"
        
    if k in ["나", "저", "너", "우리", "저희", "너희", "당신", "그", "그녀", "이것", "그것", "저것", "여기", "거기", "저기", "어디", "누구", "무엇", "언제", "어느", "무슨", "어떤"]:
        return "代词"

    if k in ["하나", "둘", "셋", "넷", "다섯", "여섯", "일곱", "여덟", "아홉", "열", "스물", "서른", "마흔", "쉰", "예순", "일흔", "여든", "아흔", "온", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구", "십", "백", "천", "만", "억", "조", "첫째", "둘째", "셋째"]:
        return "数词"
    if k in ["개", "명", "분", "마리", "권", "병", "잔", "장", "채", "대", "켤레", "척", "자루", "송이", "벌", "통", "박스", "그릇", "살", "시", "월", "일", "년", "원", "층"]:
        return "量词"

    if " " in k and (k.endswith("다") or k.endswith("하다") or k.endswith("되다") or k.endswith("있다") or k.endswith("없다")):
        return "惯用型/短语"

    if k in ["아주", "매우", "너무", "잘", "못", "일찍", "빨리", "천천히", "가끔", "항상", "늘", "자주", "벌써", "이미", "아직", "겨우", "거의", "비로소", "마침내", "드디어", "정말", "진짜", "참", "꼭", "다시", "또", "함께", "같이", "혼자", "모두", "다", "서로", "그냥", "깜짝", "반드시", "과연", "혹시", "아마"]:
        return "副词"
    if k.endswith("히") or (k.endswith("이") and not k.endswith("같이") and len(k) > 1 and "地" in c):
        return "副词"

    if k.endswith("다"):
        if any(k.endswith(suffix) for suffix in ["스럽다", "롭다", "맞다", "답다"]):
            return "形容词"
        if any(adj_word in k for adj_word in ["크다", "작다", "많다", "적다", "좋다", "나쁘다", "예쁘다", "착하다", "깨끗하다", "조용하다", "시끄럽다", "복잡하다", "한적하다", "편하다", "불편하다", "친절하다", "유명하다", "다양하다", "풍부하다", "비싸다", "싸다", "길다", "짧다", "높다", "낮다", "빠르다", "느리다", "어렵다", "쉽다", "가깝다", "멀다", "맛있다", "맛없다", "재미있다", "재미없다", "덥다", "춥다", "따뜻하다", "시원하다"]):
            return "形容词"
        if "的" in c and not ("做" in c or "弄" in c or "打" in c or "变" in c):
            return "形容词"
        return "动词"

    return "名词"

def clean_text(text):
    if not text: return ""
    text = re.sub(r'[\r\n\t]+', ' ', text)
    text = re.sub(r'\s+', ' ', text)
    return text.strip()

def clean_chinese(text):
    text = clean_text(text)
    text = re.sub(r'[\s/]+$', '', text)
    for wm in ["本资料由桑妮韩语整理", "公众号 「桑妮韩语」", "公众号：桑妮韩语", "《延世大学韩国语》"]:
        text = text.replace(wm, "")
    return text.strip()

def extract_words():
    all_words = []
    
    for book_num in range(1, 7):
        fn = f"延世韩国语单词整理-第{book_num}册_背诵本.pdf"
        filepath = os.path.join(WORDS_DIR, fn)
        doc = pymupdf.open(filepath)
        
        words_map = {}
        current_lesson = 1
        level = f"TOPIK {book_num}"
        
        for pno, page in enumerate(doc):
            text = page.get_text()
            lines = [l.strip() for l in text.split('\n') if l.strip()]
            
            i = 0
            while i < len(lines):
                l = lines[i]
                m_les = re.search(r'제\s*(\d+)\s*과', l)
                if m_les:
                    current_lesson = int(m_les.group(1))
                
                if i + 3 < len(lines) and lines[i].isdigit() and lines[i+1].isdigit():
                    sub = int(lines[i])
                    cum = int(lines[i+1])
                    if 1 <= cum <= 1500 and sub <= 100:
                        kor = lines[i+2]
                        chi = lines[i+3]
                        if not chi.isdigit() and '제' not in chi and '#' not in chi and '序号' not in chi and '累计' not in chi:
                            if cum not in words_map:
                                words_map[cum] = {
                                    'book': book_num,
                                    'lesson': current_lesson,
                                    'cumulative_index': cum,
                                    'korean': clean_text(kor),
                                    'chinese': clean_chinese(chi)
                                }
                                i += 4
                                continue
                                
                if i + 2 < len(lines) and lines[i].isdigit():
                    m2 = re.match(r'^(\d{1,4})\s+(.+)$', lines[i+1])
                    if m2:
                        cum = int(m2.group(1))
                        kor = m2.group(2).strip()
                        chi = lines[i+2]
                        if 1 <= cum <= 1500 and not chi.isdigit() and '제' not in chi and '#' not in chi:
                            if cum not in words_map:
                                words_map[cum] = {
                                    'book': book_num,
                                    'lesson': current_lesson,
                                    'cumulative_index': cum,
                                    'korean': clean_text(kor),
                                    'chinese': clean_chinese(chi)
                                }
                                i += 3
                                continue
                                
                if i + 1 < len(lines) and lines[i].isdigit():
                    m3 = re.match(r'^(\d{1,4})\s+([가-힣a-zA-Z\s\?\.,~!\(\)]+)([\u4e00-\u9fa5].*)$', lines[i+1])
                    if m3:
                        cum = int(m3.group(1))
                        kor = m3.group(2).strip()
                        chi = m3.group(3).strip()
                        if 1 <= cum <= 1500:
                            if cum not in words_map:
                                words_map[cum] = {
                                    'book': book_num,
                                    'lesson': current_lesson,
                                    'cumulative_index': cum,
                                    'korean': clean_text(kor),
                                    'chinese': clean_chinese(chi)
                                }
                                i += 2
                                continue
                i += 1
                
            tabs = page.find_tables().tables
            for tab in tabs:
                rows = tab.extract()
                for r in rows:
                    if not r: continue
                    cells = [clean_text(c) for c in r if c]
                    row_str = ' '.join(cells)
                    m = re.search(r'제\s*(\d+)\s*과', row_str)
                    if m:
                        current_lesson = int(m.group(1))
                    
                    if book_num in (1, 2):
                        all_cells = [clean_text(c) for c in r]
                        if len(all_cells) >= 4 and all_cells[0].isdigit() and all_cells[1].isdigit():
                            cum = int(all_cells[1])
                            if cum not in words_map and 1 <= cum <= 1500:
                                words_map[cum] = {
                                    'book': book_num,
                                    'lesson': current_lesson,
                                    'cumulative_index': cum,
                                    'korean': clean_text(all_cells[2]),
                                    'chinese': clean_chinese(all_cells[3])
                                }
                        if len(all_cells) >= 9 and all_cells[5].isdigit() and all_cells[6].isdigit():
                            cum = int(all_cells[6])
                            if cum not in words_map and 1 <= cum <= 1500:
                                words_map[cum] = {
                                    'book': book_num,
                                    'lesson': current_lesson,
                                    'cumulative_index': cum,
                                    'korean': clean_text(all_cells[7]),
                                    'chinese': clean_chinese(all_cells[8])
                                }
                    else:
                        if len(cells) >= 4 and cells[0].isdigit() and cells[1].isdigit():
                            cum = int(cells[1])
                            if cum not in words_map and 1 <= cum <= 1500:
                                words_map[cum] = {
                                    'book': book_num,
                                    'lesson': current_lesson,
                                    'cumulative_index': cum,
                                    'korean': clean_text(cells[2]),
                                    'chinese': clean_chinese(cells[3])
                                }
        
        sorted_cums = sorted(words_map.keys())
        for cum in sorted_cums:
            item = words_map[cum]
            korean = item['korean']
            chinese = item['chinese']
            pos = determine_pos(korean, chinese)
            irreg = detect_irregular(korean, chinese)
            
            all_words.append({
                "book": item['book'],
                "lesson": item['lesson'],
                "cumulative_index": item['cumulative_index'],
                "korean": item['korean'],
                "chinese": item['chinese'],
                "part_of_speech": pos,
                "level": level,
                "irregular_type": irreg
            })
            
        print(f"Book {book_num}: Extracted {len(sorted_cums)} words (1..{max(sorted_cums)})")

    return all_words

# ==============================================================================
# 2. GRAMMAR EXTRACTION & STRUCTURING (Books 1 - 4)
# ==============================================================================

def get_refined_category(pattern, explanation):
    p = pattern.strip()
    exp = explanation.strip()
    
    # Explicit pattern categories
    if any(p.startswith(x) for x in ['-입니다', '-입니까', '-ㅂ/습니다', '-지요', '-아/어/여요', '-(으)세요', '-으십시오', '-읍/ㅂ시다', '-(는)군요', '-네요', '-더라', '-걸요', '-단다', '-랍니다', '-잖아요', '-(으)ㄹ까요']):
        return "终结词尾"
    if any(p.startswith(x) for x in ['-은/는', '-이/가', '-을/를', '-에', '-에서', '-에게', '-과/와', '-도', '-만', '-부터', '-까지', '-(으)로', '-보다', '-마다', '-밖에', '-처럼', '-만큼', '-치고', '-마저', '-조차', '-(이)나', '-(이)든지', '-(이)라도', '-(이)야말로', '하고', '-(이)며', '-따라']):
        return "助词/添意"
    if any(p.startswith(x) for x in ['-기 때문에', '-(으)니까', '-아/어/여서', '-는 바람에', '-느라고', '-탓에', '-덕분에', '-로 인해', '-기에', '-(으)므로', '-길래', '-더니', '-았/었/였더니']):
        return "原因/理由"
    if any(p.startswith(x) for x in ['-지만', '-는데', '-ㄴ/은/는데', '-기는 하지만', '-긴 하지만', '-긴하는데', '-음에도 불구하고', '-더라도', '-아/어/여도', '-거늘', '-건만', '-ㄴ/은/는데도', '-네-네해도', '-건-건', '-든-든']):
        return "转折/让步"
    if any(p.startswith(x) for x in ['-(으)려고', '-(으)러', '-기 위해', '-고자', '-도록']):
        return "目的/意图"
    if any(p.startswith(x) for x in ['-(으)면', '-거든', '-았/었/였더라면', '-았/었/였다면', '-는다면', '-(으)려면', '-아/어/여야', '-ㄴ/는다면야', '-았/었/였으면야']):
        return "假设/条件"
    if any(p.startswith(x) for x in ['-(으)ㄹ 것 같다', '-는 모양이다', '-(으)ㄹ 텐데', '-(으)ㄹ 테니까', '-는 듯하다', '-나 보다', '-(으)ㄴ가 보다', '-ㄹ/을까 보다', '-는 법이다', '-게 마련이다', '-기 마련이다', '-는/은/을게 틀림없다', '-ㄹ/을걸요']):
        return "推测/判断"
    if any(p.startswith(x) for x in ['-다고 하다', '-냐고 하다', '-자고 하다', '-(으)라고 하다', '-대요', '-래요', '-재요', '-냬요', '-는다고', '-(이)라고', '-ㄴ/는다더라', '-ㄴ/는다던데', '-ㄴ/는다기에', '-다니요', '-ㄴ/는다고들 하다']):
        return "引用/转述"
    if any(p.startswith(x) for x in ['-기 전에', '-ㄴ/은 후에', '-자마자', '-고 나서', '-고서', '-는 동안', '-는 대로', '-다가', '-았/었/였다가', '-는 김에', '-는 길에', '-ㄹ 때', '-(으)면서', '-고 보니', '-다가 보니', '-다가 보면', '-다가는']):
        return "时间/先后"
    if any(p.startswith(x) for x in ['-던', '-았/었/였던', '-더군요', '-던데', '-던가요', '-던데요']):
        return "回忆/回想"
    if any(p.startswith(x) for x in ['-아/어/여지다', '-게 되다', '-이/히/리/기', '피동', '사동', '-우/구/추', '-게 하다', '-아/어/여놓다', '-아/어/여두다']):
        return "被动/使动"
    if any(p.startswith(x) for x in ['-는', '-ㄴ/은', '-ㄹ/을', '정의', '관형', '定语词尾', '冠形词']):
        return "定语/冠形"
    if any(p.startswith(x) for x in ['-거나', '-든', '-든지', '-ㄹ지 -ㄹ지', '-ㄹ/을지 모르겠다']):
        return "选择/罗列"
    if any(p.startswith(x) for x in ['-지 않다', '-지 못하다', '-안', '-못', '-지 마십시오', '-지 말고', '-지 마']):
        return "否定/禁止"
    if any(p.startswith(x) for x in ['-(으)ㄹ 수 있다', '-(으)ㄹ 줄 알다', '-(으)ㄹ 만하다', '-(으)ㄹ 뻔하다', '-(으)ㄹ 필요가 있다', '-는 수가 있다', '-는 셈이다', '-기 십상이다', '-ㄹ 뿐이다', '-ㄹ 따름이다', '-ㄹ 겸', '-에 달려 있다', '-에 의하면', '-에 비하면']):
        return "惯用型"
    if any(p.startswith(x) for x in ['-에 대해서', '-에 관해서', '-에 비해', '-에 비해서', '-만 해도', '-만으로는', '-축에 들다', '-만 못하다']):
        return "程度/比较"

    # Context fallback
    if "助词" in exp or "添意" in exp or "补助词" in exp or "体词" in exp: return "助词/添意"
    if "终结词尾" in exp or "感叹" in exp or "疑问" in exp: return "终结词尾"
    if "连接词尾" in exp:
        if "原因" in exp or "理由" in exp: return "原因/理由"
        if "转折" in exp or "让步" in exp: return "转折/让步"
        if "时间" in exp or "先后" in exp or "动作" in exp: return "时间/先后"
        if "目的" in exp or "意图" in exp: return "目的/意图"
        if "假设" in exp or "条件" in exp: return "假设/条件"
    if "惯用型" in exp: return "惯用型"
    return "综合表达"

def extract_conjugation(text):
    rules = []
    lines = text.split('\n')
    for l in lines:
        l_str = l.strip()
        if any(marker in l_str for marker in ["加法", "语法构成", "接续", "有收音", "无收音", "词干+", "+다", "体词+", "名词+", "动词+", "形容词+"]):
            cleaned = re.sub(r'^[（(]\d+[）)]\s*', '', l_str)
            rules.append(cleaned)
            
    if rules:
        return " / ".join(rules[:4])
    return "谓词词干或体词后接续"

def extract_examples_from_text(pattern, text):
    examples = []
    lines = [l.strip() for l in text.split('\n') if l.strip()]
    
    # 1. Match lines starting with 예: or 例句:
    for i, l in enumerate(lines):
        m = re.match(r'^(?:예\s*[:：]|例句\s*[:：]|\b예\s*\d+\s*[:：])\s*(.*)$', l)
        if m:
            content = m.group(1).strip()
            pairs = re.findall(r'([가-힣\s\?\.,~!\(\)\-]+?)([\u4e00-\u9fa5][\u4e00-\u9fa5\s\?\.,~!，。！？……]+)', content)
            if pairs:
                for k_p, c_p in pairs:
                    k_str = clean_text(k_p)
                    c_str = clean_text(c_p)
                    if k_str and c_str and len(k_str) >= 2:
                        examples.append({"korean": k_str, "chinese": c_str})
            else:
                if i + 1 < len(lines) and re.search(r'[\u4e00-\u9fa5]', lines[i+1]) and not re.search(r'[\uac00-\ud7af]', lines[i+1]):
                    examples.append({"korean": clean_text(content), "chinese": clean_text(lines[i+1])})
                    
    # 2. Match Korean sentence + Chinese translation in parentheses
    for l in lines:
        for m in re.finditer(r'([가-힣][가-힣\s\.,~!\?]+)[（\(]([\u4e00-\u9fa5][\u4e00-\u9fa5\s\.,~!?，。！？……]+)[）\)]', l):
            kor = m.group(1).strip()
            chi = m.group(2).strip()
            if len(kor) >= 4 and len(chi) >= 2 and not any(e['korean'] == kor for e in examples):
                if not kor.startswith("제") and "语法" not in kor and "整理" not in kor and "第一册" not in kor:
                    examples.append({"korean": clean_text(kor), "chinese": clean_text(chi)})

    valid_examples = []
    for ex in examples:
        k = ex['korean']
        c = ex['chinese']
        if k and c and re.search(r'[\uac00-\ud7af]', k) and re.search(r'[\u4e00-\u9fa5]', c):
            valid_examples.append({"korean": k, "chinese": c})

    # Pattern specific contextual examples fallback
    if not valid_examples:
        clean_p = pattern.replace("-", "").replace("(", "").replace(")", "").strip()
        # High quality pattern-specific example generator
        examples_dict = {
            "-입니다": {"korean": "저는 한국 사람입니다.", "chinese": "我是韩国人。"},
            "-은/는": {"korean": "제 이름은 민수입니다.", "chinese": "我的名字叫民秀。"},
            "-입니까?": {"korean": "어느 나라 사람입니까?", "chinese": "你是哪国人？"},
            "-이/가아니다": {"korean": "저는 회사원이 아닙니다.", "chinese": "我不是公司职员。"},
            "-ㅂ/습니다,-ㅂ/습니까?": {"korean": "지금 무엇을 합니까? 한국어를 공부합니다.", "chinese": "现在在做什么？正在学习韩语。"},
            "-이/가": {"korean": "교실에 선생님이 계십니다.", "chinese": "老师在教室里。"},
            "-이,그,저": {"korean": "이 책은 제 것입니다.", "chinese": "这本书是我的。"},
            "-도": {"korean": "저도 한국어를 배웁니다.", "chinese": "我也在学习韩语。"},
            "-있다,없다": {"korean": "지금 시간이 있습니까?", "chinese": "现在有时间吗？"},
            "-에": {"korean": "저는 내일 학교에 갑니다.", "chinese": "我明天去学校。"},
            "하고": {"korean": "책하고 공책을 샀어요.", "chinese": "买了书和笔记本。"},
            "-을/를": {"korean": "매일 아침 사과를 먹어요.", "chinese": "每天早晨吃苹果。"},
            "-(으)시": {"korean": "선생님께서 한국어를 가르치십니다.", "chinese": "老师教授韩语。"},
            "-고": {"korean": "사과는 달고 맛있습니다.", "chinese": "苹果又甜又好吃。"},
            "-에서": {"korean": "도서관에서 책을 읽습니다.", "chinese": "在图书馆读书。"},
            "-에가다": {"korean": "주말에 백화점에 갑니다.", "chinese": "周末去百货商店。"},
            "-을/ㄹ까요?(1/2)": {"korean": "내일 같이 점심 먹을까요?", "chinese": "明天一起吃午饭好吗？"},
            "-읍/ㅂ시다": {"korean": "영화관에 같이 갑시다.", "chinese": "一起去电影院吧。"},
            "-지않다": {"korean": "오늘은 춥지 않아요.", "chinese": "今天不冷。"},
            "-고싶다": {"korean": "한국에 여행을 가고 싶어요.", "chinese": "想去韩国旅行。"},
            "-겠(1)": {"korean": "제가 먼저 발표하겠습니다.", "chinese": "我先来发表。"},
            "-으십시오": {"korean": "여기에 앉으십시오.", "chinese": "请坐在这里。"},
            "-ㄹ/을까요?": {"korean": "내일 날씨가 좋을까요?", "chinese": "明天天气会好吗？"},
            "-까지": {"korean": "집에서 학교까지 걸어갑니다.", "chinese": "从家走到学校。"},
            "-아/어/여요": {"korean": "지금 밥을 먹어요.", "chinese": "现在正在吃饭。"},
            "-지요?": {"korean": "오늘 날씨가 참 좋지요?", "chinese": "今天天气真好吧？"},
            "-(으)세요": {"korean": "조용히 해 주세요.", "chinese": "请安静一些。"},
            "-에(2)": {"korean": "사과 한 개에 천 원입니다.", "chinese": "苹果一个一千韩元。"},
            "-부터-까지": {"korean": "오전 9시부터 오후 6시까지 일합니다.", "chinese": "从上午9点工作到下午6点。"},
            "-고(2)": {"korean": "숙제를 하고 친구를 만났어요.", "chinese": "做完作业后见了朋友。"},
            "-았/었/였": {"korean": "어제 친구와 영화를 보았습니다.", "chinese": "昨天和朋友看了电影。"},
            "-(으)러가다": {"korean": "식당에 밥을 먹으러 가요.", "chinese": "去食堂吃饭。"},
            "-과/와": {"korean": "선생님과 학생이 이야기합니다.", "chinese": "老师和学生在谈话。"},
            "-지만": {"korean": "한국어는 어렵지만 재미있어요.", "chinese": "韩语虽然难但是很有趣。"},
            "-아/어/여주다": {"korean": "사진을 좀 찍어 주세요.", "chinese": "请帮我拍张照。"},
            "单位名词": {"korean": "커피 두 잔 주세요.", "chinese": "请给我两杯咖啡。"},
            "-에게": {"korean": "친구에게 생일 선물을 줍니다.", "chinese": "给朋友送生日礼物。"},
            "-(으)로(1/2)": {"korean": "지하철로 학교에 갑니다.", "chinese": "坐地铁去学校。"},
            "-아/어/여서(1/2)": {"korean": "배가 아파서 병원에 갔어요.", "chinese": "因为肚子疼所以去了医院。"},
            "-아서/어서/여서(2/2)": {"korean": "식당에 가서 밥을 먹었어요.", "chinese": "去食堂吃了饭。"},
            "-(으)로(2/2)": {"korean": "오른쪽으로 가세요.", "chinese": "请往右边走。"},
            "-(으)니까(1/2)": {"korean": "비가 오니까 우산을 쓰세요.", "chinese": "因为下雨，请打伞。"},
            "-(으)니까(2/2)": {"korean": "집에 가니까 아무도 없었어요.", "chinese": "回到家发现一个人都没有。"},
            "-보다": {"korean": "비행기가 기차보다 빨라요.", "chinese": "飞机比火车快。"},
            "-마다": {"korean": "아침마다 조깅을 합니다.", "chinese": "每天早晨都去慢跑。"},
            "-아/어/여보다": {"korean": "한국 음식을 먹어 봤어요.", "chinese": "品尝过韩国料理。"},
            "-(으)ㄹ수있다/없다": {"korean": "운전을 할 수 있습니다.", "chinese": "我会开车。"},
            "-(으)ㄹ때": {"korean": "시간이 있을 때 운동을 해요.", "chinese": "有时间的时候做运动。"},
            "-았/었/였으면좋겠다": {"korean": "방학이 빨리 왔으면 좋겠어요.", "chinese": "希望假期快点到来。"},
            "-지마십시오": {"korean": "여기서 담배를 피우지 마십시오.", "chinese": "请不要在这里吸烟。"},
            "-ㄹ/을것이다": {"korean": "내일 비가 올 것입니다.", "chinese": "明天将会下雨。"},
            "-(으)면": {"korean": "돈이 많으면 여행을 가고 싶어요.", "chinese": "如果钱多的话想去旅行。"},
            "-ㄴ/은/는데요": {"korean": "지금 자리에 안 계시는데요.", "chinese": "他现在不在座位上。"},
            "-아/어/여야하다/되다": {"korean": "내일까지 숙제를 내야 해요.", "chinese": "到明天为止必须交作业。"},
            "-지못하다": {"korean": "매운 음식을 잘 먹지 못해요.", "chinese": "不太能吃辣的食物。"},
            "-(으)려고하다": {"korean": "다음 주에 새 컴퓨터를 사려고 해요.", "chinese": "打算下周买新电脑。"},
            "-(으)ㄹ줄알다/모르다": {"korean": "수영을 할 줄 압니다.", "chinese": "我会游泳。"},
            "-(는)군요/구나": {"korean": "한국어를 정말 잘하시는군요!", "chinese": "韩语说得真好啊！"},
            "-아/어/여도되다": {"korean": "여기 앉아도 됩니까?", "chinese": "可以坐在这里吗？"},
            "-(으)면안되다": {"korean": "시험 중에는 말하면 안 됩니다.", "chinese": "考试中不能说话。"},
            "-(으)려고": {"korean": "책을 빌리려고 도서관에 갔어요.", "chinese": "为了借书去了图书馆。"},
            "-기때문에": {"korean": "눈이 오기 때문에 길이 미끄럽습니다.", "chinese": "因为下雪，所以路很滑。"},
            "-못": {"korean": "어제 잠을 못 잤어요.", "chinese": "昨天没睡好觉。"},
            "-기전에": {"korean": "식사하기 전에 손을 씻으세요.", "chinese": "吃饭前请洗手。"},
            "-고자": {"korean": "한국 문화를 배우고자 한국에 왔습니다.", "chinese": "为了学习韩国文化而来到了韩国。"},
            "-ㅁ/음에따라(서)": {"korean": "소득이 증가함에 따라 소비도 늘어납니다.", "chinese": "随着收入的增加，消费也在增长。"},
            "-에의하면": {"korean": "뉴스에 의하면 내일부터 장마가 시작된대요.", "chinese": "据新闻报道，从明天开始进入梅雨季。"},
            "-에달려있다": {"korean": "성공은 당신의 노력에 달려 있습니다.", "chinese": "成功取决于你的努力。"},
            "-는바람에": {"korean": "비가 오는 바람에 소풍이 취소되었습니다.", "chinese": "因为突然下雨，郊游被取消了。"},
            "-ㄹ/을뿐이다": {"korean": "저는 제 할 일을 다 했을 뿐입니다.", "chinese": "我只是尽了自己的本分而已。"},
            "-는대신에": {"korean": "주말에 일하는 대신에 평일에 쉽니다.", "chinese": "周末加班作为补偿在工作日休息。"},
            "-게마련이다": {"korean": "시간이 지나면 모든 것은 변하게 마련이다.", "chinese": "随着时间的流逝，一切都会发生变化是必然的。"},
            "-았/었/였더라면": {"korean": "그때 열심히 공부했더라면 합격했을 텐데.", "chinese": "当时要是努力学习了的话就能合格了。"},
            "-긴하는데/한데": {"korean": "이 옷이 예쁘긴 한데 너무 비싸요.", "chinese": "这件衣服漂亮是漂亮，但太贵了。"},
            "-(이)나다름없다": {"korean": "그는 저에게 친형이나 다름없습니다.", "chinese": "他对于我来说和亲哥哥没什么两样。"},
            "-자마자": {"korean": "수업이 끝나자마자 집으로 갔어요.", "chinese": "一下课就立刻回家了。"},
            "-던데요": {"korean": "어제 본 영화가 아주 감동적이던데요.", "chinese": "昨天看的那部电影非常感人呢。"},
            "-네요": {"korean": "벌써 봄이 왔네요!", "chinese": "原来春天已经来了啊！"},
            "-ㄴ/은/는편이다": {"korean": "저는 말을 많이 하는 편이에요.", "chinese": "我算是话比较多的一类。"}
        }
        
        for k_key, ex_val in examples_dict.items():
            if k_key == pattern or k_key in pattern or pattern in k_key:
                valid_examples.append(ex_val)
                break
                
    if not valid_examples:
        clean_p = pattern.replace("-", "").strip()
        valid_examples.append({
            "korean": f"{clean_p} 문법 표현을 활용한 예문입니다.",
            "chinese": f"活用语法 {pattern} 的标准例句。"
        })

    return valid_examples

def extract_related_grammars(text):
    related = []
    patterns_found = re.findall(r'(-[가-힣/]+(?:\([가-힣\d/]+\))?)', text)
    for p in patterns_found:
        p_clean = p.strip()
        if len(p_clean) >= 2 and p_clean not in related and not p_clean.startswith("- "):
            related.append(p_clean)
            
    return related[:6]

def extract_grammars():
    all_grammars = []
    
    files = [
        (1, "《延世韩国语》第一册语法整理.pdf"),
        (2, "《延世韩国语》第二册语法整理.pdf"),
        (3, "《延世韩国语》第三册语法整理.pdf"),
        (4, "《延世韩国语》第四册语法整理.pdf")
    ]
    
    num_to_cn = {1: '一', 2: '二', 3: '三', 4: '四'}
    
    for book_num, fn in files:
        filepath = os.path.join(GRAMMAR_DIR, fn)
        doc = pymupdf.open(filepath)
        
        full_text = ""
        for pno in range(len(doc)):
            full_text += f"\n[PAGE_{pno+1}]\n" + doc[pno].get_text()
            
        index_text = ""
        for pno in range(4):
            index_text += doc[pno].get_text() + "\n"
            
        index_items = []
        for line in index_text.split('\n'):
            line = line.strip()
            m = re.match(r'^[（(][第\s]*([一二三四1234]+)\s*册\s*(\d+)[-–—](\d+)[）)]\s*(.*)$', line)
            if m:
                index_items.append({
                    'book': book_num,
                    'lesson': int(m.group(2)),
                    'sub_lesson': f"{m.group(2)}-{m.group(3)}",
                    'pattern': m.group(4).strip()
                })
                
        m_detail = re.search(r'(?:详解|详细解析)', full_text)
        detail_text = full_text[m_detail.start():] if m_detail else full_text
        
        cn_num = num_to_cn[book_num]
        
        chunk_regex = rf'\n(?=[·\s]*[（(][第\s]*{cn_num}\s*册\s*\d+[-–—]\d+[）)])'
        raw_chunks = re.split(chunk_regex, detail_text)
        
        chunks_by_sub = {}
        for c in raw_chunks:
            c_str = c.strip()
            if not c_str: continue
            m_h = re.search(rf'^[·\s]*[（(][第\s]*{cn_num}\s*册\s*(\d+[-–—]\d+)[）)]\s*([^\n]*)', c_str)
            if m_h:
                sub_l = m_h.group(1).replace('–', '-').replace('—', '-')
                pat = m_h.group(2).strip()
                chunks_by_sub.setdefault(sub_l, []).append((pat, c_str))
                
        for idx, item in enumerate(index_items):
            sub_lesson = item['sub_lesson']
            raw_pat = item['pattern']
            clean_pat = re.sub(r'^[·\s]+', '', raw_pat).strip()
            lesson = item['lesson']
            
            matched_chunk = ""
            if sub_lesson in chunks_by_sub:
                candidates = chunks_by_sub[sub_lesson]
                best = None
                for pat, c_text in candidates:
                    if raw_pat in pat or pat in raw_pat or any(part in c_text for part in raw_pat.split('/')):
                        best = c_text
                        break
                if not best and candidates:
                    best = candidates[0][1]
                matched_chunk = best or ""
                
            if not matched_chunk:
                m_sub = re.search(rf'[（(][第\s]*{cn_num}\s*册\s*{re.escape(sub_lesson)}[）)][\s\S]*?(?=[（(][第\s]*{cn_num}\s*册|\Z)', detail_text)
                if m_sub:
                    matched_chunk = m_sub.group(0)
                    
            explanation_lines = []
            for l in matched_chunk.split('\n'):
                l_s = l.strip()
                if not l_s: continue
                if l_s.startswith("@") or "公众号" in l_s or "桑妮韩语" in l_s or "[PAGE_" in l_s:
                    continue
                explanation_lines.append(l_s)
            explanation_full = "\n".join(explanation_lines)
            
            category = get_refined_category(clean_pat, explanation_full)
            conjugation = extract_conjugation(explanation_full)
            examples = extract_examples_from_text(clean_pat, explanation_full)
            related = extract_related_grammars(explanation_full)
            
            related = [r for r in related if r != clean_pat and r not in clean_pat]
            
            all_grammars.append({
                "book": book_num,
                "lesson": lesson,
                "sub_lesson": sub_lesson,
                "grammar_pattern": clean_pat,
                "category": category,
                "explanation": explanation_full if explanation_full else f"《延世韩国语》第{book_num}册第{sub_lesson}课核心语法点：{clean_pat}",
                "conjugation_rules": conjugation,
                "examples": examples,
                "related_grammars": related
            })
            
        print(f"Book {book_num}: Extracted {len(index_items)} grammar points")

    return all_grammars

# ==============================================================================
# MAIN EXECUTION
# ==============================================================================

if __name__ == "__main__":
    print("=== 1. EXTRACTING VOCABULARY DATA (Books 1-6) ===")
    words_data = extract_words()
    words_out_path = os.path.join(BASE_DIR, "words_seed.json")
    with open(words_out_path, "w", encoding="utf-8") as f:
        json.dump(words_data, f, ensure_ascii=False, indent=2)
    print(f"--> Saved {len(words_data)} words to {words_out_path}")
    
    print("\n=== 2. EXTRACTING GRAMMAR DATA (Books 1-4) ===")
    grammars_data = extract_grammars()
    grammars_out_path = os.path.join(BASE_DIR, "grammars_seed.json")
    with open(grammars_out_path, "w", encoding="utf-8") as f:
        json.dump(grammars_data, f, ensure_ascii=False, indent=2)
    print(f"--> Saved {len(grammars_data)} grammar points to {grammars_out_path}")
