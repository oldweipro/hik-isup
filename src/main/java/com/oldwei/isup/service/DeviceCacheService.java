package com.oldwei.isup.service;

import com.oldwei.isup.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final Map<String, Device> deviceMap = new ConcurrentHashMap<>();

    /**
     * 登录句柄 -> 设备ID的映射
     */
    private final Map<Integer, String> loginIdToDeviceIdMap = new ConcurrentHashMap<>();

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

        // 保存到设备映射
        deviceMap.put(device.getDeviceId(), device);

        // 更新登录句柄映射
        if (device.getLoginId() != null && device.getLoginId() > -1) {
            loginIdToDeviceIdMap.put(device.getLoginId(), device.getDeviceId());
        }

        log.debug("设备信息已缓存: {}, 通道数: {}", device.getDeviceId(), device.getChannels().size());
    }

    /**
     * 注册设备的登录句柄（设备上线时调用）
     * 只记录 loginId 到 deviceId 的映射，不创建设备记录
     * 具体的设备信息由同步任务来创建和更新
     *
     * @param loginId  登录句柄
     * @param deviceId 设备ID
     */
    public void registerLoginId(Integer loginId, String deviceId) {
        if (loginId != null && loginId > -1 && deviceId != null) {
            loginIdToDeviceIdMap.put(loginId, deviceId);
            log.info("设备登录句柄已注册: deviceId={}, loginId={}", deviceId, loginId);
        }
    }

    /**
     * 根据设备ID获取设备
     *
     * @param deviceId 设备ID
     * @return Optional包装的设备对象
     */
    public Optional<Device> getByDeviceId(String deviceId) {
        return Optional.ofNullable(deviceMap.get(deviceId));
    }

    /**
     * 根据登录句柄获取设备ID
     *
     * @param loginId 登录句柄
     * @return 设备ID
     */
    public String getDeviceIdByLoginId(Integer loginId) {
        return loginIdToDeviceIdMap.get(loginId);
    }

    /**
     * 根据登录句柄获取设备
     *
     * @param loginId 登录句柄
     * @return 设备对象
     */
    public Optional<Device> getByLoginId(Integer loginId) {
        String deviceId = loginIdToDeviceIdMap.get(loginId);
        if (deviceId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(deviceMap.get(deviceId));
    }

    /**
     * 获取所有设备列表
     *
     * @return 所有设备列表
     */
    public List<Device> listAll() {
        return new ArrayList<>(deviceMap.values());
    }

    /**
     * 根据条件查询设备列表
     *
     * @param predicate 过滤条件
     * @return 符合条件的设备列表
     */
    public List<Device> list(Predicate<Device> predicate) {
        return deviceMap.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 根据设备ID删除设备
     *
     * @param deviceId 设备ID
     */
    public void removeByDeviceId(String deviceId) {
        Device device = deviceMap.remove(deviceId);
        if (device != null) {
            // 移除loginId映射
            loginIdToDeviceIdMap.entrySet().removeIf(entry -> entry.getValue().equals(deviceId));
            log.debug("设备已从缓存移除: {}", deviceId);
        }
    }

    /**
     * 根据登录句柄删除设备
     *
     * @param loginId 登录句柄
     */
    public void removeByLoginId(Integer loginId) {
        String deviceId = loginIdToDeviceIdMap.remove(loginId);
        if (deviceId != null) {
            deviceMap.remove(deviceId);
            log.debug("已移除登录句柄{}对应的设备: {}", loginId, deviceId);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        deviceMap.clear();
        loginIdToDeviceIdMap.clear();
        log.info("设备缓存已清空");
    }

    /**
     * 获取缓存的设备数量
     *
     * @return 设备数量
     */
    public int size() {
        return deviceMap.size();
    }
}
