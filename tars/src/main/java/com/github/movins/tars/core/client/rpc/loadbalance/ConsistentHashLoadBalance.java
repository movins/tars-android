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
import com.github.movins.tars.api.common.util.CollectionUtils;
import com.github.movins.tars.api.common.util.Constants;
import com.github.movins.tars.api.common.util.StringUtils;
import com.github.movins.tars.api.support.log.LoggerFactory;
import com.github.movins.tars.core.client.cluster.ServantInvokerAliveChecker;
import com.github.movins.tars.core.client.cluster.ServantInvokerAliveStat;
import com.github.movins.tars.core.client.rpc.InvokerComparator;
import com.github.movins.tars.core.rpc.common.InvokeContext;
import com.github.movins.tars.core.rpc.common.Invoker;
import com.github.movins.tars.core.rpc.common.LoadBalance;
import com.github.movins.tars.core.rpc.common.exc.NoInvokerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

public class ConsistentHashLoadBalance<T> implements LoadBalance<T> {
    private static final Logger logger = LoggerFactory.getClientLogger();

    private final ServantProxyConfig config;
    private final InvokerComparator comparator = new InvokerComparator();

    private volatile TreeMap<Long, Invoker<T>> conHashInvokersCache = null;
    private volatile List<Invoker<T>> sortedInvokersCache = null;

    public ConsistentHashLoadBalance(ServantProxyConfig config) {
        this.config = config;
    }

    @Override
    public Invoker<T> select(InvokeContext invocation) throws NoInvokerException {
        long consistentHash = Math.abs(StringUtils.convertLong(invocation.getAttachment(Constants.TARS_CONSISTENT_HASH).toString(), 0));
        //hash range is 0 ~ 2^32-1
        consistentHash = consistentHash & 0xFFFFFFFFL;

        TreeMap<Long, Invoker<T>> conHashInvokers = conHashInvokersCache;
        //Consistent hash
        if (conHashInvokers != null && !conHashInvokers.isEmpty()) {
            if (!conHashInvokers.containsKey(consistentHash)) {
                SortedMap<Long, Invoker<T>> tailMap = conHashInvokers.tailMap(consistentHash);
                if (tailMap.isEmpty()) {
                    consistentHash = conHashInvokers.firstKey();
                } else {
                    consistentHash = tailMap.firstKey();
                }
            }

            Invoker<T> invoker = conHashInvokers.get(consistentHash);
            if (invoker.isAvailable()) return invoker;

            ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
            if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                //Shield then call
                logger.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
                stat.setLastRetryTime(System.currentTimeMillis());
                return invoker;
            }
        }

//        if (logger.isDebugEnabled()) {
//            logger.debug(config.getSimpleObjectName() + " can't find active invoker using consistent hash loadbalance. try to use normal hash");
//        }

        //use normal hash
        List<Invoker<T>> sortedInvokers = sortedInvokersCache;
        if (sortedInvokers == null || sortedInvokers.isEmpty()) {
            throw new NoInvokerException("no such active connection invoker");
        }

        List<Invoker<T>> list = new ArrayList<Invoker<T>>();
        for (Invoker<T> invoker : sortedInvokers) {
            if (!invoker.isAvailable()) {
                //Shield then call
                ServantInvokerAliveStat stat = ServantInvokerAliveChecker.get(invoker.getUrl());
                if (stat.isAlive() || (stat.getLastRetryTime() + (config.getTryTimeInterval() * 1000)) < System.currentTimeMillis()) {
                    list.add(invoker);
                }
            } else {
                list.add(invoker);
            }
        }
        //TODO When all is not available. Whether to randomly extract one
        if (list.isEmpty()) {
            throw new NoInvokerException(config.getSimpleObjectName() + " try to select active invoker, size=" + sortedInvokers.size() + ", no such active connection invoker");
        }

        Invoker<T> invoker = list.get((int) (consistentHash % list.size()));

        if (!invoker.isAvailable()) {
            //When all is not available. Whether to randomly extract one
            logger.info("try to use inactive invoker|" + invoker.getUrl().toIdentityString());
            ServantInvokerAliveChecker.get(invoker.getUrl()).setLastRetryTime(System.currentTimeMillis());
        }
        return invoker;
    }

    @Override
    public void refresh(Collection<Invoker<T>> invokers) {
        logger.info(config.getSimpleObjectName() + " try to refresh ConsistentHashLoadBalance's invoker cache, size=" + (invokers == null || invokers.isEmpty() ? 0 : invokers.size()));
        if (CollectionUtils.isEmpty(invokers)) {
            sortedInvokersCache = null;
            conHashInvokersCache = null;
            return;
        }

        List<Invoker<T>> sortedInvokersTmp = new ArrayList<>(invokers);
        sortedInvokersTmp.sort(comparator);

        sortedInvokersCache = sortedInvokersTmp;
        conHashInvokersCache = LoadBalanceHelper.buildConsistentHashCircle(sortedInvokersTmp, config);

        logger.info(config.getSimpleObjectName() + " refresh ConsistentHashLoadBalance's invoker cache done, conHashInvokersCache size=" + (conHashInvokersCache == null || conHashInvokersCache.isEmpty() ? 0 : conHashInvokersCache.size()) + ", sortedInvokersCache size=" + (sortedInvokersCache == null || sortedInvokersCache.isEmpty() ? 0 : sortedInvokersCache.size()));
    }

}
