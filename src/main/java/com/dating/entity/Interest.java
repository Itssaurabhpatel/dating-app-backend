package com.dating.entity;

import com.dating.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Interest extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    @Column(name = "icon", length = 100)
    private String icon;
    @Column(name = "category", length = 50)
    private String category;
}
