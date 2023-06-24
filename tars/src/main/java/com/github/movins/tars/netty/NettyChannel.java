package com.github.movins.tars.netty;

import com.github.movins.tars.api.client.ServantProxyConfig;

import io.netty.channel.Channel;

public class NettyChannel {
    private final Channel channel;
    private final ServantProxyConfig servantProxyConfig;

    public NettyChannel(Channel channel, ServantProxyConfig servantProxyConfig) {
        this.channel = channel;
        this.servantProxyConfig = servantProxyConfig;

    }

    public Channel getChannel() {
        return channel;
    }

    public ServantProxyConfig getServantProxyConfig() {
        return servantProxyConfig;
    }

    @Override
    public String toString() {
        return "NettyTarsChannel{" +
                "channel=" + channel +
                ", servantProxyConfig=" + servantProxyConfig +
                '}';
    }
}
