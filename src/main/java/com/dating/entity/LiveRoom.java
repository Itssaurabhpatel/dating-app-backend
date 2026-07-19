package com.dating.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "live_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiveRoom extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "host_id", nullable = false)
    private String hostId;
    
    @Column(name = "type", nullable = false)
    private String type; // "audio" or "video"
    
    @Column(name = "participants", nullable = false)
    @Builder.Default
    private int participants = 1;
    
    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;
    
    @Column(name = "bg_image")
    private String bgImage;
    
    @Column(name = "tags")
    private String tags; // Comma separated for simplicity since it's just mock/fast impl
}
