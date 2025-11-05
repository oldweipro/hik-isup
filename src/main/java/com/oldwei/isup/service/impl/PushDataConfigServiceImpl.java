package com.oldwei.isup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oldwei.isup.mapper.PushDataConfigMapper;
import com.oldwei.isup.model.PushDataConfig;
import com.oldwei.isup.service.IPushDataConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("pushDataConfigService")
@RequiredArgsConstructor
public class PushDataConfigServiceImpl extends ServiceImpl<PushDataConfigMapper, PushDataConfig> implements IPushDataConfigService {
}
