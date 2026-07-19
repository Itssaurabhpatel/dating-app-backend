package com.dating.repository;

import com.dating.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
    List<Report> findByReporterId(String reporterId);
    List<Report> findByReportedUserId(String reportedUserId);
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    long countByReportedUserId(String reportedUserId);

    @Modifying
    @Query("UPDATE Report r SET r.status = :status, r.reviewedBy = :moderatorId, r.resolutionNotes = :notes WHERE r.id = :reportId")
    void resolveReport(@Param("reportId") String reportId, @Param("status") Report.ReportStatus status,
                       @Param("moderatorId") String moderatorId, @Param("notes") String notes);
}
