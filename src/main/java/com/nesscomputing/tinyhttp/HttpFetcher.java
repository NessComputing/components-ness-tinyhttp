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
package com.nesscomputing.tinyhttp;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.nesscomputing.logging.Log;
import com.nesscomputing.tinyhttp.ssl.HttpsTrustManagerFactory;
import com.nesscomputing.tinyhttp.ssl.SSLConfig;

public final class HttpFetcher implements Closeable
{
    private static final Log LOG = Log.findLog();

    private final HttpParams params = new BasicHttpParams();
	private final SchemeRegistry registry = new SchemeRegistry();
	private final ClientConnectionManager connectionManager;

	private static final Scheme HTTP_SCHEME = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());

    public HttpFetcher()
    {
        this(null);
    }

    public HttpFetcher(final SSLConfig sslConfig)
    {
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
		registry.register(HTTP_SCHEME);

		if (sslConfig != null && sslConfig.isSSLEnabled()) {
		    try {
		        final TrustManager [] trustManagers = new TrustManager[] { HttpsTrustManagerFactory.getTrustManager(sslConfig) };
		        final SSLContext sslContext = SSLContext.getInstance("TLS");
		        sslContext.init(null, trustManagers, null);
		        final SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

		        registry.register(new Scheme("https", 443, sslSocketFactory));
		        LOG.debug("HTTPS enabled.");
		    }
		    catch (GeneralSecurityException ce) {
		        throw Throwables.propagate(ce);
		    }
            catch (IOException ioe) {
                throw Throwables.propagate(ioe);
            }
		}
		else {
            LOG.debug("HTTPS disabled.");
		}

        connectionManager = new SingleClientConnManager(registry);

		LOG.debug("HTTP fetcher ready.");
    }

    @Override
    public void close()
    {
        connectionManager.shutdown();
    }

   public <T> T get(final URI uri, final String login, final String pw, final HttpContentConverter<T> converter)
       throws IOException
   {
       final HttpRequestBase httpRequest = new HttpGet(uri);
       final DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, params);
       // Maximum of three retries...
		httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, false));

       if (login != null) {
           httpClient.setCredentialsProvider(new HttpFetcherCredentialsProvider(login, pw));
       }

       final HttpContentResponseHandler<T> responseHandler = new HttpContentResponseHandler<T>(converter);

       try {
			final HttpContext httpContext = new BasicHttpContext();
			final HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);

			try {
			    return responseHandler.handle(httpRequest, httpResponse);
			} finally {
				// Make sure that the content has definitely been consumed. Otherwise,
				// keep-alive does not work.
				final HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					Closeables.closeQuietly(entity.getContent());
				}
			}
		} catch (Exception e) {
			LOG.warnDebug(e, "Aborting Request!");

			httpRequest.abort();
			throw Throwables.propagate(e);
		}
	}
}
