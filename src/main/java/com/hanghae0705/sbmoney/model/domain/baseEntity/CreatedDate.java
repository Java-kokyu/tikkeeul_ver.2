package com.hanghae0705.sbmoney.model.domain.baseEntity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class CreatedDate {
    @org.springframework.data.annotation.CreatedDate
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedDate() {
        return createdAt;
    }
}
