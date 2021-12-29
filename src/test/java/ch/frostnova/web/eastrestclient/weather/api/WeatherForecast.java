package ch.frostnova.web.eastrestclient.weather.api;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.LinkedList;
import java.util.List;

@JacksonXmlRootElement(localName = "weather-forecast")
@JsonPropertyOrder({"location", "day"})
public class WeatherForecast {

    @JacksonXmlProperty(localName = "location")
    private String location;

    @JacksonXmlElementWrapper(localName = "day", useWrapping = false)
    private List<WeatherForecastDay> days = new LinkedList<>();

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<WeatherForecastDay> getDays() {
        return days;
    }

    public void setDays(List<WeatherForecastDay> days) {
        this.days = days;
    }

    public void add(WeatherForecastDay day) {
        days.add(day);
    }
}
