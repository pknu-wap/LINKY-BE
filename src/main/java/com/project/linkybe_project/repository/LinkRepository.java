package com.project.linkybe_project.repository;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    List<Link> findByUserOrderByIdDesc(User user);

    List<Link> findByUserAndCategoryOrderByIdDesc(User user, String category);

    // URL 또는 제목에 키워드가 포함된 링크 검색
    List<Link> findByUserAndUrlContainingOrUserAndTitleContainingOrderByIdDesc(
            User user1, String urlKeyword, User user2, String titleKeyword);

    Optional<Link> findByIdAndUser(Long id, User user);
}
