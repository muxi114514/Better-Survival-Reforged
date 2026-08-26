package com.mx.bettersurvival.capability;

public class GuardStaminaImpl implements IGuardStamina {

    // 初值取近满：服务端首个 tick 会按配置上限夹取并同步，客户端在收到同步前也不至于显示空条。
    private float stamina = 100.0F;
    private int guardCooldown;
    private int regenPause;

    @Override
    public float getStamina() {
        return stamina;
    }

    @Override
    public void setStamina(float stamina) {
        this.stamina = Math.max(0.0F, stamina);
    }

    @Override
    public int getGuardCooldown() {
        return guardCooldown;
    }

    @Override
    public void setGuardCooldown(int ticks) {
        this.guardCooldown = Math.max(0, ticks);
    }

    @Override
    public int getRegenPause() {
        return regenPause;
    }

    @Override
    public void setRegenPause(int ticks) {
        this.regenPause = Math.max(0, ticks);
    }
}
