package com.github.movins.tars.core.rpc.protocol.tars;

import com.github.movins.tars.api.rpc.protocol.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.IOException;
import java.nio.charset.Charset;

public class TarsClientCodec implements Codec {

    public TarsClientCodec(String charsetName) {

    }

    @Override
    public void encode(Channel channel, ByteBuf channelBuffer, Object message) throws IOException {

    }

    @Override
    public Object decode(Channel channel, ByteBuf buffer) throws IOException {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public Charset getCharset() {
        return null;
    }
}
