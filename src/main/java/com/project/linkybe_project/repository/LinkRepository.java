package com.project.linkybe_project.repository;

import com.project.linkybe_project.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Long> {
}