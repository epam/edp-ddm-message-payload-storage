package com.epam.digital.data.platform.storage.message.config;

import lombok.Data;

@Data
public class MessagePayloadSentinelConfiguration {

  private String password;
  private String username;
  private String nodes;
  private String master;
}
