package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.views.html.custom.Toast;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

import static com.simonebasile.sampleapp.views.html.HtmlElement.div;


/**
 * Interceptor that taks care of error handling.
 */
@Slf4j
public class ErrorHandlingInterceptor implements HttpInterceptor<InputStream, ApplicationRequestContext> {

    /**
     * Intercepts the request and handles exceptions.
     * If an exception is thrown, a toast is shown to the user.
     * If the exception is a ShowableException, the message is shown to the user.
     * else the user is shown a generic error message.
     * @param request the request
     * @param requestContext the context
     * @param next the next handler
     * @return the response
     */
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> intercept(HttpRequest<? extends InputStream> request, ApplicationRequestContext requestContext, HttpRequestHandler<InputStream, ApplicationRequestContext> next) {
        try {
            return next.handle(request, requestContext);
        } catch (ShowableException e) {
            return new HttpResponse<>(200, new HttpHeaders().add("HX-Reswap", "none"),
                    div().attr("id", "main").hxSwapOob("beforeend")
                    .content(new Toast(e.getMessage(), "error"))
            );
        } catch (Exception e) {
            log.error("An unexpected error was caught by the errorHandler: {}", e.getMessage(), e);
            return new HttpResponse<>(200, new HttpHeaders().add("HX-Reswap", "none"),
                    div().attr("id", "main").hxSwapOob("beforeend")
                            .content(new Toast("An unexpected error occurred", "error"))
            );
        }
    }
}
