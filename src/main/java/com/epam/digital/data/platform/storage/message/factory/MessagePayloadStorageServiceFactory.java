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

package com.epam.digital.data.platform.storage.message.factory;

import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.storage.message.config.MessagePayloadCephStorageConfiguration;
import com.epam.digital.data.platform.storage.message.repository.CephMessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.repository.MessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadKeyProvider;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadKeyProviderImpl;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

/**
 * The class for creation storage services based on supported configuration
 */
@RequiredArgsConstructor
public class MessagePayloadStorageServiceFactory {

  private final ObjectMapper objectMapper;
  private final CephS3Factory cephFactory;

  public MessagePayloadStorageService messagePayloadStorageService(
      MessagePayloadCephStorageConfiguration config) {
    return MessagePayloadStorageService.builder()
        .repository(newCephMessagePayloadRepository(config))
        .keyProvider(newMessagePayloadKeyProvider())
        .build();
  }

  private MessagePayloadKeyProvider newMessagePayloadKeyProvider() {
    return new MessagePayloadKeyProviderImpl();
  }

  private MessagePayloadRepository newCephMessagePayloadRepository(
      MessagePayloadCephStorageConfiguration config) {
    return CephMessagePayloadRepository.builder()
        .cephBucketName(config.getBucket())
        .cephService(newCephServiceS3(config))
        .objectMapper(objectMapper)
        .build();
  }

  private CephService newCephServiceS3(MessagePayloadCephStorageConfiguration config) {
    return cephFactory.createCephService(config.getHttpEndpoint(),
        config.getAccessKey(), config.getSecretKey());
  }
}
