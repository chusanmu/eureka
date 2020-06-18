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

package com.netflix.appinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * TODO: 续租信息，续租是eureka里特别重要的一个概念，eureka会决定根据此租约中的com.netflix.appinfo.EurekaInstanceConfig#getLeaseExpirationDurationInSeconds() 中设置的持续时间将实例从其视图中移除，租约还记录了上次续租的时间
 * Represents the <em>lease</em> information with <em>Eureka</em>.
 *
 * <p>
 * <em>Eureka</em> decides to remove the instance out of its view depending on
 * the duration that is set in
 * {@link EurekaInstanceConfig#getLeaseExpirationDurationInSeconds()} which is
 * held in this lease. The lease also tracks the last time it was renewed.
 * </p>
 *
 * @author Karthik Ranganathan, Greg Kim
 *
 */
@JsonRootName("leaseInfo")
public class LeaseInfo {

    /* ---------------- 默认值们 -------------- */

    public static final int DEFAULT_LEASE_RENEWAL_INTERVAL = 30;
    public static final int DEFAULT_LEASE_DURATION = 90;

    // Client settings
    /**
     * TODO: 续租间隔时间，多长时间续租一次，默认值是30s, 用于client客户端，每隔30s上报续约一次
     */
    private int renewalIntervalInSecs = DEFAULT_LEASE_RENEWAL_INTERVAL;
    /**
     * TODO: 续约持续时间(过期时间), 默认是90s, 90s倒计时，期间没有收到续约就会执行对应动作，用于Server服务端，90s内没有收到心跳，就T除掉对应实例
     */
    private int durationInSecs = DEFAULT_LEASE_DURATION;
    /**
     * 租约的注册时间
     */
    // Server populated
    private long registrationTimestamp;
    /**
     * 最近一次的续约时间，服务端记录，用于倒计时额起始值
     */
    private long lastRenewalTimestamp;
    /**
     * 下线时间
     */
    private long evictionTimestamp;
    /**
     * 上线时间
     */
    private long serviceUpTimestamp;

    public static final class Builder {

        @XStreamOmitField
        private LeaseInfo result;

        private Builder() {
            result = new LeaseInfo();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Sets the registration timestamp.
         *
         * @param ts
         *            time when the lease was first registered.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setRegistrationTimestamp(long ts) {
            result.registrationTimestamp = ts;
            return this;
        }

        /**
         * Sets the last renewal timestamp of lease.
         *
         * @param ts
         *            time when the lease was last renewed.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setRenewalTimestamp(long ts) {
            result.lastRenewalTimestamp = ts;
            return this;
        }

        /**
         * Sets the de-registration timestamp.
         *
         * @param ts
         *            time when the lease was removed.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setEvictionTimestamp(long ts) {
            result.evictionTimestamp = ts;
            return this;
        }

        /**
         * Sets the service UP timestamp.
         *
         * @param ts
         *            time when the leased service marked as UP.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setServiceUpTimestamp(long ts) {
            result.serviceUpTimestamp = ts;
            return this;
        }

        /**
         * Sets the client specified setting for eviction (e.g. how long to wait
         * without renewal event).
         *
         * @param d
         *            time in seconds after which the lease would expire without
         *            renewa.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setDurationInSecs(int d) {
            if (d <= 0) {
                result.durationInSecs = DEFAULT_LEASE_DURATION;
            } else {
                result.durationInSecs = d;
            }
            return this;
        }

        /**
         * Sets the client specified setting for renew interval.
         *
         * @param i
         *            the time interval with which the renewals will be renewed.
         * @return the {@link LeaseInfo} builder.
         */
        public Builder setRenewalIntervalInSecs(int i) {
            if (i <= 0) {
                result.renewalIntervalInSecs = DEFAULT_LEASE_RENEWAL_INTERVAL;
            } else {
                result.renewalIntervalInSecs = i;
            }
            return this;
        }

        /**
         * Build the {@link InstanceInfo}.
         *
         * @return the {@link LeaseInfo} information built based on the supplied
         *         information.
         */
        public LeaseInfo build() {
            return result;
        }
    }

    private LeaseInfo() {
    }

    /**
     * TODO: note about renewalTimestamp legacy:
     * The previous change to use Jackson ser/deser changed the field name for lastRenewalTimestamp to renewalTimestamp
     * for serialization, which causes an incompatibility with the jacksonNG codec when the server returns data with
     * field renewalTimestamp and jacksonNG expects lastRenewalTimestamp. Remove this legacy field from client code
     * in a few releases (once servers are updated to a release that generates json with the correct
     * lastRenewalTimestamp).
     */
    @JsonCreator
    public LeaseInfo(@JsonProperty("renewalIntervalInSecs") int renewalIntervalInSecs,
                     @JsonProperty("durationInSecs") int durationInSecs,
                     @JsonProperty("registrationTimestamp") long registrationTimestamp,
                     @JsonProperty("lastRenewalTimestamp") Long lastRenewalTimestamp,
                     @JsonProperty("renewalTimestamp") long lastRenewalTimestampLegacy,  // for legacy
                     @JsonProperty("evictionTimestamp") long evictionTimestamp,
                     @JsonProperty("serviceUpTimestamp") long serviceUpTimestamp) {
        this.renewalIntervalInSecs = renewalIntervalInSecs;
        this.durationInSecs = durationInSecs;
        this.registrationTimestamp = registrationTimestamp;
        this.evictionTimestamp = evictionTimestamp;
        this.serviceUpTimestamp = serviceUpTimestamp;

        if (lastRenewalTimestamp == null) {
            this.lastRenewalTimestamp = lastRenewalTimestampLegacy;
        } else {
            this.lastRenewalTimestamp = lastRenewalTimestamp;
        }
    }

    /**
     * Returns the registration timestamp.
     *
     * @return time in milliseconds since epoch.
     */
    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    /**
     * Returns the last renewal timestamp of lease.
     *
     * @return time in milliseconds since epoch.
     */
    @JsonProperty("lastRenewalTimestamp")
    public long getRenewalTimestamp() {
        return lastRenewalTimestamp;
    }

    /**
     * Returns the de-registration timestamp.
     *
     * @return time in milliseconds since epoch.
     */
    public long getEvictionTimestamp() {
        return evictionTimestamp;
    }

    /**
     * Returns the service UP timestamp.
     *
     * @return time in milliseconds since epoch.
     */
    public long getServiceUpTimestamp() {
        return serviceUpTimestamp;
    }

    /**
     * Returns client specified setting for renew interval.
     *
     * @return time in milliseconds since epoch.
     */
    public int getRenewalIntervalInSecs() {
        return renewalIntervalInSecs;
    }

    /**
     * Returns client specified setting for eviction (e.g. how long to wait w/o
     * renewal event)
     *
     * @return time in milliseconds since epoch.
     */
    public int getDurationInSecs() {
        return durationInSecs;
    }

}
