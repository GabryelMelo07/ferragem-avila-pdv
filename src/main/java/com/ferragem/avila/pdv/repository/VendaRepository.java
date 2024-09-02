package com.ferragem.avila.pdv.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ferragem.avila.pdv.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    Optional<Venda> findByConcluidaFalse();

    @Query("SELECT v FROM Venda v WHERE CAST(v.dataHoraConclusao AS date) BETWEEN :dataInicio AND :dataFim")
    Page<Venda> findByDataHoraConclusaoBetween(Pageable pageable, LocalDate dataInicio, LocalDate dataFim);
}
