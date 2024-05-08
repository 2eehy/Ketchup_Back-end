package com.devsplan.ketchup.reserve.repository;

import com.devsplan.ketchup.reserve.entity.Reserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReserveRepository extends JpaRepository<Reserve, Long> {
//    List<Reserve> findByResourcesRscCategoryAndRsvStartDttm(String rscCategory, LocalDateTime rsvStartDttm);

    List<Reserve> findByResourcesRscCategoryAndRsvStartDttmBetween(String rscCategory, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
