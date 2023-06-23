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

import java.util.concurrent.ConcurrentHashMap;

import com.github.movins.tars.api.client.ServantProxyConfig;
import com.github.movins.tars.api.rpc.common.Url;

public class ServantInvokerAliveChecker {

    private static final ConcurrentHashMap<String, ServantInvokerAliveStat> cache = new ConcurrentHashMap<String, ServantInvokerAliveStat>();

    public static ServantInvokerAliveStat get(Url url) {
        String identity = url.toIdentityString();
        ServantInvokerAliveStat stat = cache.get(identity);
        if (stat == null) {
            cache.putIfAbsent(identity, new ServantInvokerAliveStat(identity));
        }
        return cache.get(identity);
    }

    public static boolean isAlive(Url url, ServantProxyConfig config, int ret) {
        ServantInvokerAliveStat stat = get(url);
        stat.onCallFinished(ret, config);
        return stat.isAlive();
    }
}
