package cinnamon.trigger.impl

import cinnamon.PoBox
import cinnamon.servlet.HttpServletResponseCopier
import cinnamon.servlet.ResponseFilter
import cinnamon.trigger.ChangeTrigger
import cinnamon.trigger.ITrigger
import cinnamon.utils.ParamParser
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HTTP
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
public class MicroserviceChangeTrigger implements ITrigger {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public PoBox executePreCommand(PoBox poBox, ChangeTrigger changeTrigger) {
        log.debug("preCommand of MicroserviceChangeTrigger");

        try {
            def url = findRemoteUrl(changeTrigger.config)
            if (!url) {
                log.warn("Found microserviceChangeTrigger without valid remoteServer url. Config is: " +
                        changeTrigger.config)
                return poBox;
            }

            def request = poBox.request
            HttpClient httpClient = HttpClientBuilder.create().build()
            def requestCopy = RequestBuilder.create("POST")
            requestCopy.setUri(url)
            def headerNames = request.headerNames
            while (headerNames.hasMoreElements()) {
                def headerName = headerNames.nextElement()
                if (headerName.equals("microservice")) {
                    continue;
                }
                requestCopy.setHeader(headerName, request.getHeader(headerName))
            }
            for (Map.Entry entry : poBox.params) {
                requestCopy.addParameter(entry.key.toString(), entry.value.toString())
            }
            HttpResponse httpResponse = httpClient.execute(requestCopy.build())
            if (httpResponse.statusLine.statusCode != HttpStatus.SC_OK) {
                poBox.endProcessing = true
            }
            addResponseHeader(httpResponse, poBox.response, url)
        }
        catch (Exception e) {
            log.debug("Failed to execute microserviceChangeTrigger.", e);
        }
        return poBox;
    }

    @Override
    public PoBox executePostCommand(PoBox poBox, ChangeTrigger changeTrigger) {
        log.debug("postCommand MicroserviceChangeTrigger: " + changeTrigger.dump());

        try {
            def url = findRemoteUrl(changeTrigger.config)
            if (!url) {
                log.warn("Found microserviceChangeTrigger without valid remoteServer url. Config is: " +
                        changeTrigger.config)
                return poBox;
            }
            def request = poBox.request
            HttpClient httpClient = HttpClientBuilder.create().build()
            def requestCopy = RequestBuilder.create("POST")
            requestCopy.setUri(url)
            def headerNames = request.headerNames
            while (headerNames.hasMoreElements()) {
                def headerName = headerNames.nextElement()
                if (headerName.equals("microservice")) {
                    continue;
                }
                requestCopy.setHeader(headerName, request.getHeader(headerName))
            }

            // content_len must not be set according to RequestContent.process 
            requestCopy.removeHeaders(HTTP.CONTENT_LEN)

            for (Map.Entry entry : poBox.params) {
                requestCopy.addParameter(entry.key.toString(), entry.value.toString())
            }
            
            HttpServletResponseCopier responseCopier = ResponseFilter.localResponseCopier.get()
            if (responseCopier) {
                responseCopier.flushBuffer()
                requestCopy.addParameter("cinnamon-response", new String(responseCopier.copy, "UTF-8"))
            }
            
            HttpResponse httpResponse = httpClient.execute(requestCopy.build())
            addResponseHeader(httpResponse, poBox.response, url)

        }
        catch (Exception e) {
            log.debug("Failed to execute microserviceChangeTrigger.", e);
        }
        return poBox;

    }

    String findRemoteUrl(String config) {
        def configDoc = ParamParser.parseXmlToDocument(config, "error.param.config")
        def remoteServerNode = configDoc.selectNodes("//remoteServer")
        if (remoteServerNode?.size() > 0) {
            return remoteServerNode[0]?.text
        }
    }

    void addResponseHeader(HttpResponse remoteResponse, myResponse, url) {
        myResponse.addHeader("microservice-url", url)
        if (remoteResponse.entity.contentLength > 0) {
            def entity = remoteResponse.entity
            String responseContent = IOUtils.toString(entity.content)
            myResponse.addHeader("microservice-response", responseContent)
        }
        else {
            myResponse.addHeader("microservice-response", "<no-content/>")
        }
    }
}

