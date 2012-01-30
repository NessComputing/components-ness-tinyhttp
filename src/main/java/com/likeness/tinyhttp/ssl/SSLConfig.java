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
