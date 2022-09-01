/*
 * Copyright 2021 EPAM Systems.
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

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Builder
public class CephMessagePayloadRepository implements MessagePayloadRepository {

  private final String cephBucketName;
  private final CephService cephService;
  private final ObjectMapper objectMapper;

  @Override
  public void putMessagePayload(String key, MessagePayloadDto messagePayloadDto) {
    cephService.put(cephBucketName, key, serializeMessagePayload(messagePayloadDto));
  }

  @Override
  public Optional<MessagePayloadDto> getMessagePayload(String key) {
    return cephService.getAsString(cephBucketName, key).map(this::deserializeMessagePayload);
  }

  @Override
  public void delete(String... keys) {
    var keySet = Stream.of(keys).collect(Collectors.toSet());
    cephService.delete(cephBucketName, keySet);
  }

  private MessagePayloadDto deserializeMessagePayload(String messagePayload) {
    try {
      return objectMapper.readValue(messagePayload, MessagePayloadDto.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("Couldn't deserialize message payload", e);
    }
  }

  private String serializeMessagePayload(MessagePayloadDto messagePayloadDto) {
    try {
      return objectMapper.writeValueAsString(messagePayloadDto);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Couldn't serialize message payload", e);
    }
  }
}
