/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Getter
@Setter
@AllArgsConstructor
@Table("parameters")
@Builder
public class ConfigInfosEntity {

    @PrimaryKeyColumn(name = "userId", type = PrimaryKeyType.PARTITIONED)
    private String userId;

    @Column("theme")
    private String theme;

    @Column("useName")
    private Boolean useName;

    @Column("centerLabel")
    private Boolean centerLabel;

    @Column("diagonalLabel")
    private Boolean diagonalLabel;

    @Column("lineFullPath")
    private Boolean lineFullPath;

    @Column("lineParallelPath")
    private Boolean lineParallelPath;

    @Column("lineFlowMode")
    private String lineFlowMode;

    @Column("lineFlowColorMode")
    private String lineFlowColorMode;

    @Column("lineFlowAlertThreshold")
    private Integer lineFlowAlertThreshold;

    @Column("viewOverloadsTable")
    private Boolean viewOverloadsTable;

    @Column("substationLayout")
    private String substationLayout;
}
