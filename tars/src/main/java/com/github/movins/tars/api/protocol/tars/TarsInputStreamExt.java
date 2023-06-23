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

package com.github.movins.tars.api.protocol.tars;

import com.github.movins.tars.api.common.util.BeanAccessor;
import com.github.movins.tars.api.common.util.CollectionUtils;
import com.github.movins.tars.api.common.util.CommonUtils;
import com.github.movins.tars.api.protocol.tars.TarsInputStream.HeadData;
import com.github.movins.tars.api.protocol.tars.exc.TarsDecodeException;
import com.github.movins.tars.api.protocol.tars.support.TarsStructInfo;
import com.github.movins.tars.api.protocol.tars.support.TarsStrutPropertyInfo;
import com.github.movins.tars.api.protocol.util.TarsHelper;

import java.util.List;

@SuppressWarnings("unchecked")
public class TarsInputStreamExt {

    public static <T> T read(T e, int tag, boolean isRequire, TarsInputStream jis) {
        TarsStructInfo info = TarsHelper.getStructInfo(e.getClass());

        if (info == null) {
            throw new TarsDecodeException("the JavaBean[" + e.getClass().getSimpleName() + "] no annotation Struct");
        }

        if (jis.skipToTag(tag)) {
            HeadData hd = new HeadData();
            jis.readHead(hd);
            if (hd.type != TarsStructBase.STRUCT_BEGIN) {
                throw new TarsDecodeException("type mismatch.");
            }

            T result = (T) CommonUtils.newInstance(e.getClass());

            List<TarsStrutPropertyInfo> list = info.getPropertyList();
            if (CollectionUtils.isNotEmpty(list)) {
                for (TarsStrutPropertyInfo propertyInfo : list) {
                    Object value = jis.read(propertyInfo.getStamp(), propertyInfo.getOrder(), propertyInfo.isRequire());
                    BeanAccessor.setBeanValue(result, propertyInfo.getName(), value);
                }
            }
            jis.skipToStructEnd();
            return result;

        } else if (isRequire) {
            throw new TarsDecodeException("require field not exist.");
        }
        return null;
    }
}
