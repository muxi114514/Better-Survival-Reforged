package com.mx.bettersurvival.capability;

/**
 * Default implementation of INunchakuCombo.
 * Matches original 1.12 NunchakuCombo exactly:
 * - comboPower capped at 1.0, each hit sets comboTime to 30
 * - countDown decrements comboTime; when 0 both timers reset
 */
public class NunchakuComboImpl implements INunchakuCombo {

    private float comboPower;
    private int comboTime;
    private boolean isSpinning;

    @Override
    public float getComboPower() {
        return comboPower;
    }

    @Override
    public void setComboPower(float power) {
        this.comboPower = Math.min(power, 1.0F);
        this.comboTime = 30; // reset countdown on every hit (matches original)
    }

    @Override
    public int getComboTime() {
        return comboTime;
    }

    @Override
    public void setComboTime(int time) {
        this.comboTime = time;
    }

    @Override
    public void countDown() {
        this.comboTime--;
        if (this.comboTime <= 0) {
            this.comboTime = 0;
            this.comboPower = 0;
        }
    }

    @Override
    public boolean isSpinning() {
        return isSpinning;
    }

    @Override
    public void setSpinning(boolean spinning) {
        this.isSpinning = spinning;
    }
}
