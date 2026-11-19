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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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

    private Mono<String> currentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> context.getAuthentication().getName());
    }

    @GetMapping(value = "/parameters", produces = "application/json")
    @Operation(summary = "get all configuration parameters for a user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All configuration parameters for the user")})
    @PreAuthorize("true")
    public Flux<ParameterInfos> getParameters() {
        return currentUserId().flatMapMany(configService::getConfigParameters);
    }

    @GetMapping(value = "/applications/{appName}/parameters", produces = "application/json")
    @Operation(summary = "get all configuration parameters for a user and an application")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The list of configuration parameters for the application")})
    @PreAuthorize("true")
    public Flux<ParameterInfos> getParameters(@PathVariable(value = "appName") String appName) {
        return currentUserId().flatMapMany(userId -> configService.getConfigParameters(userId, appName));
    }

    @GetMapping(value = "/applications/{appName}/parameters/{name}", produces = "application/json")
    @Operation(summary = "get a configuration parameter for a given name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The configuration parameter for the application")})
    @PreAuthorize("true")
    public Mono<ResponseEntity<ParameterInfos>> getParameter(@PathVariable(value = "appName") String appName, @PathVariable("name") String name) {
        return currentUserId().flatMap(userId -> configService.getConfigParameter(userId, appName, name)
            .map(result -> ResponseEntity.ok().body(result))
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NO_CONTENT).build()));
    }

    @PutMapping(value = "/applications/{appName}/parameters/{name}", produces = "application/json")
    @Operation(summary = "update a configuration parameter for given name and value")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The parameter are updated")})
    @PreAuthorize("true")
    public Mono<ResponseEntity<Void>> updateParameter(@PathVariable(value = "appName") String appName,
                                                      @PathVariable(value = "name") String name, @RequestParam("value") String value) {
        return currentUserId().flatMap(userId -> configService.updateConfigParameter(userId, appName, name, value)
            .thenReturn(ResponseEntity.ok().build()));
    }

    @PutMapping(value = "/applications/{appName}/parameters", produces = "application/json")
    @Operation(summary = "update the  configuration parameters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The parameters are updated")})
    @PreAuthorize("true")
    public Mono<ResponseEntity<Void>> updateParameters(@PathVariable(value = "appName") String appName,
                                                      @RequestBody Map<String, Object> parameters) {
        return currentUserId().flatMap(userId -> configService.updateConfigParameters(userId, appName, parameters)
            .thenReturn(ResponseEntity.ok().build()));
    }
}
