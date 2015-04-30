package com.sensirion.smartgadget.utils;

public enum Interval {
    ONE_SECOND(1),
    THREE_SECONDS(3),
    FIVE_SECONDS(5),
    TEN_SECONDS(10),
    ONE_MINUTE(ONE_SECOND.getNumberSeconds() * 60),
    FIVE_MINUTES(ONE_MINUTE.getNumberSeconds() * 5),
    TEN_MINUTES(ONE_MINUTE.getNumberSeconds() * 10),
    ONE_HOUR(ONE_MINUTE.getNumberSeconds() * 60),
    THREE_HOURS(ONE_HOUR.getNumberSeconds() * 3),
    SIX_HOURS(ONE_HOUR.getNumberSeconds() * 6),
    ONE_DAY(ONE_HOUR.getNumberSeconds() * 24),
    ONE_WEEK(ONE_DAY.getNumberSeconds() * 7);

    private final int mNumberSeconds;

    Interval(final int numberSeconds) {
        mNumberSeconds = numberSeconds;
    }

    /**
     * Obtains the number of seconds of the interval.
     *
     * @return {@link java.lang.Integer} with the number of seconds.
     */
    public int getNumberSeconds() {
        return mNumberSeconds;
    }

    /**
     * Obtains the number of milliseconds of the interval.
     *
     * @return {@link java.lang.Integer} with the number of milliseconds.
     */
    public int getNumberMilliseconds() {
        return mNumberSeconds * 1000;
    }
}