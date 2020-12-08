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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.ArrayList;
import java.util.Comparator;

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
                .expectBody(String.class)
                .isEqualTo("[]");

        ConfigInfos configInfos = new ConfigInfos("testKey", "testValue");

        //insert config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(configInfos))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigInfos.class)
                .value(new MatcherConfigParam(configInfos));

        // assert that the broker message has been sent to notify parameters change
        Message<byte[]> messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        MessageHeaders headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

        configInfos.setValue("updatedValue");

        //update config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(configInfos))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigInfos.class)
                .value(new MatcherConfigParam(configInfos));

        // assert that the broker message has been sent to notify parameters change
        messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

        ConfigInfos configInfos2 = new ConfigInfos("testKey2", "testValue2");

        //add another config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(configInfos2))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigInfos.class)
                .value(new MatcherConfigParam(configInfos2));

        // assert that the broker message has been sent to notify parameters change
        messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

        ArrayList<ConfigInfos> configInfosList = new ArrayList<>();
        configInfosList.add(configInfos);
        configInfosList.add(configInfos2);

        //get config parameters and expect the 2 parameters
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<ArrayList<ConfigInfos>>() { })
                .value(new MatcherConfigParamList(configInfosList));

        configInfosList.get(0).setValue("updatedValue2");
        configInfosList.get(1).setValue("updatedValue2");

        //update multiple parameters
        webTestClient.put()
                .uri("/v1/multiple-parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(configInfosList))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<ArrayList<ConfigInfos>>() { })
                .value(new MatcherConfigParamList(configInfosList));

    }

    private static class MatcherConfigParam extends TypeSafeMatcher<ConfigInfos> {
        ConfigInfos source;

        public MatcherConfigParam(ConfigInfos val) {
            this.source = val;
        }

        @Override
        public boolean matchesSafely(ConfigInfos configInfos) {
            return source.getKey().equals(configInfos.getKey())
                    && source.getValue().equals(configInfos.getValue());
        }

        @Override
        public void describeTo(Description description) {
            description.toString();
        }
    }

    private static class MatcherConfigParamList extends TypeSafeMatcher<ArrayList<ConfigInfos>> {
        ArrayList<ConfigInfos> source;

        public MatcherConfigParamList(ArrayList<ConfigInfos> val) {
            this.source = val;
        }

        @Override
        public boolean matchesSafely(ArrayList<ConfigInfos> configInfos) {
            //oder doesn't matter
            source.sort(Comparator.comparing(ConfigInfos::getKey));
            configInfos.sort(Comparator.comparing(ConfigInfos::getKey));
            return source.equals(configInfos);
        }

        @Override
        public void describeTo(Description description) {
            description.toString();
        }
    }
}

