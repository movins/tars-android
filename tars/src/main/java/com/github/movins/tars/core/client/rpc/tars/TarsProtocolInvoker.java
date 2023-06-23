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

package com.github.movins.tars.core.client.rpc.tars;

import com.github.movins.tars.api.client.ServantProxyConfig;
import com.github.movins.tars.core.client.rpc.ServantProtocolInvoker;
import com.github.movins.tars.core.rpc.common.InvokeContext;
import com.github.movins.tars.core.rpc.common.Invoker;
import com.github.movins.tars.api.rpc.common.Url;
import com.github.movins.tars.api.rpc.protocol.tars.support.AnalystManager;

import java.lang.reflect.Method;

public class TarsProtocolInvoker<T> extends ServantProtocolInvoker<T> {

    public TarsProtocolInvoker(Class<T> api, ServantProxyConfig config) {
        super(api, config);
        AnalystManager.getInstance().registry(api, servantProxyConfig.getSimpleObjectName());
    }

    @Override
    public Invoker<T> create(Class<T> api, Url url) throws Exception {
        return new TarsInvoker<T>(servantProxyConfig, api, url, getClients(url));
    }

    @Override
    public InvokeContext createContext(Object proxy, Method method, Object[] args) throws Exception {
        return new TarsInvokeContext(method, args, null);
    }
}
