package emory.emoryserver.global.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class OpenApiResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getRequestURI().equals("/v3/api-docs")) {
            CharResponseWrapper responseWrapper = new CharResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, responseWrapper);

            String originalContent = responseWrapper.toString();
            String modifiedContent = originalContent.replace("http://emory-server-406346608321.asia-northeast3.run.app",
                    "https://emory-server-406346608321.asia-northeast3.run.app");

            response.setContentLength(modifiedContent.length());
            PrintWriter out = response.getWriter();
            out.write(modifiedContent);
            out.flush();
        } else {
            chain.doFilter(request, response);
        }
    }
}
