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

import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import com.epam.digital.data.platform.storage.message.repository.MessagePayloadRepository;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Builder
public class MessagePayloadStorageService {

  private final MessagePayloadKeyProvider keyProvider;
  private final MessagePayloadRepository repository;

  /**
   * Put start message payload with generated key based on process definition key and uuid then
   * return generated key
   *
   * @param processDefinitionKey specified process definition key
   * @param uuid                 specified uuid
   * @param content              to put
   * @return generated content key
   */
  public String putStartMessagePayload(String processDefinitionKey, String uuid,
      MessagePayloadDto content) {
    log.info("Put message payload by process definition key {}, uuid {}", processDefinitionKey,
        uuid);
    var key = keyProvider.generateStartMessagePayloadKey(processDefinitionKey, uuid);
    repository.putMessagePayload(key, content);
    log.info("Message payload was put to storage by key {}", key);
    return key;
  }

  /**
   * Get message payload from storage by key
   *
   * @param key specified message payload key
   * @return {@link MessagePayloadDto} content representation (optional)
   */
  public Optional<MessagePayloadDto> getMessagePayload(String key) {
    log.info("Get message payload by key {}", key);
    var result = repository.getMessagePayload(key);
    log.info("Message payload was found by key {}", key);
    return result;
  }

  /**
   * Delete message payload from storage by key
   *
   * @param key specified message payload key
   */
  public void deleteMessagePayload(String key) {
    log.info("Delete message payload by key {}", key);
    repository.delete(key);
    log.info("Message payload was deleted by key {}", key);
  }
}
