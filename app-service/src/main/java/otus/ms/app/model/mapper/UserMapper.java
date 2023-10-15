package otus.ms.app.model.mapper;

import otus.ms.app.model.dto.UserDto;
import otus.ms.app.model.dto.UserUuidDto;
import otus.ms.app.model.entity.AuthUser;
import otus.ms.app.model.entity.User;
import org.mapstruct.*;

@Mapper(componentModel =  "spring")
public interface UserMapper {

    UserUuidDto toUserUuidDto(User user);

    UserDto toUserDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "uuid", ignore = true)
    User toUser(@MappingTarget User user, UserDto userDto);

    @Mapping(target = "uuid", expression = "java(java.util.UUID.randomUUID())")
    User toUser(UserDto userDto);

    User toUser(AuthUser authUser);
}
