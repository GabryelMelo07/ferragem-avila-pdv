package com.ferragem.avila.pdv.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.ferragem.avila.pdv.model.Venda;

public interface VendaRepository extends PagingAndSortingRepository<Venda, Long> {
    Venda save(Venda venda);
    
    Optional<Venda> findById(Long id);

    Optional<Venda> findByConcluidaFalse();

    void deleteById(Long id);
    
    @Query("SELECT v FROM Venda v WHERE CAST(v.dataHoraConclusao AS date) BETWEEN :dataInicio AND :dataFim")
    Page<Venda> findByDataHoraConclusaoBetween(Pageable pageable, LocalDate dataInicio, LocalDate dataFim);
}
