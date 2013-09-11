/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.tinyhttp.ssl;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.google.common.io.Resources;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpsTrustManagerFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpsTrustManagerFactory.class);

    private HttpsTrustManagerFactory() {
    }

    public static X509TrustManager getTrustManager(final SSLConfig sslConfig) throws GeneralSecurityException, IOException
    {
        if (sslConfig.isSSLDisableVerification()) {
            return new AlwaysTrustManager();
        }
        else if (sslConfig.getSSLTruststore() == null || sslConfig.getSSLTruststorePassword() == null) {
            LOG.trace("Not using custom truststore.");
            return getDefaultTrustManager();
        } else {
            LOG.trace("Using custom truststore %s.", sslConfig.getSSLTruststore());
            final MultiTrustManager multiTrustManager = new MultiTrustManager();

            if (sslConfig.isSSLTruststoreFallback()) {
                LOG.trace("Adding fallback to default trust manager");
                multiTrustManager.addTrustManager(getDefaultTrustManager());
            }

            final KeyStore keystore = loadKeystore(sslConfig.getSSLTruststore(),
                                                   sslConfig.getSSLTruststoreType(),
                                                   sslConfig.getSSLTruststorePassword());

            multiTrustManager.addTrustManager(trustManagerFromKeystore(keystore));
            return multiTrustManager;
        }
    }

    @Nonnull
    public static X509TrustManager getDefaultTrustManager() throws GeneralSecurityException
    {
        return trustManagerFromKeystore(null);
    }

    @Nonnull
    private static KeyStore loadKeystore(@Nonnull String location,
                                         @Nonnull String keystoreType,
                                         @Nonnull String keystorePassword) throws GeneralSecurityException, IOException
    {
        final KeyStore keystore = KeyStore.getInstance(keystoreType);
        URL keystoreUrl;
        if (StringUtils.startsWithIgnoreCase(location, "classpath:")) {
            keystoreUrl = Resources.getResource(HttpsTrustManagerFactory.class, location.substring(10));
        } else {
            keystoreUrl = new URL(location);
        }
        keystore.load(keystoreUrl.openStream(), keystorePassword.toCharArray());
        return keystore;
    }

    @Nonnull
    private static X509TrustManager trustManagerFromKeystore(final KeyStore keystore) throws GeneralSecurityException
    {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        trustManagerFactory.init(keystore);

        final TrustManager[] tms = trustManagerFactory.getTrustManagers();

        for (final TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                final X509TrustManager manager = X509TrustManager.class.cast(tm);
                final X509Certificate[] acceptedIssuers = manager.getAcceptedIssuers();
                LOG.debug("Found TrustManager with %d authorities.", acceptedIssuers.length);
                for (int i = 0; i < acceptedIssuers.length; i++) {
                    X509Certificate issuer = acceptedIssuers[i];
                    LOG.trace("Issuer #%d, subject DN=<%s>, serial=<%s>", i,
                              issuer.getSubjectDN(), issuer.getSerialNumber());
                }

                return manager;
            }
        }
        throw new IllegalStateException("Could not locate X509TrustManager!");
    }
}
