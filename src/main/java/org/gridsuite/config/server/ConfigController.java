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

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */

@RestController
@RequestMapping(value = "/" + ConfigApi.API_VERSION)
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping(value = "/parameters", produces = "application/json")
    @Operation(summary = "get all configuration parameters for a user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All configuration parameters for the user")})
    public ResponseEntity<Flux<ParameterInfos>> getParameters(@RequestHeader("userId") String userId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigParameters(userId));
    }

    @GetMapping(value = "/applications/{appName}/parameters", produces = "application/json")
    @Operation(summary = "get all configuration parameters for a user and an application")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of configuration parameters for the application")})
    public ResponseEntity<Flux<ParameterInfos>> getParameters(@RequestHeader("userId") String userId, @PathVariable(value = "appName") String appName) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigParameters(userId, appName));
    }

    @GetMapping(value = "/applications/{appName}/parameters/{name}", produces = "application/json")
    @Operation(summary = "get a configuration parameter for a given name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The configuration parameter for the application")})
    public ResponseEntity<Mono<ParameterInfos>> getParameter(@RequestHeader("userId") String userId, @PathVariable(value = "appName") String appName, @PathVariable("name") String name) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.getConfigParameter(userId, appName, name));
    }

    @PutMapping(value = "/applications/{appName}/parameters/{name}", produces = "application/json")
    @Operation(summary = "update a configuration parameter for given name and value")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The parameter are updated")})
    public ResponseEntity<Mono<Void>> updateParameter(@RequestHeader("userId") String userId, @PathVariable(value = "appName") String appName,
                                                      @PathVariable(value = "name") String name, @RequestParam("value") String value) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(configService.updateConfigParameter(userId, appName, name, value));
    }
}
