package com.ronda.backend.controller;

import com.ronda.backend.dto.RatingCreateRequest;
import com.ronda.backend.dto.RatingDTO;
import com.ronda.backend.dto.TransactionDTO;
import com.ronda.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ──────────────────────────────────────────────
    //  Gestión de ofertas (aceptar / rechazar)
    // ──────────────────────────────────────────────

    /**
     * PUT /transactions/offers/{offerId}/accept
     * El vendedor acepta una oferta → crea la transaccion y marca la publicacion SOLD.
     */
    @PutMapping("/offers/{offerId}/accept")
    public ResponseEntity<TransactionDTO> acceptOffer(@PathVariable Long offerId,
                                                      Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TransactionDTO dto = transactionService.acceptOffer(offerId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * PUT /transactions/offers/{offerId}/reject
     * El vendedor rechaza una oferta.
     */
    @PutMapping("/offers/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable Long offerId,
                                            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        transactionService.rejectOffer(offerId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    // ──────────────────────────────────────────────
    //  Entrega
    // ──────────────────────────────────────────────

    /**
     * POST /transactions/{id}/delivery
     * Confirma que la entrega se realizo. Habilita la ventana de 7 dias para calificar.
     */
    @PostMapping("/{id}/delivery")
    public ResponseEntity<TransactionDTO> confirmDelivery(@PathVariable Long id,
                                                          Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TransactionDTO dto = transactionService.confirmDelivery(id, authentication.getName());
        return ResponseEntity.ok(dto);
    }

    // ──────────────────────────────────────────────
    //  Historial
    // ──────────────────────────────────────────────

    /**
     * GET /transactions/history?tipo=COMPRA|VENTA&from=...&to=...
     * Historial de operaciones del usuario autenticado.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<TransactionDTO>> getHistory(
            @RequestParam(defaultValue = "COMPRA") String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<TransactionDTO> page = transactionService.getHistory(
                authentication.getName(), tipo, from, to, pageable);
        return ResponseEntity.ok(page);
    }

    // ──────────────────────────────────────────────
    //  Calificaciones
    // ──────────────────────────────────────────────

    /**
     * POST /transactions/{id}/rating
     * Calificar a la contraparte (1-5 estrellas + comentario opcional).
     */
    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingDTO> rate(@PathVariable Long id,
                                          @Valid @RequestBody RatingCreateRequest request,
                                          Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RatingDTO dto = transactionService.rate(id, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * GET /transactions/{id}/rating
     * Ver la calificacion que el usuario dejo en esta transaccion (null si no califico).
     */
    @GetMapping("/{id}/rating")
    public ResponseEntity<RatingDTO> getMyRating(@PathVariable Long id,
                                                  Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RatingDTO dto = transactionService.getMyRating(id, authentication.getName());
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }
}
