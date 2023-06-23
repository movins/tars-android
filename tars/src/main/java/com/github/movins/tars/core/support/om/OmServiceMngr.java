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

package com.github.movins.tars.core.support.om;

import com.github.movins.tars.core.client.Communicator;
import com.github.movins.tars.core.client.CommunicatorFactory;
import com.github.movins.tars.api.common.ClientVersion;
import com.github.movins.tars.api.server.config.ConfigurationManager;
import com.github.movins.tars.core.support.config.ConfigHelper;
import com.github.movins.tars.core.support.node.NodeHelper;
import com.github.movins.tars.core.support.notify.NotifyHelper;
import com.github.movins.tars.core.support.property.CommonPropertyPolicy;
import com.github.movins.tars.core.support.property.JvmPropertyPolicy.GCNumCount;
import com.github.movins.tars.core.support.property.JvmPropertyPolicy.GCTimeSum;
import com.github.movins.tars.core.support.property.JvmPropertyPolicy.MemoryHeapCommittedAvg;
import com.github.movins.tars.core.support.property.JvmPropertyPolicy.MemoryHeapMaxAvg;
import com.github.movins.tars.core.support.property.JvmPropertyPolicy.MemoryHeapUsedAvg;
import com.github.movins.tars.core.support.property.JvmPropertyPolicy.ThreadNumAvg;
import com.github.movins.tars.core.support.property.PropertyReportHelper;
import com.github.movins.tars.core.support.property.PropertyReportHelper.Policy;
import com.github.movins.tars.core.support.trace.TarsTraceZipkinConfiguration;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class OmServiceMngr {

    private static final OmServiceMngr Instance = new OmServiceMngr();

    public static OmServiceMngr getInstance() {
        return Instance;
    }

    public void initAndStartOmService() {
        Communicator communicator = CommunicatorFactory.getInstance().getCommunicator();
        String app = ConfigurationManager.getInstance().getServerConfig().getApplication();
        String serverName = ConfigurationManager.getInstance().getServerConfig().getServerName();
        String basePath = ConfigurationManager.getInstance().getServerConfig().getBasePath();
        String modualName = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig().getModuleName();

        ConfigHelper.getInstance().setConfigInfo(communicator, app, serverName, basePath);
        NodeHelper.getInstance().setNodeInfo(communicator, app, serverName);
        NotifyHelper.getInstance().setNotifyInfo(communicator, app, serverName);
        PropertyReportHelper.getInstance().init(communicator, modualName);
        NodeHelper.getInstance().reportVersion(ClientVersion.getVersion());

        Policy avgPolicy = new CommonPropertyPolicy.Avg();
        Policy maxPolicy = new CommonPropertyPolicy.Max();
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropWaitTime, avgPolicy, maxPolicy);

        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropHeapUsed, new MemoryHeapUsedAvg());
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropHeapCommitted, new MemoryHeapCommittedAvg());
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropHeapMax, new MemoryHeapMaxAvg());
        PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropThreadCount, new ThreadNumAvg());
        for (GarbageCollectorMXBean gcMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropGcCount + gcMXBean.getName(), new GCNumCount(gcMXBean.getName()));
            PropertyReportHelper.getInstance().createPropertyReporter(OmConstants.PropGcTime + gcMXBean.getName(), new GCTimeSum(gcMXBean.getName()));
        }

        ServerStatHelper.getInstance().init(communicator);
        TarsTraceZipkinConfiguration.getInstance().init();
        ScheduledServiceMngr.getInstance().start();
    }

    public void reportWaitingTimeProperty(int value) {
        PropertyReportHelper.getInstance().reportPropertyValue(OmConstants.PropWaitTime, value);
    }
}
