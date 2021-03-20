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
import org.gridsuite.config.server.dto.ParameterInfos;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
    @Operation(summary = "get all configuration parameters or a list for the given names list")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of configuration parameters")})
    public ResponseEntity<Flux<ParameterInfos>> getParameters(@RequestHeader("userId") String userId, @RequestParam(required = false, value = "names") List<String> names) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigParameters(userId, names));
    }

    @GetMapping(value = "/parameter/{name}", produces = "application/json")
    @Operation(summary = "get the configuration parameter for the given param name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The wanted configuration parameter")})
    public ResponseEntity<Mono<ParameterInfos>> getParameter(@RequestHeader("userId") String userId, @PathVariable("name") String name) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigParameter(userId, name));
    }

    @PutMapping(value = "/parameters", produces = "application/json")
    @Operation(summary = "update the values for a set of configuration parameters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The parameters are updated")})
    public ResponseEntity<Mono<Void>> updateParameters(@RequestHeader("userId") String userId, @RequestBody List<ParameterInfos> parameterInfosList) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.updateConfigParameters(userId, parameterInfosList));
    }
}
