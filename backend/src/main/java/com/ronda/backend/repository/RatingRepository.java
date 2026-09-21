package com.ronda.backend.repository;

import com.ronda.backend.model.Rating;
import com.ronda.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /** Verificar si un usuario ya califico en una transaccion (unique constraint en Java). */
    boolean existsByTransactionIdAndFromUserId(Long transactionId, Long fromUserId);

    /** Obtener la calificacion que un usuario dejo en una transaccion. */
    Optional<Rating> findByTransactionIdAndFromUserId(Long transactionId, Long fromUserId);

    /** Todas las calificaciones recibidas por un usuario (para calcular promedio). */
    List<Rating> findByToUser(User toUser);

    /** Promedio de estrellas recibidas por un usuario. */
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.toUser.id = :userId")
    Double averageStarsByToUserId(@Param("userId") Long userId);

    /** Cantidad de calificaciones recibidas por un usuario. */
    long countByToUser(User toUser);

    /** Contar calificaciones recibidas (por ID, sin cargar User). */
    long countByToUserId(Long toUserId);

    /** Calificaciones recibidas por un usuario (para mostrar en perfil publico). */
    List<Rating> findByToUserOrderByCreatedAtDesc(User toUser);
    List<Rating> findByFromUser(com.ronda.backend.model.User fromUser);
    void deleteByTransactionId(Long transactionId);
}
