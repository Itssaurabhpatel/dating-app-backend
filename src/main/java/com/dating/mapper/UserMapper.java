package com.dating.mapper;

import com.dating.dto.RegisterRequest;
import com.dating.dto.UserDto;
import com.dating.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "selfieVerified", constant = "false")
    @Mapping(target = "idVerified", constant = "false")
    @Mapping(target = "isPremium", constant = "false")
    @Mapping(target = "profileCompletion", constant = "0")
    @Mapping(target = "roles", expression = "java(java.util.Set.of(User.Role.ROLE_USER))")
    @Mapping(target = "languages", expression = "java(new java.util.HashSet<>())")
    User toEntity(RegisterRequest request);

    @Mapping(target = "age", expression = "java(com.dating.util.DateUtils.calculateAge(user.getDateOfBirth()))")
    UserDto toDto(User user);
}
