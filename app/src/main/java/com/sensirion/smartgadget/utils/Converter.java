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

public class Converter {

    public static float convertToC(final float tempInFahrenheit) {
        return (tempInFahrenheit - 32f) * 5f / 9f;
    }

    public static float convertToF(final float tempInC) {
        return (tempInC * 9f / 5f + 32f);
    }

    public static float calcDewPoint(final float relativeHumidity, final float ambientTemperature) {
        float h = (float) (Math.log((relativeHumidity / 100.0)) + (17.62 * ambientTemperature) / (243.12 + ambientTemperature));
        return (float) (243.12 * h / (17.62 - h));
    }

    public static float calculateHeatIndexCelsius(final float relativeHumidity, final float ambientTemperatureCelsius) {
        return HeatIndexCalculator.calcHeatIndexInCelsius(relativeHumidity, ambientTemperatureCelsius);
    }

    public static float calculateHeatIndexFahrenheit(final float relativeHumidity, final float ambientTemperatureFahrenheit) {
        return HeatIndexCalculator.calcHeatIndexInFahrenheit(relativeHumidity, ambientTemperatureFahrenheit);
    }

    private static class HeatIndexCalculator {

        /**
         * Heat formula coefficients.
         */
        private final static float c1 = 16.923f;
        private final static float c2 = 0.185212f;
        private final static float c3 = 5.37941f;
        private final static float c4 = -0.100254f;
        private final static float c5 = 9.41695E-3f;
        private final static float c6 = 7.28898E-3f;
        private final static float c7 = 3.45372E-4f;
        private final static float c8 = -8.14971E-4f;
        private final static float c9 = 1.02102E-5f;
        private final static float c10 = -3.8646E-5f;
        private final static float c11 = 2.91583E-5f;
        private final static float c12 = 1.42721E-6f;
        private final static float c13 = 1.97483E-7f;
        private final static float c14 = -2.18429E-8f;
        private final static float c15 = 8.43296E-10f;
        private final static float c16 = -4.81975E-11f;

        /**
         * Heat formula boundaries.
         */
        private final static float LOW_BOUNDARY_FORMULA_FAHRENHEIT = 70f;
        private final static float UPPER_BOUNDARY_FORMULA_FAHRENHEIT = 115f;

        /**
         * This method obtains the heat index of a temperature and humidity
         * using the formula from: http://en.wikipedia.org/wiki/Heat_index that
         * comes from Stull, Richard (2000). Meteorology for Scientists and
         * Engineers, Second Edition. Brooks/Cole. p. 60. ISBN 9780534372149.
         *
         * @param relativeHumidity relative humidity
         * @param tempInCelsius    ambient temperature in Celsius.
         * @return Heat Index.
         */
        private static float calcHeatIndexInCelsius(final float relativeHumidity, final float tempInCelsius) {
            final float tempInFahrenheit = Converter.convertToF(tempInCelsius);
            final float heatIndexInFahrenheit = calcHeatIndexInFahrenheit(relativeHumidity, tempInFahrenheit);
            return Converter.convertToC(heatIndexInFahrenheit);
        }

        /**
         * This method obtains the heat index of a temperature and humidity
         * using the formula from: http://en.wikipedia.org/wiki/Heat_index that
         * comes from Stull, Richard (2000). Meteorology for Scientists and
         * Engineers, Second Edition. Brooks/Cole. p. 60. ISBN 9780534372149.
         *
         * @param h relative humidity
         * @param t ambient temperature in Fahrenheit.
         * @return Heat Index.
         */
        private static float calcHeatIndexInFahrenheit(final float h, final float t) {

            //Checks if the temperature and the humidity makes sense.
            if (t > UPPER_BOUNDARY_FORMULA_FAHRENHEIT || h < 0 || h > 100) {
                return Float.NaN;
            } else if (t < LOW_BOUNDARY_FORMULA_FAHRENHEIT) {
                // use actual temperature for heat index if below LOW_BOUNDARY_FORMULA_FAHRENHEIT
                return t;
            }

            //Prepares values for improving the readability of the method.
            final float t2 = t * t;
            final float t3 = t2 * t;
            final float h2 = h * h;
            final float h3 = h2 * h;

            return c1
                    + c2 * t
                    + c3 * h
                    + c4 * t * h
                    + c5 * t2
                    + c6 * h2
                    + c7 * t2 * h
                    + c8 * t * h2
                    + c9 * t2 * h2
                    + c10 * t3
                    + c11 * h3
                    + c12 * t3 * h
                    + c13 * t * h3
                    + c14 * t3 * h2
                    + c15 * t2 * h3
                    + c16 * t3 * h3;
        }
    }
}
