package com.tiket.tix.common.web.rxjava;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link AsyncHandlerMethodReturnValueHandler} for handling {@link Completable} returned by
 * controller's handler method.
 *
 * @author zakyalvan
 */
public class CompletableReturnValueHandler implements AsyncHandlerMethodReturnValueHandler {
    @Override
    public boolean isAsyncReturnValue(Object returnValue, MethodParameter returnType) {
        return returnValue != null && returnValue instanceof CompletableSource;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return CompletableSource.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        if(returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }

        Completable completable = (Completable) returnValue;
        final DeferredResult<Object> deferredResult = new DeferredResult<>();
        completable.subscribe(() -> deferredResult.setResult(null), deferredResult::setErrorResult);
        WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(deferredResult, mavContainer);
    }
}
