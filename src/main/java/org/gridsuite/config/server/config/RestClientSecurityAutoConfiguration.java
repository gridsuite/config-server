/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server.config;

import com.powsybl.ws.commons.security.restclient.HeaderForwardingInterceptor;
import com.powsybl.ws.commons.security.restclient.RestClientAuthorizationService;
import com.powsybl.ws.commons.security.restclient.RestClientSecurityFilter;
import com.powsybl.ws.commons.springboot.OpenApiCustomization;
import com.powsybl.ws.commons.springboot.PowsyblWsCommonProperties;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.client.RestClient;

/**
 * @author Caroline Jeandat <caroline.jeandat at rte-france.com>
 */
@AutoConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({ PowsyblWsCommonProperties.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "powsybl-ws.autoconfigure", name = "authorization-system.enable")
public class RestClientSecurityAutoConfiguration {
    private final PowsyblWsCommonProperties properties;

    public RestClientSecurityAutoConfiguration(PowsyblWsCommonProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnClass(OpenApiCustomizer.class)
    @Bean(name = "userContextSwaggerConfiguration")
    public OpenApiCustomizer userContextSwaggerConfiguration() {
        return new OpenApiCustomization();
    }

    @ConditionalOnMissingBean(RestClientAuthorizationService.class)
    @Bean(name = "authorizationService")
    public RestClientAuthorizationService authorizationService(RestClient.Builder restClientBuilder) {
        return new RestClientAuthorizationService(restClientBuilder, properties.getAuthorizationSystem().getDirectoryServerBaseUri());
    }

    @ConditionalOnClass(RestClient.class)
    @Bean(name = "headerForwardingRestClientCustomizer")
    public RestClientCustomizer restClientCustomizer() {
        return builder -> builder.requestInterceptor(new HeaderForwardingInterceptor());
    }

    @Bean
    @SuppressWarnings("java:S4502") // Authentication is handled by the gateway
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authentication is performed by the API gateway and propagated through trusted
                // headers. This service is stateless and does not use browser cookies or sessions.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .addFilterAfter(new RestClientSecurityFilter(), SecurityContextHolderFilter.class);

        return http.build();
    }
}
