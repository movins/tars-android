
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

package com.github.movins.tars.core.client;

import com.github.movins.tars.api.client.util.ParseTools;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.github.movins.tars.api.client.CommunicatorConfig;

public final class CommunicatorFactory {

    private final static CommunicatorFactory instance = new CommunicatorFactory();

    private final Map<Object, Communicator> communicatorMap = new ConcurrentHashMap<>();

    private volatile Communicator communicator = null;

    public static CommunicatorFactory getInstance() {
        return instance;
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    void setCommunicator(Communicator communicator) {
        if (communicator != null) {
            this.communicator = communicator;
        }
    }

    public Communicator getCommunicator(String locator) {
        Communicator communicator = communicatorMap.get(locator);
        if (communicator != null) {
            return communicator;
        }
        CommunicatorConfig config = null;
        if (ParseTools.hasServerNode(locator)) {
            config = new CommunicatorConfig();
            config.setLocator(locator);
        }
        communicatorMap.putIfAbsent(locator, new Communicator(config));
        return communicatorMap.get(locator);
    }

    public Communicator getCommunicator(CommunicatorConfig config) {
        Communicator communicator = communicatorMap.get(config);
        if (communicator != null) {
            return communicator;
        }
        communicatorMap.putIfAbsent(config, new Communicator(config));
        return communicatorMap.get(config);
    }
}
