package otus.ms.app.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserDto {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    private String lastName;

    @Pattern(regexp="^([\\+])?([0-9]+)(-[0-9]*)*")
    private String phone;

    @Past
    private Date birthdate;

    @Size(min = 1, max = 1)
    @Pattern(regexp = "[mfMF]")
    private String gender;

    private Integer age;
    private String city;
    private String information;
    private UUID uuid;
}
