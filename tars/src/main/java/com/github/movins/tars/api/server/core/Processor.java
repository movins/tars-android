package com.github.movins.tars.api.server.core;

import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;
import io.netty.channel.Channel;

public interface Processor {
    /**
     * @param request
     * @param clientChannel
     * @return
     */
    Response process(Request request, Channel clientChannel);

    /**
     * @param request
     * @param clientChannel
     */
    void overload(Request request, Channel clientChannel);
}
