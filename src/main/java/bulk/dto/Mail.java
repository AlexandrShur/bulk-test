package bulk.dto;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Created by alexandr.shchurenkov on 28-Apr-17.
 */
@Documented
@Constraint(validatedBy = MailValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Mail {

    String message() default "{Mail}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}