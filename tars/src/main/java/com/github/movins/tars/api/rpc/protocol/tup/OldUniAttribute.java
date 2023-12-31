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

package com.github.movins.tars.api.rpc.protocol.tup;

import com.github.movins.tars.api.protocol.tars.TarsInputStream;
import com.github.movins.tars.api.protocol.tars.TarsOutputStream;
import com.github.movins.tars.api.protocol.tars.TarsStructBase;
import com.github.movins.tars.api.protocol.util.TarsUtil;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class OldUniAttribute {

    protected  Map<String, HashMap<String, byte[]>> _data = new HashMap<String, HashMap<String, byte[]>>();

    protected Map<String, Object> cachedClassName = new HashMap<String, Object>();
    private Map<String, Object> cachedData = new HashMap<String, Object>();

    protected Charset encodeName = StandardCharsets.UTF_8;

    TarsInputStream _is = new TarsInputStream();

    public Charset getEncodeName() {
        return encodeName;
    }

    public void setEncodeName(Charset encodeName) {
        this.encodeName = encodeName;
    }

    public void clearCacheData() {
        cachedData.clear();
    }

    public Set<String> getKeySet() {
        return Collections.unmodifiableSet(_data.keySet());
    }

    public boolean isEmpty() {
        return _data.isEmpty();
    }

    public int size() {
        return _data.size();
    }

    public boolean containsKey(String key) {
        return _data.containsKey(key);
    }

    public <T> void put(String name, T t) {
        if (name == null) {
            throw new IllegalArgumentException("put key can not is null");
        }
        if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        }
        if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        }
        TarsOutputStream _out = new TarsOutputStream();
        _out.setServerEncoding(encodeName);
        _out.write(t, 0);
        byte[] _sBuffer = TarsUtil.getBufArray(_out.getByteBuffer());
        HashMap<String, byte[]> pair = new HashMap<>(1);
        ArrayList<String> listType = new ArrayList<>(1);
        checkObjectType(listType, t);
        String className = BasicClassTypeUtil.transTypeList(listType);
        pair.put(className, _sBuffer);
        cachedData.remove(name);
        _data.put(name, pair);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStruct(String name) throws ObjectCreateException {
        if (!_data.containsKey(name)) {
            return null;
        } else if (cachedData.containsKey(name)) {
            return (T) cachedData.get(name);
        } else {
            HashMap<String, byte[]> pair = _data.get(name);
            String className = null;
            byte[] data = new byte[0];
            for (Entry<String, byte[]> e : pair.entrySet()) {
                className = e.getKey();
                data = e.getValue();
                break;
            }
            try {
                T proxy = (T) getCacheProxy(className);
                _is.warp(data);
                _is.setServerEncoding(encodeName);
                TarsStructBase o = _is.directRead((TarsStructBase) proxy, 0, true);
                saveDataCache(name, o);
                return (T) o;
            } catch (Exception ex) {
                throw new ObjectCreateException(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) throws ObjectCreateException {
        if (!_data.containsKey(name)) {
            return null;
        } else if (cachedData.containsKey(name)) {
            return (T) cachedData.get(name);
        } else {
            HashMap<String, byte[]> pair = _data.get(name);
            String className = null;
            byte[] data = new byte[0];
            for (Entry<String, byte[]> e : pair.entrySet()) {
                className = e.getKey();
                data = e.getValue();
                break;
            }
            try {
                T proxy = (T) getCacheProxy(className);
                _is.warp(data);
                _is.setServerEncoding(encodeName);
                Object o = _is.read(proxy, 0, true);
                saveDataCache(name, o);
                return (T) o;
            } catch (Exception ex) {
                throw new ObjectCreateException(ex);
            }
        }
    }

    private Object getCacheProxy(String className) {
        Object proxy = null;
        if (cachedClassName.containsKey(className)) {
            proxy = cachedClassName.get(className);
        } else {
            proxy = BasicClassTypeUtil.createClassByUni(className);
            cachedClassName.put(className, proxy);
        }
        return proxy;
    }

    private void saveDataCache(String name, Object o) {
        cachedData.put(name, o);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, Object defaultValue) {
        if (!_data.containsKey(name)) {
            return (T) defaultValue;
        } else if (cachedData.containsKey(name)) {
            return (T) cachedData.get(name);
        } else {
            HashMap<String, byte[]> pair = _data.get(name);
            String className = "";
            byte[] data = new byte[0];
            for (Entry<String, byte[]> e : pair.entrySet()) {
                className = e.getKey();
                data = e.getValue();
                break;
            }
            try {
                T proxy = (T) getCacheProxy(className);
                _is.warp(data);
                _is.setServerEncoding(encodeName);
                Object o = _is.read(proxy, 0, true);
                saveDataCache(name, o);
                return (T) o;
            } catch (Exception ex) {
                saveDataCache(name, defaultValue);
                return (T) defaultValue;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(String name) throws ObjectCreateException {
        if (!_data.containsKey(name)) {
            return null;
        } else {
            HashMap<String, byte[]> pair = _data.remove(name);
            String className = "";
            byte[] data = new byte[0];
            for (Entry<String, byte[]> e : pair.entrySet()) {
                className = e.getKey();
                data = e.getValue();
                break;
            }
            try {
                T proxy = (T) BasicClassTypeUtil.createClassByUni(className);
                _is.warp(data);
                _is.setServerEncoding(encodeName);
                return (T) _is.read(proxy, 0, true);
            } catch (Exception ex) {
                throw new ObjectCreateException(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkObjectType(ArrayList<String> listType, Object o) {
        if (o.getClass().isArray()) {
            if (!o.getClass().getComponentType().toString().equals("byte")) {
                throw new IllegalArgumentException("only byte[] is supported");
            }
            if (Array.getLength(o) > 0) {
                listType.add("java.util.List");
                checkObjectType(listType, Array.get(o, 0));
            } else {
                listType.add("Array");
                listType.add("?");
            }
        } else if (o instanceof Array) {
            throw new java.lang.IllegalArgumentException("can not support Array, please use List");
        } else if (o instanceof List) {
            listType.add("java.util.List");
            List list = (List) o;
            if (list.size() > 0) {
                checkObjectType(listType, list.get(0));
            } else {
                listType.add("?");
            }
        } else if (o instanceof Map) {
            listType.add("java.util.Map");
            Map map = (Map) o;
            if (map.size() > 0) {
                Iterator it = map.keySet().iterator();
                Object key = it.next();
                Object value = map.get(key);
                listType.add(key.getClass().getName());
                checkObjectType(listType, value);
            } else {
                listType.add("?");
                listType.add("?");
            }
        } else {
            listType.add(o.getClass().getName());
        }
    }

    public byte[] encode() {
        TarsOutputStream _os = new TarsOutputStream(0);
        _os.setServerEncoding(encodeName);
        _os.write(_data, 0);
        return TarsUtil.getBufArray(_os.getByteBuffer());
    }

    public void decode(byte[] buffer) {
        _is.warp(buffer);
        _is.setServerEncoding(encodeName);
        HashMap<String, HashMap<String, byte[]>> _tempdata = new HashMap<String, HashMap<String, byte[]>>(1);
        HashMap<String, byte[]> h = new HashMap<String, byte[]>(1);
        h.put("", new byte[0]);
        _tempdata.put("", h);
        _data = (HashMap<String, HashMap<String, byte[]>>) _is.readMap(_tempdata, 0, false);
    }

}
