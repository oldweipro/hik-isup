package com.oldwei.isup.service;

import com.oldwei.isup.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 设备内存缓存服务
 * 替代数据库存储，设备信息存储在内存中，随着程序的启动而创建，程序停止而销毁
 */
@Slf4j
@Service
public class DeviceCacheService {

    /**
     * 设备ID -> Device对象的映射
     */
    private final Map<String, Device> deviceIdMap = new ConcurrentHashMap<>();

    /**
     * 登录句柄 -> Device对象列表的映射（一个loginId可能对应多个设备）
     */
    private final Map<Integer, List<Device>> loginIdMap = new ConcurrentHashMap<>();

    /**
     * 保存或更新设备信息
     *
     * @param device 设备对象
     */
    public void saveOrUpdate(Device device) {
        if (device == null || device.getDeviceId() == null) {
            log.warn("设备信息或设备ID为空，无法保存");
            return;
        }

        // 保存到设备ID映射
        deviceIdMap.put(device.getDeviceId(), device);

        // 更新登录句柄映射
        if (device.getLoginId() != null && device.getLoginId() > -1) {
            loginIdMap.computeIfAbsent(device.getLoginId(), k -> new ArrayList<>());
            List<Device> devices = loginIdMap.get(device.getLoginId());
            // 移除旧的同deviceId设备，添加新的
            devices.removeIf(d -> d.getDeviceId().equals(device.getDeviceId()));
            devices.add(device);
        }

        log.debug("设备信息已缓存: {}", device.getDeviceId());
    }

    /**
     * 根据设备ID获取设备
     *
     * @param deviceId 设备ID
     * @return Optional包装的设备对象
     */
    public Optional<Device> getByDeviceId(String deviceId) {
        return Optional.ofNullable(deviceIdMap.get(deviceId));
    }

    /**
     * 根据登录句柄获取设备列表
     *
     * @param loginId 登录句柄
     * @return 设备列表
     */
    public List<Device> getByLoginId(Integer loginId) {
        List<Device> devices = loginIdMap.get(loginId);
        return devices != null ? new ArrayList<>(devices) : Collections.emptyList();
    }

    /**
     * 获取所有设备列表
     *
     * @return 所有设备列表
     */
    public List<Device> listAll() {
        return new ArrayList<>(deviceIdMap.values());
    }

    /**
     * 根据条件查询设备列表
     *
     * @param predicate 过滤条件
     * @return 符合条件的设备列表
     */
    public List<Device> list(Predicate<Device> predicate) {
        return deviceIdMap.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 根据设备ID删除设备
     *
     * @param deviceId 设备ID
     */
    public void removeByDeviceId(String deviceId) {
        Device device = deviceIdMap.remove(deviceId);
        if (device != null && device.getLoginId() != null) {
            List<Device> devices = loginIdMap.get(device.getLoginId());
            if (devices != null) {
                devices.removeIf(d -> d.getDeviceId().equals(deviceId));
                if (devices.isEmpty()) {
                    loginIdMap.remove(device.getLoginId());
                }
            }
        }
        log.debug("设备已从缓存移除: {}", deviceId);
    }

    /**
     * 根据登录句柄删除设备
     *
     * @param loginId 登录句柄
     */
    public void removeByLoginId(Integer loginId) {
        List<Device> devices = loginIdMap.remove(loginId);
        if (devices != null) {
            devices.forEach(device -> deviceIdMap.remove(device.getDeviceId()));
            log.debug("已移除登录句柄{}对应的{}个设备", loginId, devices.size());
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        deviceIdMap.clear();
        loginIdMap.clear();
        log.info("设备缓存已清空");
    }

    /**
     * 获取缓存的设备数量
     *
     * @return 设备数量
     */
    public int size() {
        return deviceIdMap.size();
    }
}
