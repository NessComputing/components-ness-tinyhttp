package com.likeness.tinyhttp;

import java.security.Principal;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

public class HttpFetcherCredentialsProvider implements CredentialsProvider
{
    private final String login;
    private final String pw;

    public HttpFetcherCredentialsProvider(final String login, final String pw)
    {
        this.login = login;
        this.pw = pw;
    }

    @Override
    public void setCredentials(final AuthScope authscope, final Credentials credentials)
    {
        throw new UnsupportedOperationException("credentials can not be added to this provider!");
    }

    @Override
    public Credentials getCredentials(final AuthScope authScope)
    {
        return new Credentials() {

            @Override
            public Principal getUserPrincipal()
            {
                return new BasicUserPrincipal(login);
            }

            @Override
            public String getPassword()
            {
                return pw;
            }
        };
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("credentials can not be removed from this provider!");
    }
}
