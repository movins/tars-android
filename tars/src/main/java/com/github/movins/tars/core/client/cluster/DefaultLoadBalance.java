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

package com.github.movins.tars.core.client.cluster;

import com.github.movins.tars.api.client.ServantProxyConfig;
import com.github.movins.tars.api.common.util.Constants;
import com.github.movins.tars.api.common.util.StringUtils;
import com.github.movins.tars.api.support.log.LoggerFactory;
import com.github.movins.tars.core.rpc.common.InvokeContext;
import com.github.movins.tars.core.rpc.common.Invoker;
import com.github.movins.tars.core.rpc.common.exc.NoInvokerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Deprecated
public class DefaultLoadBalance {

    private static final Logger logger = LoggerFactory.getClientLogger();

    private final AtomicInteger sequence = new AtomicInteger();
    private volatile ServantProxyConfig config;

    private final InvokerComparator comparator = new InvokerComparator();

    public DefaultLoadBalance(ServantProxyConfig config) {
        this.config = config;
    }

    public <T> Invoker<T> select(Collection<Invoker<T>> invokers, InvokeContext context) throws NoInvokerException {
//        if (logger.isDebugEnabled()) {
//            logger.debug("{} try to select active invoker, size= {} ", config.getSimpleObjectName(),
//                    (invokers == null || invokers.isEmpty() ? 0 : invokers.size()));
//        }
        if (invokers == null || invokers.isEmpty()) {
            throw new NoInvokerException("no such active connection invoker");
        }
        List<Invoker<T>> list = new ArrayList<Invoker<T>>();
        for (Invoker<T> invoker : invokers) {
            if (!invoker.isAvailable()) {
                ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
                if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System
                        .currentTimeMillis()) {
                    list.add(invoker);
                }
            } else {
                list.add(invoker);
            }
        }
        if (list.isEmpty()) {
            throw new NoInvokerException(config.getSimpleObjectName() + " try to select active invoker, size="
                    + invokers.size() + ", no such active connection invoker");
        }
        Invoker<T> invoker = null;
        long hash = Math.abs(StringUtils.convertLong(context.getAttachment(Constants.TARS_HASH).toString(), 0));
        if (hash > 0) {
            list.sort(comparator);
            invoker = list.get((int) (hash % list.size()));
        } else {
            invoker = list.get((sequence.getAndIncrement() & Integer.MAX_VALUE) % list.size());
        }
        if (!invoker.isAvailable()) {
            logger.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
            ServantInvokerAliveChecker.get(invoker.getUrl()).setLastRetryTime(System.currentTimeMillis());
        }
        return invoker;
    }

    private class InvokerComparator implements Comparator<Invoker<?>> {

        @Override
        public int compare(Invoker<?> o1, Invoker<?> o2) {
            return o1.getUrl().compareTo(o2.getUrl());
        }
    }
}
