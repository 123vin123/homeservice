package com.houseease.homeservice.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookingId(UUID bookingId);

    Optional<Review> findByBookingId(UUID bookingId);

    @Query("""
        SELECT r FROM Review r
        LEFT JOIN FETCH r.booking b
        LEFT JOIN FETCH b.service
        LEFT JOIN FETCH r.customer
        LEFT JOIN FETCH r.provider p
        LEFT JOIN FETCH p.user
        WHERE r.provider.id = :providerId
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByProviderIdOrderByCreatedAtDesc(@Param("providerId") UUID providerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider.id = :providerId")
    Double getAverageRatingByProviderId(@Param("providerId") UUID providerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.provider.id = :providerId")
    Long countByProviderId(@Param("providerId") UUID providerId);
}
