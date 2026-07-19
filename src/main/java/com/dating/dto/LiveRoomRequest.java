package com.dating.dto;

import lombok.Data;
import java.util.List;

@Data
public class LiveRoomRequest {
    private String name;
    private String description;
    private String type; // "audio" or "video"
    private int maxParticipants;
    private String bgImage;
    private List<String> tags;
}
