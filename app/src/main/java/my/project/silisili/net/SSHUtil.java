package my.project.silisili.net;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSHUtil {
    public static SSLParams getSslSocketFactory() {
        SSLParams sslParams = new SSLParams();
        sslParams.trustManager = createX509TrustManager();
        sslParams.sSLSocketFactory = createSSLSocketFactory(sslParams.trustManager);
        return sslParams;
    }

    public static HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> true;
    }

    private static X509TrustManager createX509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static SSLSocketFactory createSSLSocketFactory(X509TrustManager x509TrustManager) {
        SSLSocketFactory sslSocketFactory = null;
        final TrustManager[] trustAllCerts = new TrustManager[]{x509TrustManager};
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }
}
