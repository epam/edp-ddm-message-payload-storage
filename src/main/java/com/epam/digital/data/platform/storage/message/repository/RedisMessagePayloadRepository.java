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


import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.epam.digital.data.platform.storage.message.model.MessagePayloadRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;

@Builder
public class RedisMessagePayloadRepository implements MessagePayloadRepository {

  private final ObjectMapper objectMapper;
  private MessagePayloadKeyValueRepository repository;

  @Override
  public void putMessagePayload(String key, MessagePayloadDto messagePayloadDto) {
    repository.save(MessagePayloadRedis.builder()
        .id(key)
        .data(serializeData(messagePayloadDto.getData()))
        .build());
  }

  @Override
  public Optional<MessagePayloadDto> getMessagePayload(String key) {
    var result = repository.findById(key);
    return result.map(this::toMessagePayloadDto);
  }

  @Override
  public void delete(String... keys) {
    for (String key : keys) {
      repository.deleteById(key);
    }
  }

  private MessagePayloadDto toMessagePayloadDto(MessagePayloadRedis messagePayloadRedis) {
    return MessagePayloadDto.builder()
        .data(deserializeData(messagePayloadRedis.getData()))
        .build();
  }

  private Map<String, Object> deserializeData(String data) {
    try {
      return objectMapper.readValue(data, Map.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("Couldn't deserialize message payload data", e);
    }
  }

  private String serializeData(Map<String, Object> data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't serialize message payload data", e);
    }
  }
}
