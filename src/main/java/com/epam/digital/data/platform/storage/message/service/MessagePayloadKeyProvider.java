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

/**
 * The class represents a provider that is used to generate the key to get or store message payload
 */
public interface MessagePayloadKeyProvider {

  /**
   * Method for generating the key to save start message payload, uses process definition key and
   * generated unique identifier to construct the key
   *
   * @param processDefinitionKey process definition key
   * @param uuid                 unique identifier
   * @return generated key
   */
  String generateStartMessagePayloadKey(String processDefinitionKey, String uuid);
}
