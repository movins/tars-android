package com.github.movins.tars.core.common;


import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;

import java.util.concurrent.CompletableFuture;

public interface FilterChain {


    /***
     *
     * @param request
     * @return
     * @throws Throwable
     */
    CompletableFuture<Response> doFilter(Request request) throws Throwable;

    /***
     *
     * @param request
     * @param response
     * @throws Throwable
     */
    void doFilter(Request request, Response response) throws Throwable;


}
