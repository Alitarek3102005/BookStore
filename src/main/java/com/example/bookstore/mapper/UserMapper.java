package com.example.bookstore.mapper;

import com.example.bookstore.domain.User;
import com.example.bookstore.dto.UserPatchRequest;
import com.example.bookstore.dto.UserRequest;
import com.example.bookstore.dto.UserResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", ignore = true)
    void patchEntityFromRequest(UserPatchRequest request, @MappingTarget User user);
}