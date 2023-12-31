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

package com.github.movins.tars.api.client.util;

import com.github.movins.tars.api.client.ServantProxyConfig;
import com.github.movins.tars.api.common.util.Constants;
import com.github.movins.tars.api.common.util.StringUtils;
import com.github.movins.tars.api.rpc.common.Url;
import com.github.movins.tars.api.rpc.exc.ClientException;
import com.github.movins.tars.api.support.query.prx.EndpointF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseTools {

    public static String parseSimpleObjectName(String objectName) {
        int point = objectName.indexOf(Constants.TARS_AT);
        if (point != -1) {
            return objectName.substring(0, point);
        }
        return objectName;
    }

    public static String parseSlaveName(String simpleObjectName) {
        String[] tmp = StringUtils.split(simpleObjectName, ".");
        if (tmp.length > 2) {
            return String.format("%s.%s", tmp[0], tmp[1]);
        }
        return simpleObjectName;
    }

    public static boolean hasServerNode(String objectName) {
        int point = objectName.indexOf(Constants.TARS_AT);
        return point != -1;
    }

    public static List<Url> parse(ServantProxyConfig conf) {
        String name = conf.getSimpleObjectName();
        String serverNode = conf.getObjectName();

        int point = serverNode.indexOf(Constants.TARS_AT);
        String services = serverNode.substring(point + 1);
        List<Url> list = new ArrayList<Url>();
        String[] items = services.split(":");
        for (String item : items) {
            list.add(parse(name, item, conf));
        }
        return list;
    }

    public static String parse(List<EndpointF> list, String objName) {
        StringBuilder value = new StringBuilder();
        for (EndpointF endpointF : list) {
            if (value.length() > 0) {
                value.append(":");
            }
            value.append(toFormatString(endpointF, true));
        }
        if (value.length() < 1) {
            return null;
        }
        value.insert(0, Constants.TARS_AT);
        value.insert(0, objName);
        return value.toString();
    }

    public static String toFormatString(EndpointF endpointF, boolean active) {
        StringBuilder value = new StringBuilder();
        if (!(StringUtils.isEmpty(endpointF.host) || endpointF.port <= 0)) {
            value.append(endpointF.istcp == 0 ? "udp" : "tcp").append(" ");
            value.append("-h").append(" ").append(endpointF.host).append(" ");
            value.append("-p").append(" ").append(endpointF.port).append(" ");
            value.append("-t").append(" 3000 ");
            value.append("-a").append(" ").append(active ? "1" : "0").append(" ");
            value.append("-g").append(" ").append(endpointF.grid).append(" ");
            if (endpointF.setId != null && endpointF.setId.length() > 0) {
                value.append(" ").append("-s").append(" ").append(endpointF.setId);
            }
            if (endpointF.weightType != 0) {
                value.append(" ").append("-v").append(" ").append(endpointF.weightType);
                value.append(" ").append("-w").append(" ").append(endpointF.weight);
            }
        }
        return value.toString();
    }

    private static Url parse(String objectName, String content, ServantProxyConfig conf) {
        content = content.toLowerCase();
        int point = content.indexOf("  ");
        while (point > -1) {
            content = StringUtils.replaceAll(content, "  ", " ");
            point = content.indexOf("  ");
        }
        String[] items = StringUtils.split(content, " ");
        if (items.length < 5) {
            throw new ClientException(objectName, "error occurred on parse url|endpoint=" + content);
        }

        String host = null;
        int port = 0;

        int active = 1;
        String setDivision = null;
        String enableAuth = "0";
        int weightType = 0;
        int weight = 0;
        for (int i = 0; i < items.length; i++) {
            switch (items[i]) {
                case "-h":
                    host = items[i + 1];
                    break;
                case "-p":
                    port = Integer.parseInt(items[i + 1]);
                    break;
                case "-a":
                    active = Integer.parseInt(items[i + 1]);
                    break;
                case "-s":
                    setDivision = items[i + 1];
                    break;
                case "-e":
                    enableAuth = items[i + 1];
                    break;
                case "-v":
                    weightType = Integer.parseInt(items[i + 1]);
                    break;
                case "-w":
                    weight = Integer.parseInt(items[i + 1]);
                    break;
            }
        }
        if (StringUtils.isEmpty(host) || port == -1) {
            throw new ClientException(objectName, "error occurred on parse url|endpoint=" + content);
        }

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(Constants.TARS_CLIENT_CONNECTIONS, String.valueOf(conf.getConnections()));
        parameters.put(Constants.TARS_CLIENT_ACTIVE, Boolean.toString(active == 1));
        parameters.put(Constants.TARS_CLIENT_SETDIVISION, setDivision);
        parameters.put(Constants.TARS_CLIENT_CONNECTTIMEOUT, String.valueOf(conf.getConnectTimeout()));
        parameters.put(Constants.TARS_CLIENT_SYNCTIMEOUT, String.valueOf(conf.getSyncTimeout()));
        parameters.put(Constants.TARS_CLIENT_ASYNCTIMEOUT, String.valueOf(conf.getAsyncTimeout()));
        parameters.put(Constants.TARS_CLIENT_UDPMODE, Boolean.toString(items[0].toLowerCase().equals("udp")));
        parameters.put(Constants.TARS_CLIENT_TCPNODELAY, Boolean.toString(conf.isTcpNoDelay()));
        parameters.put(Constants.TARS_CLIENT_CHARSETNAME, conf.getCharsetName());
        parameters.put(Constants.TARS_CLIENT_ENABLEAUTH, enableAuth);
        parameters.put(Constants.TARS_CLIENT_WEIGHT_TYPE, String.valueOf(weightType));
        parameters.put(Constants.TARS_CLIENT_WEIGHT, String.valueOf(weight));

        return new Url(conf.getProtocol(), host, port, objectName, parameters);
    }
}
