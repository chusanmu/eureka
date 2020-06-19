package com.netflix.discovery.shared.transport.jersey;

import com.sun.jersey.client.apache4.ApacheHttpClient4;

/**
 * 该接口用于得到获取一个实际做事的client
 * @author David Liu
 */
public interface EurekaJerseyClient {

    /**
     * 此处绑定了，实现必须是基于apache的ApacheHttpClient4
     * @return
     */
    ApacheHttpClient4 getClient();

    /**
     * 清理资源
     * Clean up resources.
     */
    void destroyResources();
}
