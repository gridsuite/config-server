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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Service
public class ConfigService {

    private final ParametersRepository configRepository;

    private static final String CATEGORY_BROKER_OUTPUT = ConfigService.class.getName() + ".output-broker-messages";

    static final String HEADER_USER_ID = "userId";
    static final String HEADER_PARAMETERS_NAMES = "parametersNames";

    private final EmitterProcessor<Message<String>> configUpdatePublisher = EmitterProcessor.create();

    @Bean
    public Supplier<Flux<Message<String>>> publishConfigUpdate() {
        return () -> configUpdatePublisher.log(CATEGORY_BROKER_OUTPUT, Level.FINE);
    }

    @Autowired
    public ConfigService(ParametersRepository configRepository) {
        this.configRepository = configRepository;
    }

    Flux<ParameterInfos> getConfigParameters(String userId, List<String> names) {
        Flux<ParameterEntity> entities = CollectionUtils.isEmpty(names) ? configRepository.findAllByUserId(userId) :
                configRepository.findByUserIdAndNameIn(userId, names);
        return entities.map(ParameterEntity::toConfigInfos);
    }

    Mono<ParameterInfos> getConfigParameter(String userId, String name) {
        return configRepository.findByUserIdAndName(userId, name).map(ParameterEntity::toConfigInfos);
    }

    Mono<Void> updateConfigParameters(String userId, List<ParameterInfos> parameterInfosList) {
        return Flux.fromIterable(parameterInfosList)
                .flatMap(c -> updateConfigParameter(userId, c))
                .doOnComplete(() -> configUpdatePublisher.onNext(MessageBuilder.withPayload("")
                        .setHeader(HEADER_USER_ID, userId)
                        .setHeader(HEADER_PARAMETERS_NAMES, parameterInfosList.stream().map(ParameterInfos::getName).collect(Collectors.toList()))
                        .build()))
                .then();
    }

    private Mono<ParameterInfos> updateConfigParameter(String userId, ParameterInfos parameterInfos) {
        return configRepository.findByUserIdAndName(userId, parameterInfos.getName())
                .switchIfEmpty(Mono.just(new ParameterEntity(userId, parameterInfos.getName(), parameterInfos.getValue())))
                .flatMap(parameterEntity -> {
                    parameterEntity.setValue(parameterInfos.getValue());
                    return configRepository.save(parameterEntity);
                })
                .map(ParameterEntity::toConfigInfos);
    }
}
