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

package com.github.movins.tars.api.rpc.protocol.tars;

import com.github.movins.tars.api.client.rpc.Request;
import com.github.movins.tars.api.protocol.tars.TarsInputStream;
import com.github.movins.tars.api.rpc.protocol.ServantResponse;

import java.nio.charset.Charset;
import java.util.Map;

public class TarsServantResponse extends ServantResponse implements java.io.Serializable {
    private static final long serialVersionUID = 3163555867604946654L;
    private short version;
    private byte packetType;
    private int messageType;
    @SuppressWarnings("unused")
    private int requestId;
    private int ret;
    private Map<String, String> status;
    private String remark = null;
    private int timeout;
    private Map<String, String> context;

    private Object result;

    private Charset charsetName;
    private TarsInputStream inputStream;
    private TarsServantRequest request;
    private Throwable cause = null;

    public TarsServantResponse(int requestId) {
        super(requestId);
    }

    public TarsServantResponse() {
        super(0);
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public byte getPacketType() {
        return packetType;
    }

    public void setPacketType(byte packetType) {
        this.packetType = packetType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public Map<String, String> getStatus() {
        return status;
    }

    public void setStatus(Map<String, String> status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public TarsInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(TarsInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Request getRequest() {
        return this.request;
    }


    public void setRequest(TarsServantRequest request) {
        this.request = request;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Charset getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(Charset charsetName) {
        this.charsetName = charsetName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }


}
