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

package com.github.movins.tars.core.rpc.common;

import com.github.movins.tars.core.rpc.common.exc.NoInvokerException;

import java.util.Collection;

public interface LoadBalance<T> {

    /**
     * Use load balancing to select invoker
     * @param invokeContext
     * @return
     * @throws NoInvokerException
     */
    Invoker<T> select(InvokeContext invokeContext) throws NoInvokerException;

    /**
     * Refresh local invoker
     * @param invokers
     */
    void refresh(Collection<Invoker<T>> invokers);
}
