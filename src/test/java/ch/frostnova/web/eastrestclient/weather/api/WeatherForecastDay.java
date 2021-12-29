package ch.frostnova.web.eastrestclient.weather.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;

@JsonPropertyOrder({"date", "condition", "temperature"})
public class WeatherForecastDay {

    @JsonProperty("date")
    private LocalDate localDate;

    @JsonProperty("condition")
    private Condition condition;

    @JsonProperty("temperature")
    private Temperature temperature;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }
}
