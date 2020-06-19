package com.netflix.discovery.shared.transport;

import com.netflix.discovery.shared.resolver.EurekaEndpoint;

/**
 * EurekaHttpClient创建工厂接口，负责创建low level低等级的client, 用于数据传输级别的
 * A low level client factory interface. Not advised to be used by top level consumers.
 *
 * @author David Liu
 */
public interface TransportClientFactory {

    /**
     * 根据终端 EurekaEndpoint创建一个底层的可发送Http请求的client
     * @param serviceUrl
     * @return
     */
    EurekaHttpClient newClient(EurekaEndpoint serviceUrl);

    void shutdown();

}
