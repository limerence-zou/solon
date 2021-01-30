package org.noear.solon.cloud.model;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.utils.LocalUtils;
import org.noear.solon.core.Signal;
import org.noear.solon.core.SignalSim;
import org.noear.solon.core.SignalType;

import java.io.Serializable;
import java.util.*;

/**
 * 服务节点模型
 *
 * @author noear
 * @since 1.2
 */
public class Instance implements Serializable {
    /**
     * 服务名
     */
    private String service;

    public String service() {
        return service;
    }

    /**
     * 地址（ip:port）
     */
    private String address;

    public String address() {
        return address;
    }

    public Instance address(String address) {
        this.address = address;
        return this;
    }

    /**
     * 协议（http, ws, tcp...）
     */
    private String protocol;

    public String protocol() {
        return protocol;
    }

    public Instance protocol(String protocol) {
        if (Utils.isNotEmpty(protocol)) {
            this.protocol = protocol;
        }

        return this;
    }

    /**
     * 权重
     */
    private double weight = 1.0D;

    public double weight() {
        return weight;
    }

    public Instance weight(double weight) {
        this.weight = weight;
        return this;
    }

    /**
     * 元信息
     */
    private Map<String, String> meta = new HashMap<>();

    public Map<String, String> meta() {
        return meta;
    }

    public Instance metaPut(String name, String value) {
        if (value != null) {
            meta.put(name, value);
        }

        return this;
    }

    public String metaGet(String name) {
        return meta.get(name);
    }

    public Instance metaPutAll(Map<String, String> map) {
        if (map != null) {
            meta.putAll(map);

            protocol(map.get("protocol"));
        }

        return this;
    }

    public Instance metaRemove(String name) {
        meta.remove(name);

        return this;
    }

    /**
     * 标签
     */
    private List<String> tags;

    public List<String> tags() {
        return tags;
    }

    public Instance tagsAdd(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        tags.add(tag);
        return this;
    }

    public Instance tagsAddAll(Collection<String> list) {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        tags.addAll(list);
        return this;
    }


    public Instance(String service, String address) {
        this.service = service;
        this.address = address;
    }


    private static Instance local;

    public static Instance local() {
        if (local == null) {
            local = localNew(new SignalSim(Solon.global().port(), "http", SignalType.HTTP));
        }

        return local;
    }

    public static Instance localNew(Signal signal) {
        Instance n1 = new Instance(
                Solon.cfg().appName(),
                LocalUtils.getLocalAddress() + ":" + signal.port());

        n1.protocol(signal.protocol());

        n1.metaPutAll(Solon.cfg().argx());
        n1.metaRemove("server.port");
        n1.metaPut("protocol", signal.protocol());

        n1.tagsAdd("solon");
        if (Utils.isNotEmpty(Solon.cfg().appGroup())) {
            n1.tagsAdd(Solon.cfg().appGroup());
        }

        if (Utils.isNotEmpty(Solon.cfg().appName())) {
            n1.tagsAdd(Solon.cfg().appName());
        }

        return n1;
    }
}