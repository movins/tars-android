/**
 * Tencent is pleased to support the open source community by making Tars available.
 * <p>
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.github.movins.tars.api.server.config;

import com.github.movins.tars.api.client.CommunicatorConfig;
import com.github.movins.tars.api.common.util.Config;
import com.github.movins.tars.api.common.util.StringUtils;

import java.io.IOException;

public class ConfigurationManager {

    private static final ConfigurationManager instance = new ConfigurationManager();

    private ConfigurationManager() {
    }

    private volatile ServerConfig serverConfig;

    private volatile CommunicatorConfig communicatorConfig;

    public static ConfigurationManager getInstance() {
        return instance;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig config) {
        this.serverConfig = config;
    }

    public CommunicatorConfig getCommunicatorConfig() {
        return communicatorConfig;
    }

    public void setCommunicatorConfig(CommunicatorConfig communicatorConfig) {
        this.communicatorConfig = communicatorConfig;
    }

    public void init() throws ConfigurationException {
        loadServerConfig();
    }

    private void loadServerConfig() throws ConfigurationException {
        String key = null, value = null;
        try {
            ServerConfig cfg = new ServerConfig().load(Config.parseFile(System.getProperty("config")));

            key = "app";
            value = cfg.getApplication();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }

            key = "server";
            value = cfg.getServerName();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }

            key = "localip";
            value = cfg.getLocalIP();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }

            key = "local";
            value = cfg.getLocal() == null ? null : cfg.getLocal().toString();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }

            key = "logpath";
            value = cfg.getLogPath();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }
            key = "logLevel";
            value = cfg.getLogLevel();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }

            key = "datapath";
            value = cfg.getDataPath();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException("invalid config");
            }
            this.serverConfig = cfg;
            this.communicatorConfig = cfg.getCommunicatorConfig();
        } catch (IOException ioe) {
            throw new ConfigurationException("read config file error.", ioe);
        } catch (Exception e) {
            if (e instanceof ConfigurationException) {
                throw new ConfigurationException("contains invalid config|key=" + key + ", value=" + value, e);
            } else {
                throw new ConfigurationException("error occurred on load server config", e);
            }
        }
    }
}
