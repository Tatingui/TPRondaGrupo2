package com.ronda.backend.service;

import com.ronda.backend.dto.RatingCreateRequest;
import com.ronda.backend.dto.RatingDTO;
import com.ronda.backend.dto.TransactionDTO;
import com.ronda.backend.exception.BadRequestException;
import com.ronda.backend.exception.ForbiddenException;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.*;
import com.ronda.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio para operaciones concretadas (transacciones) y calificaciones.
 * Responsabilidades:
 * - Crear transaccion al aceptar una oferta
 * - Consultar historial de compras/ventas con filtros
 * - Registrar calificaciones respetando la ventana de 7 dias
 * - Recalcular reputacion del usuario calificado
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RatingRepository ratingRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final PublicationRepository publicationRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              RatingRepository ratingRepository,
                              OfferRepository offerRepository,
                              UserRepository userRepository,
                              PublicationRepository publicationRepository) {
        this.transactionRepository = transactionRepository;
        this.ratingRepository = ratingRepository;
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.publicationRepository = publicationRepository;
    }

    // ──────────────────────────────────────────────
    //  Aceptar / Rechazar ofertas
    // ──────────────────────────────────────────────

    /**
     * El vendedor acepta una oferta: cambia su estado, crea la Transaction
     * y marca la publicacion como SOLD.
     */
    @Transactional
    public TransactionDTO acceptOffer(Long offerId, String sellerEmail) {
        Offer offer = findOffer(offerId);
        User seller = findUser(sellerEmail);
        validateSellerOwnsOffer(offer, seller);
        validateOfferPending(offer);

        // Marcar oferta como aceptada
        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepository.save(offer);

        // Marcar publicacion como vendida
        Publication pub = offer.getPublication();
        pub.setState(PublicationState.SOLD);
        publicationRepository.save(pub);

        // Crear la transaccion
        Transaction transaction = new Transaction();
        transaction.setPublication(pub);
        transaction.setBuyer(offer.getBuyer());
        transaction.setSeller(seller);
        transaction.setAcceptedOffer(offer);
        transaction.setFinalAmount(offer.getAmount());
        // deliveryDate queda null hasta que se confirme la entrega
        Transaction saved = transactionRepository.save(transaction);

        return toDTO(saved, seller.getId());
    }

    /**
     * El vendedor rechaza una oferta.
     */
    @Transactional
    public void rejectOffer(Long offerId, String sellerEmail) {
        Offer offer = findOffer(offerId);
        User seller = findUser(sellerEmail);
        validateSellerOwnsOffer(offer, seller);
        validateOfferPending(offer);

        offer.setStatus(OfferStatus.REJECTED);
        offerRepository.save(offer);
    }

    /**
     * El vendedor confirma que la entrega se realizo.
     * Esto habilita la ventana de 7 dias para calificar.
     */
    @Transactional
    public TransactionDTO confirmDelivery(Long transactionId, String email) {
        Transaction tx = findTransaction(transactionId);
        User user = findUser(email);

        // Solo el vendedor o el comprador pueden confirmar
        boolean isParticipant = tx.getBuyer().getId().equals(user.getId())
                || tx.getSeller().getId().equals(user.getId());
        if (!isParticipant) {
            throw new ForbiddenException("No sos parte de esta operación");
        }
        if (tx.getDeliveryDate() != null) {
            throw new BadRequestException("La entrega ya fue confirmada");
        }

        tx.setDeliveryDate(LocalDateTime.now());
        Transaction saved = transactionRepository.save(tx);
        return toDTO(saved, user.getId());
    }

    // ──────────────────────────────────────────────
    //  Historial
    // ──────────────────────────────────────────────

    /**
     * Historial de compras o ventas del usuario, con filtro opcional por fechas.
     * @param tipo "COMPRA" o "VENTA"
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getHistory(String email, String tipo,
                                           LocalDateTime from, LocalDateTime to,
                                           Pageable pageable) {
        User user = findUser(email);

        Page<Transaction> page;
        boolean isCompra = "COMPRA".equalsIgnoreCase(tipo);

        if (from != null && to != null) {
            page = isCompra
                    ? transactionRepository.findByBuyerAndCreatedAtBetweenOrderByCreatedAtDesc(user, from, to, pageable)
                    : transactionRepository.findBySellerAndCreatedAtBetweenOrderByCreatedAtDesc(user, from, to, pageable);
        } else {
            page = isCompra
                    ? transactionRepository.findByBuyerOrderByCreatedAtDesc(user, pageable)
                    : transactionRepository.findBySellerOrderByCreatedAtDesc(user, pageable);
        }

        return page.map(tx -> toDTO(tx, user.getId()));
    }

    // ──────────────────────────────────────────────
    //  Calificaciones
    // ──────────────────────────────────────────────

    /**
     * Calificar a la contraparte de una transaccion.
     * Valida: usuario es parte, entrega confirmada, dentro de 7 dias, no califico antes.
     */
    @Transactional
    public RatingDTO rate(Long transactionId, RatingCreateRequest request, String email) {
        Transaction tx = findTransaction(transactionId);
        User fromUser = findUser(email);

        // Validar que es participante y esta en ventana
        if (!tx.canBeRatedBy(fromUser.getId())) {
            throw new BadRequestException(
                    tx.getDeliveryDate() == null
                            ? "La entrega aún no fue confirmada"
                            : "El plazo de 7 días para calificar ya venció"
            );
        }

        // Validar que no haya calificado antes
        if (ratingRepository.existsByTransactionIdAndFromUserId(transactionId, fromUser.getId())) {
            throw new BadRequestException("Ya calificaste en esta operación");
        }

        // Determinar a quien se califica (la contraparte)
        User toUser = tx.getBuyer().getId().equals(fromUser.getId())
                ? tx.getSeller()
                : tx.getBuyer();

        Rating rating = new Rating();
        rating.setTransaction(tx);
        rating.setFromUser(fromUser);
        rating.setToUser(toUser);
        rating.setStars(request.getStars());
        String comment = request.getComment();
        rating.setComment(comment != null && !comment.isBlank() ? comment.trim() : null);

        Rating saved = ratingRepository.save(rating);

        return toRatingDTO(saved);
    }

    /**
     * Ver la calificacion que el usuario dejo en una transaccion (si existe).
     */
    @Transactional(readOnly = true)
    public RatingDTO getMyRating(Long transactionId, String email) {
        User user = findUser(email);
        return ratingRepository.findByTransactionIdAndFromUserId(transactionId, user.getId())
                .map(this::toRatingDTO)
                .orElse(null);
    }

    // ──────────────────────────────────────────────
    //  Reputacion (usado por UserService y PublicationService)
    // ──────────────────────────────────────────────

    /**
     * Promedio de estrellas recibidas. Devuelve 0 si no tiene calificaciones.
     */
    public double getAverageRating(Long userId) {
        Double avg = ratingRepository.averageStarsByToUserId(userId);
        return avg != null ? avg : 0;
    }

    /**
     * Cantidad de calificaciones recibidas.
     */
    public int getRatingCount(Long userId) {
        return (int) ratingRepository.countByToUserId(userId);
    }

    /**
     * Cantidad de operaciones como comprador.
     */
    public int getPurchaseCount(Long userId) {
        return (int) transactionRepository.countByBuyerId(userId);
    }

    /**
     * Cantidad de operaciones como vendedor.
     */
    public int getSaleCount(Long userId) {
        return (int) transactionRepository.countBySellerId(userId);
    }

    // ──────────────────────────────────────────────
    //  Conversiones
    // ──────────────────────────────────────────────

    private TransactionDTO toDTO(Transaction tx, Long viewerUserId) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(tx.getId());
        dto.setMontoFinal(tx.getFinalAmount());
        dto.setFecha(tx.getCreatedAt());
        dto.setFechaEntrega(tx.getDeliveryDate());
        dto.setTituloPublicacion(tx.getPublication().getTitle());

        // Primera imagen si existe
        List<String> imgs = tx.getPublication().getImageUrls();
        dto.setImagenUrl(imgs != null && !imgs.isEmpty() ? imgs.get(0) : null);

        // Tipo y contraparte segun quien consulta
        boolean isCompra = tx.getBuyer().getId().equals(viewerUserId);
        dto.setTipo(isCompra ? "COMPRA" : "VENTA");

        User contraparte = isCompra ? tx.getSeller() : tx.getBuyer();
        dto.setNombreContraparte(contraparte.getNombre());
        dto.setIdContraparte(contraparte.getId());

        // Puede calificar?
        dto.setPuedeCalificar(tx.canBeRatedBy(viewerUserId));
        dto.setYaCalificado(ratingRepository.existsByTransactionIdAndFromUserId(tx.getId(), viewerUserId));

        return dto;
    }

    private RatingDTO toRatingDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setStars(rating.getStars());
        dto.setComment(rating.getComment());
        dto.setFromUserName(rating.getFromUser().getNombre());
        dto.setFromUserId(rating.getFromUser().getId());
        dto.setCreatedAt(rating.getCreatedAt());
        return dto;
    }

    // ──────────────────────────────────────────────
    //  Helpers privados
    // ──────────────────────────────────────────────

    private Offer findOffer(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada"));
    }

    private Transaction findTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operación no encontrada"));
    }

    private User findUser(String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private void validateSellerOwnsOffer(Offer offer, User seller) {
        if (!offer.getPublication().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("Solo el vendedor puede gestionar esta oferta");
        }
    }

    private void validateOfferPending(Offer offer) {
        if (offer.getEffectiveStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("La oferta ya no está pendiente (estado: "
                    + offer.getEffectiveStatus() + ")");
        }
    }
}
