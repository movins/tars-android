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

package com.github.movins.tars.core.server.core;

import java.lang.reflect.Method;

public class ServantHomeSkeleton extends AppService {

    private String name = null;
    private Object servantImpl = null;
    private Class<?> apiClass = null;

    public ServantHomeSkeleton(String name, Object servantImpl, Class<?> apiClass, int loadLimit) {
        this.name = name;
        this.servantImpl = servantImpl;
        this.apiClass = apiClass;
    }

    public Object getServant() {
        return servantImpl;
    }

    public Object invoke(Method method, Object... args) throws Exception {
        Object value = null;
        try {
            preInvokeCapHomeSkeleton();
            value = method.invoke(this.servantImpl, fixParamValueType(method, args));
        } finally {
            if (!ContextManager.getContext().response().isAsyncMode()) {
                postInvokeCapHomeSkeleton();
            }
        }
        return value;
    }

    private Object[] fixParamValueType(Method method, Object args[]) {
        if (args == null || args.length == 0) return args;
        Class<?> parameterTypes[] = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) return args;

        if (args.length != parameterTypes.length) return args;

        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = fixValueDataType(parameterTypes[i], args[i]);
        }

        return args;
    }

    private Object fixValueDataType(Class<?> dataType, Object value) {
        Object dataValue = value;

        if (dataType != null && dataValue != null) {
            if ("short".equals(dataType.getName())) {
                dataValue = Short.valueOf(dataValue.toString());
            } else if ("byte".equals(dataType.getName())) {
                dataValue = Byte.valueOf(dataValue.toString());
            } else if (char.class == dataType) {
                dataValue = ((String) value).charAt(0);
            } else if ("float".equals(dataType.getName())) {
                dataValue = Float.valueOf(dataValue.toString());
            }
        }

        return dataValue;
    }

    public void preInvokeCapHomeSkeleton() {
    }

    public void postInvokeCapHomeSkeleton() {
    }

    public Class<?> getApiClass() {
        return this.apiClass;
    }


    public String name() {
        return this.name;
    }


    private AppContext appContext;

    public void setAppContext(AppContext context) {
        appContext = context;
    }

    public AppContext getAppContext() {
        return appContext;
    }
}
