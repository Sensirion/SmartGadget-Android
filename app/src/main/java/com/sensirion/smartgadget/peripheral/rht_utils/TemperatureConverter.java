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
package com.sensirion.smartgadget.peripheral.rht_utils;

import android.support.annotation.NonNull;


public abstract class TemperatureConverter {

    /**
     * Convert a given temperature to Celsius.
     *
     * @param temperature that is going to be converted to Celsius.
     * @param unit        of the temperature. {@link TemperatureUnit}
     * @return {@link Float} with the temperature in Celsius.
     */
    public static float convertTemperatureToCelsius(final float temperature, @NonNull final TemperatureUnit unit) {
        switch (unit) {
            case CELSIUS:
                return temperature;
            case FAHRENHEIT:
                return TemperatureConverter.convertFahrenheitToCelsius(temperature);
            case KELVIN:
                return TemperatureConverter.convertKelvinToCelsius(temperature);
            default:
                throw new IllegalArgumentException(String.format("convertTemperatureToCelsius -> Value Unit %s is not implemented yet", unit));
        }
    }

    /**
     * Convert a given temperature to Fahrenheit.
     *
     * @param temperature that is going to be converted to Fahrenheit.
     * @param unit        of the temperature. {@link TemperatureUnit}
     * @return {@link Float} with the temperature in Fahrenheit.
     */
    public static float convertTemperatureToFahrenheit(final float temperature, @NonNull final TemperatureUnit unit) {
        switch (unit) {
            case CELSIUS:
                return convertCelsiusToFahrenheit(temperature);
            case FAHRENHEIT:
                return temperature;
            case KELVIN:
                return TemperatureConverter.convertKelvinToFahrenheit(temperature);
            default:
                throw new IllegalArgumentException(String.format("convertTemperatureToFahrenheit -> Value Unit %s is not implemented yet", unit));
        }
    }

    /**
     * Convert a given temperature to Kelvin.
     *
     * @param temperature that is going to be converted to Kelvin.
     * @param unit        of the temperature. {@link TemperatureUnit}
     * @return {@link Float} with the temperature in Kelvin.
     */
    public static float convertTemperatureToKelvin(final float temperature, @NonNull final TemperatureUnit unit) {
        switch (unit) {
            case CELSIUS:
                return convertCelsiusToKelvin(temperature);
            case FAHRENHEIT:
                return convertFahrenheitToKelvin(temperature);
            case KELVIN:
                return temperature;
            default:
                throw new IllegalArgumentException(String.format("convertTemperatureToKelvin -> Value Unit %s is not implemented yet", unit));
        }
    }

    /**
     * Convert a given Celsius temperature into Fahrenheit.
     *
     * @param temperatureInCelsius that will be converted into Fahrenheit.
     * @return {@link Float} with the temperature in Fahrenheit.
     */
    public static float convertCelsiusToFahrenheit(final float temperatureInCelsius) {
        return (temperatureInCelsius * 9f / 5f + 32f);
    }

    /**
     * Convert a given Celsius temperature into Kelvin.
     *
     * @param temperatureInCelsius that will be converted into Kelvin.
     * @return {@link Float} with the temperature in Kelvin.
     */
    public static float convertCelsiusToKelvin(final float temperatureInCelsius) {
        return temperatureInCelsius + 273.15f;
    }

    /**
     * Convert a given Fahrenheit temperature into Celsius.
     *
     * @param tempInFahrenheit that will be converted into Celsius.
     * @return {@link Float} with the temperature in Celsius.
     */
    public static float convertFahrenheitToCelsius(final float tempInFahrenheit) {
        return (tempInFahrenheit - 32f) * 5f / 9f;
    }

    /**
     * Convert a given Fahrenheit temperature into Kelvin.
     *
     * @param tempInFahrenheit that will be converted into Kelvin.
     * @return {@link Float} with the temperature in Kelvin.
     */
    public static float convertFahrenheitToKelvin(final float tempInFahrenheit) {
        return convertCelsiusToKelvin(convertFahrenheitToCelsius(tempInFahrenheit));
    }

    /**
     * Convert a given Kelvin temperature into Celsius.
     *
     * @param temperatureInKelvin that will be converted into Celsius.
     * @return {@link Float} with the temperature in Celsius.
     */
    public static float convertKelvinToCelsius(final float temperatureInKelvin) {
        return temperatureInKelvin - 273.15f;
    }

    /**
     * Convert a given Kelvin temperature into Fahrenheit.
     *
     * @param temperatureInKelvin that will be converted into Fahrenheit.
     * @return {@link Float} with the temperature in Fahrenheit.
     */
    public static float convertKelvinToFahrenheit(final float temperatureInKelvin) {
        return convertCelsiusToFahrenheit(convertKelvinToCelsius(temperatureInKelvin));
    }

    /**
     * This method calculates the Heat Index of a temperature and humidity
     * using the formula from: http://en.wikipedia.org/wiki/Heat_index
     * from Stull, Richard (2000). Meteorology for Scientists and
     * Engineers, Second Edition. Brooks/Cole. p. 60. ISBN 9780534372149.
     *
     * @param temperatureInFahrenheit ambient temperature in Fahrenheit.
     * @param humidity relative humidity
     * @return {@link Float} with the Heat Index in Fahrenheit.
     */
    public static float calcHeatIndexInFahrenheit(final float temperatureInFahrenheit, final float humidity) {
        /**
         * Heat formula coefficients.
         */
        final float c1 = 16.923f;
        final float c2 = 0.185212f;
        final float c3 = 5.37941f;
        final float c4 = -0.100254f;
        final float c5 = 9.41695E-3f;
        final float c6 = 7.28898E-3f;
        final float c7 = 3.45372E-4f;
        final float c8 = -8.14971E-4f;
        final float c9 = 1.02102E-5f;
        final float c10 = -3.8646E-5f;
        final float c11 = 2.91583E-5f;
        final float c12 = 1.42721E-6f;
        final float c13 = 1.97483E-7f;
        final float c14 = -2.18429E-8f;
        final float c15 = 8.43296E-10f;
        final float c16 = -4.81975E-11f;

        /**
         * Heat formula boundaries.
         */
        final float LOW_BOUNDARY_FORMULA_FAHRENHEIT = 70f;
        final float UPPER_BOUNDARY_FORMULA_FAHRENHEIT = 115f;

        //Checks if the temperature and the humidity makes sense.
        if (temperatureInFahrenheit < LOW_BOUNDARY_FORMULA_FAHRENHEIT || temperatureInFahrenheit > UPPER_BOUNDARY_FORMULA_FAHRENHEIT || humidity < 0 || humidity > 100) {
            return Float.NaN;
        }

        //Prepares values for improving the readability of the method.
        final float t2 = temperatureInFahrenheit * temperatureInFahrenheit;
        final float t3 = t2 * temperatureInFahrenheit;
        final float h2 = humidity * humidity;
        final float h3 = h2 * humidity;

        return c1
                + c2 * temperatureInFahrenheit
                + c3 * humidity
                + c4 * temperatureInFahrenheit * humidity
                + c5 * t2
                + c6 * h2
                + c7 * t2 * humidity
                + c8 * temperatureInFahrenheit * h2
                + c9 * t2 * h2
                + c10 * t3
                + c11 * h3
                + c12 * t3 * humidity
                + c13 * temperatureInFahrenheit * h3
                + c14 * t3 * h2
                + c15 * t2 * h3
                + c16 * t3 * h3;
    }
}
