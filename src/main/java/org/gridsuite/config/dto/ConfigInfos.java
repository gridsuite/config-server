/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class ConfigInfos {

    String userId;
    String theme;
    Boolean useName;
    Boolean centerLabel;
    Boolean diagonalLabel;
    Boolean lineFullPath;
    Boolean lineParallelPath;
    String lineFlowMode;
    String lineFlowColorMode;
    Integer lineFlowAlertThreshold;
    Boolean viewOverloadsTable;
    String substationLayout;
}
