/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.config.server;

import org.gridsuite.config.server.dto.ParameterInfos;
import org.gridsuite.config.server.repository.ParametersRepository;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.util.List;

import static org.gridsuite.config.server.service.NotificationService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoConfigureWebTestClient
@EnableWebFlux
@SpringBootTest(classes = {ConfigApplication.class, TestChannelBinderConfiguration.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OutputDestination output;

    @Autowired
    private ParametersRepository parametersRepository;

    @AfterEach
    void setup() {
        parametersRepository.deleteAll().block();
    }

    @Test
    void testCreateParameters() {
        //get all config parameters for 'foo' application -> expect empty list
        webTestClient.get()
                .uri("/v1/applications/foo/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .isEqualTo(List.of());

        ParameterInfos parameterInfos1 = new ParameterInfos("testKey", "testValue");
        List<ParameterInfos> paramInfosList = List.of(parameterInfos1);

        //insert config parameter 'testKey' for 'foo' application
        webTestClient.put()
                .uri("/v1/applications/foo/parameters/testKey?value=testValue")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk();

        //get all config parameters for 'foo' application and expect the added one
        webTestClient.get()
                .uri("/v1/applications/foo/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameter change
        Message<byte[]> message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        MessageHeaders headers = message.getHeaders();
        assertEquals("userId", headers.get(HEADER_USER_ID));
        assertEquals("foo", headers.get(HEADER_APP_NAME));
        assertEquals("testKey", headers.get(HEADER_PARAMETER_NAME));

        parameterInfos1.setValue("updatedValue");

        //update config parameter 'testKey' for 'foo' application
        webTestClient.put()
                .uri("/v1/applications/foo/parameters/testKey?value=updatedValue")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk();

        //get all config parameters for 'foo' application and expect the updated one
        webTestClient.get()
                .uri("/v1/applications/foo/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameter change
        message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        headers = message.getHeaders();
        assertEquals("userId", headers.get(HEADER_USER_ID));
        assertEquals("foo", headers.get(HEADER_APP_NAME));
        assertEquals("testKey", headers.get(HEADER_PARAMETER_NAME));

        ParameterInfos parameterInfos2 = new ParameterInfos("testKey2", "testValue2");

        paramInfosList = List.of(parameterInfos1, parameterInfos2);

        //add another config parameter 'testKey2' for 'foo' application
        webTestClient.put()
                .uri("/v1/applications/foo/parameters/testKey2?value=testValue2")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk();

        //get all config parameters for 'foo' application and expect the 2 params
        webTestClient.get()
                .uri("/v1/applications/foo/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(paramInfosList));

        // assert that the broker message has been sent to notify parameter change
        message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        headers = message.getHeaders();
        assertEquals("userId", headers.get(HEADER_USER_ID));
        assertEquals("foo", headers.get(HEADER_APP_NAME));
        assertEquals("testKey2", headers.get(HEADER_PARAMETER_NAME));

        assertNull(output.receive(1000));
    }

    @Test
    void testGetParameters() {
        //get all config parameters -> expect empty list
        webTestClient.get()
                .uri("/v1/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .isEqualTo(List.of());

        //get all common config parameters -> expect empty list
        webTestClient.get()
                .uri("/v1/applications/common/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .isEqualTo(List.of());

        //get all config parameters for 'bar' application -> expect empty list
        webTestClient.get()
                .uri("/v1/applications/bar/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .isEqualTo(List.of());

        ParameterInfos parameterInfos1 = new ParameterInfos("commonParam", "value1");
        ParameterInfos parameterInfos2 = new ParameterInfos("specificParam", "value2");

        //insert common config parameter 'commonParam'
        webTestClient.put()
                .uri("/v1/applications/common/parameters/commonParam?value=value1")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk();

        // assert that the broker message has been sent to notify parameter change
        Message<byte[]> message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        MessageHeaders headers = message.getHeaders();
        assertEquals("userId", headers.get(HEADER_USER_ID));
        assertEquals("common", headers.get(HEADER_APP_NAME));
        assertEquals("commonParam", headers.get(HEADER_PARAMETER_NAME));

        //insert config parameter 'specificParam' for 'bar' application
        webTestClient.put()
                .uri("/v1/applications/bar/parameters/specificParam?value=value2")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk();

        // assert that the broker message has been sent to notify parameter change
        message = output.receive(1000);
        assertEquals("", new String(message.getPayload()));
        headers = message.getHeaders();
        assertEquals("userId", headers.get(HEADER_USER_ID));
        assertEquals("bar", headers.get(HEADER_APP_NAME));
        assertEquals("specificParam", headers.get(HEADER_PARAMETER_NAME));

        //get all commmon config parameters and expect the added one
        webTestClient.get()
                .uri("/v1/applications/common/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(List.of(parameterInfos1)));

        //get all config parameters for 'bar' application and expect the added one
        webTestClient.get()
                .uri("/v1/applications/bar/parameters")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ParameterInfos.class)
                .value(new MatcherConfigParamList(List.of(parameterInfos2)));

        //get a common config parameter
        webTestClient.get()
                .uri("/v1/applications/common/parameters/commonParam")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ParameterInfos.class)
                .value(new MatcherConfigParam(parameterInfos1));

        //get a specific parameter for 'bar' application
        webTestClient.get()
                .uri("/v1/applications/bar/parameters/specificParam")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ParameterInfos.class)
                .value(new MatcherConfigParam(parameterInfos2));

        //get an unknown common parameter
        webTestClient.get()
                .uri("/v1/applications/common/parameters/specificParam")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isNoContent();

        //get an unknown parameter for 'bar' application
        webTestClient.get()
                .uri("/v1/applications/bar/parameters/commonParam")
                .header("userId", "userId")
                .exchange()
                .expectStatus().isNoContent();

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
