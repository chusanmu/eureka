/*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.discovery;

import javax.annotation.Nullable;
import java.util.List;

import com.google.inject.ImplementedBy;
import com.netflix.discovery.shared.transport.EurekaTransportConfig;

/**
 * TODO:Eureka-client配置接口，用于给Eureka-server注册实例
 * Configuration information required by the eureka clients to register an
 * instance with <em>Eureka</em> server.
 *
 * <p>
 * Most of the required information is provided by the default configuration
 * {@link DefaultEurekaClientConfig}. The users just need to provide the eureka
 * server service urls. The Eureka server service urls can be configured by 2
 * mechanisms
 *
 * 1) By registering the information in the DNS. 2) By specifying it in the
 * configuration.
 * </p>
 *
 *
 * Once the client is registered, users can look up information from
 * {@link EurekaClient} based on <em>virtual hostname</em> (also called
 * VIPAddress), the most common way of doing it or by other means to get the
 * information necessary to talk to other instances registered with
 * <em>Eureka</em>.
 *
 * <p>
 * Note that all configurations are not effective at runtime unless and
 * otherwise specified.
 * </p>
 * 有一个内置实现，guice依赖管理使用此
 * @author Karthik Ranganathan
 *
 */
@ImplementedBy(DefaultEurekaClientConfig.class)
public interface EurekaClientConfig {

    /**
     * TODO: 从eureka-server拉取注册信息频率，默认是30s
     * 它决定了eureka client端 timedSupervisorTask#CacheRefreshThread任务的执行频率
     * Indicates how often(in seconds) to fetch the registry information from
     * the eureka server.
     *
     * @return the fetch interval in seconds.
     */
    int getRegistryFetchIntervalSeconds();

    /**
     * Replication:赋值，回响
     * TODO: 更新实例信息的变化到eureka服务端的间隔时间，默认是30s, 使用InstanceInfoReplicator上传InstanceInfo信息
     * Indicates how often(in seconds) to replicate instance changes to be
     * replicated to the eureka server.
     *
     * @return the instance replication interval in seconds.
     */
    int getInstanceInfoReplicationIntervalSeconds();

    /**
     * TODO: 执行上面任务的初始化delay延迟，默认值40s
     * Indicates how long initially (in seconds) to replicate instance info
     * to the eureka server
     */
    int getInitialInstanceInfoReplicationIntervalSeconds();

    /**
     * TODO: 去server端获取到所有的serviceURL集群地址的时间间隔，默认是5分钟，配置serviceURL时只需要配置一个就能拿到所有，靠的就是它定时去轮询
     * Indicates how often(in seconds) to poll for changes to eureka server
     * information.
     *
     * <p>
     * Eureka servers could be added or removed and this setting controls how
     * soon the eureka clients should know about it.
     * </p>
     *
     * @return the interval to poll for eureka service url changes.
     */
    int getEurekaServiceUrlPollIntervalSeconds();

    /**
     * Gets the proxy host to eureka server if any.
     *
     * @return the proxy host.
     */
    String getProxyHost();

    /**
     * Gets the proxy port to eureka server if any.
     *
     * @return the proxy port.
     */
    String getProxyPort();

    /**
     * Gets the proxy user name if any.
     *
     * @return the proxy user name.
     */
    String getProxyUserName();

    /**
     * Gets the proxy password if any.
     *
     * @return the proxy password.
     */
    String getProxyPassword();

    /**
     * TODO: 是否要对发送请求的内容进行GZIP压缩，默认是true
     * true:会添加一个jersey的过滤器GZIPContentEncodingFilter在发送请求之前加个请求头Accept-Encoding:gzip
     * Indicates whether the content fetched from eureka server has to be
     * compressed whenever it is supported by the server. The registry
     * information from the eureka server is compressed for optimum network
     * traffic.
     *
     * @return true, if the content need to be compressed, false otherwise.
     * @deprecated gzip content encoding will be always enforced in the next minor Eureka release (see com.netflix.eureka.GzipEncodingEnforcingFilter).
     */
    boolean shouldGZipContent();

    /**
     * TODO: 控制发送请求时的readTimeout值，默认是8s ，比如默认使用jersey的话，那就是控制它的读取超时时间喽
     * Indicates how long to wait (in seconds) before a read from eureka server
     * needs to timeout.
     *
     * @return time in seconds before the read should timeout.
     */
    int getEurekaServerReadTimeoutSeconds();

    /**
     * 控制connectionTimeout,默认值5s
     * Indicates how long to wait (in seconds) before a connection to eureka
     * server needs to timeout.
     *
     * <p>
     * Note that the connections in the client are pooled by
     * {@link org.apache.http.client.HttpClient} and this setting affects the actual
     * connection creation and also the wait time to get the connection from the
     * pool.
     * </p>
     *
     * @return time in seconds before the connections should timeout.
     */
    int getEurekaServerConnectTimeoutSeconds();

    /**
     * TODO: 获取备注册中心的实现类，若EurekaClient连接的Server挂了，就使用它去连其他的，如果想做兜底，可以使用它，比如使用nacos
     * 内部并未提供任何实现类
     * Gets the name of the implementation which implements
     * {@link BackupRegistry} to fetch the registry information as a fall back
     * option for only the first time when the eureka client starts.
     *
     * <p>
     * This may be needed for applications which needs additional resiliency for
     * registry information without which it cannot operate.
     * </p>
     *
     * @return the class name which implements {@link BackupRegistry}.
     */
    String getBackupRegistryImpl();

    /**
     * TODO: client端是可以并发发出N多个请求请求server端的，这里做出了限制，避免单个client实例把server端给搞垮了
     * 控制maxTotalConnections,也就是发送请求的连接池的最大容量，默认值是200，如果是Apache的HC，那就是控制它的连接池大小
     * Gets the total number of connections that is allowed from eureka client
     * to all eureka servers.
     *
     * @return total number of allowed connections from eureka client to all
     *         eureka servers.
     */
    int getEurekaServerTotalConnections();

    /**
     * TODO: 单台host的允许连接总数，默认值是50
     * Gets the total number of connections that is allowed from eureka client
     * to a eureka server host.
     *
     * @return total number of allowed connections from eureka client to a
     *         eureka server.
     */
    int getEurekaServerTotalConnectionsPerHost();


    /* ---------------- 下面这些配置均只有在eureka服务器ip地址列表是在DNS中才会用到，默认为null -------------- */


    /**
     * TODO: 表示eureka注册中心的路径，如果配置为eureka, 则为http://x.x.x/eureka/
     * Gets the URL context to be used to construct the <em>service url</em> to
     * contact eureka server when the list of eureka servers come from the
     * DNS.This information is not required if the contract returns the service
     * urls by implementing {@link #getEurekaServerServiceUrls(String)}.
     *
     * <p>
     * The DNS mechanism is used when
     * {@link #shouldUseDnsForFetchingServiceUrls()} is set to <em>true</em> and
     * the eureka client expects the DNS to configured a certain way so that it
     * can fetch changing eureka servers dynamically.
     * </p>
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the string indicating the context {@link java.net.URI} of the eureka
     *         server.
     */
    String getEurekaServerURLContext();

    /**
     * 获取eureka服务器的端口
     * Gets the port to be used to construct the <em>service url</em> to contact
     * eureka server when the list of eureka servers come from the DNS.This
     * information is not required if the contract returns the service urls by
     * implementing {@link #getEurekaServerServiceUrls(String)}.
     *
     * <p>
     * The DNS mechanism is used when
     * {@link #shouldUseDnsForFetchingServiceUrls()} is set to <em>true</em> and
     * the eureka client expects the DNS to configured a certain way so that it
     * can fetch changing eureka servers dynamically.
     * </p>
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the string indicating the port where the eureka server is
     *         listening.
     */
    String getEurekaServerPort();

    /**
     * 获取要查询的DNS名称来获得eureka服务器
     * Gets the DNS name to be queried to get the list of eureka servers.This
     * information is not required if the contract returns the service urls by
     * implementing {@link #getEurekaServerServiceUrls(String)}.
     *
     * <p>
     * The DNS mechanism is used when
     * {@link #shouldUseDnsForFetchingServiceUrls()} is set to <em>true</em> and
     * the eureka client expects the DNS to configured a certain way so that it
     * can fetch changing eureka servers dynamically.
     * </p>
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return the string indicating the DNS name to be queried for eureka
     *         servers.
     */
    String getEurekaServerDNSName();

    /**
     * TODO: 是否使用DNS方式获取Eureka-server url地址，默认值是false
     * Indicates whether the eureka client should use the DNS mechanism to fetch
     * a list of eureka servers to talk to. When the DNS name is updated to have
     * additional servers, that information is used immediately after the eureka
     * client polls for that information as specified in
     * {@link #getEurekaServiceUrlPollIntervalSeconds()}.
     *
     * <p>
     * Alternatively, the service urls can be returned
     * {@link #getEurekaServerServiceUrls(String)}, but the users should implement
     * their own mechanism to return the updated list in case of changes.
     * </p>
     *
     * <p>
     * <em>The changes are effective at runtime.</em>
     * </p>
     *
     * @return true if the DNS mechanism should be used for fetching urls, false otherwise.
     */
    boolean shouldUseDnsForFetchingServiceUrls();

    /**
     * TODO: 重要，是否注册自己这个实例到server上，默认是true, 一般来说，如果你自己就是server端，可以不用注册上去，没有必要
     * 如果不注册自己，很多定时任务是不需要的，因此server端建议关闭
     * Indicates whether or not this instance should register its information
     * with eureka server for discovery by others.
     *
     * <p>
     * In some cases, you do not want your instances to be discovered whereas
     * you just want do discover other instances.
     * </p>
     *
     * @return true if this instance should register with eureka, false
     *         otherwise
     */
    boolean shouldRegisterWithEureka();

    /**
     * 它是在eurekaClient#shutDown方法里被调用的
     * Indicates whether the client should explicitly unregister itself from the remote server
     * on client shutdown.
     * 
     * @return true if this instance should unregister with eureka on client shutdown, false otherwise
     */
    default boolean shouldUnregisterOnShutdown() {
        return true;
    }

    /**
     * TODO: 实例是否使用同一zone里的eureka服务器，默认为true,理想状态下，eureka客户端与服务器端是在同一zone下
     * Indicates whether or not this instance should try to use the eureka
     * server in the same zone for latency and/or other reason.
     *
     * <p>
     * Ideally eureka clients are configured to talk to servers in the same zone
     * </p>
     *
     * <p>
     * <em>The changes are effective at runtime at the next registry fetch cycle as specified by
     * {@link #getRegistryFetchIntervalSeconds()}</em>
     * </p>
     *
     * @return true if the eureka client should prefer the server in the same
     *         zone, false otherwise.
     */
    boolean shouldPreferSameZoneEureka();

    /**
     * 是否允许server端返回302重定向到其他机器去处理，默认值false
     * Indicates whether server can redirect a client request to a backup server/cluster.
     * If set to false, the server will handle the request directly, If set to true, it may
     * send HTTP redirect to the client, with a new server location.
     *
     * @return true if HTTP redirects are allowed
     */
    boolean allowRedirects();

    /**
     * 当client本地的实例们和server返回的实例们出现差时，比如状态变更，元数据变更，是否记录Log
     * Indicates whether to log differences between the eureka server and the
     * eureka client in terms of registry information.
     *
     * <p>
     * Eureka client tries to retrieve only delta changes from eureka server to
     * minimize network traffic. After receiving the deltas, eureka client
     * reconciles the information from the server to verify it has not missed
     * out some information. Reconciliation failures could happen when the
     * client has had network issues communicating to server.If the
     * reconciliation fails, eureka client gets the full registry information.
     * </p>
     *
     * <p>
     * While getting the full registry information, the eureka client can log
     * the differences between the client and the server and this setting
     * controls that.
     * </p>
     * <p>
     * <em>The changes are effective at runtime at the next registry fetch cycle as specified by
     * {@link #getRegistryFetchIntervalSeconds()}</em>
     * </p>
     *
     * @return true if the eureka client should log delta differences in the
     *         case of reconciliation failure.
     */
    boolean shouldLogDeltaDiff();

    /**
     * TODO: 是否尽在增量获取，true:每次全量获取，false:每次增量获取
     * Indicates whether the eureka client should disable fetching of delta and
     * should rather resort to getting the full registry information.
     *
     * <p>
     * Note that the delta fetches can reduce the traffic tremendously, because
     * the rate of change with the eureka server is normally much lower than the
     * rate of fetches.
     * </p>
     * <p>
     * <em>The changes are effective at runtime at the next registry fetch cycle as specified by
     * {@link #getRegistryFetchIntervalSeconds()}</em>
     * </p>
     *
     * @return true to enable fetching delta information for registry, false to
     *         get the full registry.
     */
    boolean shouldDisableDelta();

    /**
     * Comma separated list of regions for which the eureka registry information will be fetched. It is mandatory to
     * define the availability zones for each of these regions as returned by {@link #getAvailabilityZones(String)}.
     * Failing to do so, will result in failure of discovery client startup.
     *
     * @return Comma separated list of regions for which the eureka registry information will be fetched.
     * <code>null</code> if no remote region has to be fetched.
     */
    @Nullable
    String fetchRegistryForRemoteRegions();

    /**
     * Gets the region (used in AWS datacenters) where this instance resides.
     *
     * @return AWS region where this instance resides.
     */
    String getRegion();

    /**
     * Gets the list of availability zones (used in AWS data centers) for the
     * region in which this instance resides.
     *
     * <p>
     * <em>The changes are effective at runtime at the next registry fetch cycle as specified by
     * {@link #getRegistryFetchIntervalSeconds()}</em>
     * </p>
     * @param region the region where this instance is deployed.
     *
     * @return the list of available zones accessible by this instance.
     */
    String[] getAvailabilityZones(String region);

    /**
     * eureka server的url集合
     * Gets the list of fully qualified {@link java.net.URL}s to communicate with eureka
     * server.
     *
     * <p>
     * Typically the eureka server {@link java.net.URL}s carry protocol,host,port,context
     * and version information if any.
     * <code>Example: http://ec2-256-156-243-129.compute-1.amazonaws.com:7001/eureka/v2/</code>
     * <p>
     *
     * <p>
     * <em>The changes are effective at runtime at the next service url refresh cycle as specified by
     * {@link #getEurekaServiceUrlPollIntervalSeconds()}</em>
     * </p>
     * @param myZone the zone in which the instance is deployed.
     *
     * @return the list of eureka server service urls for eureka clients to talk
     *         to.
     */
    List<String> getEurekaServerServiceUrls(String myZone);

    /**
     * 获取实例时 是否过滤，仅保留up状态的实例，默认值是true
     * Indicates whether to get the <em>applications</em> after filtering the
     * applications for instances with only {@link com.netflix.appinfo.InstanceInfo.InstanceStatus#UP} states.
     *
     * <p>
     * <em>The changes are effective at runtime at the next registry fetch cycle as specified by
     * {@link #getRegistryFetchIntervalSeconds()}</em>
     * </p>
     *
     * @return true to filter, false otherwise.
     */
    boolean shouldFilterOnlyUpInstances();

    /**
     * 控制连接线程池的，最大空闲时间就断开，默认30s
     * Indicates how much time (in seconds) that the HTTP connections to eureka
     * server can stay idle before it can be closed.
     *
     * <p>
     * In the AWS environment, it is recommended that the values is 30 seconds
     * or less, since the firewall cleans up the connection information after a
     * few mins leaving the connection hanging in limbo
     * </p>
     *
     * @return time in seconds the connections to eureka can stay idle before it
     *         can be closed.
     */
    int getEurekaConnectionIdleTimeoutSeconds();

    /**
     * 是否从eureka服务端获取注册信息，默认为true
     * Indicates whether this client should fetch eureka registry information from eureka server.
     *
     * @return {@code true} if registry information has to be fetched, {@code false} otherwise.
     */
    boolean shouldFetchRegistry();

    /**
     *
     * Indicates whether the client is only interested in the registry information for a single VIP.
     *
     * @return the address of the VIP (name:port).
     * <code>null</code> if single VIP interest is not present.
     */
    @Nullable
    String getRegistryRefreshSingleVipAddress();

    /**
     * The thread pool size for the heartbeatExecutor to initialise with
     * 默认5，心跳执行程序
     *
     * @return the heartbeatExecutor thread pool size
     */
    int getHeartbeatExecutorThreadPoolSize();

    /**
     * Heartbeat executor exponential back off related property.
     * It is a maximum multiplier value for retry delay, in case where a sequence of timeouts
     * occurred.
     *
     * @return maximum multiplier value for retry delay
     */
    int getHeartbeatExecutorExponentialBackOffBound();

    /**
     * The thread pool size for the cacheRefreshExecutor to initialise with
     * 缓存刷新线程池的初始化线程数，默认值5
     * @return the cacheRefreshExecutor thread pool size
     */
    int getCacheRefreshExecutorThreadPoolSize();

    /**
     * Cache refresh executor exponential back off related property.
     * It is a maximum multiplier value for retry delay, in case where a sequence of timeouts
     * occurred.
     * 默认值0
     * @return maximum multiplier value for retry delay
     */
    int getCacheRefreshExecutorExponentialBackOffBound();

    /**
     * Get a replacement string for Dollar sign <code>$</code> during serializing/deserializing information in eureka server.
     *
     * @return Replacement string for Dollar sign <code>$</code>.
     */
    String getDollarReplacement();

    /**
     * Get a replacement string for underscore sign <code>_</code> during serializing/deserializing information in eureka server.
     *
     * @return Replacement string for underscore sign <code>_</code>.
     */
    String getEscapeCharReplacement();

    /**
     * If set to true, local status updates via
     * {@link com.netflix.appinfo.ApplicationInfoManager#setInstanceStatus(com.netflix.appinfo.InstanceInfo.InstanceStatus)}
     * will trigger on-demand (but rate limited) register/updates to remote eureka servers
     *
     * @return true or false for whether local status updates should be updated to remote servers on-demand
     */
    boolean shouldOnDemandUpdateStatusChange();

    /**
     * If set to true, the {@link EurekaClient} initialization should throw an exception at constructor time
     * if an initial registration to the remote servers is unsuccessful.
     *
     * Note that if {@link #shouldRegisterWithEureka()} is set to false, then this config is a no-op
     *
     * @return true or false for whether the client initialization should enforce an initial registration
     */
    default boolean shouldEnforceRegistrationAtInit() {
        return false;
    }

    /**
     * This is a transient config and once the latest codecs are stable, can be removed (as there will only be one)
     *
     * @return the class name of the encoding codec to use for the client. If none set a default codec will be used
     */
    String getEncoderName();

    /**
     * This is a transient config and once the latest codecs are stable, can be removed (as there will only be one)
     *
     * @return the class name of the decoding codec to use for the client. If none set a default codec will be used
     */
    String getDecoderName();

    /**
     * @return {@link com.netflix.appinfo.EurekaAccept#name()} for client data accept
     */
    String getClientDataAccept();

    /**
     * To avoid configuration API pollution when trying new/experimental or features or for the migration process,
     * the corresponding configuration can be put into experimental configuration section. Config format is:
     * eureka.experimental.freeFormConfigString
     *
     * @return a property of experimental feature
     */
    String getExperimental(String name);

    /**
     * For compatibility, return the transport layer config class
     *
     * @return an instance of {@link EurekaTransportConfig}
     */
    EurekaTransportConfig getTransportConfig();
}
