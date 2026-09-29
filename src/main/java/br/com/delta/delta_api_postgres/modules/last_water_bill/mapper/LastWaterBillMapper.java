package br.com.delta.delta_api_postgres.modules.last_water_bill.mapper;

import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io.LastWaterBillIO;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.CreateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.request.UpdateLastWaterBillRequest;
import br.com.delta.delta_api_postgres.modules.last_water_bill.entity.LastWaterBill;
import org.springframework.stereotype.Component;

@Component
public class LastWaterBillMapper {

    public LastWaterBillIO fromCreateRequest(CreateLastWaterBillRequest request) {

        return new LastWaterBillIO(
                null,
                request.userId(),
                request.month(),
                request.totalValue(),
                request.m3Value()
        );
    }

    public LastWaterBillIO fromUpdateRequest(
            Integer id,
            UpdateLastWaterBillRequest request
    ) {

        return new LastWaterBillIO(
                id,
                request.userId(),
                request.month(),
                request.totalValue(),
                request.m3Value()
        );
    }

    public LastWaterBillIO toIO(LastWaterBill lastWaterBill) {

        return new LastWaterBillIO(
                lastWaterBill.getId(),
                lastWaterBill.getUserId(),
                lastWaterBill.getMonth(),
                lastWaterBill.getTotalValue(),
                lastWaterBill.getM3Value()
        );
    }

    public LastWaterBill toEntity(LastWaterBillIO io) {

        LastWaterBill lastWaterBill = new LastWaterBill();

        lastWaterBill.setUserId(io.userId());
        lastWaterBill.setMonth(io.month());
        lastWaterBill.setTotalValue(io.totalValue());
        lastWaterBill.setM3Value(io.m3Value());

        return lastWaterBill;
    }

    public void updateEntity(
            LastWaterBill lastWaterBill,
            LastWaterBillIO io
    ) {

        lastWaterBill.setUserId(io.userId());
        lastWaterBill.setMonth(io.month());
        lastWaterBill.setTotalValue(io.totalValue());
        lastWaterBill.setM3Value(io.m3Value());
    }
}
