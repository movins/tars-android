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

package com.github.movins.tars.core.client.support;

import com.github.movins.tars.api.client.CommunicatorConfig;
import com.github.movins.tars.api.client.rpc.RPCClient;
import com.github.movins.tars.core.common.util.concurrent.TaskQueue;
import com.github.movins.tars.core.common.util.concurrent.TaskThreadFactory;
import com.github.movins.tars.core.common.util.concurrent.TaskThreadPoolExecutor;
import com.github.movins.tars.api.support.log.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ClientPoolManager {
    private static final Logger logger = LoggerFactory.getClientLogger();
    private final static ConcurrentHashMap<CommunicatorConfig, ThreadPoolExecutor> clientThreadPoolMap = new ConcurrentHashMap<CommunicatorConfig, ThreadPoolExecutor>();

    public static ThreadPoolExecutor getClientThreadPoolExecutor(CommunicatorConfig communicatorConfig) {
        ThreadPoolExecutor clientPoolExecutor = clientThreadPoolMap.get(communicatorConfig);
        if (clientPoolExecutor == null) {
            synchronized (RPCClient.class) {
                clientPoolExecutor = clientThreadPoolMap.get(communicatorConfig);
                if (clientPoolExecutor == null) {
                    clientThreadPoolMap.put(communicatorConfig, createThreadPool(communicatorConfig));
                    clientPoolExecutor = clientThreadPoolMap.get(communicatorConfig);
                }
            }
        }
        return clientPoolExecutor;
    }

    private static ThreadPoolExecutor createThreadPool(CommunicatorConfig communicatorConfig) {
        int corePoolSize = communicatorConfig.getCorePoolSize();
        int maxPoolSize = communicatorConfig.getMaxPoolSize();
        int keepAliveTime = communicatorConfig.getKeepAliveTime();
        int queueSize = communicatorConfig.getQueueSize();
        TaskQueue taskqueue = new TaskQueue(queueSize);
        String namePrefix = "tars-client-executor-";
//        logger.info("create client thread pool, communicator config is {}", communicatorConfig.toString());
        TaskThreadPoolExecutor executor = new TaskThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, taskqueue, new TaskThreadFactory(namePrefix));
        taskqueue.setParent(executor);
        return executor;
    }


    private static int convertInt(String value, int defaults) {
        if (value == null) {
            return defaults;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaults;
        }
    }
}
