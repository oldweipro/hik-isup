package com.oldwei.isup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oldwei.isup.mapper.DeviceMapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("deviceService")
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
}
