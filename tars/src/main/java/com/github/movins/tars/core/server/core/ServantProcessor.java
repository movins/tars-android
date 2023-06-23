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


import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.client.rpc.Response;
import com.github.movins.tars.api.rpc.protocol.tars.TarsServantRequest;
import io.netty.channel.Channel;

public class ServantProcessor implements Processor {

    private final TarsServantProcessor processor = new TarsServantProcessor();

    @Override
    public Response process(Request request, Channel channel) {
        Response response = null;

        if (request instanceof TarsServantRequest) {
            response = processor.process(request, channel);
        } else {
            throw new IllegalArgumentException("unknown request type.");
        }
        return response;
    }

    @Override
    public void overload(Request request, Channel session) {

    }

}
