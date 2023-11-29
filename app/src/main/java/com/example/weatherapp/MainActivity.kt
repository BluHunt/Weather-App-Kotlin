package com.example.weatherapp
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.weatherapp.databinding.CardWeatherLocationBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    // API key for weather service
    private val apiKey = "5b33bad694b5d961d31d5a70a4f767f6"

    // FusedLocationProviderClient for location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables to store current latitude and longitude
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // View binding instance for the activity's layout
    private lateinit var binding: CardWeatherLocationBinding // Your activity's binding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Inflating the binding for the activity and initializing variables
        binding = CardWeatherLocationBinding.inflate(layoutInflater)
        val parentLayout: LinearLayout = findViewById(R.id.parentLayout)
        val cardBinding: CardWeatherLocationBinding = CardWeatherLocationBinding.inflate(layoutInflater)

        // Creating a list to store references to added cards
        val cardList = mutableListOf<CardView>()

        // Obtaining an inflater
        val inflater = LayoutInflater.from(this)

        // List of locations for weather data
        val locations = listOf("My Current Location","New York", "Singapore", "Mumbai", "Delhi", "Sydney","Melbourne")

        // SwipeRefreshLayout initialization
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // FusedLocationProviderClient initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Checking for location permission
        checkLocationPermission()

        // Loop through each location in the list
        for (location in locations) {
            // Inflate the card layout for weather information
            val cardView = inflater.inflate(R.layout.card_weather_location, parentLayout, false) as CardView
            val locationTextView = cardView.findViewById<TextView>(R.id.locationNameTextView)
            val temperatureTextView = cardView.findViewById<TextView>(R.id.temperatureTextView)
            val descriptionTextView = cardView.findViewById<TextView>(R.id.descriptionTextView)
            val timeTextView = cardView.findViewById<TextView>(R.id.timeTextView)
            // Load saved data for the current location from SharedPreferences
            val savedData = loadDataForLocation(location)
            val temperature = savedData.first
            val description = savedData.second
            val timestamp = savedData.third
            // Check if saved temperature and description are available
            if (temperature != null && description != null) {
                // Populate the TextViews with saved data
                locationTextView.text = location
                temperatureTextView.text = temperature
                descriptionTextView.text = description
                timeTextView.text = timestamp
                // Log the saved data
                Log.d("Saved Data", "Location: $location, Temp: $temperature, Desc: $description, Time: $timestamp")
            }

            // Check for network availability
            if (isNetworkAvailable()) {
                // Fetch weather data if network is available
                if (location == "My Current Location") {
                    fetchWeatherDataByCoordinates(currentLatitude, currentLongitude, apiKey) { getTemperature, getDescription ->
                        // Update TextViews with fetched data
                        locationTextView.text = location
                        temperatureTextView.text = getTemperature
                        descriptionTextView.text = getDescription
                        timeTextView.text = convertMillisToDateTime()
                        // Save fetched data in SharedPreferences with a timestamp
                        saveDataForLocationWithTimestamp("My Current Location", getTemperature, getDescription, convertMillisToDateTime())
                    }
                } else {
                    fetchWeatherDataByLocation(location, apiKey) { getTemperature, getDescription ->
                        // Update TextViews with fetched data
                        locationTextView.text = location
                        temperatureTextView.text = getTemperature
                        descriptionTextView.text = getDescription
                        timeTextView.text = convertMillisToDateTime()
                        // Save fetched data in SharedPreferences with a timestamp
                        saveDataForLocationWithTimestamp(location, getTemperature, getDescription, convertMillisToDateTime())
                    }
                }
            }

            // Add the card view to the parent layout and store a reference to the card
            parentLayout.addView(cardView)
            cardList.add(cardView)
        }

        swipeRefreshLayout.setOnRefreshListener {
            val transition = android.transition.Fade()
            transition.duration = 300 // Set the duration as needed
            TransitionManager.beginDelayedTransition(parentLayout, transition)
            if (isNetworkAvailable()){
                // Loop through each child in the parent layout
                for (i in 0 until parentLayout.childCount) {
                    val cardView = parentLayout.getChildAt(i) as CardView
                    val locationTextView = cardView.findViewById<TextView>(R.id.locationNameTextView)
                    val temperatureTextView = cardView.findViewById<TextView>(R.id.temperatureTextView)
                    val descriptionTextView = cardView.findViewById<TextView>(R.id.descriptionTextView)
                    val timeTextView = cardView.findViewById<TextView>(R.id.timeTextView)
                    val location = locationTextView.text.toString()

                    // Check if it's the current location or other locations
                    if (location == "My Current Location") {
                        // Fetch weather data for the current location by coordinates
                        fetchWeatherDataByCoordinates(currentLatitude, currentLongitude, apiKey) { getttemperature, getdescription ->
                            locationTextView.text = location
                            temperatureTextView.text = getttemperature
                            descriptionTextView.text = getdescription
                            timeTextView.text = convertMillisToDateTime()
                            saveDataForLocationWithTimestamp("My Current Location", getttemperature, getdescription, convertMillisToDateTime())
                        }
                    } else {
                        // Fetch weather data for other locations
                        fetchWeatherDataByLocation(location, apiKey) { gettemperature, getdescription ->
                            locationTextView.text = location
                            temperatureTextView.text = gettemperature
                            descriptionTextView.text = getdescription
                            timeTextView.text = convertMillisToDateTime()
                            saveDataForLocationWithTimestamp(location, gettemperature, getdescription, convertMillisToDateTime())
                        }
                    }
                }
                // Update the UI by stopping the refresh animation
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    // Retrofit API service interface
    interface WeatherApiService {
        @GET("weather")
        fun getCurrentWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String
        ): Call<WeatherResponse>

        @GET("weather")
        fun getCurrentWeatherByCoordinates(
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
            @Query("appid") apiKey: String
        ): Call<WeatherResponse>
    }

    // Function to check network availability
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Function to convert milliseconds to a date-time string
    fun convertMillisToDateTime(): String {
        val currentDateTime = Calendar.getInstance().timeInMillis
        val date = Date(currentDateTime)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }

    // Functions related to location and permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun checkLocationPermission() {
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            // Permission already granted, proceed with location retrieval
            requestLocationUpdates()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location retrieval
                requestLocationUpdates()
            } else {
                // Permission denied, handle accordingly (inform user, etc.)
            }
        }
    }
    private fun requestLocationUpdates() {
        if (!isLocationEnabled()) {
            // Location services are disabled, prompt the user to enable them
            // Implement logic to prompt the user to enable location services
            return
        }

        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.lastLocation?.let { location ->
                    // Handle location updates
                    handleLocationUpdate(location)
                }
            }
        }

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (locationRequest != null) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        }
    }
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }
    private fun handleLocationUpdate(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }

    // RetrofitClient object for API requests
    object RetrofitClient {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

        fun create(): WeatherApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(WeatherApiService::class.java)
        }
    }

    // Function to save data for a location in SharedPreferences with timestamp
    private fun saveDataForLocationWithTimestamp(location: String, temperature: String, description: String, currentTime: String) {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("temperature", temperature)
        editor.putString("description", description)
        editor.putString("timestamp", currentTime)
        editor.apply()
    }

    // Function to load data for a location from SharedPreferences
    private fun loadDataForLocation(location: String): Triple<String?, String?, String?> {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val temperature = sharedPreferences.getString("temperature", null)
        val description = sharedPreferences.getString("description", null)
        val timestamp = sharedPreferences.getString("timestamp",null)
        return Triple(temperature, description, timestamp)
    }

    // Functions to fetch weather data using Retrofit By Coordinates
    private fun fetchWeatherDataByCoordinates(latitude: Double, longitude: Double, apiKey: String, callback: (temperature: String, description: String) -> Unit) {
        val service = RetrofitClient.create()

        service.getCurrentWeatherByCoordinates(latitude, longitude, apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        val temperature = weatherResponse?.main?.temp?.toInt()?.minus(273.15)
                            ?.let { Math.ceil(it).toString() }
                            ?: "Weather unavailable"
                        val description =
                            weatherResponse?.weather?.firstOrNull()?.description ?: "Weather unavailable"
                        callback.invoke("$temperature°C", description)
                    } else {
                        callback.invoke("Weather unavailable", "Weather unavailable")
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    callback.invoke("Weather request failed", "Weather request failed")
                }
            })
    }

    // Functions to fetch weather data using Retrofit By Location
    private fun fetchWeatherDataByLocation(location: String, apiKey: String, callback: (temperature: String, description: String) -> Unit) {
        val service = RetrofitClient.create()
        service.getCurrentWeather(location, apiKey)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        val temperature = weatherResponse?.main?.temp?.toInt()?.minus(273.15)
                            ?.let { Math.ceil(it).toString() }
                            ?: "Weather unavailable"
                        val description =
                            weatherResponse?.weather?.firstOrNull()?.description ?: "Weather unavailable"
                        callback.invoke("$temperature°C", description)
                    } else {
                        callback.invoke("Weather unavailable", "Weather unavailable")
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    callback.invoke("Weather request failed", "Weather request failed")
                }
            })
    }

}
