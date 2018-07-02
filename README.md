# README

## Introduction

Additional support for Spring web mvc, e.g. RxJava 2 type return value handlers.

Clone this repository and then build using normal maven command.

```sh

$ mvn install

```

## Usages

Add this starter into your project as maven dependency in ```pom.xml``` (Some parts omitted).

```xml
<project>
    <properties>
        <rxjava.version>2.1.16</rxjava.version>
    </properties>

    <dependencies>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>io.reactivex.rxjava2</groupId>
            <artifactId>rxjava</artifactId>
            <version>${rxjava.version}</version>
        </dependency>
    
        <depencency>
            <groupId>com.tiket.tix.common.webmvc</groupId>
            <artifactId>spring-webmvc-support-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </depencency>
        
    </dependencies>
</project>

```

### RxJava

This starter enable us to return ```io.reactivex.Single``` type directly from controller's handler method. No need to create ```org.springframework.web.context.request.async.DeferredResult``` manually to create async handler.

Look at following java snippets as example.

```java

@RestController
@RequestMapping("/registry")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;
    
    @PostMapping
    DeferredResult<Person> registerPerson(@Validated @RequestBody Person person, BindingResult bindings) {
        Single<Person> registration = Single.just(bindings)
            .flatMap(errors -> errors.hasErrors() ? 
                Single.error(new DataBindingException(errors)) : 
                registrationService.registerPerson(person).subscribeOn(Schedulers.io()));
        
        DeferredResult<Person> deferredResult = new DeferredResult<>();
        registration.subscribe(deferredResult::setResult, deferredResult::setErrorResult);
        return deferredResult;
    }
}

```

Instead, return ```io.reactivex.Single``` type directly as shown by following snippet

```java

@RestController
@RequestMapping("/registry")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;
    
    @PostMapping
    Single<Person> registerPerson(@Validated @RequestBody Person person, BindingResult bindings) {
        return Single.just(bindings)
            .flatMap(errors -> errors.hasErrors() ? 
                Single.error(new DataBindingException(errors)) : 
                registrationService.registerPerson(person).subscribeOn(Schedulers.io()));
    }
}

```

Now, we save three (__redundant__) lines of code, drive us to more readable code!

In background, this support enabled by ```org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler``` implementation as following.

```java

package com.tiket.tix.common.web.rxjava;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link AsyncHandlerMethodReturnValueHandler} for handling {@link Single} returned by
 * controller's handler method.
 *
 * @author zakyalvan
 */
public class SingleReturnValueHandler implements AsyncHandlerMethodReturnValueHandler {
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return SingleSource.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public boolean isAsyncReturnValue(Object returnValue, MethodParameter returnType) {
        return returnValue != null && returnValue instanceof SingleSource;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {

        if(returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        Single<Object> singleValue = (Single) returnValue;
        final DeferredResult<Object> deferredResult = new DeferredResult<>();
        singleValue.subscribe(deferredResult::setResult, deferredResult::setErrorResult);
        WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(deferredResult, mavContainer);
    }
}

```

In case you don't want to use this starter, add above return value handler type into your project and configure with following extension of ```org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter```.

```java

@Configuration
public class EnableSingleReturnValueConfiguration extends WebMvcConfigurerAdapter { 
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) { 
        returnValueHandlers.add(singleReturnValueHandler()); 
    }

    @Bean
    SingleReturnValueHandler singleReturnValueHandler() {
        return new SingleReturnValueHandler();
    }
}

```

Please running ```com.tiket.tix.common.web.rxjava.SingleReturnValueHandlerTests``` type inside test directory for testing this feature.