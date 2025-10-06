/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
@Configuration
@Slf4j
public class WebFluxShutdownConfig implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        log.error("Application startup failed. Forcing JVM shutdown and exit.");

        try {
            // 2 seconds to try to exit properly then try with halt
            Thread shutdownThread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    log.error("Forcing JVM exit after shutdown timeout");
                    Runtime.getRuntime().halt(1); // halt() is stronger than exit()
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            shutdownThread.setDaemon(false);
            shutdownThread.start();

            // Try to exit properly
            System.exit(1);
        } catch (Exception e) {
            log.error("Error during forced shutdown", e);
            Runtime.getRuntime().halt(1);
        }
    }
}
