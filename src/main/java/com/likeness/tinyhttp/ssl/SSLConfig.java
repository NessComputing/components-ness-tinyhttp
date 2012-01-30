package com.likeness.tinyhttp.ssl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public abstract class SSLConfig
{
    @Config("ssl.enabled")
    @Default("true")
    public boolean isSSLEnabled()
    {
        return true;
    }

    @Config("ssl.disable-verification")
    @Default("false")
    public boolean isSSLDisableVerification()
    {
        return false;
    }

    @Config("ssl.truststore")
    @DefaultNull
    @Nullable
    public String getSSLTruststore()
    {
        return null;
    }

    @Config("ssl.truststore.type")
    @Default("JKS")
    @Nonnull
    public String getSSLTruststoreType()
    {
        return "JKS";
    }

    @Config("ssl.truststore.password")
    @DefaultNull
    @Nullable
    public String getSSLTruststorePassword()
    {
        return null;
    }

    /**
     * Fall back to the system trust store.
     *
     * @return true if the client should fall back to default truststore if the custom truststore can
     *         not validate a request.
     */
    @Config("ssl.truststore.fallback")
    @Default("true")
    public boolean isSSLTruststoreFallback()
    {
        return true;
    }
}
