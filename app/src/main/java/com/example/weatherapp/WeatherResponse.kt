package com.example.weatherapp


data class WeatherResponse(
    val main: MainInfo,
    val weather: List<WeatherInfo>
)

data class MainInfo(
    val temp: Double
)

data class WeatherInfo(
    val description: String
)
