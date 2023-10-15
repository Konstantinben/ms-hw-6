package otus.ms.app.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import otus.ms.app.model.dto.UserDto;
import otus.ms.app.model.entity.AuthUser;
import otus.ms.app.model.entity.User;
import otus.ms.app.model.exception.AccessForbiddenException;
import otus.ms.app.model.mapper.UserMapper;
import otus.ms.app.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import otus.ms.app.security.UserSessionUtil;

import javax.validation.Valid;
import java.util.UUID;

@Tag(name = "UserController", description = "Контроллер для работы с пользователями")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserSessionUtil userSessionUtil;

    @GetMapping ("/{uuid}")
    @PreAuthorize("hasAuthority('users:read')")
    @Operation(summary = "Get user")
    public UserDto getByUuid(@PathVariable("uuid") UUID userUuid) {
        User user = getAuthorizedUserAndCheckUuid(userUuid, true, true);
        return userMapper.toUserDto(user);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('users:write')")
    @Operation(summary = "Update user")
    public UserDto updateProfile(@PathVariable("uuid") UUID userUuid, @RequestBody @Valid UserDto profile) {
        User user = getAuthorizedUserAndCheckUuid(userUuid, true, false);
        userRepository.save(userMapper.toUser(user, profile));
        return userMapper.toUserDto(user);
    }

    @DeleteMapping ("/{uuid}")
    @PreAuthorize("hasAuthority('users:write')")
    @Operation(summary = "Delet user")
    public UserDto deleteByUuid(@PathVariable("uuid") UUID userUuid) {
        User user = getAuthorizedUserAndCheckUuid(userUuid, false, false);
        userRepository.delete(user);

        return userMapper.toUserDto(user);
    }

    private User getAuthorizedUserAndCheckUuid(UUID userUuid, boolean createIfNotExists, boolean insertId) {
        AuthUser authUser = null;
        try {
            authUser = userSessionUtil.getAuthorizedUser();
        } catch (Exception e) {
            log.error("User " + authUser.getUuid() + " cannot update foreign profile.");
            throw new AuthenticationCredentialsNotFoundException("User not authorized.", e);
        }
        if (!userUuid.equals(authUser.getUuid())) {
            log.error("User " + authUser.getUuid() + " cannot update foreign profile.");
            throw new AccessForbiddenException("User " + authUser.getUuid() + " cannot update foreign profile.");
        }
        User user = null;
        final AuthUser authenticatedUser = authUser;
        if (createIfNotExists) {
            user = userRepository
                    .findByUuid(userUuid)
                    .orElseGet(() -> {
                        User emptyUser = userMapper.toUser(authenticatedUser);
                        emptyUser.setId(insertId ? authenticatedUser.getId() : null);
                        return emptyUser;
                    });
        } else {
            user = userRepository
                    .findByUuid(userUuid)
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Cannod find user by " + userUuid));
        }
        return user;
    }
}


