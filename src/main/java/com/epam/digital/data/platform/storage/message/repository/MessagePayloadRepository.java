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

import com.epam.digital.data.platform.storage.message.dto.MessagePayloadDto;
import io.micrometer.tracing.annotation.NewSpan;
import java.util.Optional;

/**
 * The repository for getting and storing message payload
 */
public interface MessagePayloadRepository {

  /**
   * Put messagePayload to repository
   *
   * @param key               document id
   * @param messagePayloadDto {@link MessagePayloadDto} content representation
   */
  @NewSpan
  void putMessagePayload(String key, MessagePayloadDto messagePayloadDto);

  /**
   * Retrieve messagePayload by key
   *
   * @param key document id
   * @return {@link MessagePayloadDto} content representation (optional)
   *
   * @throws IllegalArgumentException if stored content couldn't be parsed to
   *                                  {@link MessagePayloadDto}
   */
  @NewSpan
  Optional<MessagePayloadDto> getMessagePayload(String key);

  /**
   * Delete forms by provided keys
   *
   * @param keys specified form keys
   */
  @NewSpan("deleteFormDataByKeys")
  void delete(String... keys);

}
