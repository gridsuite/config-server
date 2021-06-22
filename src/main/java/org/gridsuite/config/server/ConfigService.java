/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import org.gridsuite.config.server.dto.ParameterInfos;
import org.gridsuite.config.server.repository.ParameterEntity;
import org.gridsuite.config.server.repository.ParametersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */

@Service
public class ConfigService {

    private final ParametersRepository configRepository;

    private static final String CATEGORY_BROKER_OUTPUT = ConfigService.class.getName() + ".output-broker-messages";

    private static final Logger LOGGER = LoggerFactory.getLogger(CATEGORY_BROKER_OUTPUT);

    static final String HEADER_USER_ID = "userId";
    static final String HEADER_APP_NAME = "appName";
    static final String HEADER_PARAMETER_NAME = "parameterName";

    @Autowired
    private StreamBridge configUpdatePublisher;

    @Autowired
    public ConfigService(ParametersRepository configRepository) {
        this.configRepository = configRepository;
    }

    Flux<ParameterInfos> getConfigParameters(String userId) {
        return configRepository.findAllByUserId(userId).map(ParameterEntity::toConfigInfos);
    }

    Flux<ParameterInfos> getConfigParameters(String userId, String appName) {
        return configRepository.findAllByUserIdAndAppName(userId, appName).map(ParameterEntity::toConfigInfos);
    }

    Mono<ParameterInfos> getConfigParameter(String userId, String appName, String name) {
        return configRepository.findByUserIdAndAppNameAndName(userId, appName, name).map(ParameterEntity::toConfigInfos);
    }

    Mono<Void> updateConfigParameter(String userId, String appName, String name, String value) {
        return updateParameter(userId, appName, name, value)
                .doOnSuccess(p -> {
                    Message<String> message = MessageBuilder.withPayload("")
                            .setHeader(HEADER_USER_ID, userId)
                            .setHeader(HEADER_APP_NAME, appName)
                            .setHeader(HEADER_PARAMETER_NAME, name)
                            .build();
                    sendUpdateMessage(message);
                })
                .then();
    }

    private Mono<ParameterInfos> updateParameter(String userId, String appName, String name, String value) {
        return configRepository.findByUserIdAndAppNameAndName(userId, appName, name)
                .switchIfEmpty(Mono.just(new ParameterEntity(userId, appName, name, value)))
                .flatMap(parameterEntity -> {
                    parameterEntity.setValue(value);
                    return configRepository.save(parameterEntity);
                })
                .map(ParameterEntity::toConfigInfos);
    }

    private void sendUpdateMessage(Message<String> message) {
        LOGGER.debug("Sending message : {}", message);
        configUpdatePublisher.send("publishConfigUpdate-out-0", message);
    }
}
