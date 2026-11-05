package br.com.delta.delta_api_postgres.modules.analytics.swagger;

import br.com.delta.delta_api_postgres.modules.analytics.dto.io.AlertIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.AuditHistoryChainIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.CapexComparisonIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ConsumptionDailyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ConsumptionDistributionIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceStatusIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.DeviceSummaryIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.InvestmentImpactIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.LeakIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.MonthlyVariationIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.MonitoringHourlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.OrganizationEsgIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.OrganizationMonthlyKpiIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyMonthlyIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.PropertyRankingIO;
import br.com.delta.delta_api_postgres.modules.analytics.dto.io.ResidentialEfficiencyRankingIO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Indicadores", description = "Consumo, metas, dispositivos, alertas e investimentos das instalações autorizadas para o usuário autenticado.")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Sessão ausente, inválida ou expirada"),
        @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao recurso ou consulta não permitida")
})
public interface AnalyticsSwagger {

    @Operation(summary = "Consultar consumo diário", description = "Retorna os totais diários em litros, custo estimado e acumulados da instalação indicada. O usuário precisa ter acesso à instalação.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Série diária consultada"), @ApiResponse(responseCode = "404", description = "Instalação inexistente ou fora do acesso do usuário")})
    ResponseEntity<List<ConsumptionDailyIO>> findConsumptionDailyByPropertyId(@Parameter(description = "Identificador da instalação", example = "152") Integer propertyId);

    @Operation(summary = "Consultar ranking de instalações", description = "Classifica as instalações vinculadas ao usuário por eficiência e custo, usando consumo total e litros por metro quadrado.")
    @ApiResponse(responseCode = "200", description = "Ranking consultado")
    ResponseEntity<List<PropertyRankingIO>> findPropertyRanking();

    @Operation(summary = "Consultar variação mensal", description = "Retorna o consumo mensal e a variação percentual em relação ao mês anterior para a instalação indicada.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Variação mensal consultada"), @ApiResponse(responseCode = "404", description = "Instalação inexistente ou fora do acesso do usuário")})
    ResponseEntity<List<MonthlyVariationIO>> findMonthlyVariationByPropertyId(@Parameter(description = "Identificador da instalação", example = "152") Integer propertyId);

    @Operation(summary = "Consultar distribuição de consumo", description = "Retorna os dados de distribuição e quartis de consumo disponíveis para a consulta autorizada.")
    @ApiResponse(responseCode = "200", description = "Distribuição consultada")
    ResponseEntity<List<ConsumptionDistributionIO>> findConsumptionDistribution();

    @Operation(summary = "Comparar cenários de investimento", description = "Retorna cenários de investimento acessíveis ao usuário, com investimento, redução prevista, economia anual e prazo de retorno em meses.")
    @ApiResponse(responseCode = "200", description = "Cenários comparados")
    ResponseEntity<List<CapexComparisonIO>> findCapexComparison();

    @Operation(summary = "Consultar ranking residencial", description = "Retorna a eficiência residencial do usuário autenticado. O identificador informado deve corresponder ao usuário da sessão.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ranking residencial consultado"), @ApiResponse(responseCode = "403", description = "O identificador não corresponde ao usuário autenticado")})
    ResponseEntity<List<ResidentialEfficiencyRankingIO>> findResidentialEfficiencyRankingByUserId(@Parameter(description = "Identificador do usuário autenticado", example = "42") Integer userId);

    @Operation(summary = "Consultar histórico de auditoria", description = "Retorna o histórico encadeado de alterações tarifárias. Pode ser filtrado por região tarifária; sem filtro, lista o histórico disponível.")
    @ApiResponse(responseCode = "200", description = "Histórico consultado")
    ResponseEntity<List<AuditHistoryChainIO>> findAuditHistory(@Parameter(description = "Identificador da região tarifária para filtrar o histórico", example = "1") @RequestParam(required = false) Integer regionRateId);

    @Operation(summary = "Consultar indicadores mensais da organização", description = "Retorna consumo, custo, comparação com mês anterior, projeção, metas e quantidade de instalações por situação para as organizações associadas ao usuário.")
    @ApiResponse(responseCode = "200", description = "Indicadores mensais consultados")
    ResponseEntity<List<OrganizationMonthlyKpiIO>> findOrganizationMonthlyKpi();

    @Operation(summary = "Consultar indicadores mensais das instalações", description = "Retorna consumo, custo, área, meta e situação de cada instalação. Quando informado, month filtra o mês de referência no formato AAAA-MM-DD.")
    @ApiResponse(responseCode = "200", description = "Indicadores das instalações consultados")
    ResponseEntity<List<PropertyMonthlyIO>> findPropertyMonthly(@Parameter(description = "Data de referência do mês no formato AAAA-MM-DD", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month);

    @Operation(summary = "Consultar monitoramento por hora", description = "Retorna leituras agregadas por instalação e hora dentro da janela recente. O intervalo de agregação é opcional.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Série de monitoramento consultada"), @ApiResponse(responseCode = "400", description = "Quantidade de horas ou janela inválida")})
    ResponseEntity<List<MonitoringHourlyIO>> findMonitoringHourly(@Parameter(description = "Quantidade de horas anteriores à leitura mais recente", example = "24") @RequestParam(defaultValue = "24") Integer hours, @Parameter(description = "Tamanho da janela de agregação em minutos; se omitido, retorna as janelas disponíveis", example = "60") @RequestParam(required = false) Integer windowMinutes);

    @Operation(summary = "Consultar situação dos dispositivos", description = "Retorna conectividade, bateria, identificação e última comunicação dos dispositivos vinculados às instalações acessíveis ao usuário.")
    @ApiResponse(responseCode = "200", description = "Situação dos dispositivos consultada")
    ResponseEntity<List<DeviceStatusIO>> findDeviceStatus();

    @Operation(summary = "Consultar resumo dos dispositivos", description = "Retorna totais de instalações, dispositivos pareados, dispositivos online, baterias baixas e instalações sem dispositivo por organização.")
    @ApiResponse(responseCode = "200", description = "Resumo consultado")
    ResponseEntity<List<DeviceSummaryIO>> findDeviceSummary();

    @Operation(summary = "Consultar vazamentos", description = "Retorna eventos de vazamento das instalações do usuário. O filtro status é opcional e usa o valor de situação retornado pela API.")
    @ApiResponse(responseCode = "200", description = "Eventos de vazamento consultados")
    ResponseEntity<List<LeakIO>> findLeaks(@Parameter(description = "Situação do vazamento para filtrar", example = "ACTIVE") @RequestParam(required = false) String status);

    @Operation(summary = "Consultar indicadores ESG", description = "Retorna estimativas de água economizada, equivalências, metas e cobertura mensal para as organizações associadas ao usuário.")
    @ApiResponse(responseCode = "200", description = "Indicadores ESG consultados")
    ResponseEntity<List<OrganizationEsgIO>> findOrganizationEsg();

    @Operation(summary = "Consultar alertas", description = "Retorna alertas das instalações vinculadas ao usuário. Os filtros de severidade, tipo e somente ativos são opcionais.")
    @ApiResponse(responseCode = "200", description = "Alertas consultados")
    ResponseEntity<List<AlertIO>> findAlerts(@Parameter(description = "Severidade do alerta", example = "CRITICAL") @RequestParam(required = false) String severity, @Parameter(description = "Tipo do alerta", example = "LEAK") @RequestParam(required = false) String alertType, @Parameter(description = "Quando true, retorna somente alertas ainda não resolvidos", example = "true") @RequestParam(required = false) Boolean onlyActive);

    @Operation(summary = "Consultar impacto dos investimentos", description = "Retorna impacto estimado dos cenários acessíveis à organização, incluindo economia anual e equivalências de água.")
    @ApiResponse(responseCode = "200", description = "Impactos consultados")
    ResponseEntity<List<InvestmentImpactIO>> findInvestmentImpact();
}
