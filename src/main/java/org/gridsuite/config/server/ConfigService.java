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
import org.gridsuite.config.server.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */

@Service
public class ConfigService {

    private final ParametersRepository configRepository;

    @Autowired
    private NotificationService notificationService;

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
                .doOnSuccess(p -> notificationService.emitConfigParameterChanges(userId, appName, name))
                .then();
    }

    private Mono<ParameterInfos> updateParameter(String userId, String appName, String name, String value) {
        return configRepository.findByUserIdAndAppNameAndName(userId, appName, name)
                .switchIfEmpty(Mono.fromCallable(() -> {
                    var uuid = UUID.randomUUID();
                    var e = new ParameterEntity(uuid, userId, appName, name, value);
                    e.setNew(true);
                    return e;
                }))
                .flatMap(parameterEntity -> {
                    parameterEntity.setValue(value);
                    return configRepository.save(parameterEntity);
                })
                .map(ParameterEntity::toConfigInfos);
    }

    Mono<Void> updateConfigParameters(String userId, String appName, Map<String, Object> parameters) {
        return Flux.fromIterable(parameters.entrySet())
                .flatMap(entry -> updateConfigParameter(userId, appName, entry.getKey(), entry.getValue().toString()))
                .then();
    }
}
