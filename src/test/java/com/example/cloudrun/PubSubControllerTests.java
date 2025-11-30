/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cloudrun;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Este arquivo de teste usa MockMvc para simular requisições HTTP para o PubSubController.
 * Mockito não é utilizado aqui porque o PubSubController não possui dependências externas
 * que precisem ser mockadas. O foco é testar a interação do controlador com o framework Spring
 * e a desserialização do corpo da requisição.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PubSubControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void addEmptyBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(result.getResponse().getContentAsString()).contains("Bad Request: invalid Pub/Sub message format");
    }

    @Test
    public void addNoMessage() throws Exception {
        String mockBody = "{}";

        MvcResult result = mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(result.getResponse().getContentAsString()).contains("Bad Request: invalid Pub/Sub message format");
    }

    @Test
    public void addInvalidMimetype() throws Exception {
        String mockBody = "{\"message\":{\"data\":\"dGVzdA==\","
                + "\"attributes\":{},\"messageId\":\"91010751788941\""
                + ",\"publishTime\":\"2017-09-25T23:16:42.302Z\"}}";

        mockMvc.perform(post("/")
                        .contentType(MediaType.TEXT_HTML)
                        .content(mockBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void addMinimalBody() throws Exception {
        String mockBody = "{\"message\":{}}";

        MvcResult result = mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockBody))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Hello World!");
    }

    @Test
    public void addFullBody() throws Exception {
        // O corpo da mensagem é "test" em Base64
        String mockBody = "{\"message\":{\"data\":\"dGVzdA==\","
                + "\"attributes\":{},\"messageId\":\"91010751788941\""
                + ",\"publishTime\":\"2017-09-25T23:16:42.302Z\"}}";
        MvcResult result = mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockBody))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Hello test!");
    }
}
