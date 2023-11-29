## Weather App Android Project

This Android application fetches weather information using the OpenWeatherMap API and displays it for multiple locations. It supports fetching data for 'My Current Location,' as well as predefined locations like New York, Singapore, Mumbai, and more. The app utilizes Retrofit for network requests and SharedPreferences for local data storage.

### Features:

- Fetches weather data by coordinates and location names using Retrofit.
- Displays weather information for multiple locations.
- Saves weather data in SharedPreferences for offline access with timestamps.
- Supports swipe-to-refresh functionality for updating weather data.

### Technologies & Components:

- Retrofit: Used for making API requests to fetch weather data.
- SwipeRefreshLayout: Implemented for updating weather data by swiping down.
- SharedPreferences: Utilized to store and retrieve weather data for offline access.
- FusedLocationProviderClient: Handles location services to fetch the device's current coordinates.

### Structure:

The main activity fetches weather data for each location listed, displaying it in a card-based UI. It dynamically fetches data from the API for the 'My Current Location' and predefined locations. It also provides the functionality to refresh data for all locations simultaneously via the swipe-to-refresh gesture.

### Note:

Ensure to replace the API key for OpenWeatherMap (`apiKey = "YOUR_API_KEY"`) in the code with a valid key obtained from the OpenWeatherMap website.

Feel free to explore and contribute to enhance this weather app for Android!
