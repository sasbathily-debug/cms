package com.projet.user.mapper;

import com.projet.user.domain.User;
import com.projet.user.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
}
