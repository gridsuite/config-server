/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server.repository;

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
    @Builder.Default
    private String theme = "Dark";

    @Column("useName")
    @Builder.Default
    private Boolean useName = true;

    @Column("centerLabel")
    @Builder.Default
    private Boolean centerLabel = false;

    @Column("diagonalLabel")
    @Builder.Default
    private Boolean diagonalLabel = false;

    @Column("lineFullPath")
    @Builder.Default
    private Boolean lineFullPath = true;

    @Column("lineParallelPath")
    @Builder.Default
    private Boolean lineParallelPath = true;

    @Column("lineFlowMode")
    @Builder.Default
    private String lineFlowMode = "feeders";

    @Column("lineFlowColorMode")
    @Builder.Default
    private String lineFlowColorMode = "nominalVoltage";

    @Column("lineFlowAlertThreshold")
    @Builder.Default
    private Integer lineFlowAlertThreshold = 100;

    @Column("viewOverloadsTable")
    @Builder.Default
    private Boolean viewOverloadsTable = false;

    @Column("substationLayout")
    @Builder.Default
    private String substationLayout = "horizontal";
}
