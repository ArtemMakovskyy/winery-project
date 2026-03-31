package com.winestoreapp.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewEvent extends ApplicationEvent {
    private final Long wineId;
    private final Double newAverageRating;
    private final ReviewAccessType accessType;

    public ReviewEvent(Object source, Long wineId, Double newAverageRating, ReviewAccessType accessType) {
        super(source);
        this.wineId = wineId;
        this.newAverageRating = newAverageRating;
        this.accessType = accessType;
    }
}
