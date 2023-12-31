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

package com.github.movins.tars.api.common.logger;

import java.util.ServiceLoader;

public class LoggerFactoryManager {
    private LoggerFactory handler;

    private static final LoggerFactoryManager instance = new LoggerFactoryManager();

    public static LoggerFactoryManager getInstance() {
        return instance;
    }

    LoggerFactoryManager() {
        ServiceLoader<LoggerFactory> loaders = ServiceLoader.load(LoggerFactory.class);
        if (loaders.iterator().hasNext()) {
            handler = loaders.iterator().next();
        } else {
            handler = null;
        }

    }

    public void setHandler(LoggerFactory handler) {
        this.handler = handler;
    }

    public LoggerFactory getHandler() {
        return handler;
    }
}
