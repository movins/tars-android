package com.github.movins.tars.core.support.trace;

import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;
import com.github.movins.tars.core.client.util.Pair;
import com.github.movins.tars.core.common.Filter;
import com.github.movins.tars.core.common.FilterChain;
import com.github.movins.tars.core.rpc.exc.TimeoutException;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantRequest;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantResponse;
import com.github.movins.tars.api.server.config.ConfigurationManager;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.CompletableFuture;

public class TraceCallbackFilter implements Filter {

    private boolean isTrace = false;

    @Override
    public void init() {
        isTrace = ConfigurationManager.getInstance().getServerConfig().getSampleRate() > 0;
    }

    @Override
    public void doFilter(Request request, Response response, FilterChain chain)
            throws Throwable {
        if (!isTrace) {
            chain.doFilter(request, response);
            return;
        }
        if (response == null || !(request instanceof TarsServantRequest) || !TraceUtil.checkServant(((TarsServantRequest) request).getServantName())) {
            chain.doFilter(request, response);
            return;
        }
        Pair<Tracer, Span> entry = TraceManager.getInstance().getCurrentSpan(response.getRequestId());
        if (entry != null && entry.getFirst() != null && entry.getSecond() != null) {
            try (Scope scope = entry.getFirst().scopeManager().activate(entry.getSecond(), true)) {
                TarsServantResponse tarsServantResponse = (TarsServantResponse) response;
                if (tarsServantResponse.getCause() instanceof TimeoutException) {
                    scope.span().log(tarsServantResponse.getCause().getMessage());
                } else {
                    scope.span().setTag("tars.retcode", tarsServantResponse.getRet());
                }

            }
            TraceManager.getInstance().removeSpan(response.getRequestId());
        }

        chain.doFilter(request, response);
    }

    @Override
    public CompletableFuture<Response> doFilter(Request request, FilterChain chain) throws Throwable {
        return null;
    }

    @Override
    public void destroy() {


    }


}
