package bank.testovoe.mapper;

import bank.testovoe.dto.user.UserDto;
import bank.testovoe.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }
}

