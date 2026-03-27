package com.mx.bettersurvival.capability;

public interface INunchakuCombo {
    float getComboPower();

    void setComboPower(float power);

    int getComboTime();

    void setComboTime(int time);

    void countDown();

    boolean isSpinning();

    void setSpinning(boolean spinning);
}
