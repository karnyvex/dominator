package com.eve.dominator.repository;

import com.eve.dominator.model.MarketStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketStatisticsRepository extends JpaRepository<MarketStatistics, Long> {

    @Query("SELECT ms FROM MarketStatistics ms WHERE ms.typeId = :typeId AND ms.regionId = :regionId ORDER BY ms.date DESC")
    List<MarketStatistics> findByTypeIdAndRegionIdOrderByDateDesc(@Param("typeId") Integer typeId, @Param("regionId") Long regionId);

    @Query("SELECT ms FROM MarketStatistics ms WHERE ms.typeId = :typeId AND ms.regionId = :regionId AND ms.date >= :fromDate")
    List<MarketStatistics> findRecentStatistics(@Param("typeId") Integer typeId, @Param("regionId") Long regionId, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT AVG(ms.volume) FROM MarketStatistics ms WHERE ms.typeId = :typeId AND ms.regionId = :regionId AND ms.date >= :fromDate")
    Optional<Double> getAverageVolume(@Param("typeId") Integer typeId, @Param("regionId") Long regionId, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT AVG(ms.averagePrice) FROM MarketStatistics ms WHERE ms.typeId = :typeId AND ms.regionId = :regionId AND ms.date >= :fromDate")
    Optional<Double> getAveragePrice(@Param("typeId") Integer typeId, @Param("regionId") Long regionId, @Param("fromDate") LocalDate fromDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM MarketStatistics ms WHERE ms.regionId = :regionId")
    void deleteByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT COUNT(ms) FROM MarketStatistics ms WHERE ms.regionId = :regionId")
    long countByRegionId(@Param("regionId") Long regionId);
}
