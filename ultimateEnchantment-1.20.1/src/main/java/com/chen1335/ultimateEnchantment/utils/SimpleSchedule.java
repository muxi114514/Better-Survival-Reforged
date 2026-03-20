package com.chen1335.ultimateEnchantment.utils;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimpleSchedule {

    private static final Map<Dist, List<Schedule>> DIST_SCHEDULES = ImmutableMap.of(
            Dist.CLIENT, new ArrayList<>(),
            Dist.DEDICATED_SERVER, new ArrayList<>()
    );

    public static void addSchedule(Level level, Schedule schedule) {
        if (level.isClientSide) {
            DIST_SCHEDULES.get(Dist.CLIENT).add(schedule);
        } else {
            DIST_SCHEDULES.get(Dist.DEDICATED_SERVER).add(schedule);
        }
    }

    public static void addSchedule(Dist dist, Schedule schedule) {
        DIST_SCHEDULES.get(dist).add(schedule);
    }

    public static void update(Dist dist) {
        Iterator<Schedule> iterator = DIST_SCHEDULES.get(dist).iterator();
        while (iterator.hasNext()) {
            Schedule schedule = iterator.next();
            schedule.tick();
            if (schedule.finished()) {
                schedule.run();
                iterator.remove();
            }
        }
    }

    public abstract static class Schedule {
        public int time = 0;

        public abstract boolean finished();

        public void tick() {
            time++;
        }

        public abstract void run();
    }

    public static class Wait extends Schedule {

        public Wait(Runnable runnable, int waitTick) {
            timeLeft = waitTick;
            this.runnable = runnable;
        }

        private int timeLeft = 0;
        private final Runnable runnable;

        @Override
        public boolean finished() {
            return timeLeft <= 0;
        }

        @Override
        public void tick() {
            timeLeft--;
            super.tick();
        }

        @Override
        public void run() {
            runnable.run();
        }
    }
}
