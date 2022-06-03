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

package com.epam.digital.data.platform.storage.message.service;

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.epam.digital.data.platform.storage.message.repository.CephMessagePayloadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagePayloadStorageServiceTest {

  private final String bucketName = "bucket";

  @Mock
  private CephService cephService;
  private MessagePayloadStorageService storageService;
  private MessagePayloadKeyProvider messagePayloadKeyProvider;

  @BeforeEach
  void init() {
    var repository = CephMessagePayloadRepository.builder()
        .objectMapper(new ObjectMapper())
        .cephBucketName(bucketName)
        .cephService(cephService)
        .build();
    messagePayloadKeyProvider = new MessagePayloadKeyProviderImpl();
    storageService = MessagePayloadStorageService.builder()
        .keyProvider(messagePayloadKeyProvider)
        .repository(repository)
        .build();
  }

  @Test
  void testPutStartMessagePayload() {
    var procDefKey = "procDefKey";
    var uuid = UUID.randomUUID().toString();
    var formData = MessagePayloadDto.builder()
        .data(Map.of("testField", "testValue"))
        .build();
    var formDataStr = "{\"data\":{\"testField\":\"testValue\"}}";
    var key = messagePayloadKeyProvider.generateStartMessagePayloadKey(procDefKey, uuid);

    storageService.putStartMessagePayload(procDefKey, uuid, formData);
    verify(cephService).put(bucketName, key, formDataStr);
  }

  @Test
  void testDeleteByProcInstId() {
    var procDefKey = "procDefKey";
    var uuid = UUID.randomUUID().toString();
    var key = messagePayloadKeyProvider.generateStartMessagePayloadKey(procDefKey, uuid);

    storageService.deleteMessagePayload(key);

    verify(cephService).delete(bucketName, Set.of(key));
  }

  @Test
  @SneakyThrows
  void testGetFromDataWithStorageKey() {
    var key = "cephKey";
    var formDataAsStr = "{\"data\":{\"testField\":\"testValue\"}}";

    when(cephService.getAsString(bucketName, key)).thenReturn(Optional.of(formDataAsStr));

    var result = storageService.getMessagePayload(key);

    assertThat(result).isPresent();
    assertThat(result.get().getData()).isNotNull()
        .containsEntry("testField", "testValue");
  }
}