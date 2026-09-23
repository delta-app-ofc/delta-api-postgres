package br.com.delta.delta_api_postgres.modules.last_water_bill.service;

import br.com.delta.delta_api_postgres.common.exception.ResourceAlreadyExistsException;
import br.com.delta.delta_api_postgres.common.exception.ResourceNotFoundException;
import br.com.delta.delta_api_postgres.modules.last_water_bill.dto.io.LastWaterBillIO;
import br.com.delta.delta_api_postgres.modules.last_water_bill.entity.LastWaterBill;
import br.com.delta.delta_api_postgres.modules.last_water_bill.mapper.LastWaterBillMapper;
import br.com.delta.delta_api_postgres.modules.last_water_bill.repository.LastWaterBillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastWaterBillServiceTest {

    private static final Integer BILL_ID = 1;
    private static final Integer USER_ID = 10;
    private static final LocalDate MONTH = LocalDate.of(2026, 8, 1);
    private static final BigDecimal TOTAL_VALUE = new BigDecimal("150.75");
    private static final BigDecimal M3_VALUE = new BigDecimal("18.50");

    @Mock
    private LastWaterBillRepository lastWaterBillRepository;

    @Mock
    private LastWaterBillMapper lastWaterBillMapper;

    @InjectMocks
    private LastWaterBillService lastWaterBillService;

    @Test
    void create_quandoDadosValidos_deveSalvarERetornarFatura() {
        // Arrange
        LastWaterBillIO input = inputBillIO();
        LastWaterBill bill = bill(null, USER_ID, MONTH);
        LastWaterBill savedBill = bill(BILL_ID, USER_ID, MONTH);
        LastWaterBillIO expected = savedBillIO();

        when(lastWaterBillRepository.existsByUserIdAndMonth(USER_ID, MONTH)).thenReturn(false);
        when(lastWaterBillMapper.toEntity(input)).thenReturn(bill);
        when(lastWaterBillRepository.save(bill)).thenReturn(savedBill);
        when(lastWaterBillMapper.toIO(savedBill)).thenReturn(expected);

        // Act
        LastWaterBillIO result = lastWaterBillService.create(input);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(lastWaterBillRepository).save(bill);
        verify(lastWaterBillMapper).toIO(savedBill);
    }

    @Test
    void create_quandoJaExisteFaturaParaUsuarioEMes_deveLancarResourceAlreadyExistsException() {
        LastWaterBillIO input = inputBillIO();
        when(lastWaterBillRepository.existsByUserIdAndMonth(USER_ID, MONTH)).thenReturn(true);

        assertThatThrownBy(() -> lastWaterBillService.create(input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Já existe uma fatura cadastrada para esse usuário nesse mês");

        verify(lastWaterBillRepository, never()).save(any());
        verifyNoInteractions(lastWaterBillMapper);
    }

    @Test
    void findAll_quandoExistemFaturas_deveRetornarTodasMapeadas() {
        LastWaterBill firstBill = bill(BILL_ID, USER_ID, MONTH);
        LastWaterBill secondBill = bill(2, 20, LocalDate.of(2026, 9, 1));
        LastWaterBillIO firstIO = savedBillIO();
        LastWaterBillIO secondIO = new LastWaterBillIO(
                2,
                20,
                LocalDate.of(2026, 9, 1),
                new BigDecimal("200.00"),
                new BigDecimal("25.00")
        );

        when(lastWaterBillRepository.findAll()).thenReturn(List.of(firstBill, secondBill));
        when(lastWaterBillMapper.toIO(firstBill)).thenReturn(firstIO);
        when(lastWaterBillMapper.toIO(secondBill)).thenReturn(secondIO);

        List<LastWaterBillIO> result = lastWaterBillService.findAll();

        assertThat(result).containsExactly(firstIO, secondIO);
        verify(lastWaterBillMapper).toIO(firstBill);
        verify(lastWaterBillMapper).toIO(secondBill);
    }

    @Test
    void findAll_quandoNaoExistemFaturas_deveRetornarListaVazia() {
        when(lastWaterBillRepository.findAll()).thenReturn(List.of());

        List<LastWaterBillIO> result = lastWaterBillService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(lastWaterBillMapper);
    }

    @Test
    void findById_quandoFaturaExiste_deveRetornarFaturaMapeada() {
        LastWaterBill bill = bill(BILL_ID, USER_ID, MONTH);
        LastWaterBillIO expected = savedBillIO();

        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.of(bill));
        when(lastWaterBillMapper.toIO(bill)).thenReturn(expected);

        LastWaterBillIO result = lastWaterBillService.findById(BILL_ID);

        assertThat(result).isEqualTo(expected);
        verify(lastWaterBillMapper).toIO(bill);
    }

    @Test
    void findById_quandoFaturaNaoExiste_deveLancarResourceNotFoundException() {
        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lastWaterBillService.findById(BILL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Fatura não encontrada");

        verifyNoInteractions(lastWaterBillMapper);
    }

    @Test
    void update_quandoUsuarioEMesNaoMudaram_deveAtualizarSemVerificarDuplicidade() {
        LastWaterBillIO input = new LastWaterBillIO(
                BILL_ID,
                USER_ID,
                MONTH,
                new BigDecimal("175.00"),
                new BigDecimal("20.00")
        );
        LastWaterBill bill = bill(BILL_ID, USER_ID, MONTH);
        LastWaterBill updatedBill = bill(BILL_ID, USER_ID, MONTH);
        updatedBill.setTotalValue(input.totalValue());
        updatedBill.setM3Value(input.m3Value());

        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.of(bill));
        when(lastWaterBillRepository.save(bill)).thenReturn(updatedBill);
        when(lastWaterBillMapper.toIO(updatedBill)).thenReturn(input);

        LastWaterBillIO result = lastWaterBillService.update(BILL_ID, input);

        assertThat(result).isEqualTo(input);

        LastWaterBillIO expected = input;

        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.of(bill));
        when(lastWaterBillRepository.save(bill)).thenReturn(updatedBill);
        when(lastWaterBillMapper.toIO(updatedBill)).thenReturn(expected);

        result = lastWaterBillService.update(BILL_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(lastWaterBillRepository, never()).existsByUserIdAndMonth(any(), any());
        verify(lastWaterBillMapper).updateEntity(bill, input);
        verify(lastWaterBillRepository).save(bill);
    }

    @Test
    void update_quandoUsuarioOuMesMudaramENaoHaDuplicidade_deveAtualizarFatura() {
        Integer newUserId = 20;
        LocalDate newMonth = LocalDate.of(2026, 9, 1);
        LastWaterBillIO input = new LastWaterBillIO(
                BILL_ID,
                newUserId,
                newMonth,
                TOTAL_VALUE,
                M3_VALUE
        );
        LastWaterBill bill = bill(BILL_ID, USER_ID, MONTH);
        LastWaterBill updatedBill = bill(BILL_ID, newUserId, newMonth);

        LastWaterBillIO expected = input;

        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.of(bill));
        when(lastWaterBillRepository.existsByUserIdAndMonth(newUserId, newMonth)).thenReturn(false);
        when(lastWaterBillRepository.save(bill)).thenReturn(updatedBill);
        when(lastWaterBillMapper.toIO(updatedBill)).thenReturn(input);

        LastWaterBillIO result = lastWaterBillService.update(BILL_ID, input);

        assertThat(result).isEqualTo(input);

        when(lastWaterBillMapper.toIO(updatedBill)).thenReturn(expected);

        result = lastWaterBillService.update(BILL_ID, input);

        assertThat(result).isEqualTo(expected);
        verify(lastWaterBillRepository).existsByUserIdAndMonth(newUserId, newMonth);
        verify(lastWaterBillMapper).updateEntity(bill, input);
        verify(lastWaterBillRepository).save(bill);
    }

    @Test
    void update_quandoFaturaNaoExiste_deveLancarResourceNotFoundException() {
        LastWaterBillIO input = inputBillIO();
        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lastWaterBillService.update(BILL_ID, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Fatura não encontrada");

        verify(lastWaterBillRepository, never()).save(any());
        verifyNoInteractions(lastWaterBillMapper);
    }

    @Test
    void update_quandoNovoUsuarioEMesJaPossuemFatura_deveLancarResourceAlreadyExistsException() {
        Integer newUserId = 20;
        LocalDate newMonth = LocalDate.of(2026, 9, 1);
        LastWaterBillIO input = new LastWaterBillIO(
                BILL_ID,
                newUserId,
                newMonth,
                TOTAL_VALUE,
                M3_VALUE
        );
        LastWaterBill bill = bill(BILL_ID, USER_ID, MONTH);

        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.of(bill));
        when(lastWaterBillRepository.existsByUserIdAndMonth(newUserId, newMonth)).thenReturn(true);

        assertThatThrownBy(() -> lastWaterBillService.update(BILL_ID, input))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Já existe uma fatura cadastrada para esse usuário nesse mês");

        verify(lastWaterBillRepository, never()).save(any());
        verifyNoInteractions(lastWaterBillMapper);
    }

    @Test
    void delete_quandoFaturaExiste_deveExcluirFatura() {
        LastWaterBill bill = bill(BILL_ID, USER_ID, MONTH);
        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.of(bill));

        lastWaterBillService.delete(BILL_ID);

        verify(lastWaterBillRepository).delete(bill);
    }

    @Test
    void delete_quandoFaturaNaoExiste_deveLancarResourceNotFoundException() {
        when(lastWaterBillRepository.findById(BILL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lastWaterBillService.delete(BILL_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Fatura não encontrada");

        verify(lastWaterBillRepository, never()).delete(any());
    }

    private LastWaterBillIO inputBillIO() {
        return new LastWaterBillIO(null, USER_ID, MONTH, TOTAL_VALUE, M3_VALUE);
    }

    private LastWaterBillIO savedBillIO() {
        return new LastWaterBillIO(BILL_ID, USER_ID, MONTH, TOTAL_VALUE, M3_VALUE);
    }

    private LastWaterBill bill(Integer id, Integer userId, LocalDate month) {
        LastWaterBill bill = new LastWaterBill();
        bill.setId(id);
        bill.setUserId(userId);
        bill.setMonth(month);
        bill.setTotalValue(TOTAL_VALUE);
        bill.setM3Value(M3_VALUE);
        return bill;
    }
}
