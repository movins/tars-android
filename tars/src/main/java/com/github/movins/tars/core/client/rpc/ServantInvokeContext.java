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

package com.github.movins.tars.core.client.rpc;

import com.github.movins.tars.api.protocol.util.TarsHelper;
import com.github.movins.tars.core.rpc.common.InvokeContext;
import com.github.movins.tars.core.rpc.common.Invoker;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ServantInvokeContext implements InvokeContext, Serializable {
    private Invoker<?> invoker;
    private String methodName;
    private Object[] arguments;
    private Class<?>[] parameterTypes;
    private final boolean isAsync;
    private final boolean isPromiseFuture;
    private final boolean isNormal;
    private Map<String, String> attachments;

    public ServantInvokeContext(Method method, Object[] arguments, Map<String, String> attachments) {
        this(method, arguments, attachments, null);
    }

    public ServantInvokeContext(Method method, Object[] arguments, Map<String, String> attachments, Invoker<?> invoker) {
        this.setInvoker(invoker);
        this.setMethodName(method.getName());
        this.setParameterTypes(method.getParameterTypes());
        this.setArguments(arguments);
        this.setAttachments(attachments);

        if (invoker != null) {
            this.addAttachmentsIfAbsent(invoker.getUrl().getParameters());
        }
        this.isAsync = TarsHelper.isAsync(methodName);
        this.isPromiseFuture = TarsHelper.isPromiseFuture(methodName);
        this.isNormal = !this.isAsync && !this.isPromiseFuture ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public boolean isPromiseFuture() {
        return isPromiseFuture;
    }

    public Invoker<?> getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments == null ? new HashMap<>() : attachments;
    }

    public void setAttachment(String key, String value) {
        attachments.put(key, value);
    }

    public void setAttachmentIfAbsent(String key, String value) {
        if (!attachments.containsKey(key)) {
            attachments.put(key, value);
        }
    }

    public void addAttachments(Map<String, String> attachments) {
        this.attachments.putAll(attachments);
    }

    public void addAttachmentsIfAbsent(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            setAttachmentIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    public Object getAttachment(String key) {
        return attachments.get(key);
    }

    public Object getAttachment(String key, String defaultValue) {
        Object value = attachments.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public String toString() {
        return "ServantInvokeContext{" +
                "methodName='" + methodName + '\'' +
                ", isAsync=" + isAsync +
                ", isPromiseFuture=" + isPromiseFuture +
                '}';
    }

    public boolean isNormal() {
        return isNormal;
    }
}
