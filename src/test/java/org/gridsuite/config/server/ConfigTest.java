/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import org.gridsuite.config.server.dto.ParameterInfos;
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

        ParameterizedTypeReference<ArrayList<ParameterInfos>> listTypeReference = new ParameterizedTypeReference<>() { };

        //get config parameters -> expect empty list
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo("[]");

        ParameterInfos parameterInfos = new ParameterInfos("testKey", "testValue");
        ArrayList<ParameterInfos> paramInfosList1 = new ArrayList<>();
        paramInfosList1.add(parameterInfos);

        //insert config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(paramInfosList1))
                .exchange()
                .expectStatus().isOk();

        //get config parameters and expect the added one
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(listTypeReference)
                .value(new MatcherConfigParamList(paramInfosList1));

        // assert that the broker message has been sent to notify parameters change
        Message<byte[]> messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        MessageHeaders headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

        paramInfosList1.get(0).setValue("updatedValue");

        //update config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(paramInfosList1))
                .exchange()
                .expectStatus().isOk();

        //get config parameters and expect the updated one
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(listTypeReference)
                .value(new MatcherConfigParamList(paramInfosList1));

        // assert that the broker message has been sent to notify parameters change
        messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

        ParameterInfos parameterInfos2 = new ParameterInfos("testKey2", "testValue2");
        ArrayList<ParameterInfos> paramInfosList2 = new ArrayList<>();
        paramInfosList2.add(parameterInfos2);

        paramInfosList1.add(parameterInfos2);

        //add another config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(paramInfosList2))
                .exchange()
                .expectStatus().isOk();

        //get config parameters and expect the 2 params
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(listTypeReference)
                .value(new MatcherConfigParamList(paramInfosList1));

        // assert that the broker message has been sent to notify parameters change
        messageSwitch = output.receive(1000);
        assertEquals("", new String(messageSwitch.getPayload()));
        headersSwitch = messageSwitch.getHeaders();
        assertEquals("userId", headersSwitch.get(ConfigService.HEADER_USER_ID));

    }

    private static class MatcherConfigParamList extends TypeSafeMatcher<ArrayList<ParameterInfos>> {
        ArrayList<ParameterInfos> source;

        public MatcherConfigParamList(ArrayList<ParameterInfos> val) {
            this.source = val;
        }

        @Override
        public boolean matchesSafely(ArrayList<ParameterInfos> parameterInfos) {
            //order doesn't matter
            source.sort(Comparator.comparing(ParameterInfos::getName));
            parameterInfos.sort(Comparator.comparing(ParameterInfos::getName));
            return source.equals(parameterInfos);
        }

        @Override
        public void describeTo(Description description) {
            description.toString();
        }
    }
}

