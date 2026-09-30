package br.com.unicuritiba.gymcrm.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.unicuritiba.gymcrm.model.Movimentacao;
import br.com.unicuritiba.gymcrm.model.TipoMovimentacao;

public interface MovimentacaoRepository
		extends JpaRepository<Movimentacao, Long> {

	List<Movimentacao> findByTipo(TipoMovimentacao tipo);

	List<Movimentacao> findByCategoriaIgnoreCase(String categoria);

	List<Movimentacao> findByDataBetween(LocalDate inicio, LocalDate fim);

	/** Soma dos valores de um tipo dentro do período (0 quando não há registros). */
	@Query("select coalesce(sum(m.valor), 0) from Movimentacao m "
			+ "where m.tipo = :tipo and m.data between :inicio and :fim")
	BigDecimal somarPorTipoNoPeriodo(
			@Param("tipo") TipoMovimentacao tipo,
			@Param("inicio") LocalDate inicio,
			@Param("fim") LocalDate fim);
}
