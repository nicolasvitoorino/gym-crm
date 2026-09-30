package br.com.unicuritiba.gymcrm.exception;

public class MovimentacaoNaoEncontradaException extends RuntimeException {

	public MovimentacaoNaoEncontradaException(Long id) {
		super("Movimentação não encontrada: id " + id);
	}
}
