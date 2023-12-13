package com.rupay.forex.repository;

import com.rupay.forex.model.Rupay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RupayRepository extends JpaRepository<Rupay, Long> {
    @Query("SELECT DISTINCT productName FROM Rupay WHERE fileName = :fileName")
    List<String> findProductNameByFileName(String fileName);
//    @Query(value = "SELECT * FROM rupay WHERE date = :uploadedDate AND product_name = :productName" +
//            " UNION " +
//            "SELECT * FROM (" +
//            "SELECT * FROM rupay WHERE date = CAST(:uploadedDate AS date) - INTERVAL '1' DAY AND product_name = :productName " +
//            "AND NOT EXISTS (SELECT * FROM rupay r2 WHERE r2.date = :uploadedDate AND r2.product_name = :productName " +
//            "AND r2.transaction_cycle = 4) ORDER BY transaction_cycle DESC LIMIT 1) AS result", nativeQuery = true)
//    List<Rupay> findAllByProductNameAndUploadedDate(@Param("productName") String productName,
//                                                    @Param("uploadedDate") LocalDate uploadedDate);

    @Query(value = "SELECT * FROM rupay WHERE product_name = :productName AND CASE WHEN transaction_cycle = 4 AND NOT EXISTS(" +
            "SELECT * FROM rupay WHERE date = :uploadedDate AND product_name = :productName AND transaction_cycle = 4) " +
            " THEN date = CAST(:uploadedDate AS date) -  INTERVAL '1' DAY " +
            " ELSE date = :uploadedDate END", nativeQuery = true)
    List<Rupay> findAllByProductNameAndUploadedDate(@Param("productName") String productName,
                                                    @Param("uploadedDate") LocalDate uploadedDate);

    /*

    SELECT * FROM rupay WHERE  product_name LIKE '%POS%'
     AND CASE WHEN transaction_cycle = 4 THEN date = date '2023-07-02' - integer '1'
     ELSE date = '2023-07-02'
     END;

      Seq Scan on rupay  (cost=0.00..2.64 rows=8 width=310)
      Filter: (((product_name)::text ~~ '%POS%'::text) AND
      CASE WHEN (transaction_cycle = 4) THEN (date = '2023-07-01'::date)
      ELSE (date = '2023-07-02'::date) END)
    (2 rows)

     SELECT * FROM rupay WHERE product_name LIKE '%POS%' AND
     CASE WHEN transaction_cycle = 4 AND
     NOT EXISTS(SELECT 1 FROM rupay WHERE date = '2023-07-02'
     AND product_name LIKE '%POS%' AND transaction_cycle = 4)
     THEN date = '2023-07-01' ELSE date = '2023-07-02' END;

      Seq Scan on rupay  (cost=2.56..5.20 rows=8 width=310)
     Filter: (((product_name)::text ~~ '%POS%'::text) AND CASE WHEN ((transaction_cycle = 4) AND (NOT $0))
     THEN (date = '2023-07-01'::date) ELSE (date = '2023-07-02'::date) END)
     InitPlan 1 (returns $0)
     ->  Seq Scan on rupay rupay_1  (cost=0.00..2.56 rows=1 width=0)
           Filter: (((product_name)::text ~~ '%POS%'::text) AND (date = '2023-07-02'::date) AND (transaction_cycle = 4))
        (5 rows)
     */

    @Query(value = "SELECT * FROM rupay WHERE product_name = :productName AND date BETWEEN :startDate AND :endDate" +
            " UNION ALL " +
            "SELECT * FROM(" +
            "SELECT * FROM rupay WHERE product_name = :productName AND date = CAST(:startDate AS date) - INTERVAL '1' DAY AND " +
            "NOT EXISTS(SELECT * FROM rupay r WHERE r.product_name = :productName AND r.date = :endDate " +
            "AND r.transaction_cycle = 4) ORDER BY transaction_cycle DESC LIMIT 1) AS result", nativeQuery = true)
    List<Rupay> findAllByProductNameAndUploadedDateBetweenStartDateAndEndDate(@Param("productName") String productName,
                                                                              @Param("startDate")LocalDate startDate,
                                                                              @Param("endDate")LocalDate endDate);
}