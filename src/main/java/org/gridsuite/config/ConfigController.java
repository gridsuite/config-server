/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config;

import org.gridsuite.config.dto.ConfigInfos;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@RestController
@RequestMapping(value = "/" + ConfigApi.API_VERSION)
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping(value = "/parameters")
    public ResponseEntity<Mono<ConfigInfos>> getParameters(@RequestHeader("userId") String userId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigUiParameters(userId));
    }

    @PutMapping(value = "/parameters")
    public ResponseEntity<Mono<ConfigInfos>> updateParams(@RequestHeader("userId") String userId, @RequestBody ConfigInfos configUiInfos) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.updateParameters(userId, configUiInfos));
    }
}
