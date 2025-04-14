package com.turkcell.billingservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        Object roles = claims.get("realm_access");

        if (roles instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) roles;
            Object rolesArray = realmAccess.get("roles");

            if (rolesArray instanceof Iterable) {
                @SuppressWarnings("unchecked")
                Iterable<String> rolesList = (Iterable<String>) rolesArray;
                return StreamSupport.stream(rolesList.spliterator(), false)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        return java.util.Collections.emptyList();
    }
} 