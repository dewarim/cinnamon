package cinnamon.servlet;


import cinnamon.global.ConfThreadLocal;
import cinnamon.trigger.impl.MicroserviceChangeTrigger;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Based on: http://stackoverflow.com/questions/8933054/how-to-log-response-content-from-a-java-web-server
 */
public class ResponseFilter implements Filter {

    Logger log = LoggerFactory.getLogger(ResponseFilter.class);


    @Override
    public void init(FilterConfig config) throws ServletException {
        // NOOP.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (response.getCharacterEncoding() == null) {
            response.setCharacterEncoding("UTF-8"); // Or whatever default. UTF-8 is good for World Domination.
        }

        HttpServletResponseCopier responseCopier = new HttpServletResponseCopier((HttpServletResponse) response);

        try {
            chain.doFilter(request, responseCopier);
            responseCopier.flushBuffer();
        } finally {
            byte[] copy = responseCopier.getCopy();
//            log.debug("response.copy: " + new String(copy, "UTF-8"));

            HttpServletResponse httpServletResponse = ((HttpServletResponse) response);
            for (String url : httpServletResponse.getHeaders("microservice")) {
//                new MicroserviceChangeTrigger().executePostCommand()
                HttpClient httpClient = HttpClientBuilder.create().build();
                RequestBuilder requestCopy = RequestBuilder.create("POST");
                requestCopy.setUri(url);
                HttpServletRequest httpServletRequest = ((HttpServletRequest) request);
                Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (headerName.equals("microservice")) {
                        continue;
                    }
                    requestCopy.setHeader(headerName, httpServletRequest.getHeader(headerName));
                }
                for (Map.Entry<String, String[]> entry : httpServletRequest.getParameterMap().entrySet()) {
                    for (String paramVal : entry.getValue()) {
                        requestCopy.addParameter(entry.getKey(), paramVal);
                    }
                }

                requestCopy.addParameter("cinnamonResponse", new String(copy, "UTF-8"));
                HttpResponse httpResponse = httpClient.execute(requestCopy.build());
                log.debug("Microservice response status:" + httpResponse.getStatusLine());
                log.debug("Microservice response content:" + EntityUtils.toString(httpResponse.getEntity()));
            }

        }
    }

    @Override
    public void destroy() {
        // NOOP.
    }
}
