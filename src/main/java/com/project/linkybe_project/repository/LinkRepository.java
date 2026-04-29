package com.project.linkybe_project.repository;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findByUser(User user);

    List<Link> findByUserOrderByIdDesc(User user);
}
