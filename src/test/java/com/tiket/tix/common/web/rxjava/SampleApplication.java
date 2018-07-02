package com.tiket.tix.common.web.rxjava;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Sample application to be tested.
 *
 * @author zakyalvan
 */
@SpringBootApplication
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    /**
     * Sample controller for testing purpose.
     */
    @RestController
    @RequestMapping("/person")
    public static class PeopleController {
        private RegistrationService registrationService;

        public PeopleController(RegistrationService registrationService) {
            this.registrationService = registrationService;
        }

        @PostMapping
        @ResponseStatus(code = HttpStatus.CREATED)
        Single<Person> registerPerson(@Validated @RequestBody Person person, BindingResult bindings) {
            return Single.just(bindings)
                    .flatMap(errors -> errors.hasErrors() ?
                            Single.error(new DataBindingException(errors)) :
                            registrationService.registerPerson(person).subscribeOn(Schedulers.io()));
        }

        @GetMapping("/{id}")
        Single<Person> personDetails(@PathVariable String id) {
            return Single.just(id)
                    .flatMap(identifier -> registrationService.checkRegistered(identifier)
                            .andThen(registrationService.personDetails(identifier)).subscribeOn(Schedulers.io()))
                    .onErrorResumeNext(error -> Single.error(new PersonNotFoundException(error)));
        }
    }

    public interface RegistrationService {
        Single<Person> registerPerson(Person person);
        Single<Person> personDetails(String id);
        Completable checkRegistered(String id);
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @SuppressWarnings("serial")
    public static class Person implements Serializable {
        @NotBlank
        private String firstName;

        private String lastName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateOfBirth;

        @NotNull
        private Gender gender;

        @Email
        @NotBlank
        private String emailAddress;

        @NotBlank
        private String phoneNumber;

        @Builder(builderMethodName = "newPerson")
        protected Person(String firstName, String lastName, LocalDate dateOfBirth, Gender gender, String emailAddress, String phoneNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.dateOfBirth = dateOfBirth;
            this.gender = gender;
            this.emailAddress = emailAddress;
            this.phoneNumber = phoneNumber;
        }
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @SuppressWarnings("serial")
    public static class RegisteredPerson extends Person {
        private String id;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime registeredTime;

        @Builder(builderMethodName = "registeredPerson", builderClassName = "RegisteredPersonBuilder")
        protected RegisteredPerson(String id, String firstName, String lastName, LocalDate dateOfBirth, Gender gender, String emailAddress, String phoneNumber, LocalDateTime registeredTime) {
            super(firstName, lastName, dateOfBirth, gender, emailAddress, phoneNumber);
            this.id = id;
            this.registeredTime = registeredTime;
        }

        public static class RegisteredPersonBuilder {

        }
    }

    public enum Gender {
        MALE,
        FEMALE
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public static class DataBindingException extends RuntimeException {
        private final Errors errors;

        public DataBindingException(Errors errors) {
            this.errors = errors;
        }

        public Errors getErrors() {
            return errors;
        }
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public static class PersonNotFoundException extends RuntimeException {
        public PersonNotFoundException(Throwable cause) {
            super(cause);
        }
    }
}
