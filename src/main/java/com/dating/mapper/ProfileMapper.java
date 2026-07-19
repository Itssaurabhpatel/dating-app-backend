package com.dating.mapper;

import com.dating.dto.ProfileRequest;
import com.dating.dto.ProfileResponse;
import com.dating.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProfileMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "profileCompletion", ignore = true)
    @Mapping(target = "isVisible", constant = "true")
    @Mapping(target = "hideAge", constant = "false")
    @Mapping(target = "hideDistance", constant = "false")
    @Mapping(target = "lastActive", ignore = true)
    Profile toEntity(ProfileRequest request);

    @Mapping(target = "age", expression = "java(com.dating.util.DateUtils.calculateAge(profile.getDateOfBirth()))")
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "interests", ignore = true)
    ProfileResponse toResponse(Profile profile);
}
