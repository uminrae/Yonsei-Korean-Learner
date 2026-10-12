# -*- coding: utf-8 -*-
"""
Validation & Quality Audit for Yonsei Korean Room Seed Datasets
"""

import os
import sys
sys.stdout.reconfigure(encoding='utf-8')
import json
from collections import Counter

BASE_DIR = r"c:\Users\min.qeoleu\Desktop\手机kor"
WORDS_FILE = os.path.join(BASE_DIR, "words_seed.json")
GRAMMARS_FILE = os.path.join(BASE_DIR, "grammars_seed.json")

def validate_words():
    print("================ 1. WORDS VALIDATION ================")
    assert os.path.exists(WORDS_FILE), f"File not found: {WORDS_FILE}"
    
    with open(WORDS_FILE, "r", encoding="utf-8") as f:
        words = json.load(f)
        
    assert isinstance(words, list), "words_seed.json is not a JSON array"
    print(f"Total Words Count: {len(words)}")
    
    required_keys = {"book", "lesson", "cumulative_index", "korean", "chinese", "part_of_speech", "level", "irregular_type"}
    
    by_book = {}
    pos_counter = Counter()
    irreg_counter = Counter()
    
    for idx, w in enumerate(words):
        assert set(w.keys()) == required_keys, f"Item {idx} keys mismatch: {w.keys()}"
        assert isinstance(w["book"], int) and 1 <= w["book"] <= 6, f"Invalid book: {w}"
        assert isinstance(w["lesson"], int) and 1 <= w["lesson"] <= 10, f"Invalid lesson: {w}"
        assert isinstance(w["cumulative_index"], int) and w["cumulative_index"] >= 1, f"Invalid cumulative_index: {w}"
        assert isinstance(w["korean"], str) and w["korean"].strip(), f"Empty korean: {w}"
        assert isinstance(w["chinese"], str) and w["chinese"].strip(), f"Empty chinese: {w}"
        assert isinstance(w["part_of_speech"], str) and w["part_of_speech"].strip(), f"Empty POS: {w}"
        assert isinstance(w["level"], str) and w["level"].strip(), f"Empty level: {w}"
        assert w["irregular_type"] is None or isinstance(w["irregular_type"], str), f"Invalid irregular_type: {w}"
        
        by_book.setdefault(w["book"], []).append(w)
        pos_counter[w["part_of_speech"]] += 1
        if w["irregular_type"]:
            irreg_counter[w["irregular_type"]] += 1
            
    print("\n--- Book Breakdown & Continuous Index Check ---")
    for b in sorted(by_book.keys()):
        book_words = by_book[b]
        cums = [x["cumulative_index"] for x in book_words]
        lessons = sorted(list(set(x["lesson"] for x in book_words)))
        
        # Check continuous sequence from 1 to max(cums)
        expected_seq = list(range(1, len(book_words) + 1))
        assert cums == expected_seq, f"Book {b} cumulative index sequence mismatch! Expected 1..{len(book_words)}, got min={min(cums)}, max={max(cums)}"
        print(f"  Book {b}: {len(book_words)} words | Cumulative: 1..{max(cums)} (100% Contiguous) | Lessons: {lessons[0]}..{lessons[-1]}")
        
    print("\n--- Part of Speech Distribution ---")
    for pos, count in pos_counter.most_common():
        print(f"  {pos}: {count} ({count/len(words)*100:.1f}%)")
        
    print("\n--- Irregular Types Distribution ---")
    for irreg, count in irreg_counter.most_common():
        print(f"  {irreg}: {count}")

    print("\n[SUCCESS] words_seed.json passed all structural and data integrity checks!")
    return words

def validate_grammars():
    print("\n================ 2. GRAMMARS VALIDATION ================")
    assert os.path.exists(GRAMMARS_FILE), f"File not found: {GRAMMARS_FILE}"
    
    with open(GRAMMARS_FILE, "r", encoding="utf-8") as f:
        grammars = json.load(f)
        
    assert isinstance(grammars, list), "grammars_seed.json is not a JSON array"
    print(f"Total Grammars Count: {len(grammars)}")
    
    required_keys = {"book", "lesson", "sub_lesson", "grammar_pattern", "category", "explanation", "conjugation_rules", "examples", "related_grammars"}
    
    by_book = {}
    cat_counter = Counter()
    total_examples = 0
    
    for idx, g in enumerate(grammars):
        assert set(g.keys()) == required_keys, f"Grammar item {idx} keys mismatch: {g.keys()}"
        assert isinstance(g["book"], int) and 1 <= g["book"] <= 4, f"Invalid book: {g}"
        assert isinstance(g["lesson"], int) and 1 <= g["lesson"] <= 10, f"Invalid lesson: {g}"
        assert isinstance(g["sub_lesson"], str) and "-" in g["sub_lesson"], f"Invalid sub_lesson: {g}"
        assert isinstance(g["grammar_pattern"], str) and g["grammar_pattern"].strip(), f"Empty pattern: {g}"
        assert isinstance(g["category"], str) and g["category"].strip(), f"Empty category: {g}"
        assert isinstance(g["explanation"], str) and g["explanation"].strip(), f"Empty explanation: {g}"
        assert isinstance(g["conjugation_rules"], str) and g["conjugation_rules"].strip(), f"Empty conjugation_rules: {g}"
        assert isinstance(g["examples"], list) and len(g["examples"]) >= 1, f"Empty examples: {g}"
        assert isinstance(g["related_grammars"], list), f"Invalid related_grammars: {g}"
        
        for ex in g["examples"]:
            assert isinstance(ex, dict) and "korean" in ex and "chinese" in ex, f"Invalid example object: {ex}"
            assert ex["korean"].strip() and ex["chinese"].strip(), f"Empty example content: {ex}"
            total_examples += 1
            
        by_book.setdefault(g["book"], []).append(g)
        cat_counter[g["category"]] += 1
        
    print("\n--- Book Breakdown & Sub-lesson Check ---")
    for b in sorted(by_book.keys()):
        book_grammars = by_book[b]
        lessons = sorted(list(set(x["lesson"] for x in book_grammars)))
        print(f"  Book {b}: {len(book_grammars)} grammar points | Lessons: {lessons[0]}..{lessons[-1]}")
        
    print(f"\nTotal Bilingual Example Pairs: {total_examples}")
    
    print("\n--- Category Distribution ---")
    for cat, count in cat_counter.most_common():
        print(f"  {cat}: {count}")

    print("\n[SUCCESS] grammars_seed.json passed all structural and data integrity checks!")
    return grammars

if __name__ == "__main__":
    validate_words()
    validate_grammars()
