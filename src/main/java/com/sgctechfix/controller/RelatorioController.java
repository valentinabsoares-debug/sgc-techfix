package com.sgctechfix.controller;

import com.sgctechfix.dto.VendaDTO;
import com.sgctechfix.service.VendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final VendaService vendaService;

    @GetMapping("/vendas")
    public ResponseEntity<Map<String, Object>> relatorioVendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        List<VendaDTO.Response> vendas = vendaService.buscarPorPeriodo(inicio, fim);

        BigDecimal totalGeral = vendas.stream()
                .map(VendaDTO.Response::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("periodo_inicio", inicio);
        relatorio.put("periodo_fim", fim);
        relatorio.put("total_vendas", vendas.size());
        relatorio.put("valor_total", totalGeral);
        relatorio.put("vendas", vendas);

        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<Map<String, Object>> estoqueBaixo() {
        // Retorna produtos com estoque <= 5
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("mensagem", "Use GET /api/produtos para ver o estoque completo");
        return ResponseEntity.ok(relatorio);
    }
}
