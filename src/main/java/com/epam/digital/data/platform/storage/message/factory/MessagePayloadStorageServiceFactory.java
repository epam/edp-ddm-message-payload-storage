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
import com.epam.digital.data.platform.storage.message.config.MessagePayloadRedisStorageConfiguration;
import com.epam.digital.data.platform.storage.message.repository.CephMessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.repository.MessagePayloadKeyValueRepository;
import com.epam.digital.data.platform.storage.message.repository.MessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.repository.RedisMessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadKeyProvider;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadKeyProviderImpl;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import io.lettuce.core.internal.HostAndPort;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.repository.support.RedisRepositoryFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

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

  public MessagePayloadStorageService redisMessagePayloadStorageService(
      MessagePayloadRedisStorageConfiguration config) {
    return MessagePayloadStorageService.builder()
        .repository(newRedisMessagePayloadRepository(config))
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

  private MessagePayloadRepository newRedisMessagePayloadRepository(
      MessagePayloadRedisStorageConfiguration config) {
    return RedisMessagePayloadRepository.builder()
        .objectMapper(objectMapper)
        .repository(newMessagePayloadKeyValueRepository(newRedisTemplate(config)))
        .build();
  }

  private MessagePayloadKeyValueRepository newMessagePayloadKeyValueRepository(
      RedisTemplate<String, Object> template) {
    RedisKeyValueAdapter keyValueAdapter = new RedisKeyValueAdapter(
        template.opsForHash().getOperations());
    RedisKeyValueTemplate keyValueTemplate = new RedisKeyValueTemplate(keyValueAdapter,
        new RedisMappingContext());

    RepositoryFactorySupport factory = new RedisRepositoryFactory(keyValueTemplate);
    return factory.getRepository(MessagePayloadKeyValueRepository.class);
  }

  private RedisConnectionFactory redisConnectionFactory(
      MessagePayloadRedisStorageConfiguration configuration) {
    var redisSentinelConfig = new RedisSentinelConfiguration();

    redisSentinelConfig.setMaster(configuration.getSentinel().getMaster());
    setSentinelNodes(redisSentinelConfig, configuration);
    redisSentinelConfig.setUsername(configuration.getUsername());
    redisSentinelConfig.setPassword(configuration.getPassword());

    var connectionFactory = new LettuceConnectionFactory(redisSentinelConfig);
    connectionFactory.afterPropertiesSet();
    return connectionFactory;
  }

  private RedisTemplate<String, Object> newRedisTemplate(
      MessagePayloadRedisStorageConfiguration configuration) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory(configuration));
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  private void setSentinelNodes(RedisSentinelConfiguration sentinelConfiguration,
      MessagePayloadRedisStorageConfiguration storageConfiguration) {
    var nodes = Splitter.on(',')
        .trimResults()
        .omitEmptyStrings()
        .splitToList(storageConfiguration.getSentinel().getNodes())
        .stream()
        .map(HostAndPort::parse)
        .collect(Collectors.toList());

    for (HostAndPort node : nodes) {
      sentinelConfiguration.sentinel(node.getHostText(), node.getPort());
    }
  }
}
