package com.mx.bettersurvival.capability;

/**
 * 格挡精力（持盾攻击 / 硬破防机制的资源载体）。
 *
 * <p>纯数据：精力值、破防冷却（&gt;0 时禁止重新举盾）、回复暂停计时。
 * 服务端权威，经 {@code GuardStaminaSyncPacket} 同步到本人客户端供 HUD 与攻击闸门读取。
 */
public interface IGuardStamina {

    float getStamina();

    void setStamina(float stamina);

    /** 破防冷却（tick）：&gt;0 期间不能重新举盾。 */
    int getGuardCooldown();

    void setGuardCooldown(int ticks);

    /** 回复暂停（tick）：消耗精力后延迟一段再开始回复。 */
    int getRegenPause();

    void setRegenPause(int ticks);
}
