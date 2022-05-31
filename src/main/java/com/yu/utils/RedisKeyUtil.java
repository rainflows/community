package com.yu.utils;


/**
 * Redis-key工具
 *
 * @author yu
 * @date 2022/05/19
 */
public class RedisKeyUtil {

    /**
     * split
     */
    private static final String SPLIT = ":";

    /**
     * 前缀_点赞实体
     */
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    /**
     * 前缀_用户点赞
     */
    private static final String PREFIX_USER_LIKE = "like:user";

    /**
     * 前缀_受关注者
     */
    private static final String PREFIX_FOLLOWEE = "followee";
    /**
     * 前缀_粉丝
     */
    private static final String PREFIX_FOLLOWER = "follower";

    /**
     * 前缀_kaptcha
     */
    private static final String PREFIX_KAPTCHA = "kaptcha";

    /**
     * 前缀_凭证
     */
    private static final String PREFIX_TICKET = "ticket";

    /**
     * 前缀_用户
     */
    private static final String PREFIX_USER = "user";

    /**
     * 前缀_UV
     */
    private static final String PREFIX_UV = "uv";

    /**
     * 前缀_DAU
     */
    private static final String PREFIX_DAU = "dau";

    /**
     * 前缀_帖子
     */
    private static final String PREFIX_POST = "post";


    /**
     * 得到某个实体的赞
     * 形式 like:entity:entityType:entityId -> set(userId)
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return {@link String}
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + entityType + SPLIT + entityId;
    }

    /**
     * 得到某个用户的赞
     * 形式 like:user:userId -> int
     *
     * @param userId 用户id
     * @return {@link String}
     */
    public static String getUserLikeKey(int userId) {

        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 得到某个用户关注的实体
     * 形式:followee:userId:entityType -> zset(entityId,now)
     *
     * @return {@link String}
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 得到某个实体拥有的粉丝
     * 形式:follower:entityType:entityId -> zset(entityId,now)
     *
     * @return {@link String}
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + entityId;
    }

    /**
     * 得到登录验证码
     *
     * @param owner
     * @return {@link String}
     */
    public static String getKaptchaKey(String owner) {

        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 得到登录凭证
     *
     * @param ticket 凭证
     * @return {@link String}
     */
    public static String getTicketKey(String ticket) {

        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 获取用户的键
     *
     * @param userId 用户id
     * @return {@link String}
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 得到uvkey
     *
     * @param date 日期
     * @return {@link String}
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 得到uvkey
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return {@link String}
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 得到daukey
     *
     * @return {@link String}
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 得到daukey
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return {@link String}
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 得到帖子分数key
     *
     * @return {@link String}
     */
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
