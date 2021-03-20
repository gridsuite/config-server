/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */

@Repository
public interface ParametersRepository extends ReactiveCassandraRepository<ParameterEntity, String> {

    Flux<ParameterEntity> findAllByUserId(String userId);

    Flux<ParameterEntity> findAllByUserIdAndAppName(String userId, String appName);

    Mono<ParameterEntity> findByUserIdAndAppNameAndName(String userId, String appName, String name);
}
