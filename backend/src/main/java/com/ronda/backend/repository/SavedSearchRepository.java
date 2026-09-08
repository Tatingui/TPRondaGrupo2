package com.ronda.backend.repository;

import com.ronda.backend.model.SavedSearch;
import com.ronda.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findByUserOrderByCreatedAtDesc(User user);
    Optional<SavedSearch> findByIdAndUser(Long id, User user);
}
