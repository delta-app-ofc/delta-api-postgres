package br.com.delta.delta_api_postgres.modules.last_water_bill.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io.LastWaterBillIO;
import br.com.delta.delta_api_postgres.modules.last_water_bill.entity.LastWaterBill;
import br.com.delta.delta_api_postgres.modules.last_water_bill.mapper.LastWaterBillMapper;
import br.com.delta.delta_api_postgres.modules.last_water_bill.repository.LastWaterBillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LastWaterBillService {

    private final LastWaterBillRepository lastWaterBillRepository;
    private final LastWaterBillMapper lastWaterBillMapper;

    public LastWaterBillIO create(LastWaterBillIO io) {

        if (lastWaterBillRepository.existsByUserIdAndMonth(
                io.userId(),
                io.month()
        )) {
            throw new ResourceAlreadyExistsException(
                    "Já existe uma fatura cadastrada para esse usuário nesse mês"
            );
        }

        LastWaterBill lastWaterBill = lastWaterBillMapper.toEntity(io);

        LastWaterBill saved = lastWaterBillRepository.save(lastWaterBill);

        return lastWaterBillMapper.toIO(saved);
    }

    @Transactional(readOnly = true)
    public List<LastWaterBillIO> findAll() {

        return lastWaterBillRepository.findAll()
                .stream()
                .map(lastWaterBillMapper::toIO)
                .toList();
    }

    @Transactional(readOnly = true)
    public LastWaterBillIO findById(Integer id) {

        LastWaterBill lastWaterBill = lastWaterBillRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fatura não encontrada")
                );

        return lastWaterBillMapper.toIO(lastWaterBill);
    }

    public LastWaterBillIO update(
            Integer id,
            LastWaterBillIO io
    ) {

        LastWaterBill lastWaterBill = lastWaterBillRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fatura não encontrada")
                );

        boolean userOrMonthChanged =
                !lastWaterBill.getUserId().equals(io.userId())
                        || !lastWaterBill.getMonth().equals(io.month());

        if (userOrMonthChanged
                && lastWaterBillRepository.existsByUserIdAndMonth(
                        io.userId(),
                        io.month()
                )) {
            throw new ResourceAlreadyExistsException(
                    "Já existe uma fatura cadastrada para esse usuário nesse mês"
            );
        }

        lastWaterBillMapper.updateEntity(lastWaterBill, io);

        LastWaterBill updated = lastWaterBillRepository.save(lastWaterBill);

        return lastWaterBillMapper.toIO(updated);
    }

    public void delete(Integer id) {

        LastWaterBill lastWaterBill = lastWaterBillRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fatura não encontrada")
                );

        lastWaterBillRepository.delete(lastWaterBill);
    }
}
