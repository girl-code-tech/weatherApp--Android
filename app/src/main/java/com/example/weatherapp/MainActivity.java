package com.example.weatherapp;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<weatherRVModel> weatherRVModelArrayList;
    private weatherRVAdapter weatherRVAdapter;
    private LocationManager location_manager;
    private int permission_code = 1;
    private String str_CityName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); //to make our application appear full-screen
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new weatherRVAdapter(this, weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, permission_code);
        }
        Location location = location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        str_CityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(str_CityName);
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "Please, Provide a City Name", Toast.LENGTH_SHORT).show();
                }

                else{
                    cityNameTV.setText(str_CityName);
                    getWeatherInfo(city);
                }
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==permission_code)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Please, Provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude)
    {
        String CityName = "not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr: addresses)
            {
                if(adr!=null)
                {
                    String city = adr.getLocality();
                    if(city != null && !city.equals(""))
                    {
                        CityName = city;
                    }
                }


                else{
                    Log.d("Tag", "City Not Found");
                    Toast.makeText(this, "User's City Not Found", Toast.LENGTH_SHORT).show();
                }
            }

        }catch (IOException e)
        {
            e.printStackTrace();
        }

        return CityName;

    }


    private void getWeatherInfo(String cityName)
    {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=41e7054811ae42c8867104256221101&q="+cityName+"&days=1&aqi=no&alerts=yes";
        cityNameTV.setText(str_CityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();
                try {
                    String str_temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(str_temperature.concat("°C"));

                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1)
                    {
                        Picasso.get().load("https://www.google.com/search?q=day+cloudy+image&rlz=1C1SQJL_enPK984PK984&tbm=isch&source=iu&ictx=1&vet=1&fir=wUr_c91VT8etqM%252CrHs-ybTg3s_5nM%252C_%253BAeBE8kwLwWPJeM%252CrHs-ybTg3s_5nM%252C_%253BnZkXo1Cv26YAMM%252CLhPzifaMIgQIEM%252C_%253BgStDWeLEQ4piPM%252CXtSF_1eibgPjiM%252C_%253BmbiQdqn4zR1sKM%252CYgbzK9g78Dy4KM%252C_%253B9_hgayJ9sc4FIM%252CLhPzifaMIgQIEM%252C_%253Bu616UQJ1XCSerM%252CSSAckPYjbLvOKM%252C_%253BKhUSiQB42R_g-M%252CfwkI_Dm3PLJz8M%252C_%253BEcshNRV4BdghXM%252CaHVdl_HXJIzhuM%252C_%253BZ_T7v9akIRNq_M%252CLhPzifaMIgQIEM%252C_%253BSzPNer7jlMKtaM%252CPL0aSBpwDZ4wGM%252C_%253BA3gcJieBKQEQ3M%252C3LnfE05RnfPRCM%252C_&usg=AI4_-kRUZv_IzDQReHQKI7Whmg-yCafv4Q&sa=X&ved=2ahUKEwj1hvaK_6v1AhUUCGMBHUXeAUkQ9QF6BAgHEAE&biw=1366&bih=625&dpr=1#imgrc=cZNfRuDe6ZomkM").into(backIV);
                    }
                    else{
                        Picasso.get().load("https://www.google.com/search?q=dark+night+image&tbm=isch&ved=2ahUKEwilyNGU_6v1AhVKweAKHYR1CJ4Q2-cCegQIABAA&oq=dark+night+image&gs_lcp=CgNpbWcQAzIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEOgYIABAIEB46BAgAEEM6BggAEAcQHjoICAAQBxAFEB5QxSZYmjdgpzpoAHAAeACAAcoCiAGtEZIBBzAuMi43LjGYAQCgAQGqAQtnd3Mtd2l6LWltZ8ABAQ&sclient=img&ei=56veYeW4O8qCgweE66HwCQ&bih=625&biw=1366&rlz=1C1SQJL_enPK984PK984#imgrc=hpTexCCh2JuhaM").into(backIV);
                    }


                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");

                    for (int i=0; i<hourArray.length(); i++)
                    {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time =  hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new weatherRVModel(time, temper, img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please, provide a valid city name", Toast.LENGTH_SHORT).show();

            }
        });

        requestQueue.add(jsonObjectRequest);

    }
}