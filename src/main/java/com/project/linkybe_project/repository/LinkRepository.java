package com.project.linkybe_project.repository;

import com.project.linkybe_project.entity.Link;
import com.project.linkybe_project.entity.SummaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Link 엔티티의 기본 CRUD와 기기별 조회 쿼리를 제공
public interface LinkRepository extends JpaRepository<Link, Long> {

    // 특정 기기의 링크를 최신 ID 순으로 조회
    List<Link> findByDeviceUuidOrderByIdDesc(String deviceUuid);

    // 특정 기기의 링크 중 지정한 카테고리만 최신 ID 순으로 조회
    List<Link> findByDeviceUuidAndCategoryOrderByIdDesc(String deviceUuid, String category);

    // 특정 기기의 링크 중 URL이나 제목에 키워드가 포함된 항목을 조회
    List<Link> findByDeviceUuidAndUrlContainingOrDeviceUuidAndTitleContainingOrderByIdDesc(
            String deviceUuid1, String urlKeyword, String deviceUuid2, String titleKeyword);

    // 링크 ID와 기기 UUID가 모두 일치하는 링크를 조회
    Optional<Link> findByIdAndDeviceUuid(Long id, String deviceUuid);

    // 같은 URL의 완료된 요약 중 가장 최근 데이터를 조회
    Optional<Link> findFirstByUrlAndSummaryStatusAndSummaryIsNotNullOrderByIdDesc(
            String url, SummaryStatus summaryStatus);

    // 특정 기기에 저장된 모든 링크를 삭제하고 삭제 개수를 반환
    long deleteByDeviceUuid(String deviceUuid);
}
