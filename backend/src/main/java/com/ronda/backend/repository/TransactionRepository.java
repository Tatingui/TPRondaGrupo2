package com.ronda.backend.repository;

import com.ronda.backend.model.Transaction;
import com.ronda.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Compras del usuario (el es el buyer), paginadas. */
    Page<Transaction> findByBuyerOrderByCreatedAtDesc(User buyer, Pageable pageable);

    /** Ventas del usuario (el es el seller), paginadas. */
    Page<Transaction> findBySellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    /** Compras filtradas por rango de fechas. */
    Page<Transaction> findByBuyerAndCreatedAtBetweenOrderByCreatedAtDesc(
            User buyer, LocalDateTime from, LocalDateTime to, Pageable pageable);

    /** Ventas filtradas por rango de fechas. */
    Page<Transaction> findBySellerAndCreatedAtBetweenOrderByCreatedAtDesc(
            User seller, LocalDateTime from, LocalDateTime to, Pageable pageable);

    /** Verificar si ya existe una transaccion para una oferta (evitar duplicados). */
    boolean existsByAcceptedOfferId(Long offerId);

    /** Contar operaciones como comprador. */
    long countByBuyer(User buyer);

    /** Contar operaciones como comprador (por ID, sin cargar User). */
    long countByBuyerId(Long buyerId);

    /** Contar operaciones como vendedor. */
    long countBySeller(User seller);

    /** Contar operaciones como vendedor (por ID, sin cargar User). */
    long countBySellerId(Long sellerId);

    List<Transaction> findByBuyer(User buyer);
    List<Transaction> findBySeller(User seller);
}
