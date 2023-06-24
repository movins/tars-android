package com.github.movins.tars.core.server.core;

import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;
import com.github.movins.tars.core.common.AbstractFilterChain;
import com.github.movins.tars.core.common.Filter;
import com.github.movins.tars.core.common.FilterKind;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantRequest;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TarsServerFilterChain extends AbstractFilterChain<ServantHomeSkeleton> {

    public TarsServerFilterChain(List<Filter> filters, String servant,
                                 FilterKind kind, ServantHomeSkeleton target) {
        super(filters, servant, kind, target);
    }

    @Override
    protected void doRealInvoke(Request request, Response response)
            throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            Object value = target.invoke(tarsServantRequest.getMethodInfo().getMethod(), tarsServantRequest.getMethodParameters());
            TarsServantResponse tarsServantResponse = (TarsServantResponse) response;
            tarsServantResponse.setResult(value);
        }
    }

    @Override
    protected CompletableFuture<Response> doRealInvoke(Request request) throws Throwable {
        return null;
    }

    @Override
    public CompletableFuture<Response> doFilter(Request request) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            TarsServantRequest tarsServantRequest = (TarsServantRequest) request;
            Object value = target.invoke(tarsServantRequest.getMethodInfo().getMethod(), tarsServantRequest.getMethodParameters());
            TarsServantResponse tarsServantResponse = new TarsServantResponse(request.getRequestId());
            tarsServantResponse.setResult(value);
            return CompletableFuture.completedFuture(tarsServantResponse);
        } else {
            throw new RuntimeException("[Tars] invoke error!");
        }
    }
}
