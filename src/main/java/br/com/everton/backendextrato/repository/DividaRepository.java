package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Divida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DividaRepository extends JpaRepository<Divida, Long> {
}
