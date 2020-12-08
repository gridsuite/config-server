/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import org.gridsuite.config.server.repository.ConfigInfosRepository;
import org.gridsuite.config.server.dto.ConfigInfos;
import org.gridsuite.config.server.repository.ConfigInfosEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Service
public class ConfigService {

    private final ConfigInfosRepository configRepository;

    private static final String CATEGORY_BROKER_OUTPUT = ConfigService.class.getName() + ".output-broker-messages";

    static final String HEADER_USER_ID = "userId";

    private final EmitterProcessor<Message<String>> configUpdatePublisher = EmitterProcessor.create();

    @Bean
    public Supplier<Flux<Message<String>>> publishConfigUpdate() {
        return () -> configUpdatePublisher.log(CATEGORY_BROKER_OUTPUT, Level.FINE);
    }

    @Autowired
    public ConfigService(ConfigInfosRepository configRepository) {
        this.configRepository = configRepository;
    }

    Flux<ConfigInfos> getConfigParameters(String userId) {
        Flux<ConfigInfosEntity> configInfosEntityFlux = configRepository.findAllByUserId(userId);
        return configInfosEntityFlux.map(ConfigService::toConfigInfos);
    }

    Mono<ConfigInfos> updateParameter(String userId, ConfigInfos configInfos) {
        Mono<ConfigInfosEntity> configInfosEntityMono = configRepository.findByUserIdAndKey(userId, configInfos.getKey());
        return configInfosEntityMono.switchIfEmpty(createParameters(userId, configInfos)).flatMap(configInfosEntity -> {
            configInfosEntity.setValue(configInfos.getValue());
            return save(userId, configInfosEntity);
        }).map(ConfigService::toConfigInfos);
    }

    Flux<ConfigInfos> updateParameters(String userId, List<ConfigInfos> configInfosList) {
        ArrayList<Mono<ConfigInfos>> monos = new ArrayList<>();
        configInfosList.forEach(configInfos -> monos.add(updateParameter(userId, configInfos)));
        Flux<ConfigInfos> configInfosFlux = Flux.merge(monos);
        return configInfosFlux.doOnComplete(() -> getConfigParameters(userId));
    }

    Mono<ConfigInfosEntity> createParameters(String userId, ConfigInfos configInfos) {
        ConfigInfosEntity configInfosEntity = new ConfigInfosEntity(userId, configInfos.getKey(), configInfos.getValue());
        return save(userId, configInfosEntity);
    }

    Mono<ConfigInfosEntity> save(String userId, ConfigInfosEntity configInfosEntity) {
        return configRepository.save(configInfosEntity).doOnSuccess(e ->
                configUpdatePublisher.onNext(MessageBuilder.withPayload("")
                        .setHeader(HEADER_USER_ID, userId)
                        .build()));
    }

    private static ConfigInfos toConfigInfos(ConfigInfosEntity configInfosEntity) {
        return new ConfigInfos(configInfosEntity.getKey(), configInfosEntity.getValue());
    }

}
