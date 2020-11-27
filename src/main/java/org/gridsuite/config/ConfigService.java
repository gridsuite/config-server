/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config;

import org.gridsuite.config.dto.ConfigInfos;
import org.gridsuite.config.repository.ConfigInfosEntity;
import org.gridsuite.config.repository.ConfigInfosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    Mono<ConfigInfos> getConfigUiParameters(String userId) {
        Mono<ConfigInfosEntity> configUiInfosEntityMono = configRepository.findByUserId(userId);
        return configUiInfosEntityMono.map(ConfigService::toConfigUiInfos)
                .switchIfEmpty(initialiseDefaultParameters(userId).map(ConfigService::toConfigUiInfos));
    }

    private Mono<ConfigInfosEntity> initialiseDefaultParameters(String userId) {
        ConfigInfosEntity newConfigInfosEntity = ConfigInfosEntity.builder()
                .userId(userId)
                .substationLayout("horizontal")
                .lineFlowMode("feeders")
                .lineFlowColorMode("nominalVoltage")
                .centerLabel(false)
                .diagonalLabel(false)
                .lineFlowAlertThreshold(100)
                .lineFullPath(true)
                .lineParallelPath(true)
                .theme("Dark")
                .useName(true)
                .viewOverloadsTable(false)
                .build();
        return configRepository.save(newConfigInfosEntity);
    }

    Mono<ConfigInfos> updateParameters(String userId, ConfigInfos configInfos) {
        Mono<ConfigInfosEntity> configInfosEntityMono = configRepository.findByUserId(userId);
        return configInfosEntityMono.flatMap(configInfosEntity -> {
            updateConfigUiInfosEntity(configInfosEntity, configInfos);
            configUpdatePublisher.onNext(MessageBuilder.withPayload("")
                    .setHeader(HEADER_USER_ID, userId)
                    .build());
            return configRepository.save(configInfosEntity);
        }).map(ConfigService::toConfigUiInfos);
    }

    private static ConfigInfos toConfigUiInfos(ConfigInfosEntity configInfosEntity) {
        return ConfigInfos.builder()
                .substationLayout(configInfosEntity.getSubstationLayout())
                .lineFlowMode(configInfosEntity.getLineFlowMode())
                .lineFlowColorMode(configInfosEntity.getLineFlowColorMode())
                .centerLabel(configInfosEntity.getCenterLabel())
                .diagonalLabel(configInfosEntity.getDiagonalLabel())
                .lineFlowAlertThreshold(configInfosEntity.getLineFlowAlertThreshold())
                .lineFullPath(configInfosEntity.getLineFullPath())
                .lineParallelPath(configInfosEntity.getLineParallelPath())
                .theme(configInfosEntity.getTheme())
                .useName(configInfosEntity.getUseName())
                .viewOverloadsTable(configInfosEntity.getViewOverloadsTable())
                .build();
    }

    private static void updateConfigUiInfosEntity(ConfigInfosEntity configInfosEntity, ConfigInfos configInfos) {
        if (configInfos.getTheme() != null) {
            configInfosEntity.setTheme(configInfos.getTheme());
        }
        if (configInfos.getCenterLabel() != null) {
            configInfosEntity.setCenterLabel(configInfos.getCenterLabel());
        }
        if (configInfos.getDiagonalLabel() != null) {
            configInfosEntity.setDiagonalLabel(configInfos.getDiagonalLabel());
        }
        if (configInfos.getLineFlowAlertThreshold() != null) {
            configInfosEntity.setLineFlowAlertThreshold(configInfos.getLineFlowAlertThreshold());
        }
        if (configInfos.getLineFullPath() != null) {
            configInfosEntity.setLineFullPath(configInfos.getLineFullPath());
        }
        if (configInfos.getLineFlowColorMode() != null) {
            configInfosEntity.setLineFlowColorMode(configInfos.getLineFlowColorMode());
        }
        if (configInfos.getLineFlowMode() != null) {
            configInfosEntity.setLineFlowMode(configInfos.getLineFlowMode());
        }
        if (configInfos.getLineParallelPath() != null) {
            configInfosEntity.setLineParallelPath(configInfos.getLineParallelPath());
        }
        if (configInfos.getSubstationLayout() != null) {
            configInfosEntity.setSubstationLayout(configInfos.getSubstationLayout());
        }
        if (configInfos.getUseName() != null) {
            configInfosEntity.setUseName(configInfos.getUseName());
        }
        if (configInfos.getViewOverloadsTable() != null) {
            configInfosEntity.setViewOverloadsTable(configInfos.getViewOverloadsTable());
        }
    }

}
