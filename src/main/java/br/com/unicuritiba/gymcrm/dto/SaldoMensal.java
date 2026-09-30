package br.com.unicuritiba.gymcrm.dto;

import java.math.BigDecimal;

/** Relatório calculado em tempo de consulta; o saldo não é persistido. */
public record SaldoMensal(
		String mes,
		BigDecimal totalReceitas,
		BigDecimal totalDespesas,
		BigDecimal saldo) {
}
