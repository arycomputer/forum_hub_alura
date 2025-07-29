package br.com.desafioalura.forumhub.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface ValidPassword {

    String message() default "A senha deve conter no mínimo 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}