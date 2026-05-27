package com.project.linkybe_project.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeywordExtractor {

    // 불용어 목록 (의미 없는 단어들 제거)
    // 이 단어들은 빈도가 높아도 키워드로 쓰지 않음
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            // 한국어 불용어
            "이", "그", "저", "것", "수", "등", "및", "또", "더", "안",
            "에", "의", "을", "를", "이", "가", "은", "는", "으로", "에서",
            "하는", "있는", "있다", "한다", "된다", "이다", "하다", "되다",
            "이런", "저런", "그런", "하지만", "그러나", "따라서", "때문에",
            "위해", "통해", "대한", "관한", "위한", "라는", "라고", "라며",
            "같은", "같이", "처럼", "에게", "에서", "으로", "부터", "까지",
            "경우", "통한", "관련", "내용", "다음", "이번", "해당", "기반",

            // 영어 불용어
            "the", "a", "an", "is", "are", "was", "were", "be", "been",
            "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "shall", "can", "need",
            "in", "on", "at", "to", "for", "of", "with", "by", "from",
            "and", "or", "but", "if", "as", "it", "its", "this", "that",
            "not", "no", "so", "up", "out", "about", "into", "than",
            "then", "than", "there", "their", "they", "he", "she", "we",
            "you", "your", "our", "my", "his", "her", "all", "any", "each"
    ));

    /**
     * 텍스트에서 상위 6개 키워드를 추출한다.
     *
     * @param text HTML에서 태그가 제거된 순수 텍스트
     * @return 빈도 높은 순으로 정렬된 키워드 최대 6개
     */
    public List<String> extract(String text) {

        // 1단계: 특수문자 제거 (한글, 영문, 숫자, 공백만 남김)
        //   정규식 설명: [^가-힣a-zA-Z0-9\\s] → 허용 목록 외 모든 문자 제거
        String cleaned = text.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ");

        // 2단계: 공백 기준으로 단어 분리 + 소문자 변환
        //   "\\s+" → 공백이 여러 개여도 하나로 취급
        String[] words = cleaned.toLowerCase().split("\\s+");

        // 3단계: 빈도 계산 (Map에 단어별 등장 횟수 저장)
        Map<String, Integer> freqMap = new HashMap<>();
        for (String word : words) {
            // 2글자 미만, 불용어는 건너뜀
            if (word.length() < 2) continue;
            if (STOP_WORDS.contains(word)) continue;

            // freqMap에 없으면 0으로 시작해서 +1
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
        }

        // 4단계: 빈도 내림차순 정렬 후 상위 6개만 선택
        //   TreeMap으로 먼저 알파벳 정렬 → 동일 빈도일 때 항상 같은 결과 보장 (결정론적)
        Map<String, Integer> sortedByAlpha = new TreeMap<>(freqMap);

        return sortedByAlpha.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // 빈도 내림차순
                .limit(6)                    // 상위 6개
                .map(Map.Entry::getKey)      // 키워드 이름만 추출
                .collect(Collectors.toList());
    }
}