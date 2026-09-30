package br.com.unicuritiba.gymcrm.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.YearMonth;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.unicuritiba.gymcrm.dto.SaldoMensal;
import br.com.unicuritiba.gymcrm.exception.MovimentacaoNaoEncontradaException;
import br.com.unicuritiba.gymcrm.model.Movimentacao;
import br.com.unicuritiba.gymcrm.model.TipoMovimentacao;
import br.com.unicuritiba.gymcrm.repository.MovimentacaoRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

	private final MovimentacaoRepository repositorio;

	MovimentacaoController(MovimentacaoRepository repositorio) {
		this.repositorio = repositorio;
	}

	@PostMapping
	public ResponseEntity<Movimentacao> cadastrar(@Valid @RequestBody Movimentacao movimentacao) {
		movimentacao.setId(null);
		Movimentacao salva = repositorio.save(movimentacao);
		return ResponseEntity.created(URI.create("/movimentacoes/" + salva.getId())).body(salva);
	}

	/** Lista tudo; filtros opcionais por tipo e/ou categoria. */
	@GetMapping
	public ResponseEntity<List<Movimentacao>> listar(
			@RequestParam(required = false) TipoMovimentacao tipo,
			@RequestParam(required = false) String categoria) {

		List<Movimentacao> resultado;
		if (tipo != null && categoria != null) {
			resultado = repositorio.findByTipo(tipo).stream()
					.filter(m -> categoria.equalsIgnoreCase(m.getCategoria()))
					.toList();
		} else if (tipo != null) {
			resultado = repositorio.findByTipo(tipo);
		} else if (categoria != null) {
			resultado = repositorio.findByCategoriaIgnoreCase(categoria);
		} else {
			resultado = repositorio.findAll();
		}
		return ResponseEntity.ok(resultado);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Movimentacao> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(buscarOuFalhar(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Movimentacao> atualizar(
			@PathVariable Long id,
			@Valid @RequestBody Movimentacao dados) {

		buscarOuFalhar(id);
		dados.setId(id);
		return ResponseEntity.ok(repositorio.save(dados));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		buscarOuFalhar(id);
		repositorio.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	/** Relatório de saldo do mês: GET /movimentacoes/saldo?mes=2026-09 */
	@GetMapping("/saldo")
	public ResponseEntity<SaldoMensal> saldoMensal(@RequestParam YearMonth mes) {
		BigDecimal receitas = repositorio.somarPorTipoNoPeriodo(
				TipoMovimentacao.RECEITA, mes.atDay(1), mes.atEndOfMonth());
		BigDecimal despesas = repositorio.somarPorTipoNoPeriodo(
				TipoMovimentacao.DESPESA, mes.atDay(1), mes.atEndOfMonth());
		return ResponseEntity.ok(
				new SaldoMensal(mes.toString(), receitas, despesas, receitas.subtract(despesas)));
	}

	private Movimentacao buscarOuFalhar(Long id) {
		return repositorio.findById(id)
				.orElseThrow(() -> new MovimentacaoNaoEncontradaException(id));
	}
}
