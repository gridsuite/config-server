/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.config.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SpringBootTest
class ReactiveEndpointSecurityTest {
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void allEndpointsMustHaveSecurityAnnotation() {
        List<String> unsecuredEndpoints = new ArrayList<>();

        mappings.getHandlerMethods().forEach((info, handler) -> {
            Method method = handler.getMethod();
            Class<?> controller = handler.getBeanType();
            if (!getControllerPackages().contains(controller.getPackageName())
                || getExcludedControllers().contains(controller)) {
                return;
            }

            if (!hasSecurityAnnotation(method, controller)) {
                unsecuredEndpoints.add(
                    controller.getSimpleName() + "#" + method.getName()
                );
            }
        });

        assertThat(unsecuredEndpoints).isEmpty();
    }

    private List<String> getControllerPackages() {
        return List.of("org.gridsuite.config.server");
    }

    /**
     * Supervision endpoints are filtered by the gateway and are not directly accessible,
     * so we don't need to check permissions for them.
     */
    private List<Class<?>> getExcludedControllers() {
        return List.of();
    }

    private boolean hasSecurityAnnotation(Method method, Class<?> controller) {
        return AnnotatedElementUtils.hasAnnotation(method, PreAuthorize.class)
            || AnnotatedElementUtils.hasAnnotation(controller, PreAuthorize.class);
    }
}
