package com.likeness.tinyhttp;

import com.likeness.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;


/**
 * A generic content response handler for the Http Client. It handles all cases of redirect, compressed responses etc.
 */
public class HttpContentResponseHandler<T>
{
    private static final Log log = Log.findLog();

    private final HttpContentConverter<T> contentConverter;

    /**
     * Creates a new ContentResponseHandler.
     *
     * @param contentConverter The content converter to use to convert the response into the reply object.
     */
    public HttpContentResponseHandler(final HttpContentConverter<T> contentConverter)
    {
        this.contentConverter = contentConverter;
    }

    /**
     * Processes the client response.
     */
    public T handle(final HttpRequest request, final HttpResponse response)
        throws IOException
    {
        // Find the response stream - the error stream may be valid in cases
        // where the input stream is not.
        InputStream is = null;
        try {
            final HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                is = httpEntity.getContent();
                    }
        }
        catch (IOException e) {
            log.warnDebug(e, "Could not locate response body stream");
            // normal for 401, 403 and 404 responses, for example...
        }

        if (is == null) {
            // Fall back to zero length response.
            is = new NullInputStream(0);
        }

        final Header header = response.getFirstHeader("Content-Encoding");
        if (header != null) {
            final String encoding = StringUtils.trimToEmpty(header.getValue());

            if (StringUtils.equalsIgnoreCase(encoding, "gzip") || StringUtils.equalsIgnoreCase(encoding, "x-gzip")) {
                log.debug("Found GZIP stream");
                is = new GZIPInputStream(is);
            }
            else if (StringUtils.equalsIgnoreCase(encoding, "deflate")) {
                log.debug("Found deflate stream");
                final Inflater inflater = new Inflater(true);
                is = new InflaterInputStream(is, inflater);
            }
        }
        return contentConverter.convert(request, response, is);
    }
}
