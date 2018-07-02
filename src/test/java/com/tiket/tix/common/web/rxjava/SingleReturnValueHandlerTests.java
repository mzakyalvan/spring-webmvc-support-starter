package com.tiket.tix.common.web.rxjava;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = SampleApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SingleReturnValueHandlerTests extends AbstractJUnit4SpringContextTests {
    @Autowired
    private SampleApplication.RegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int serverPort;

    @Before
    public void setUp() {
        RestAssured.baseURI = UriComponentsBuilder.newInstance()
                .scheme("http").host("localhost").port(serverPort).path("person")
                .build().toUriString();
    }

    @Test
    public void givenValidPersonData_whenRegisteringPerson_thenReturnRegisteredPerson() throws Exception {
        final SampleApplication.Person person = SampleApplication.Person.newPerson()
                .firstName("Zaky").lastName("Alvan")
                .dateOfBirth(LocalDate.of(1985, Month.JUNE, 18))
                .gender(SampleApplication.Gender.MALE)
                .emailAddress("zaky.alvan@tiket.com")
                .phoneNumber("081320144088")
                .build();

        final String id = UUID.randomUUID().toString();
        final LocalDateTime registeredTime = LocalDateTime.now();

        when(registrationService.registerPerson(person)).thenReturn(Single.just(SampleApplication.RegisteredPerson.registeredPerson()
                    .id(id)
                    .firstName(person.getFirstName()).lastName(person.getLastName())
                    .dateOfBirth(person.getDateOfBirth())
                    .gender(person.getGender())
                    .emailAddress(person.getEmailAddress())
                    .phoneNumber(person.getPhoneNumber())
                    .registeredTime(registeredTime)
                    .build()));

        final String requestBody = objectMapper.writeValueAsString(person);

        given().body(requestBody).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when().log().all(true).post()
                .then().log().all(true).assertThat()
                    .statusCode(201).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .body("id", equalTo(id))
                    .body("firstName", equalTo("Zaky"))
                    .body("lastName", equalTo("Alvan"))
                    .body("dateOfBirth", equalTo("1985-06-18"))
                    .body("gender", equalTo("MALE"))
                    .body("emailAddress", equalTo("zaky.alvan@tiket.com"))
                    .body("phoneNumber", equalTo("081320144088"))
                    .body("registeredTime", equalTo(registeredTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));
    }

    @Test
    public void givenInvalidPersonData_whenRegisteringPerson_thenReturnRegisteredPerson() throws Exception {
        /**
         * Invalid person data, no firstName and lastName given.
         */
        final SampleApplication.Person person = SampleApplication.Person.newPerson()
                .dateOfBirth(LocalDate.of(1985, Month.JUNE, 18))
                .gender(SampleApplication.Gender.MALE)
                .emailAddress("zaky.alvan@tiket.com")
                .phoneNumber("081320144088")
                .build();

        final String requestBody = objectMapper.writeValueAsString(person);

        given().body(requestBody).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when().log().all(true).post()
                .then().log().all(true).assertThat().statusCode(400);
    }

    @Test
    public void givenValidPersonIdentifier_whenRetrievePersonDetails_thenReturnRegisteredPerson() {
        final String id = UUID.randomUUID().toString();
        final LocalDateTime registeredTime = LocalDateTime.now();

        when(registrationService.checkRegistered(id)).thenReturn(Completable.complete());

        when(registrationService.personDetails(id)).thenReturn(Single.just(SampleApplication.RegisteredPerson.registeredPerson()
                .id(id)
                .firstName("Zaky").lastName("Alvan")
                .dateOfBirth(LocalDate.of(1985, Month.JUNE, 18))
                .gender(SampleApplication.Gender.MALE)
                .emailAddress("zaky.alvan@tiket.com")
                .phoneNumber("081320144088")
                .registeredTime(registeredTime)
                .build()));

        given().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when().log().all(true).get(id)
                .then().log().all(true).assertThat()
                    .statusCode(200).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .body("id", equalTo(id))
                    .body("firstName", equalTo("Zaky"))
                    .body("lastName", equalTo("Alvan"))
                    .body("dateOfBirth", equalTo("1985-06-18"))
                    .body("gender", equalTo("MALE"))
                    .body("emailAddress", equalTo("zaky.alvan@tiket.com"))
                    .body("phoneNumber", equalTo("081320144088"))
                    .body("registeredTime", equalTo(registeredTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));
    }

    @Test
    public void givenInvalidPersonIdentifier_whenRetrievePersonDetails_thenReturnRegisteredPerson() {
        final String id = UUID.randomUUID().toString();

        when(registrationService.checkRegistered(id))
                .thenReturn(Completable.error(new RuntimeException()));

        given().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when().log().all(true).get(id)
                .then().log().all(true).assertThat().statusCode(404);
    }
}
