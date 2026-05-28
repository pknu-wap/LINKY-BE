package com.project.linkybe_project.repository;

import com.project.linkybe_project.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    List<Link> findByDeviceUuidOrderByIdDesc(String deviceUuid);

    List<Link> findByDeviceUuidAndCategoryOrderByIdDesc(String deviceUuid, String category);

    List<Link> findByDeviceUuidAndUrlContainingOrDeviceUuidAndTitleContainingOrderByIdDesc(
            String deviceUuid1, String urlKeyword, String deviceUuid2, String titleKeyword);

    Optional<Link> findByIdAndDeviceUuid(Long id, String deviceUuid);
}
