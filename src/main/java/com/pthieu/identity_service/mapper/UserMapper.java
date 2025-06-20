package com.pthieu.identity_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.pthieu.identity_service.dto.request.UserCreationRequest;
import com.pthieu.identity_service.dto.request.UserUpdateRequest;
import com.pthieu.identity_service.dto.response.UserResponse;
import com.pthieu.identity_service.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @org.mapstruct.Mapping(target = "id", ignore = true)
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "username", ignore = true)
    void updateUserFromRequest(@MappingTarget User user, UserUpdateRequest request);
}
