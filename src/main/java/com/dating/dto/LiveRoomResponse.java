package com.dating.dto;

import com.dating.entity.LiveRoom;
import lombok.Data;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LiveRoomResponse {
    private String id;
    private String name;
    private String description;
    private String host;
    private String type;
    private int participants;
    private int maxParticipants;
    private String bgImage;
    private List<String> tags;

    public static LiveRoomResponse fromEntity(LiveRoom room) {
        LiveRoomResponse res = new LiveRoomResponse();
        res.setId(room.getId());
        res.setName(room.getName());
        res.setDescription(room.getDescription());
        res.setHost(room.getHostId());
        res.setType(room.getType());
        res.setParticipants(room.getParticipants());
        res.setMaxParticipants(room.getMaxParticipants());
        res.setBgImage(room.getBgImage());
        if (room.getTags() != null && !room.getTags().isEmpty()) {
            res.setTags(Arrays.asList(room.getTags().split(",")));
        } else {
            res.setTags(List.of());
        }
        return res;
    }
}
