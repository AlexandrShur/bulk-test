package bulk.model;

import bulk.dto.Mail;
import lombok.Setter;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by alexandr.shchurenkov on 27-Apr-17.
 */
@Entity
@Table(name="users")
@Getter
@Setter
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 2, max = 80)
    private String name;

    @Size(min = 3, max = 80)
    @Mail
    private String email;

}