package com.likeness.tinyhttp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public interface HttpContentConverter<T>
{
    T convert(final HttpRequest request, HttpResponse response, InputStream inputStream) throws IOException;
}
