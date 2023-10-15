package otus.ms.app.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.*;
import java.util.Date;
import java.util.UUID;

@Table("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    @Pattern(regexp="^([\\+])?([0-9]+)(-[0-9]*)*")
    private String phone;

    @NotBlank
    private String firstName;

    private String lastName;

    @Past
    private Date birthdate;

    @Min(1)
    @Max(130)
    private Short age;

    @Size(min = 1, max = 1)
    @Pattern(regexp = "[mfMF]")
    private String gender;

    private String city;

    private String information;

    private UUID uuid;

}
