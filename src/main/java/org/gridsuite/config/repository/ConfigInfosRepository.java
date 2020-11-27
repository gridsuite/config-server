/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Repository
public interface ConfigInfosRepository extends ReactiveCassandraRepository<ConfigInfosEntity, Integer> {

    Mono<ConfigInfosEntity> findByUserId(String userId);

    Mono<ConfigInfosEntity> save(ConfigInfosEntity configInfosEntity);

}
