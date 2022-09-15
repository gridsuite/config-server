/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.config.server.service;

import org.gridsuite.config.server.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Service
public class NotificationService {

    private static final String CATEGORY_BROKER_OUTPUT = ConfigService.class.getName() + ".output-broker-messages";

    private static final Logger LOGGER = LoggerFactory.getLogger(CATEGORY_BROKER_OUTPUT);

    public static final String HEADER_USER_ID = "userId";
    public static final String HEADER_APP_NAME = "appName";
    public static final String HEADER_PARAMETER_NAME = "parameterName";

    @Autowired
    private StreamBridge streamBridge;

    private void sendUpdateMessage(Message<String> message) {
        LOGGER.debug("Sending message : {}", message);
        streamBridge.send("publishConfigUpdate-out-0", message);
    }

    public void emitConfigParameterChanges(String userId, String appName, String name) {
        Message<String> message = MessageBuilder.withPayload("")
                .setHeader(HEADER_USER_ID, userId)
                .setHeader(HEADER_APP_NAME, appName)
                .setHeader(HEADER_PARAMETER_NAME, name)
                .build();
        sendUpdateMessage(message);
    }
}
