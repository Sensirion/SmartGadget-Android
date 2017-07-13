/*
 * Copyright (c) 2017, Sensirion AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sensirion AG nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
