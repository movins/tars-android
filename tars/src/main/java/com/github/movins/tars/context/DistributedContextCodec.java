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
package com.github.movins.tars.context;

import java.util.Map;

public interface DistributedContextCodec {

    /**
     * @param context
     * @return
     */
    byte[] context2Bytes(Map<String, Object> context);

    /**
     * @param context
     * @param src
     */
    Map<String, Object> loadBytes(byte[] src);

    /**
     * @param context
     * @param src
     * @param offset
     * @param length
     */
    Map<String, Object> loadBytes(byte[] src, int offset, int length);

    /**
     * @param context
     * @return
     */
    String context2String(Map<String, Object> context);

    /**
     * @param context
     * @param src
     */
    Map<String, Object> loadString(String src);

}
