package com.project.linkybeproject.repository;

import com.project.linkybeproject.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// JpaRepository<관리할 엔티티, 그 엔티티의 ID 타입> 을 상속받습니다.
public interface LinkRepository extends JpaRepository<Link, Long> {
    // 여기에 아무 코드를 안 적어도, 저장(save), 조회(findAll) 같은 기본 기능이 다 제공됩니다!
}