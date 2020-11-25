package com.devo.feeds.output

import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

/**
 * Container of X509 [KeyStore] and [TrustManager]
 */
class X509Credentials(
    keystoreStream: InputStream,
    keystorePass: String,
    trustedCerts: Map<String, InputStream> = emptyMap()
) {
    private val pass = keystorePass.toCharArray()
    private val ks = KeyStore.getInstance("PKCS12").apply {
        keystoreStream.use { load(it, pass) }
    }.also { ks ->
        trustedCerts.forEach { (alias, bytes) ->
            bytes.use {
                val cert = CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate
                ks.setCertificateEntry(alias, cert)
            }
        }
    }
    private val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
        init(ks, pass)
    }
    private val tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(ks)
        }
    var sslContext: SSLContext =
        SSLContext.getInstance("TLS").apply { init(kmf.keyManagers, tmf.trustManagers, null) }
        private set

    /**
     * Create [X509Credentials] from [keystorePath] and [keystorePass]
     */
    constructor(keystorePath: String, keystorePass: String, trustedCerts: Map<String, String> = emptyMap()) : this(
        File(keystorePath).inputStream(),
        keystorePass,
        trustedCerts.map { (alias, certPath) ->
            alias to File(certPath).inputStream()
        }.toMap()
    )
}
