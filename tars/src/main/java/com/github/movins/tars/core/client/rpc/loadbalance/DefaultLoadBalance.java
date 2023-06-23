/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.github.movins.tars.core.client.rpc.loadbalance;

import com.github.movins.tars.api.client.ServantProxyConfig;
import com.github.movins.tars.api.common.util.Constants;
import com.github.movins.tars.api.common.util.StringUtils;
import com.github.movins.tars.core.rpc.common.InvokeContext;
import com.github.movins.tars.core.rpc.common.Invoker;
import com.github.movins.tars.core.rpc.common.LoadBalance;
import com.github.movins.tars.core.rpc.common.exc.NoInvokerException;

import java.util.Collection;

public class DefaultLoadBalance<T> implements LoadBalance<T> {

    private final RoundRobinLoadBalance<T> roundRobinLoadBalance;
    private final ServantProxyConfig config;

    private volatile Collection<Invoker<T>> lastRefreshInvokers = null;

    private volatile HashLoadBalance<T> hashLoadBalance = null;
    private final Object hashLoadBalanceLock = new Object();

    private volatile ConsistentHashLoadBalance<T> consistentHashLoadBalance = null;
    private final Object consistentHashLoadBalanceLock = new Object();

    public DefaultLoadBalance(ServantProxyConfig config) {
        this.config = config;
        this.roundRobinLoadBalance = new RoundRobinLoadBalance<T>(config);
    }

    @Override
    public Invoker<T> select(InvokeContext invocation) throws NoInvokerException {
        long hash = 0;
        if (invocation.getAttachment(Constants.TARS_HASH) != null) {
            hash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_HASH).toString(), 0));
        }
        long consistentHash = 0;
        if (invocation.getAttachment(Constants.TARS_CONSISTENT_HASH) != null) {
            consistentHash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_CONSISTENT_HASH).toString(), 0));
        }

        if (consistentHash > 0) {
            if (consistentHashLoadBalance == null) {
                synchronized (consistentHashLoadBalanceLock) {
                    if (consistentHashLoadBalance == null) {
                        ConsistentHashLoadBalance<T> tmp = new ConsistentHashLoadBalance<T>(config);
                        tmp.refresh(lastRefreshInvokers);
                        consistentHashLoadBalance = tmp;
                    }
                }
            }
            return consistentHashLoadBalance.select(invocation);
        }

        if (hash > 0) {
            if (hashLoadBalance == null) {
                synchronized (hashLoadBalanceLock) {
                    if (hashLoadBalance == null) {
                        HashLoadBalance<T> tmp = new HashLoadBalance<T>(config);
                        tmp.refresh(lastRefreshInvokers);
                        hashLoadBalance = tmp;
                    }
                }
            }
            return hashLoadBalance.select(invocation);
        }

        return roundRobinLoadBalance.select(invocation);
    }

    @Override
    public void refresh(Collection<Invoker<T>> invokers) {
        lastRefreshInvokers = invokers;

        synchronized (hashLoadBalanceLock) {
            if (hashLoadBalance != null) {
                hashLoadBalance.refresh(invokers);
            }
        }

        synchronized (consistentHashLoadBalanceLock) {
            if (consistentHashLoadBalance != null) {
                consistentHashLoadBalance.refresh(invokers);
            }
        }

        roundRobinLoadBalance.refresh(invokers);
    }

}
