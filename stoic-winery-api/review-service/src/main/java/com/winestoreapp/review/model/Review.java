package com.winestoreapp.review.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@SQLDelete(sql = "UPDATE reviews SET is_deleted = true WHERE id=?")
@Where(clause = "is_deleted=false")
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wine_id", nullable = false)
    private Long wineId;

    private String message;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private LocalDateTime reviewDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public Review(Long userId, Long wineId, String message, int rating) {
        this.userId = userId;
        this.wineId = wineId;
        this.message = message;
        this.rating = rating;
        this.reviewDate = LocalDateTime.now();
    }
}
