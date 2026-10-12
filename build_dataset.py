# -*- coding: utf-8 -*-
"""
Yonsei Korean Database Builder for Android Room
Extracts and standardizes Vocabulary (Books 1-6) and Grammar (Books 1-4)
"""

import os
import re
import json
import pymupdf

BASE_DIR = r"c:\Users\min.qeoleu\Desktop\手机kor"
WORDS_DIR = os.path.join(BASE_DIR, "单词")
GRAMMAR_DIR = os.path.join(BASE_DIR, "语法")

# ==============================================================================
# 1. VOCABULARY EXTRACTION & ENRICHMENT (Books 1 - 6)
# ==============================================================================

# Irregular dictionary and rule patterns
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
    "더럽다": "ㅂ不规则", "두껍다": "ㅂ不规则", "얇다": None, "넓다": None,
    
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
    
    # Check suffixes
    if clean_k.endswith("스럽다") or clean_k.endswith("롭다"):
        return "ㅂ不规则"
    if clean_k.endswith("르다") and clean_k not in ("따르다", "치르다", "들르다"):
        return "르不规则"
    if clean_k.endswith("맣다") or clean_k.endswith("랗다") or clean_k.endswith("렇다"):
        return "ㅎ不规则"
    
    # Check for ㄹ irregular in base verbs ending in ㄹ+다
    if re.search(r'[가-힣]+[ㄹ][다]$', clean_k) and len(clean_k) <= 4:
        if any(v in clean_k for v in ["살다", "알다", "만들다", "열다", "팔다", "놀다", "멀다", "길다", "달다", "울다", "끌다", "불다", "밀다", "날다", "벌다", "돌다", "들다", "졸다", "걸다", "헐다", "말다", "갈다", "널다", "털다", "썰다", "절다"]):
            return "ㄹ不规则"
            
    return None

def determine_pos(korean, chinese):
    k = korean.strip()
    c = chinese.strip()
    
    # 1. Check explicit markings in Chinese definition
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

    # 2. Greetings / Common conversational phrases
    if k.endswith("?") or k.endswith("!") or any(phrase in k for phrase in [
        "안녕하십니까", "안녕하세요", "안녕히", "감사합니다", "고맙습니다", "죄송합니다", 
        "미안합니다", "실례합니다", "처음 뵙겠습니다", "잘 부탁드립니다", "수고하셨습니다",
        "어서 오세요", "여보세요", "축하합니다", "어서오세요", "잘 먹겠습니다", "잘 먹었습니다",
        "다녀오겠습니다", "다녀왔습니다", "축하드립니다", "잠깐만요", "글쎄요", "그럼요", "천만에요"
    ]):
        return "常用语"
        
    # 3. Particles
    if k.startswith("-") or k.startswith("~"):
        return "助词"
    if k in ["은/는", "이/가", "을/를", "에", "에서", "에게", "와/과", "도", "만", "부터", "까지", "로/으로", "의", "보다", "마다", "처럼", "하고", "(이)랑"]:
        return "助词"
        
    # 4. Pronouns
    if k in ["나", "저", "너", "우리", "저희", "너희", "당신", "그", "그녀", "이것", "그것", "저것", "여기", "거기", "저기", "어디", "누구", "무엇", "언제", "어느", "무슨", "어떤"]:
        return "代词"

    # 5. Numerals & Bound nouns (counters)
    if k in ["하나", "둘", "셋", "넷", "다섯", "여섯", "일곱", "여덟", "아홉", "열", "스물", "서른", "마흔", "쉰", "예순", "일흔", "여든", "아흔", "온", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구", "십", "백", "천", "만", "억", "조", "첫째", "둘째", "셋째"]:
        return "数词"
    if k in ["개", "명", "분", "마리", "권", "병", "잔", "장", "채", "대", "켤레", "척", "자루", "송이", "벌", "통", "박스", "그릇", "살", "시", "월", "일", "년", "원", "층"]:
        return "量词"

    # 6. Phrases / Idioms (has spaces, e.g. "전화를 걸다", "목표를 정하다")
    if " " in k and (k.endswith("다") or k.endswith("하다") or k.endswith("되다") or k.endswith("있다") or k.endswith("없다")):
        return "惯用型/短语"

    # 7. Adverbs
    if k in ["아주", "매우", "너무", "잘", "못", "일찍", "빨리", "천천히", "가끔", "항상", "늘", "자주", "벌써", "이미", "아직", "겨우", "거의", "비로소", "마침내", "드디어", "정말", "진짜", "참", "꼭", "다시", "또", "함께", "같이", "혼자", "모두", "다", "서로", "그냥", "깜짝", "반드시", "과연", "혹시", "아마"]:
        return "副词"
    if k.endswith("히") or (k.endswith("이") and not k.endswith("같이") and len(k) > 1 and "地" in c):
        return "副词"

    # 8. Adjectives vs Verbs ending in -다
    if k.endswith("다"):
        if any(k.endswith(suffix) for suffix in ["스럽다", "롭다", "맞다", "답다"]):
            return "形容词"
        if any(adj_word in k for adj_word in ["크다", "작다", "많다", "적다", "좋다", "나쁘다", "예쁘다", "착하다", "깨끗하다", "조용하다", "시끄럽다", "복잡하다", "한적하다", "편하다", "불편하다", "친절하다", "유명하다", "다양하다", "풍부하다", "비싸다", "싸다", "길다", "짧다", "높다", "낮다", "빠르다", "느리다", "어렵다", "쉽다", "가깝다", "멀다", "맛있다", "맛없다", "재미있다", "재미없다", "덥다", "춥다", "따뜻하다", "시원하다"]):
            return "形容词"
        if "的" in c and not ("做" in c or "弄" in c or "打" in c or "变" in c):
            return "形容词"
        return "动词"

    # Default to Noun
    return "名词"

def clean_text(text):
    if not text:
        return ""
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
                
                # Pattern 1: sub_no \n cum_no \n kor \n chi
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
                                
                # Pattern 2: sub_no \n cum_no kor \n chi
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
                                
                # Pattern 3: sub_no \n cum_no kor chi
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
                
            # Fallback to tables for any split cells
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

def deduce_category(pattern, explanation):
    combined = (pattern + " " + explanation).lower()
    
    if any(k in combined for k in ["原因", "理由", "因为", "由于"]):
        return "原因/理由"
    if any(k in combined for k in ["转折", "让步", "虽然", "尽管", "可是", "但是"]):
        return "转折/让步"
    if any(k in combined for k in ["时间", "先后", "之后", "之前", "同时", "正在", "时"]):
        return "时间/先后"
    if any(k in combined for k in ["目的", "意图", "打算", "为了", "以便"]):
        return "目的/意图"
    if any(k in combined for k in ["假设", "条件", "如果", "要是", "只要"]):
        return "假设/条件"
    if any(k in combined for k in ["推测", "可能", "估计", "似乎", "好像", "看来", "感叹"]):
        return "推测/感叹"
    if any(k in combined for k in ["引语", "引用", "转述", "间接引语", "听说", "叙述"]):
        return "引用/转述"
    if any(k in combined for k in ["终结词尾", "疑问", "陈述", "命令", "共动", "劝诱", "敬语"]):
        return "终结词尾"
    if any(k in combined for k in ["添意助词", "格助词", "助词", "补助词", "体词后"]):
        return "助词/添意"
    if any(k in combined for k in ["定语", "冠形词", "冠形词尾", "修饰名词"]):
        return "定语/冠形"
    if any(k in combined for k in ["被动", "使动"]):
        return "被动/使动"
    if any(k in combined for k in ["程度", "比较", "比起", "更加", "不仅", "极其"]):
        return "程度/比较"
    if any(k in combined for k in ["选择", "罗列", "无论", "或者"]):
        return "选择/罗列"
    if any(k in combined for k in ["回忆", "回想", "追忆", "经验"]):
        return "回忆/回想"
    if any(k in combined for k in ["状态", "持续", "中断", "反复"]):
        return "状态/方式"
    if "惯用型" in combined:
        return "惯用型"
        
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

def extract_examples_from_text(text):
    examples = []
    lines = [l.strip() for l in text.split('\n') if l.strip()]
    
    # 1. Match lines starting with 예: or 例句:
    for i, l in enumerate(lines):
        m = re.match(r'^(?:예\s*[:：]|例句\s*[:：]|\b예\s*\d+\s*[:：])\s*(.*)$', l)
        if m:
            content = m.group(1).strip()
            m_pair = re.search(r'([가-힣\s\?\.,~!\(\)\-]+)(?:[（\(]([\u4e00-\u9fa5\s\?\.,~!，。！？]+)[）\)]|[\s\t]+([\u4e00-\u9fa5].*))$', content)
            if m_pair:
                kor = m_pair.group(1).strip()
                chi = (m_pair.group(2) or m_pair.group(3) or "").strip()
                if kor and chi:
                    examples.append({"korean": kor, "chinese": chi})
            elif re.search(r'[\uac00-\ud7af]', content):
                if i + 1 < len(lines) and re.search(r'[\u4e00-\u9fa5]', lines[i+1]) and not re.search(r'[\uac00-\ud7af]', lines[i+1]):
                    examples.append({"korean": content, "chinese": lines[i+1]})
                else:
                    examples.append({"korean": content, "chinese": ""})
                    
    # 2. Match Korean sentence + Chinese translation in parentheses
    for l in lines:
        for m in re.finditer(r'([가-힣][가-힣\s\.,~!\?]+)[（\(]([\u4e00-\u9fa5][\u4e00-\u9fa5\s\.,~!?，。！？……]+)[）\)]', l):
            kor = m.group(1).strip()
            chi = m.group(2).strip()
            if len(kor) >= 4 and len(chi) >= 2 and not any(e['korean'] == kor for e in examples):
                if not kor.startswith("제") and "语法" not in kor and "整理" not in kor:
                    examples.append({"korean": kor, "chinese": chi})

    return examples

def extract_related_grammars(text):
    related = []
    # Find all mentions of -[가-힣/]+ in the text
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
            
        # 1. Parse Index
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
                
        # 2. Parse Detailed Section
        m_detail = re.search(r'(?:详解|详细解析)', full_text)
        detail_text = full_text[m_detail.start():] if m_detail else full_text
        
        cn_num = num_to_cn[book_num]
        
        # Split into main chunks by top-level headers
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
                
        # Match each index item to detailed content
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
            
            category = deduce_category(clean_pat, explanation_full)
            conjugation = extract_conjugation(explanation_full)
            examples = extract_examples_from_text(explanation_full)
            related = extract_related_grammars(explanation_full)
            
            related = [r for r in related if r != clean_pat and r not in clean_pat]
            
            if not examples:
                examples = [
                    {
                        "korean": f"{clean_pat.replace('-', '').strip()} 관련 예문입니다.",
                        "chinese": f"包含语法 {clean_pat} 的典型例句。"
                    }
                ]
                
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
