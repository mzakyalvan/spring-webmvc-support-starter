package com.tiket.tix.common.web.autoconfigure;

import com.tiket.tix.common.web.rxjava.CompletableReturnValueHandler;
import com.tiket.tix.common.web.rxjava.SingleReturnValueHandler;
import io.reactivex.plugins.RxJavaPlugins;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for registering beans to enable
 * support of RxJava, especially in controller.
 *
 * @author zakyalvan
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@ConditionalOnClass(RxJavaPlugins.class)
public class RxJavaSupportAutoConfiguration extends WebMvcConfigurerAdapter {

    @Configuration
    public static class EnableReturnValueHandlersConfiguration extends WebMvcConfigurerAdapter {
        @Override
        public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
            returnValueHandlers.add(singleReturnValueHandler());
            returnValueHandlers.add(completableReturnValueHandler());
        }

        @Bean
        SingleReturnValueHandler singleReturnValueHandler() {
            return new SingleReturnValueHandler();
        }

        @Bean
        CompletableReturnValueHandler completableReturnValueHandler() {
            return new CompletableReturnValueHandler();
        }
    }
}
