package com.mx.bettersurvival.capability;

/**
 * Nunchaku combo state capability.
 * Matches original 1.12 BetterSurvival mechanics:
 * - comboPower: 0.0 → 1.0 float, accumulates on hit, decays via countdown
 * - comboTime: 30-tick countdown, resets on hit, combo resets when reaches 0
 * - spinning: whether the nunchaku is currently being spun (held left-click)
 */
public interface INunchakuCombo {
    float getComboPower();

    void setComboPower(float power);

    int getComboTime();

    void setComboTime(int time);

    void countDown();

    boolean isSpinning();

    void setSpinning(boolean spinning);
}
