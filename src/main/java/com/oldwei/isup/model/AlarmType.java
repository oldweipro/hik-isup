package com.oldwei.isup.model;

import lombok.Getter;

/**
 * 报警类型枚举（对应 EN_ALARM_TYPE）
 * 来源：HCISUPSDK（通用）开发指南
 */
@Getter
public enum AlarmType {

    // 存储与磁盘相关报警
    ALARM_TYPE_DISK_FULL(0, "硬盘已满报警"),
    ALARM_TYPE_DISK_WRERROR(1, "硬盘读写出错报警"),

    // 视频信号相关报警
    ALARM_TYPE_VIDEO_LOST(5, "视频（信号）丢失报警"),
    ALARM_TYPE_EXTERNAL(6, "外部（信号量）报警"),
    ALARM_TYPE_VIDEO_COVERED(7, "视频遮盖报警"),
    ALARM_TYPE_MOTION(8, "移动侦测报警"),
    ALARM_TYPE_STANDARD_NOTMATCH(9, "视频制式不匹配报警"),

    // 设备行为相关报警
    ALARM_TYPE_SPEEDLIMIT_EXCEED(10, "超速报警"),
    ALARM_TYPE_PIR(11, "PIR 报警"),
    ALARM_TYPE_WIRELESS(12, "无线报警"),
    ALARM_TYPE_CALL_HELP(13, "呼救报警"),
    ALARM_TYPE_DISARM(14, "布撤防报警"),
    ALARM_TYPE_STREAM_PRIVATE(15, "码流隐私状态改变报警"),
    ALARM_TYPE_PIC_UPLOAD_FAIL(16, "设备上传图片失败报警"),
    ALARM_TYPE_LOCAL_REC_EXCEPTION(17, "设备本地录像（取证）异常报警"),
    ALARM_TYPE_UPGRADE_FAIL(18, "设备版本升级失败报警"),
    ALARM_TYPE_ILLEGAL_ACCESS(19, "非法访问报警"),

    // 声音与交通相关报警
    ALARM_TYPE_SOUNDLIMIT_EXCEED(80, "声音分贝数超标报警"),
    ALARM_TYPE_TRIFFIC_VIOLATION(90, "违章报警"),
    ALARM_TYPE_ALARM_CONTROL(96, "布防报警"),

    // 智能分析报警
    ALARM_TYPE_FACE_DETECTION(97, "人脸侦测报警"),
    ALARM_TYPE_DEFOUSE_DETECTION(98, "虚焦侦测报警"),
    ALARM_TYPE_AUDIO_EXCEPTION(99, "音频异常报警"),
    ALARM_TYPE_SCENE_CHANGE(100, "场景变更侦测报警"),
    ALARM_TYPE_TRAVERSE_PLANE(101, "越界侦测报警"),
    ALARM_TYPE_ENTER_AREA(102, "进入区域侦测报警"),
    ALARM_TYPE_LEAVE_AREA(103, "离开区域侦测报警"),
    ALARM_TYPE_INTRUSION(104, "区域入侵侦测报警"),
    ALARM_TYPE_LOITER(105, "徘徊侦测报警"),
    ALARM_TYPE_LEFT_TAKE(106, "遗留物品拿取侦测报警"),
    ALARM_TYPE_CAR_STOP(107, "停车侦测报警"),
    ALARM_TYPE_MOVE_FAST(108, "快速移动侦测报警"),
    ALARM_TYPE_HIGH_DENSITY(109, "人员聚集侦测报警"),
    ALARM_TYPE_PDC_BY_TIME(110, "按时间段统计客流量报警"),
    ALARM_TYPE_PDC_BY_FRAME(111, "单帧统计客流量报警"),
    ALARM_TYPE_LEFT(112, "物品遗留侦测报警"),
    ALARM_TYPE_TAKE(113, "物品拿取侦测报警"),
    ALARM_TYPE_ROLLOVER(114, "侧翻报警"),
    ALARM_TYPE_COLLISION(115, "碰撞报警"),

    // 网络与流量报警
    ALARM_TYPE_FLOW_OVERRUN(256, "流量超限报警"),
    ALARM_TYPE_WARN_FLOW_OVERRUN(257, "人员超限提醒"),

    // 设备/通道/硬盘状态报警
    ALARM_TYPE_DEV_CHANGED_STATUS(700, "设备状态改变报警"),
    ALARM_TYPE_CHAN_CHANGED_STATUS(701, "通道状态改变报警"),
    ALARM_TYPE_HD_CHANGED_STATUS(702, "硬盘状态改变报警"),
    ALARM_TYPE_DEV_TIMING_STATUS(703, "定时上传设备状态报警"),
    ALARM_TYPE_CHAN_TIMING_STATUS(704, "定时上传通道状态报警"),
    ALARM_TYPE_HD_TIMING_STATUS(705, "定时上传硬盘状态报警"),
    ALARM_TYPE_RECORD_ABNORMAL(706, "录像异常报警"),

    // 环境监测报警
    ALARM_TYPE_ENV_LIMIT(8800, "动环环境量超限报警"),
    ALARM_TYPE_ENV_REAL_TIME(8801, "动环环境量实时数据上传报警"),
    ALARM_TYPE_ENV_EXCEPTION(8802, "动环环境量异常上传报警"),

    // 车辆驾驶行为相关报警
    ALARM_TYPE_HIGH_TEMP(40961, "温度过高报警"),
    ALARM_TYPE_ACC_EXCEPTION(40962, "加速异常报警"),
    ALARM_TYPE_RAPID_ACCELERATION(40963, "急加速报警"),
    ALARM_TYPE_RAPID_DECELERATION(40964, "急减速报警"),
    ALARM_TYPE_COLLISION_V40(40965, "碰撞报警"),
    ALARM_TYPE_ROLLOVER_V40(40966, "侧翻报警"),
    ALARM_TYPE_RAPID_TURN_LEFT(40967, "急左转弯报警"),
    ALARM_TYPE_RAPID_TURN_RIGHT(40968, "急右转弯报警"),
    ALARM_TYPE_ABNORMAL_DRIVING_BEHAVIOR(40969, "异常驾驶行为报警"),
    ALARM_TYPE_OVERLOAD(40970, "超载报警"),
    ALARM_TYPE_LEFT_CROSS_LINE(40971, "左压线报警"),
    ALARM_TYPE_RIGHT_CROSS_LINE(40972, "右压线报警"),
    ALARM_TYPE_OPEN_DOOR_WITH_SPEED(40973, "带速开门报警"),
    ALARM_TYPE_ADAS(40974, "主动安全（ADAS）报警"),
    ALARM_TYPE_RADAR(41009, "雷达报警");

    private final int code;
    private final String description;

    AlarmType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举实例
     */
    public static AlarmType fromCode(int code) {
        for (AlarmType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
