package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Lancamento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    List<Lancamento> findByContaIdOrderByDataAscIdAsc(Long contaId);
    List<Lancamento> findByContaIdAndDataBetweenOrderByDataAscIdAsc(Long contaId, LocalDate de, LocalDate ate);
    List<Lancamento> findByContaIdAndDataBeforeOrderByDataAscIdAsc(Long contaId, LocalDate de);
}
