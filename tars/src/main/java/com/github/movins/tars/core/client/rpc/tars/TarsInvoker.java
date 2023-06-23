/**
 * Tencent is pleased to support the open source community by making Tars available.
 * <p>
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.github.movins.tars.core.client.rpc.tars;

import com.github.movins.tars.api.rpc.protocol.tars.TarsServantResponse;

import com.github.movins.tars.api.client.ServantProxyConfig;
import com.github.movins.tars.core.client.cluster.ServantInvokerAliveChecker;
import com.github.movins.tars.api.client.rpc.RPCClient;
import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;
import com.github.movins.tars.core.client.rpc.ServantInvokeContext;
import com.github.movins.tars.core.client.rpc.ServantInvoker;
import com.github.movins.tars.core.common.Filter;
import com.github.movins.tars.core.common.FilterChain;
import com.github.movins.tars.core.common.FilterKind;
import com.github.movins.tars.api.common.util.Constants;
import com.github.movins.tars.core.common.util.DyeingSwitch;
import com.github.movins.tars.context.DistributedContext;
import com.github.movins.tars.context.DistributedContextManager;
import com.github.movins.tars.api.protocol.tars.support.TarsMethodInfo;
import com.github.movins.tars.api.protocol.util.TarsHelper;
import com.github.movins.tars.api.rpc.common.Url;
import com.github.movins.tars.core.rpc.exc.NotConnectedException;
import com.github.movins.tars.core.rpc.exc.ServerException;
import com.github.movins.tars.api.rpc.exc.TarsException;
import com.github.movins.tars.core.rpc.exc.TimeoutException;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantRequest;
import com.github.movins.tars.api.rpc.protocol.tars.support.AnalystManager;
import com.github.movins.tars.core.server.core.AppContextManager;
import com.github.movins.tars.core.support.stat.InvokeStatHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TarsInvoker<T> extends ServantInvoker<T> {

    final List<Filter> filters;

    public TarsInvoker(ServantProxyConfig config, Class<T> api, Url url, RPCClient[] clients) {
        super(config, api, url, clients);
        System.out.println(AppContextManager.getInstance().getAppContext() == null);
        filters = AppContextManager.getInstance().getAppContext() == null ? null : AppContextManager.getInstance().getAppContext().getFilters(FilterKind.CLIENT);
    }

    @Override
    public void setAvailable(boolean available) {
        super.setAvailable(available);
    }

    protected Object doInvokeServant(final ServantInvokeContext inv) throws Throwable {
        final long begin = System.currentTimeMillis();
        int ret = Constants.INVOKE_STATUS_SUCC;
        try {
            Method method = getApi().getMethod(inv.getMethodName(), inv.getParameterTypes());
            if (inv.isPromiseFuture()) {
                return invokeWithPromiseFuture(method, inv.getArguments(), inv.getAttachments());// return Future Result
            } else {
                TarsServantResponse response = invokeWithSync(method, inv.getArguments(), inv.getAttachments());
                ret = response.getRet() == TarsHelper.SERVERSUCCESS ? Constants.INVOKE_STATUS_SUCC : Constants.INVOKE_STATUS_EXEC;
                if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                    throw ServerException.makeException(response.getRet(), response.getRemark());
                }
                if (response.getCause() != null) {
                    throw new TarsException(response.getCause());
                }
                return response.getResult();
            }
        } catch (Throwable e) {
            if (e instanceof TimeoutException) {
                ret = Constants.INVOKE_STATUS_TIMEOUT;
            } else if (e instanceof NotConnectedException) {
                ret = Constants.INVOKE_STATUS_NETCONNECTTIMEOUT;
            } else {
                ret = Constants.INVOKE_STATUS_EXEC;
            }
            throw e;
        } finally {
            if (inv.isNormal()) {
                setAvailable(ServantInvokerAliveChecker.isAlive(getUrl(), config, ret));
                InvokeStatHelper.getInstance().addProxyStat(objName)
                        .addInvokeTimeByClient(config.getMasterName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(),
                                config.getSlaveSetID(), inv.getMethodName(), getUrl().getHost(), getUrl().getPort(), ret, System.currentTimeMillis() - begin);
            }
        }
    }

    private RPCClient getClient() {
        return clients.length == 1 ? clients[0] : clients[(index.getAndIncrement() & Integer.MAX_VALUE) % clients.length];
    }

    private TarsServantResponse invokeWithSync(Method method, Object args[], Map<String, String> context) throws Throwable {
        RPCClient client = getClient();
        TarsServantRequest request = new TarsServantRequest();
        request.setVersion(TarsHelper.VERSION);
        request.setMessageType(isHashInvoke(context) ? TarsHelper.MESSAGETYPEHASH : TarsHelper.MESSAGETYPENULL);
        request.setPacketType(TarsHelper.NORMAL);
        request.setServantName(objName);
        request.setFunctionName(method.getName());
        request.setApi(super.getApi());
        request.setMethodInfo(AnalystManager.getInstance().getMethodMap(super.getApi()).get(method));
        request.setMethodParameters(args);
        request.setContext(context);
        request.setInvokeStatus(Request.InvokeStatus.SYNC_CALL);
        DistributedContext distributedContext = DistributedContextManager.getDistributedContext();
        Boolean bDyeing = distributedContext.get(DyeingSwitch.BDYEING);
        if (bDyeing != null && bDyeing == true) {
            request.setMessageType(request.getMessageType() | TarsHelper.MESSAGETYPEDYED);
            HashMap<String, String> status = new HashMap<>();
            String routeKey = distributedContext.get(DyeingSwitch.DYEINGKEY);
            String fileName = distributedContext.get(DyeingSwitch.FILENAME);
            status.put(DyeingSwitch.STATUS_DYED_KEY, routeKey == null ? "" : routeKey);
            status.put(DyeingSwitch.STATUS_DYED_FILENAME, fileName == null ? "" : fileName);
            request.setStatus(status);
        }
        FilterChain filterChain = new TarsClientFilterChain(filters, objName, FilterKind.CLIENT, client, Request.InvokeStatus.SYNC_CALL);
        CompletableFuture<Response> responseCompletableFuture = filterChain.doFilter(request);
        return (TarsServantResponse) responseCompletableFuture.get(request.getTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * promise call
     * @param method
     * @param args
     * @param context
     */
    private <V> CompletableFuture<V> invokeWithPromiseFuture(Method method, Object args[], Map<String, String> context) throws Throwable {
        final RPCClient client = getClient();
        final TarsServantRequest request = new TarsServantRequest();
        request.setVersion(TarsHelper.VERSION);
        request.setMessageType(isHashInvoke(context) ? TarsHelper.MESSAGETYPEHASH : TarsHelper.MESSAGETYPENULL);
        request.setPacketType(TarsHelper.NORMAL);
        request.setServantName(objName);
        request.setFunctionName(method.getName().replaceAll(Constants.TARS_METHOD_PROMISE_START_WITH, ""));
        request.setContext(context);
        request.setMethodParameters(args); //completableFuture send Callback
        final TarsMethodInfo methodInfo = AnalystManager.getInstance().getMethodMap(super.getApi()).get(method);
        request.setInvokeStatus(Request.InvokeStatus.FUTURE_CALL);
        request.setApi(super.getApi());
        request.setMethodInfo(methodInfo);

        DistributedContext distributedContext = DistributedContextManager.getDistributedContext();
        Boolean bDyeing = distributedContext.get(DyeingSwitch.BDYEING);
        if (bDyeing != null && bDyeing == true) {
            request.setMessageType(request.getMessageType() | TarsHelper.MESSAGETYPEDYED);
            HashMap<String, String> status = new HashMap<>();
            String routeKey = distributedContext.get(DyeingSwitch.DYEINGKEY);
            String fileName = distributedContext.get(DyeingSwitch.FILENAME);
            status.put(DyeingSwitch.STATUS_DYED_KEY, routeKey == null ? "" : routeKey);
            status.put(DyeingSwitch.STATUS_DYED_FILENAME, fileName == null ? "" : fileName);
            request.setStatus(status);
        }
        //sync call all filter
        final FilterChain filterChain = new TarsClientFilterChain(filters, objName, FilterKind.CLIENT, client, Request.InvokeStatus.FUTURE_CALL);
        CompletableFuture<Response> tarsResponse = filterChain.doFilter(request);
        return tarsResponse.thenCompose(tarsresponseObj -> {
            TarsServantResponse tarsServantResponse = (TarsServantResponse) tarsresponseObj;
            if (tarsServantResponse != null) {
                tarsResponse.completeExceptionally(tarsServantResponse.getCause());
            }
            return CompletableFuture.completedFuture((V) tarsServantResponse.getResult());
        });
    }

    private boolean isHashInvoke(Map<String, String> context) {
        return context != null && context.containsKey(Constants.TARS_HASH);
    }
}
