package com.ferragem.avila.pdv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ferragem.avila.pdv.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    List<Venda> findByConcluidaTrue();

    @Query(value = "SELECT id FROM venda ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Long findLastId();
}
