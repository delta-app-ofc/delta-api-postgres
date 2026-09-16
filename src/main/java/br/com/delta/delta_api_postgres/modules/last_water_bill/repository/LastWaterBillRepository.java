package br.com.delta.delta_api_postgres.modules.last_water_bill.repository;

import br.com.delta.delta_api_postgres.modules.last_water_bill.entity.LastWaterBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LastWaterBillRepository
        extends JpaRepository<LastWaterBill, Integer> {

    List<LastWaterBill> findByUserId(Integer userId);

    boolean existsByUserIdAndMonth(
            Integer userId,
            LocalDate month
    );
}
