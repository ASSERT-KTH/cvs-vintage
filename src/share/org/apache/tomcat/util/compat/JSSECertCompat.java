package org.apache.tomcat.util.compat;

import java.io.ByteArrayInputStream;
import java.net.Socket;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.security.cert.CertificateFactory;
import javax.security.cert.X509Certificate;

public class JSSECertCompat extends CertCompat {
    /** Return the client certificate.
     */
    public Object getX509Certificates(Socket s)
    {

        // Make sure it is a  SSLSocket.
        if (s == null)
            return null;
        if (!(s instanceof SSLSocket))
            return null;
        SSLSocket socket = (SSLSocket) s;

        // Look up the current SSLSession
        SSLSession session = socket.getSession();
        if (session == null)
            return null;

        // Convert JSSE's certificate format to the ones we need
        X509Certificate jsseCerts[] = null;
        java.security.cert.X509Certificate x509Certs[] = null;
        try {
            jsseCerts = session.getPeerCertificateChain();
            if (jsseCerts == null)
                jsseCerts = new X509Certificate[0];
            x509Certs =
              new java.security.cert.X509Certificate[jsseCerts.length];
            for (int i = 0; i < x509Certs.length; i++) {
                byte buffer[] = jsseCerts[i].getEncoded();
                CertificateFactory cf =
                  CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream =
                  new ByteArrayInputStream(buffer);
                x509Certs[i] = (java.security.cert.X509Certificate)
                  cf.generateCertificate(stream);
            }
        } catch (Throwable t) {
            return null;
        }

        if ((x509Certs == null) || (x509Certs.length < 1))
            return null;

        return x509Certs;
    }
}
