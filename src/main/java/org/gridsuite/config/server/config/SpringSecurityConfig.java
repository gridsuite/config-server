/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.config.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Configuration
@EnableWebFluxSecurity
public class SpringSecurityConfig {

    @Bean
    @SuppressWarnings("java:S4502") // Authentication is handled by the gateway
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
            // Authentication is performed by the API gateway and propagated through trusted
            // headers. This service is stateless and does not use browser cookies or sessions.
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());

        return http.build();
    }
}
