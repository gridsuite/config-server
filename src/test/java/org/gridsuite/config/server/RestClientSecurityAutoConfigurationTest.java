/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import com.powsybl.ws.commons.security.restclient.RestClientSecurityFilter;
import org.gridsuite.config.server.config.RestClientSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class RestClientSecurityAutoConfigurationTest {

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestClientSecurityAutoConfiguration.class))
            .withUserConfiguration(RestClientBuilderConfiguration.class);

    @Test
    void shouldConfigureServletSecurityWhenEnabled() {
        webContextRunner.withPropertyValues("powsybl-ws.autoconfigure.authorization-system.enable=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(RestClientSecurityAutoConfiguration.class);
                    assertThat(context).hasSingleBean(OpenApiCustomizer.class);
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);
                    assertThat(context.getBean(SecurityFilterChain.class).getFilters())
                            .anyMatch(RestClientSecurityFilter.class::isInstance);
                });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RestClientBuilderConfiguration {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}
