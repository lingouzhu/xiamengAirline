package xiaMengAirline.evaluator.aviation2017;

/**
 * Created by peicheng on 17/6/29.
 */
public class Configuration {
    /**
     * 目标函数值 = 参数1*调机航班数 + 参数2*取消航班数 + 参数3*机型发生变化的航班数 +参数4*联程拉直航班对的个数 +参数5*航班总延误时间（小时） +参数6*航班总提前时间（小时）
     * + 参数7 * 非可行标识（0-1变量） + 参数8 * 违背约束数量 。
     */
    public static final int adjustFlightParam = 5000;
    public static final int cancelFlightParam = 1000;
    public static final int flightTypeChangeParam = 1000;
    public static final int connectFlightStraightenParam = 750;
    public static final int delayFlightParam = 100;
    public static final int aheadFlightParam = 150;
    //非可行解的惩罚值参数
    public static final double infeasibilityPenaltyParam = 5 * 10e6;
    //违背约束的单项惩罚值参数
    public static final double constraintViolationPenaltyParam = 10;

    //限制条件
    public static final long maxAheadTime = 6 * 60 * 60 * 1000;
    public static final long maxIntervalTime =  50 * 60 * 1000;
    public static final long maxDomesticDelayTime =  24 * 60 * 60 * 1000;
    public static final long maxAbroadDelayTime =  36 * 60 * 60 * 1000;

}
