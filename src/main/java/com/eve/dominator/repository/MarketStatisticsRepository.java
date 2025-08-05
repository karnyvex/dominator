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

    @Query("SELECT COUNT(ms) FROM MarketStatistics ms WHERE ms.typeId = :typeId")
    long countByTypeId(@Param("typeId") Integer typeId);

    @Query("SELECT DISTINCT ms.typeId FROM MarketStatistics ms WHERE ms.regionId = :regionId ORDER BY ms.typeId LIMIT :limit")
    List<Integer> findDistinctTypeIdsByRegionId(@Param("regionId") Long regionId, @Param("limit") int limit);

    @Query("SELECT ms FROM MarketStatistics ms WHERE ms.regionId = :regionId")
    List<MarketStatistics> findByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT MAX(ms.date) FROM MarketStatistics ms WHERE ms.regionId = :regionId")
    Optional<LocalDate> findLatestDateByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT DISTINCT ms.typeId FROM MarketStatistics ms WHERE ms.regionId = :regionId AND " +
           "((ms.vwapWeek IS NOT NULL AND ms.vwapWeek > 0 AND ms.volumeWeek IS NOT NULL AND ms.volumeWeek > 0) OR " +
           "(ms.vwapMonth IS NOT NULL AND ms.vwapMonth > 0 AND ms.volumeMonth IS NOT NULL AND ms.volumeMonth > 0) OR " +
           "(ms.vwapQuarter IS NOT NULL AND ms.vwapQuarter > 0 AND ms.volumeQuarter IS NOT NULL AND ms.volumeQuarter > 0) OR " +
           "(ms.vwapYear IS NOT NULL AND ms.vwapYear > 0 AND ms.volumeYear IS NOT NULL AND ms.volumeYear > 0))")
    List<Integer> findTypeIdsWithVwapData(@Param("regionId") Long regionId);

    @Query("SELECT DISTINCT ms.typeId FROM MarketStatistics ms WHERE ms.regionId = :regionId AND " +
           "ms.vwapWeek IS NOT NULL AND ms.vwapWeek > 0 AND ms.volumeWeek IS NOT NULL AND ms.volumeWeek > 0")
    List<Integer> findTypeIdsWithWeeklyVwapData(@Param("regionId") Long regionId);

    @Query("SELECT DISTINCT ms.typeId FROM MarketStatistics ms WHERE ms.regionId = :regionId AND " +
           "ms.vwapMonth IS NOT NULL AND ms.vwapMonth > 0 AND ms.volumeMonth IS NOT NULL AND ms.volumeMonth > 0")
    List<Integer> findTypeIdsWithMonthlyVwapData(@Param("regionId") Long regionId);

    @Query("SELECT DISTINCT ms.typeId FROM MarketStatistics ms WHERE ms.regionId = :regionId AND " +
           "ms.vwapQuarter IS NOT NULL AND ms.vwapQuarter > 0 AND ms.volumeQuarter IS NOT NULL AND ms.volumeQuarter > 0")
    List<Integer> findTypeIdsWithQuarterlyVwapData(@Param("regionId") Long regionId);

    @Query("SELECT DISTINCT ms.typeId FROM MarketStatistics ms WHERE ms.regionId = :regionId AND " +
           "ms.vwapYear IS NOT NULL AND ms.vwapYear > 0 AND ms.volumeYear IS NOT NULL AND ms.volumeYear > 0")
    List<Integer> findTypeIdsWithYearlyVwapData(@Param("regionId") Long regionId);
}
