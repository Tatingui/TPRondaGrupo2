package com.ronda.backend.repository;

import com.ronda.backend.model.User;
import com.ronda.backend.model.UserFavorite;
import com.ronda.backend.model.UserFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserFavoriteId> {
    List<UserFavorite> findByUser(User user);
    Optional<UserFavorite> findByUserIdAndPublicationId(Long userId, Long publicationId);
    void deleteByUserIdAndPublicationId(Long userId, Long publicationId);
    boolean existsByUserIdAndPublicationId(Long userId, Long publicationId);
}
