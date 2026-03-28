package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Lancamento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    List<Lancamento> findByUserEmailIgnoreCaseAndContaIdOrderByDataAscIdAsc(String userEmail, Long contaId);
    List<Lancamento> findByUserEmailIgnoreCaseAndContaIdAndDataBetweenOrderByDataAscIdAsc(String userEmail, Long contaId, LocalDate de, LocalDate ate);
    List<Lancamento> findByUserEmailIgnoreCaseAndContaIdAndDataBeforeOrderByDataAscIdAsc(String userEmail, Long contaId, LocalDate de);
    Optional<Lancamento> findByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    boolean existsByIdAndUserEmailIgnoreCase(Long id, String userEmail);
}
