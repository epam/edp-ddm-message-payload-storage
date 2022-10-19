/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.epam.digital.data.platform.storage.message.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.epam.digital.data.platform.storage.message.model.MessagePayloadRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RedisMessagePayloadRepositoryTest {

  @Mock
  private MessagePayloadKeyValueRepository repository;
  private MessagePayloadRepository messagePayloadRepository;

  @BeforeEach
  void setUp() {
    messagePayloadRepository = RedisMessagePayloadRepository.builder()
        .objectMapper(new ObjectMapper())
        .repository(repository)
        .build();
  }

  @Test
  void testPutMessagePayload() {
    var key = "key";

    var messagePayloadDto = MessagePayloadDto.builder()
        .data(Map.of("testField", "testValue"))
        .build();
    var messagePayload = MessagePayloadRedis.builder()
        .data("{\"testField\":\"testValue\"}")
        .id(key)
        .build();

    messagePayloadRepository.putMessagePayload(key, messagePayloadDto);
    verify(repository).save(messagePayload);
  }

  @Test
  void testDeleteMessagePayload() {
    var key = "key";

    messagePayloadRepository.delete(key);

    verify(repository).deleteById(key);
  }

  @Test
  @SneakyThrows
  void testGetMessagePayload() {
    var key = "key";
    var messagePayload = MessagePayloadRedis.builder()
        .data("{\"testField\":\"testValue\"}")
        .id(key)
        .build();

    when(repository.findById(key)).thenReturn(Optional.of(messagePayload));

    var result = messagePayloadRepository.getMessagePayload(key);

    assertThat(result).isPresent();
    assertThat(result.get().getData()).isNotNull()
        .containsEntry("testField", "testValue");
  }
}