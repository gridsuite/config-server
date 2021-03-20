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
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
    public void testCreateParameters() {
        //get all config parameters -> expect empty list
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .isEqualTo(List.of());

        ParameterInfos parameterInfos1 = new ParameterInfos("testKey", "testValue");
        List<ParameterInfos> paramInfosList = List.of(parameterInfos1);

        //insert config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(paramInfosList))
                .exchange()
                .expectStatus().isOk();

        //get all config parameters and expect the added one
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameters change
        Message<byte[]> message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        MessageHeaders headers = message.getHeaders();
        assertEquals("userId", headers.get(ConfigService.HEADER_USER_ID));
        assertEquals(List.of("testKey"), headers.get(ConfigService.HEADER_PARAMETERS_NAMES));

        parameterInfos1.setValue("updatedValue");

        //update config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(paramInfosList))
                .exchange()
                .expectStatus().isOk();

        //get all config parameters and expect the updated one
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameters change
        message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        headers = message.getHeaders();
        assertEquals("userId", headers.get(ConfigService.HEADER_USER_ID));
        assertEquals(List.of("testKey"), headers.get(ConfigService.HEADER_PARAMETERS_NAMES));

        ParameterInfos parameterInfos2 = new ParameterInfos("testKey2", "testValue2");

        paramInfosList = List.of(parameterInfos1, parameterInfos2);

        //add another config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(List.of(parameterInfos2)))
                .exchange()
                .expectStatus().isOk();

        //get all config parameters and expect the 2 params
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameters change
        message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        headers = message.getHeaders();
        assertEquals("userId", headers.get(ConfigService.HEADER_USER_ID));
        assertEquals(List.of("testKey2"), headers.get(ConfigService.HEADER_PARAMETERS_NAMES));

        assertNull(output.receive(1000));
    }

    @Test
    public void testGetParameters() {
        //get all config parameters -> expect empty list
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .isEqualTo(List.of());

        ParameterInfos parameterInfos1 = new ParameterInfos("testKey1", "testValue1");
        ParameterInfos parameterInfos2 = new ParameterInfos("testKey2", "testValue2");
        List<ParameterInfos> paramInfosList = List.of(parameterInfos1, parameterInfos2);

        //insert config parameters
        webTestClient.put()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .body(BodyInserters.fromValue(paramInfosList))
                .exchange()
                .expectStatus().isOk();

        //get all config parameters and expect the added one
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameters change
        Message<byte[]> message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        MessageHeaders headers = message.getHeaders();
        assertEquals("userId", headers.get(ConfigService.HEADER_USER_ID));
        assertEquals(List.of("testKey1", "testKey2"), headers.get(ConfigService.HEADER_PARAMETERS_NAMES));

        //get a specific parameter
        webTestClient.get()
                .uri("/v1/parameter/testKey1")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ParameterInfos.class)
                .value(new MatcherConfigParam(parameterInfos1));

        //get a specific parameter
        webTestClient.get()
                .uri("/v1/parameter/testKey2")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ParameterInfos.class)
                .value(new MatcherConfigParam(parameterInfos2));

        //get a specific parameter
        webTestClient.get()
                .uri("/v1/parameter/foo")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ParameterInfos.class)
                .isEqualTo(null);

        //get a list of config parameters
        webTestClient.get()
                .uri("/v1/parameters?")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        //get a list of config parameters
        webTestClient.get()
                .uri("/v1/parameters?names")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        //get a list of config parameters
        webTestClient.get()
                .uri("/v1/parameters?names=")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        //get a list of config parameters
        webTestClient.get()
                .uri("/v1/parameters?names=testKey1")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(List.of(parameterInfos1)));

        //get a list of config parameters
        webTestClient.get()
                .uri("/v1/parameters?names=testKey2,")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(List.of(parameterInfos2)));

        //get a list of config parameters
        webTestClient.get()
                .uri("/v1/parameters?names=testKey1,testKey2")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        assertNull(output.receive(1000));
    }

    private static class MatcherConfigParamList extends TypeSafeMatcher<List<ParameterInfos>> {
        List<ParameterInfos> source;

        public MatcherConfigParamList(List<ParameterInfos> val) {
            this.source = val;
        }

        @Override
        public boolean matchesSafely(List<ParameterInfos> parameterInfos) {
            return source.equals(parameterInfos);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(source);
        }
    }

    private static class MatcherConfigParam extends TypeSafeMatcher<ParameterInfos> {
        ParameterInfos source;

        public MatcherConfigParam(ParameterInfos val) {
            this.source = val;
        }

        @Override
        public boolean matchesSafely(ParameterInfos parameterInfos) {
            return source.getName().equals(parameterInfos.getName())
                    && source.getValue().equals(parameterInfos.getValue());
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(source);
        }
    }
}

