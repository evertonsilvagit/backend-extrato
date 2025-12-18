package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {
}
