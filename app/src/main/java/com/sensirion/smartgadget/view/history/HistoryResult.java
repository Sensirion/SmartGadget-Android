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
package com.sensirion.smartgadget.view.history;

import android.support.annotation.NonNull;

import com.sensirion.smartgadget.peripheral.rht_utils.RHTDataPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HistoryResult {

    @NonNull
    private final Map<String, List<RHTDataPoint>> mResultValues =
            Collections.synchronizedMap(
                    new HashMap<String, List<RHTDataPoint>>()
            );

    public HistoryResult(@NonNull final List<String> devices) {
        for (final String device : devices) {
            mResultValues.put(device, Collections.synchronizedList(new LinkedList<RHTDataPoint>()));
        }
    }

    /**
     * Obtains the history results.
     *
     * @return Iterable of {@link java.util.List <@link RHTDataPoint>}>} with the results
     */
    @NonNull
    public Map<String, List<RHTDataPoint>> getResults() {
        return mResultValues;
    }

    /**
     * Adds a datapoint to the result list.
     *
     * @param dataPoint that is going to be added.
     */
    public void addResult(@NonNull final String deviceAddress,
                          @NonNull final RHTDataPoint dataPoint) {
        mResultValues.get(deviceAddress).add(dataPoint);
    }

    /**
     * Obtains the number of values retrieved by the database.
     *
     * @return <code>int</code> with the number of values.
     */
    public int size() {
        int size = 0;
        synchronized (mResultValues) {
            for (List<RHTDataPoint> results : mResultValues.values()) {
                size += results.size();
            }
        }
        return size;
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        synchronized (mResultValues) {
            for (final String deviceAddress : mResultValues.keySet()) {
                for (final RHTDataPoint datapoint : mResultValues.get(deviceAddress)) {
                    sb.append(
                            String.format(
                                    "\nDevice with address: %s - %s",
                                    deviceAddress,
                                    datapoint.toString())
                    );
                }
            }
        }
        return sb.toString();
    }
}
