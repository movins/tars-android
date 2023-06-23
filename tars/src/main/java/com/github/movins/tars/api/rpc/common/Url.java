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

package com.github.movins.tars.api.rpc.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("serial")
public final class Url implements Comparable<Url>, Serializable {

    private final String protocol;
    private final String host;
    private final int port;
    private final String path;
    private final Map<String, String> parameters;
    private volatile transient String identity;

    public Url(String protocol, String host, int port) {
        this(protocol, host, port, null, (Map<String, String>) null);
    }

    public Url(String protocol, String host, int port, String path) {
        this(protocol, host, port, path, (Map<String, String>) null);
    }

    public Url(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, host, port, null, parameters);
    }

    public Url(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    public String getPath() {
        return path;
    }

    public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public int getParameter(String key, int defaultValue) {
        String value = getParameter(key, null);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public long getParameter(String key, long defaultValue) {
        String value = getParameter(key, null);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key, null);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public String toIdentityString() {
        if (identity != null) {
            return identity;
        }
        StringBuilder s = new StringBuilder();
        s.append(this.protocol).append("://").append(this.host).append(":").append(this.port).append(getAbsolutePath());
        identity = s.toString();
        return identity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Url other = (Url) obj;
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (!Objects.equals(this.port, other.port)) {
            return false;
        }
        return Objects.equals(this.protocol, other.protocol);
    }

    @Override
    public int compareTo(Url url) {
        int i = this.host.compareTo(url.host);
        if (i == 0) {
            i = Integer.compare(this.port, url.port);
        }
        return i;
    }
}
