/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import org.gridsuite.config.server.repository.ParametersRepository;
import org.gridsuite.config.server.dto.ParameterInfos;
import org.gridsuite.config.server.repository.ParameterEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Service
public class ConfigService {

    private final ParametersRepository configRepository;

    private static final String CATEGORY_BROKER_OUTPUT = ConfigService.class.getName() + ".output-broker-messages";

    static final String HEADER_USER_ID = "userId";

    private final EmitterProcessor<Message<String>> configUpdatePublisher = EmitterProcessor.create();

    @Bean
    public Supplier<Flux<Message<String>>> publishConfigUpdate() {
        return () -> configUpdatePublisher.log(CATEGORY_BROKER_OUTPUT, Level.FINE);
    }

    @Autowired
    public ConfigService(ParametersRepository configRepository) {
        this.configRepository = configRepository;
    }

    Flux<ParameterInfos> getConfigParameters(String userId) {
        Flux<ParameterEntity> configInfosEntityFlux = configRepository.findAllByUserId(userId);
        return configInfosEntityFlux.map(ConfigService::toConfigInfos);
    }

    Mono<ParameterInfos> getConfigParameter(String userId, String name) {
        return configRepository.findByUserIdAndName(userId, name).map(ConfigService::toConfigInfos);
    }

    Mono<ParameterInfos> updateParameter(String userId, ParameterInfos parameterInfos) {
        Mono<ParameterEntity> configInfosEntityMono = configRepository.findByUserIdAndName(userId, parameterInfos.getName());
        return configInfosEntityMono.switchIfEmpty(createParameters(userId, parameterInfos)).flatMap(parameterEntity -> {
            parameterEntity.setValue(parameterInfos.getValue());
            return save(parameterEntity);
        }).map(ConfigService::toConfigInfos);
    }

    Mono<Void> updateParameters(String userId, List<ParameterInfos> parameterInfosList) {
        return Flux.fromIterable(parameterInfosList).flatMap(c -> updateParameter(userId, c)).doOnComplete(() ->
                configUpdatePublisher.onNext(MessageBuilder.withPayload("")
                        .setHeader(HEADER_USER_ID, userId)
                        .build())).then();
    }

    Mono<ParameterEntity> createParameters(String userId, ParameterInfos parameterInfos) {
        ParameterEntity parameterEntity = new ParameterEntity(userId, parameterInfos.getName(), parameterInfos.getValue());
        return save(parameterEntity);
    }

    Mono<ParameterEntity> save(ParameterEntity parameterEntity) {
        return configRepository.save(parameterEntity);
    }

    private static ParameterInfos toConfigInfos(ParameterEntity parameterEntity) {
        return new ParameterInfos(parameterEntity.getName(), parameterEntity.getValue());
    }

}
