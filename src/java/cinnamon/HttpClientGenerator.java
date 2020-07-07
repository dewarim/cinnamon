package cinnamon;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

public class HttpClientGenerator {

    public static HttpClient createHttpClient() {
//        // from: https://stackoverflow.com/questions/24350150/how-to-override-dns-in-http-connections-in-java
//        /* Custom DNS resolver */
//        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
//            @Override
//            public InetAddress[] resolve(final String host) throws UnknownHostException {
//                if (host.equalsIgnoreCase("localhost")) {
//                    /* If we match the host we're trying to talk to,
//                       return the IP address we want, not what is in DNS */
//                    return new InetAddress[]{ InetAddress.getByName("127.0.0.1")};
//                } else {
//                    /* Else, resolve it as we would normally */
//                    return super.resolve(host);
//                }
//            }
//        };
//
//        /* HttpClientConnectionManager allows us to use custom DnsResolver */
//        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
//                /* We're forced to create a SocketFactory Registry.  Passing null
//                   doesn't force a default Registry, so we re-invent the wheel. */
//                RegistryBuilder.<ConnectionSocketFactory> create()
//                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
//                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
//                        .build(),
//                null, /* Default ConnectionFactory */
//                null, /* Default SchemePortResolver */
//                dnsResolver  /* Our DnsResolver */
//        );

        return HttpClientBuilder.create()
//                .setConnectionManager(connManager)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

}
