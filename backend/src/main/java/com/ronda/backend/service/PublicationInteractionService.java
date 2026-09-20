package com.ronda.backend.service;

import com.ronda.backend.dto.OfferCreateRequest;
import com.ronda.backend.dto.OfferDTO;
import com.ronda.backend.dto.QuestionDTO;
import com.ronda.backend.exception.BadRequestException;
import com.ronda.backend.exception.ForbiddenException;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.Offer;
import com.ronda.backend.model.OfferStatus;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.PublicationState;
import com.ronda.backend.model.Question;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.OfferRepository;
import com.ronda.backend.repository.PublicationRepository;
import com.ronda.backend.repository.QuestionRepository;
import com.ronda.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Acciones de un interesado sobre una publicacion (preguntar, ofertar)
 * y la respuesta del vendedor a las preguntas.
 */
@Service
public class PublicationInteractionService {

    /** Plazo de vigencia de una oferta; pasado este tiempo caduca. */
    static final int OFFER_VALIDITY_HOURS = 48;

    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OfferRepository offerRepository;

    public PublicationInteractionService(PublicationRepository publicationRepository,
                                         UserRepository userRepository,
                                         QuestionRepository questionRepository,
                                         OfferRepository offerRepository) {
        this.publicationRepository = publicationRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.offerRepository = offerRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestions(Long publicationId) {
        findPublication(publicationId);
        return questionRepository.findByPublicationIdOrderByCreatedAtDesc(publicationId).stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionDTO ask(Long publicationId, String text, String email) {
        User asker = findUser(email);
        Publication pub = findPublication(publicationId);
        if (isSeller(pub, asker)) {
            throw new BadRequestException("No podés preguntar en tu propia publicación");
        }
        requireActive(pub);

        Question question = new Question();
        question.setPublication(pub);
        question.setAsker(asker);
        question.setText(text.trim());
        return toQuestionDTO(questionRepository.save(question));
    }

    @Transactional
    public QuestionDTO answer(Long questionId, String text, String email) {
        User user = findUser(email);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada"));
        if (!isSeller(question.getPublication(), user)) {
            throw new ForbiddenException("Solo el vendedor puede responder");
        }
        if (question.getAnswer() != null) {
            throw new BadRequestException("La pregunta ya fue respondida");
        }
        question.setAnswer(text.trim());
        question.setAnsweredAt(LocalDateTime.now());
        return toQuestionDTO(questionRepository.save(question));
    }

    @Transactional
    public OfferDTO makeOffer(Long publicationId, OfferCreateRequest request, String email) {
        User buyer = findUser(email);
        Publication pub = findPublication(publicationId);
        if (isSeller(pub, buyer)) {
            throw new BadRequestException("No podés ofertar en tu propia publicación");
        }
        requireActive(pub);

        boolean hasPending = offerRepository
                .findFirstByPublicationIdAndBuyerIdOrderByCreatedAtDesc(publicationId, buyer.getId())
                .map(o -> o.getEffectiveStatus() == OfferStatus.PENDING)
                .orElse(false);
        if (hasPending) {
            throw new BadRequestException("Ya tenés una oferta pendiente para esta publicación");
        }

        Offer offer = new Offer();
        offer.setPublication(pub);
        offer.setBuyer(buyer);
        offer.setSeller(pub.getSeller());
        offer.setAmount(request.getAmount());
        String message = request.getMessage();
        offer.setMessage(message != null && !message.isBlank() ? message.trim() : null);
        offer.setStatus(OfferStatus.PENDING);
        offer.setExpiresAt(LocalDateTime.now().plusHours(OFFER_VALIDITY_HOURS));
        return convertirOferta(offerRepository.save(offer));
    }

    public static OfferDTO convertirOferta(Offer offer) {
        if (offer == null) return null;
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setPublicationId(offer.getPublication().getId());
        dto.setAmount(offer.getAmount());
        dto.setMessage(offer.getMessage());
        dto.setStatus(offer.getEffectiveStatus());
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setExpiresAt(offer.getExpiresAt());
        return dto;
    }

    private QuestionDTO toQuestionDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setText(q.getText());
        dto.setAnswer(q.getAnswer());
        dto.setAskerName(q.getAsker() != null ? q.getAsker().getNombre() : null);
        dto.setCreatedAt(q.getCreatedAt());
        dto.setAnsweredAt(q.getAnsweredAt());
        return dto;
    }

    private boolean isSeller(Publication pub, User user) {
        return pub.getSeller() != null && pub.getSeller().getId().equals(user.getId());
    }

    private void requireActive(Publication pub) {
        if (pub.getState() != PublicationState.ACTIVE) {
            throw new BadRequestException("La publicación no está activa");
        }
    }

    private Publication findPublication(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
    }

    private User findUser(String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
