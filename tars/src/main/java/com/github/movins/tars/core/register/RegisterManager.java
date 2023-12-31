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

package com.github.movins.tars.core.register;

import java.util.ServiceLoader;

public class RegisterManager {
    private RegisterHandler handler;

    private static final RegisterManager instance = new RegisterManager();

    public static RegisterManager getInstance() {
         return instance;
    }

    RegisterManager() {
        ServiceLoader<RegisterHandler> loaders = ServiceLoader.load(RegisterHandler.class);
        if (loaders.iterator().hasNext()) {
            handler = loaders.iterator().next();
        } else {
            handler = null;
        }
    }

    public void setHandler(RegisterHandler handler) {
        this.handler = handler;
    }

    public RegisterHandler getHandler() {
        return handler;
    }
}
