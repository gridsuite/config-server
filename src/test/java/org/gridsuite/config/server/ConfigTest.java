/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import org.gridsuite.config.server.dto.ConfigInfos;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.Assert.assertEquals;

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

    @Autowired
    private OutputDestination output;

    @Test
    public void test() throws Exception {

        //get config parameters -> expect default value
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigInfos.class)
                .value(new MatcherConfigParam(ConfigInfos.builder()
                        .userId("userId")
                        .theme("Dark")
                        .useName(true)
                        .centerLabel(false)
                        .diagonalLabel(false)
                        .lineFullPath(true)
                        .lineParallelPath(true)
                        .lineFlowMode("feeders")
                        .lineFlowColorMode("nominalVoltage")
                        .lineFlowAlertThreshold(100)
                        .viewOverloadsTable(false)
                        .substationLayout("horizontal")
                        .build()));

        ConfigInfos configUiInfos = ConfigInfos.builder()
                .userId("userId")
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

        //update config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(configUiInfos))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigInfos.class)
                .value(new MatcherConfigParam(configUiInfos));

        // assert that the broker message has been sent to notify parameters change
        Message<byte[]> messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        MessageHeaders headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

        //get config parameters and expect the updated ones
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigInfos.class)
                .value(new MatcherConfigParam(configUiInfos));
    }

    private static class MatcherConfigParam extends TypeSafeMatcher<ConfigInfos> {
        ConfigInfos source;

        public MatcherConfigParam(ConfigInfos val) {
            this.source = val;
        }

        @Override
        public boolean matchesSafely(ConfigInfos configInfos) {
            return source.getUserId().equals(configInfos.getUserId())
                    && source.getCenterLabel().equals(configInfos.getCenterLabel())
                    && source.getDiagonalLabel().equals(configInfos.getDiagonalLabel())
                    && source.getLineFullPath().equals(configInfos.getLineFullPath())
                    && source.getLineFlowAlertThreshold().equals(configInfos.getLineFlowAlertThreshold())
                    && source.getLineFlowColorMode().equals(configInfos.getLineFlowColorMode())
                    && source.getLineParallelPath().equals(configInfos.getLineParallelPath())
                    && source.getUseName().equals(configInfos.getUseName())
                    && source.getViewOverloadsTable().equals(configInfos.getViewOverloadsTable())
                    && source.getLineFlowMode().equals(configInfos.getLineFlowMode())
                    && source.getTheme().equals(configInfos.getTheme())
                    && source.getSubstationLayout().equals(configInfos.getSubstationLayout());
        }

        @Override
        public void describeTo(Description description) {
            description.toString();
        }
    }
}

