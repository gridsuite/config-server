/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server.repository;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.gridsuite.config.server.dto.ParameterInfos;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Getter
@Setter
@Table("parameters")
// TODO is it possible not to implement persistable ?
public class ParameterEntity implements Persistable<UUID> {

    // Explicitly using a constructor without isNew otherwise it ignores @Transient:
    // https://github.com/spring-projects/spring-data-r2dbc/issues/320#issuecomment-601092991
    public ParameterEntity(UUID id, String userId, String appName, String name, String value) {
        this.id = id;
        this.userId = userId;
        this.appName = appName;
        this.name = name;
        this.value = value;
    }

    @Id
    //TODO spring-data-r2dbc doesn't allow composite ids, remove this when possible
    private UUID id;

    private String userId;

    private String appName;

    private String name;

    private String value;

    public ParameterInfos toConfigInfos() {
        return new ParameterInfos(this.getName(), this.getValue());
    }

    @Transient
    //TODO is it possible to have spring-data-r2dbc autogenerate UUIDs for us ?
    //if not implementing Persistable, and setting the id to null, I got:
    //NULL not allowed for column "ID";
    private boolean isNew = false;
}
