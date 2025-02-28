/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.common.pojo.heartbeat;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Component heartbeat response
 */
@Data
@ApiModel("Component heartbeat response")
public class ComponentHeartbeatResponse {

    @ApiModelProperty(value = "Component name, such as: Agent, Sort...")
    private String component;

    @ApiModelProperty(value = "Component instance, can be ip, name...")
    private String instance;

    @ApiModelProperty(value = "Stream status heartbeat")
    private String statusHeartbeat;

    @ApiModelProperty(value = "Stream metric heartbeat")
    private String metricHeartbeat;

    @ApiModelProperty(value = "Report time of heartbeat")
    private Long reportTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;

}
