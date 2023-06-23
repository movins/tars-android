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

import com.github.movins.tars.api.protocol.util.TarsHelper;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantRequest;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class AsyncContext {
    public static final String PORTAL_CAP_ASYNC_CONTEXT_ATTRIBUTE = "internal.asynccontext";

    private Context<TarsServantRequest, TarsServantResponse> context = null;
    private static final Logger flowLogger = LoggerFactory.getLogger("tarsserver");

    public static AsyncContext startAsync() throws IOException {
        Context<TarsServantRequest, TarsServantResponse> context = ContextManager.getContext();
        AsyncContext aContext = new AsyncContext(context);
        context.response().asyncCallStart();
        context.setAttribute(PORTAL_CAP_ASYNC_CONTEXT_ATTRIBUTE, aContext);
        return aContext;
    }

    private AsyncContext(Context<TarsServantRequest, TarsServantResponse> context) {
        this.context = context;
    }

    private ServantHomeSkeleton getCapHomeSkeleton() {
        AppContext appContext = AppContextManager.getInstance().getAppContext();
        return appContext.getCapHomeSkeleton(this.context.request().getServantName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) this.context.getAttribute(name);
    }

    public <T> T getAttribute(String name, T defaultValue) {
        return (T) this.context.getAttribute(name, defaultValue);
    }

    public <T> void setAttribute(String name, T value) {
        this.context.setAttribute(name, value);
    }

    public Context getContext() {
        return this.context;
    }

    public void writeException(Throwable ex) throws IOException {
        TarsServantResponse response = this.context.response();
        response.setRet(TarsHelper.SERVERUNKNOWNERR);
        response.setCause(ex);
        response.setResult(null);
        response.asyncCallEnd();

        getCapHomeSkeleton().postInvokeCapHomeSkeleton();
        Long startTime = this.context.getAttribute(Context.INTERNAL_START_TIME);
        TarsServantProcessor.printServiceFlowLog(flowLogger, this.context.request(), response.getRet(), (System.currentTimeMillis() - startTime), ex.toString());

    }

    public void writeResult(Object result) throws IOException {
        TarsServantResponse response = this.context.response();
        response.setRet(TarsHelper.SERVERSUCCESS);
        response.setCause(null);
        response.setResult(result);
        response.asyncCallEnd();

        getCapHomeSkeleton().postInvokeCapHomeSkeleton();
        Long startTime = this.context.getAttribute(Context.INTERNAL_START_TIME);
        TarsServantProcessor.printServiceFlowLog(flowLogger, this.context.request(), response.getRet(), (System.currentTimeMillis() - startTime), "");
    }
}
