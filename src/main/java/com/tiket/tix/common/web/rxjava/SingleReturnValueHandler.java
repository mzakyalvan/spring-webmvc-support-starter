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
 * {@link AsyncHandlerMethodReturnValueHandler} for handling {@link Single} retruned by
 * controller's handler method.
 *
 * @author zakyalvan
 */
public class SingleReturnValueHandler implements AsyncHandlerMethodReturnValueHandler {
    @Override
    public boolean isAsyncReturnValue(Object returnValue, MethodParameter returnType) {
        return returnValue != null && returnValue instanceof SingleSource;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return SingleSource.class.isAssignableFrom(returnType.getParameterType());
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
