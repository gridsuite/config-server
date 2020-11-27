/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.griduite.config.server;

import org.gridsuite.config.ConfigApplication;
import org.gridsuite.config.dto.ConfigInfos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@EnableWebFlux
@SpringBootTest
@ContextHierarchy({@ContextConfiguration(classes = {ConfigApplication.class, TestChannelBinderConfiguration.class})})
public class ConfigTest extends AbstractEmbeddedCassandraSetup {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void test() throws Exception {

        //get config parameters -> expect default value
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("{\"userId\":null,\"theme\":\"Dark\",\"useName\":true,\"centerLabel\":false,\"diagonalLabel\":false,\"lineFullPath\":true,\"lineParallelPath\":true,\"lineFlowMode\":\"feeders\",\"lineFlowColorMode\":\"nominalVoltage\",\"lineFlowAlertThreshold\":100,\"viewOverloadsTable\":false,\"substationLayout\":\"horizontal\"}");

        ConfigInfos configUiInfos = ConfigInfos.builder()
                .centerLabel(true)
                .diagonalLabel(true)
                .lineFlowAlertThreshold(80)
                .lineFlowColorMode("updatedLineFlowColorMode")
                .lineFlowMode("updatedLineFlowMode")
                .lineFullPath(false)
                .lineParallelPath(false)
                .substationLayout("vertical")
                .theme("Light")
                .useName(false)
                .viewOverloadsTable(true)
                .build();

        //get config parameters -> expect default value
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(configUiInfos))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("{\"userId\":null,\"theme\":\"Light\",\"useName\":false,\"centerLabel\":true,\"diagonalLabel\":true,\"lineFullPath\":false,\"lineParallelPath\":false,\"lineFlowMode\":\"updatedLineFlowMode\",\"lineFlowColorMode\":\"updatedLineFlowColorMode\",\"lineFlowAlertThreshold\":80,\"viewOverloadsTable\":true,\"substationLayout\":\"vertical\"}");

        //get config parameters and expect the updated ones
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("{\"userId\":null,\"theme\":\"Light\",\"useName\":false,\"centerLabel\":true,\"diagonalLabel\":true,\"lineFullPath\":false,\"lineParallelPath\":false,\"lineFlowMode\":\"updatedLineFlowMode\",\"lineFlowColorMode\":\"updatedLineFlowColorMode\",\"lineFlowAlertThreshold\":80,\"viewOverloadsTable\":true,\"substationLayout\":\"vertical\"}");

    }
}
