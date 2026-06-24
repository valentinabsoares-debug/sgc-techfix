package com.sgctechfix.repository;

import com.sgctechfix.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByClienteId(Long clienteId);

    List<Venda> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);

    List<Venda> findAllByOrderByDataDesc();

    @Query("SELECT v FROM Venda v WHERE v.cliente.id = :clienteId ORDER BY v.data DESC")
    List<Venda> findByClienteIdOrderByDataDesc(@Param("clienteId") Long clienteId);
}
