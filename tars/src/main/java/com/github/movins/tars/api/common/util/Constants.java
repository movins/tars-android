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

package com.github.movins.tars.api.common.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Constants {

    String SERVER_NODE_CACHE_FILENAME = "tarsnodes.dat";

    int INVOKE_STATUS_SUCC = 0;
    int INVOKE_STATUS_EXEC = 1;
    int INVOKE_STATUS_TIMEOUT = 2;
    int INVOKE_STATUS_NETCONNECTTIMEOUT = 3;

    int default_max_sample_count = 200;
    int default_sample_rate = 1000;

    String DEFAULT_MODULE_NAME = "tars-client";
    String default_stat = "tars.tarsstat.StatObj";

    int DEFAULT_CONNECTION = 4;
    int DEFAULT_CONNECTION_TIMEOUT = 3000;
    int DEFAULT_SYNC_TIME = 3000;
    int DEFAULT_ASYNC_TIME = 3000;

    int default_refresh_interval = 60 * 1000;
    int default_report_interval = 60 * 1000;

    int default_background_queuesize = 20000;

    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
   String DEFAULT_CHARSET_STRING = "UTF-8";
    int DEFAULT_QUEUE_SIZE = 20000;
    int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    int DEFAULT_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    int DEFAULT_KEEPALIVE_TIME = 120;

    int default_check_interval = 60 * 1000;
    int default_try_time_interval = 30;
    int default_min_timeout_invoke = 20;
    int default_frequence_fail_invoke = 50;
    float default_frequence_fail_radio = 0.5f;

    String TARS_PROTOCOL = "tars";
    String HTTP_PROTOCOL = "http";
    String PROTO_PROTOCOL = "protobuff";
    String TARS_AT = "@";
    String TARS_API = "api";
    String TARS_JCE_VERSION = "version";

    String TARS_CLIENT_CONNECTIONS = "connections";
    String TARS_CLIENT_CONNECTTIMEOUT = "connectTimeout";
    String TARS_CLIENT_SYNCTIMEOUT = "syncTimeout";
    String TARS_CLIENT_ASYNCTIMEOUT = "asyncTimeout";
    String TARS_CLIENT_SETDIVISION = "setDivision";
    String TARS_CLIENT_TCPNODELAY = "tcpNoDelay";
    String TARS_CLIENT_ACTIVE = "active";
    String TARS_CLIENT_UDPMODE = "udpMode";
    String TARS_CLIENT_CHARSETNAME = "charsetName";

    String TARS_HASH = "tars_hash";
    String TARS_CONSISTENT_HASH = "taf_consistent_hash";

    String TARS_TUP_CLIENT = "tup_client";
    String TARS_ONE_WAY_CLIENT = "one_way_client";
    String TARS_NOT_CLIENT = "not_tars_client";
    String TARS_CLIENT_ENABLEAUTH = "enableAuth";

    String TARS_CLIENT_WEIGHT_TYPE = "weightType";
    String TARS_CLIENT_WEIGHT = "weight";
    String TARS_CLIENT_GRAYFLAG = "taf.framework.GrayFlag";

    String TARS_METHOD_ASYNC_START_WITH = "async_";
    String TARS_METHOD_PROMISE_START_WITH = "promise_";
}
