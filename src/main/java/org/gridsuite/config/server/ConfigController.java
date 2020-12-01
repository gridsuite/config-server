/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.gridsuite.config.server.dto.ConfigInfos;
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

    @GetMapping(value = "/parameters", produces = "application/json")
    @Operation(summary = "get the values of configuration parameters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of configuration parameters")})
    public ResponseEntity<Mono<ConfigInfos>> getParameters(@RequestHeader("userId") String userId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigParameters(userId));
    }

    @PutMapping(value = "/parameters", produces = "application/json")
    @Operation(summary = "update the values for a set of configuration parameters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The parameters are updated")})
    public ResponseEntity<Mono<ConfigInfos>> updateParams(@RequestHeader("userId") String userId, @RequestBody ConfigInfos configUiInfos) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.updateParameters(userId, configUiInfos));
    }
}
