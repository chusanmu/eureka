/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.discovery.shared.resolver;

import java.util.List;

/**
 * 集群解析器，用于解析配置的eureka server地址们
 * @author Tomasz Bak
 */
public interface ClusterResolver<T extends EurekaEndpoint> {

    /**
     * eureka集群所在的region区域
     * @return
     */
    String getRegion();

    /**
     * 该区域下所有的端点endpoints
     * @return
     */
    List<T> getClusterEndpoints();
}
