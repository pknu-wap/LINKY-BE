package com.project.linkybe_project.dto;

// 링크 미리보기 화면에 필요한 메타데이터를 담음.
public record LinkMetadataDto(
        // 링크 미리보기에 표시할 제목임.
        String title,
        // 링크 미리보기에 표시할 대표 이미지 URL임.
        String imageUrl,
        // 링크 미리보기에 표시할 설명 문구임.
        String description,
        // 메타데이터를 추출한 원본 URL임.
        String originalUrl
) {
}
