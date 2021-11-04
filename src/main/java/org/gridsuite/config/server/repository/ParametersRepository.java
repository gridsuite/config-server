/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */

@Repository
public interface ParametersRepository extends ReactiveCrudRepository<ParameterEntity, UUID> {

    Flux<ParameterEntity> findAllByUserId(String userId);

    Flux<ParameterEntity> findAllByUserIdAndAppName(String userId, String appName);

    Mono<ParameterEntity> findByUserIdAndAppNameAndName(String userId, String appName, String name);
}
