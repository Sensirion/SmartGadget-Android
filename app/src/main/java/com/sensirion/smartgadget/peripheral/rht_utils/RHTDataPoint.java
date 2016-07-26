package com.sensirion.smartgadget.peripheral.rht_utils;

import android.support.annotation.NonNull;

/**
 * Convenience class for storing the obtained humidity and temperature.
 */
public class RHTDataPoint implements Comparable<RHTDataPoint> {

    private static final String TAG = RHTDataPoint.class.getSimpleName();

    private final float mTemperatureInCelsius;
    private final float mRelativeHumidity;
    private final long mTimestamp;

    /**
     * Constructor for the datapoint in case we are using CELSIUS as temperature unit..
     *
     * @param temperatureInCelsius in Celsius.
     * @param relativeHumidity     of the datapoint.
     * @param timestamp            where this datapoint was obtained in milliseconds UTC.
     */
    public RHTDataPoint(final float temperatureInCelsius, final float relativeHumidity, final long timestamp) {
        this(temperatureInCelsius, relativeHumidity, timestamp, TemperatureUnit.CELSIUS);
    }

    /**
     * Constructor for the datapoint.
     *
     * @param temperature      in the selected unit in the @param temperatureUnit.
     * @param relativeHumidity of the datapoint.
     * @param timestamp        where this datapoint was obtained in milliseconds UTC.
     * @param temperatureUnit  can be TemperatureUnit.CELSIUS or TemperatureUnit.FAHRENHEIT or TemperatureUnit.KELVIN.
     */
    public RHTDataPoint(final float temperature, final float relativeHumidity, final long timestamp, final TemperatureUnit temperatureUnit) {
        mTemperatureInCelsius = TemperatureConverter.convertTemperatureToCelsius(temperature, temperatureUnit);
        mRelativeHumidity = relativeHumidity;
        mTimestamp = timestamp;
    }


    /**
     * Obtains the relative humidity of the datapoint.
     *
     * @return {@link Float} with the relative humidity.
     */
    public float getRelativeHumidity() {
        return mRelativeHumidity;
    }

    /**
     * Obtains the temperature of the datapoint in Celsius.
     *
     * @return {@link Float} with the datapoint in Celsius.
     */
    public float getTemperatureCelsius() {
        return mTemperatureInCelsius;
    }

    /**
     * Obtains the temperature of the datapoint in Fahrenheit.
     *
     * @return {@link Float} with the datapoint in Fahrenheit.
     */
    @SuppressWarnings("unused")
    public float getTemperatureFahrenheit() {
        return TemperatureConverter.convertCelsiusToFahrenheit(mTemperatureInCelsius);
    }

    /**
     * Obtains the temperature of the datapoint in Kelvin
     *
     * @return {@link Float} with the datapoint in Kelvin.
     */
    @SuppressWarnings("unused")
    public float getTemperatureKelvin() {
        return TemperatureConverter.convertCelsiusToKelvin(mTemperatureInCelsius);
    }

    /**
     * This method returns the dew point of the datapoint in Celsius.
     *
     * @return {@link Float} with the datapoint in Celsius.
     */
    @SuppressWarnings("unused")
    public float getDewPointCelsius() {
        float h = (float) (Math.log((mRelativeHumidity / 100.0)) + (17.62 * mTemperatureInCelsius) / (243.12 + mTemperatureInCelsius));
        return (float) (243.12 * h / (17.62 - h));
    }

    /**
     * This method returns the dew point of the datapoint in Fahrenheit.
     *
     * @return {@link Float} with the datapoint in Fahrenheit.
     */
    @SuppressWarnings("unused")
    public float getDewPointFahrenheit() {
        return TemperatureConverter.convertCelsiusToFahrenheit(getDewPointCelsius());
    }

    /**
     * This method returns the dew point of the datapoint in Kelvin.
     *
     * @return {@link Float} with the datapoint in Kelvin.
     */
    @SuppressWarnings("unused")
    public float getDewPointKelvin() {
        return TemperatureConverter.convertCelsiusToKelvin(getDewPointCelsius());
    }

    /**
     * This method obtains the heat index of a temperature and humidity
     * using the formula from: http://en.wikipedia.org/wiki/Heat_index that
     * comes from Stull, Richard (2000). Meteorology for Scientists and
     * Engineers, Second Edition. Brooks/Cole. p. 60. ISBN 9780534372149.
     *
     * @return {@link Float} with the Heat Index in Celsius.
     */
    @SuppressWarnings("unused")
    public float getHeatIndexCelsius() {
        return TemperatureConverter.convertFahrenheitToCelsius(getHeatIndexFahrenheit());
    }

    /**
     * This method obtains the heat index of a temperature and humidity
     * using the formula from: http://en.wikipedia.org/wiki/Heat_index that
     * comes from Stull, Richard (2000). Meteorology for Scientists and
     * Engineers, Second Edition. Brooks/Cole. p. 60. ISBN 9780534372149.
     *
     * @return {@link Float} with the Heat Index in Fahrenheit.
     */
    public float getHeatIndexFahrenheit() {
        final float temperatureInFahrenheit = TemperatureConverter.convertCelsiusToFahrenheit(mTemperatureInCelsius);
        return TemperatureConverter.calcHeatIndexInFahrenheit(temperatureInFahrenheit, mRelativeHumidity);
    }

    /**
     * This method obtains the heat index of a temperature and humidity
     * using the formula from: http://en.wikipedia.org/wiki/Heat_index that
     * comes from Stull, Richard (2000). Meteorology for Scientists and
     * Engineers, Second Edition. Brooks/Cole. p. 60. ISBN 9780534372149.
     *
     * @return {@link Float} with the Heat Index in Kelvin.
     */
    @SuppressWarnings("unused")
    public float getHeatIndexKelvin() {
        return TemperatureConverter.convertKelvinToCelsius(getHeatIndexCelsius());
    }

    /**
     * Returns the moment when it was obtained the data point in milliseconds.
     *
     * @return the timestamp in milliseconds.
     */
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Temperature in Celsius: ").append(getTemperatureCelsius());
        sb.append(" Relative Humidity: ").append(getRelativeHumidity());
        sb.append(" TimestampMs: ").append(getTimestamp());
        sb.append(" Seconds from now: ").append((int) ((System.currentTimeMillis() - getTimestamp()) / 1000l)).append(" second(s).");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@NonNull final RHTDataPoint anotherDatapoint) {
        if (anotherDatapoint.getTimestamp() - mTimestamp > 0) {
            return -1;
        }
        return 1;
    }

}