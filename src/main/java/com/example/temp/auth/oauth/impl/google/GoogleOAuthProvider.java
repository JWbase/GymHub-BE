package com.example.temp.auth.oauth.impl.google;

import com.example.temp.auth.oauth.OAuthProvider;
import com.example.temp.auth.oauth.OAuthProviderType;
import com.example.temp.auth.oauth.OAuthResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class GoogleOAuthProvider implements OAuthProvider {

    private final GoogleOAuthClient googleOAuthClient;
    private final GoogleOAuthProperties properties;

    @Override
    public boolean support(OAuthProviderType providerType) {
        return Objects.equals(OAuthProviderType.GOOGLE, providerType);
    }

    @Override
    public OAuthResponse fetch(String authCode) {
        GoogleToken googleToken = fetchToken(authCode);
        GoogleUserInfo googleUserInfo = fetchUserInfo(googleToken);
        return OAuthResponse.of(OAuthProviderType.GOOGLE, googleUserInfo);
    }

    private GoogleUserInfo fetchUserInfo(GoogleToken googleToken) {
        try {
            return googleOAuthClient.fetchUserInfo(googleToken.getValueUsingAuthorizationHeader());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is5xxServerError()) {
                throw new IllegalArgumentException("Google 서버에서 문제가 발생했습니다.");
            }
            throw e;
        }
    }

    private GoogleToken fetchToken(String authCode) {
        try {
            return googleOAuthClient.fetchToken(getFetchTokenParams(authCode));
        } catch (WebClientResponseException.BadRequest e) {
            throw new IllegalArgumentException("적절하지 않은 Auth Code 입니다.");
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is5xxServerError()) {
                throw new IllegalArgumentException("Google 서버에서 문제가 발생했습니다.");
            }
            throw e;
        }
    }

    private MultiValueMap<String, String> getFetchTokenParams(String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", properties.clientId());
        params.add("client_secret", properties.clientSecret());
        params.add("code", authCode);
        params.add("redirect_uri", properties.redirectUri());
        params.add("grant_type", "authorization_code");
        return params;
    }
}
