package com.github.movins.tars.core.client.rpc.tars;

import com.github.movins.tars.api.client.rpc.RPCClient;
import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;
import com.github.movins.tars.core.common.AbstractFilterChain;
import com.github.movins.tars.core.common.Filter;
import com.github.movins.tars.core.common.FilterKind;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TarsClientFilterChain extends AbstractFilterChain<RPCClient> {

    private Request.InvokeStatus type;

    public TarsClientFilterChain(List<Filter> filters, String servant,
                                 FilterKind kind, RPCClient target, Request.InvokeStatus type) {
        super(filters, servant, kind, target);
        this.type = type;

    }


    @Override
    protected CompletableFuture<Response> doRealInvoke(Request request) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            return target.send(request);
        } else {
            throw new RuntimeException("[tars] tarsClient Filterchian invoke error!");
        }

    }

    @Override
    protected void doRealInvoke(Request request, Response response) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            target.send(request);
        } else {
            throw new RuntimeException("[tars] tarsClient Filterchian invoke error!");
        }
    }


    public CompletableFuture<Response> doFilter(Request request) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            return target.send(request);
        } else {
            throw new RuntimeException("[tars] tarsClient FilterChain invoke error!");
        }
    }

}