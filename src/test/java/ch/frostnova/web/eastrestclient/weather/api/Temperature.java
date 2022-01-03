package ch.frostnova.web.eastrestclient.weather.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Temperature {

    @JsonProperty("value")
    @JacksonXmlProperty(isAttribute = true)
    private final BigDecimal value;
    @JsonProperty("unit")
    @JacksonXmlProperty(isAttribute = true)
    private final Unit unit;

    @JsonCreator
    public Temperature(@JsonProperty("value") Number value,
                       @JsonProperty("unit") Unit unit) {
        this.value = new BigDecimal(requireNonNull(value).toString());
        this.unit = requireNonNull(unit);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return String.format("%.1f %s", value, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Temperature that = (Temperature) o;
        return Objects.equals(value, that.value) && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    public enum Unit {
        CELSIUS("°C"),
        FARENHEIT("°F"),
        KELVIN("K");

        private final String display;

        Unit(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
