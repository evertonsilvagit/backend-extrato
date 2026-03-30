package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Divida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DividaRepository extends JpaRepository<Divida, Long> {
    List<Divida> findAllByUserEmailIgnoreCase(String userEmail);
    Optional<Divida> findByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    boolean existsByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    boolean existsByCategoria_IdAndUserEmailIgnoreCase(Long categoriaId, String userEmail);
}
