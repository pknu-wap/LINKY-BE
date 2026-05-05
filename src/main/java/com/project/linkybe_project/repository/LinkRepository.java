package com.project.linkybe_project.repository;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    // 해당 유저의 전체 링크 (최신순)
    List<Link> findByUserOrderByIdDesc(User user);

    // 카테고리 필터
    List<Link> findByUserAndCategoryOrderByIdDesc(User user, String category);

    // URL 또는 제목 키워드 검색
    List<Link> findByUserAndUrlContainingOrUserAndTitleContainingOrderByIdDesc(
            User user1, String urlKeyword, User user2, String titleKeyword);

    // 삭제 / 수정용 — user + id 동시 검증 (핵심: 타 유저 링크 접근 차단)
    Optional<Link> findByIdAndUser(Long id, User user);

    // 해당 유저의 링크인지 존재 여부만 확인
    boolean existsByIdAndUser(Long id, User user);
}
