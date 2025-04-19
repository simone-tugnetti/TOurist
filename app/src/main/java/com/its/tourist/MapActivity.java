package com.its.tourist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.android.libraries.places.api.model.Place.Type.POINT_OF_INTEREST;
import static com.google.android.libraries.places.api.model.Place.Type.MUSEUM;
import static com.google.android.libraries.places.api.model.Place.Type.MOVIE_THEATER;
import static com.google.android.libraries.places.api.model.Place.Type.RESTAURANT;
import static com.google.android.libraries.places.api.model.Place.Field.ID;


/**
 * @author Simone Tugnetti, Razvan Apostol, Federica Vacca
 * MapActivity
 * Classe usata per gestire la mappa ed i suoi contenuti
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ToolbarArcBackground mToolbarArcBackground;
    private AppBarLayout mAppBarLayout;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private Location mLastLocation;
    private LocationCallback locationCallback;
    private View mapView;
    private GlobalVariable global;
    private List<LatLng> polyline;
    private boolean isFiltered;

    private final float DEFAULT_ZOOM = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);

        global = GlobalVariable.getInstance();
        global.setBackPeopleMap(false);

        isFiltered = false;

        mToolbarArcBackground = findViewById(R.id.toolbarArcBackground);
        mAppBarLayout = findViewById(R.id.appbar);

        Places.initialize(this, getResources().getString(R.string.google_maps_key));

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        placesClient = Places.createClient(this);

        visualizzaMappa();
        treeObserve();
        toolbar();
        getWindow().getDecorView().post(() -> mToolbarArcBackground.startAnimate());
        getCurrentWeather();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(mapStyle());
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMinZoomPreference(11);
        CustomMarkerInfoWindowView adapter = new CustomMarkerInfoWindowView(this);
        mMap.setInfoWindowAdapter(adapter);
        mMap.setOnInfoWindowClickListener(adapter);
        mMap.setPadding(0, mToolbarArcBackground.getHeight(), 0,
                findViewById(R.id.buttonContainer).getHeight());

        circoscrizioneTorino();

        setPositionBtnGeo();
        checkGPS();

        filtriMarker();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if(isFiltered) {
            Toast.makeText(this, "Reset dei luoghi di interesse", Toast.LENGTH_LONG)
                    .show();
            changePlaces(POINT_OF_INTEREST, false);
        } else if(global.isBackPeopleMap()) {
            super.onBackPressed();
        } else {
            makeAlertDialog("Scegli un'opzione", "", true);
        }
    }


    /**
     * Metodo per visualizzare la mappa
     * Viene visualizzata la mappa all'interno di un fragment
     */
    private void visualizzaMappa () {
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        assert fm != null;
        fm.getMapAsync(this);
        mapView = fm.getView();
    }


    /**
     * Metodo per la gestione dei stili della mappa
     * Viene letto un json con gli stili della mappa, questa cambierà in base all'ora del telefono.
     * Se l'ora è inferiore alle 6 del mattino o 18 del pomeriggio, allora la mappa sarà in uno
     * stile notturno, altrimenti avrà uno stile giornaliero
     * @return MapStyleOptions Lo stile della mappa
     */
    private MapStyleOptions mapStyle() {
        MapStyleOptions style;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALIAN);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour < 6 || hour > 18){
            style = MapStyleOptions.loadRawResourceStyle(
                    this,R.raw.map_style_night);
        } else {
            style = MapStyleOptions.loadRawResourceStyle(
                    this,R.raw.map_style_day);
        }
        return style;
    }


    /**
     * Metodo per la lettura delle coordinate torinesi
     * Vengono lette le coordinate da un file txt chiamato turinCoordinates.txt
     * che si trova all'interno della cartella assets
     * @return String le coordinate lette
     */
    public String metodoLetturaCoordinate () {
        try {
            InputStream is = getAssets().open("turinCoordinates.txt");
            int size = is.available();
            byte[] buffer = new byte[size];

            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Metodo per la circoscrizione di Torino
     * Utilizzando le coordinate ricevute, viene creato il confine torinese utilizzando Polyline
     */
    private void circoscrizioneTorino () {
        String[] coordinate = metodoLetturaCoordinate().split(";");
        polyline = new ArrayList<>();
        for (String s : coordinate) {
            String[] LatLng = s.split(",");
            polyline.add(new LatLng(Double.parseDouble(LatLng[1]), Double.parseDouble(LatLng[0])));
        }
        PolylineOptions rectOptions = new PolylineOptions().addAll(polyline);
        rectOptions.color(ContextCompat.getColor(this, R.color.base_primary));
        rectOptions.width(8);
        mMap.addPolyline(rectOptions);
    }


    /**
     * Metodo di posizionamento pulsante geolocalizzazione
     * Viene utilizzato per spostare il pulsante di geolocalizzazione in basso a destra
     */
    private void setPositionBtnGeo() {
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                    .getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);
        }
    }


    /**
     * Metodo per la richiesta di localizzazione
     * Viene creata una richiesta di localizzazione da utilizzare per i relativi servizi
     * @return LocationRequest La richiesta di localizzazione
     */
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    /**
     * Metodo per la verifica del GPS
     * Se viene rilevata l'attivazione, si avvia la procedura per la geolocalizzazione dell'utente
     */
    private void checkGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> getDeviceLocation());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult(MapActivity.this, 51);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    /**
     * Metodo per trovare la posizione del device
     * Utilizzando un servizio chiamato fused location provider, si può ottenere la posizione
     * effettiva dell'utente
     */
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        providerDeviceLocation(task);
                    } else {
                        Toast.makeText(MapActivity.this,
                                "Non è possibile trovare l'ultima posizione nota",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Metodo per la verifica della posizione dell'utente all'interno del territorio Torinese
     * Verrà verificato se l'utente risulterà all'interno dell'area geografica di Torino.
     * In caso contrario, verrà visualizzato un messaggio di informazione.
     * @param position La posizione dell'utente
     */
    private void checkUserIntoTurin(LatLng position) {
        if(!PolyUtil.containsLocation(position, polyline, true)){
            makeAlertDialog(
                    "Attenzione",
                    "Attualmente non ti trovi nella città di Torino." +
                            "\nVerranno comunque visulizzati i luoghi di interesse intorno a te " +
                            "\uD83D\uDE09",
                    false
            );
        }
    }


    /**
     * Metodo principale per ottenere la localizzazione dell'utente
     * Tramite il ricevimento di una localizzazione ricevuta da un Task,
     * è possibile stabilire la posizione dell'utente e, se quest'ultima non è stata trovata,
     * verrà eseguita un'ulteriore ricerca effettuando una nuova richiesta di localizzazione.
     * @param task task ricevuto per poter ottenere la localizzazione dell'utente
     */
    private void providerDeviceLocation(Task<Location> task) {
        mLastLocation = task.getResult();
        if (mLastLocation != null) {
            LatLng mLastLocationLatLng = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocationLatLng, DEFAULT_ZOOM));
            checkUserIntoTurin(mLastLocationLatLng);
            places(POINT_OF_INTEREST);
        } else {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult == null) {
                        return;
                    }
                    mLastLocation = locationResult.getLastLocation();
                    LatLng mLastLocationLatLng = new LatLng(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocationLatLng,
                            DEFAULT_ZOOM));
                    checkUserIntoTurin(mLastLocationLatLng);
                    mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                }
            };
            mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(),
                    locationCallback, null);
            places(POINT_OF_INTEREST);
        }
    }


    /**
     * Metodo per trovare i punti di interesse
     * Viene utilizzato per controllare se sono presenti dei luoghi di interesse attorno all'utente,
     * raccogliendone poi le informazioni necessarie per ogni singolo posto dandone come riferimento l'ID
     * @param placeType Il tipo di luogo da visualizzare
     */
    private void places(Place.Type placeType) {
        List<Place.Field> placeFetchFields = Arrays.asList(ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                Place.Field.PRICE_LEVEL, Place.Field.OPENING_HOURS, Place.Field.TYPES,
                Place.Field.PHONE_NUMBER);
        FindCurrentPlaceRequest findPlaceRequest =
                FindCurrentPlaceRequest.newInstance(Collections.singletonList(ID));
        Task<FindCurrentPlaceResponse> placeResponse =
                placesClient.findCurrentPlace(findPlaceRequest);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FindCurrentPlaceResponse response = task.getResult();
                assert response != null;
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                    FetchPlaceRequest requestFetch = FetchPlaceRequest
                            .newInstance(Objects.requireNonNull(placeLikelihood.getPlace().getId()),
                                    placeFetchFields);
                    placesClient.fetchPlace(requestFetch).addOnSuccessListener((responseFetch) ->

                            makePlace(responseFetch, placeType)

                    ).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            Log.e("Fetch not found", "Place not found: " +
                                    exception.getMessage());
                        }
                    });

                }
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    Log.e("Find not found", "Place not found: " + exception.getMessage());
                }
            }
        });
    }


    /**
     * Metodo per la creazione e visualizzazione dei luoghi verificati
     * Vengono stampati sulla mappa i luoghi tramite marker se i dati richiesti dall'utente coincidono con
     * quelli del luogo di riferimento, oppure se non sono disponibili.
     * @param responseFetch La risposta da FetchPlaceRequest per ottenere il luogo con le info necessarie
     * @param placeType Il tipo di luogo da visualizzare
     */
    private void makePlace(FetchPlaceResponse responseFetch, Place.Type placeType) {
        Place place = responseFetch.getPlace();
        List<Place.Type> types = place.getTypes();
        assert types != null;
        if(types.contains(placeType)){
            MarkerOptions markerOptions = new MarkerOptions();
            MarkerTags tag = new MarkerTags();
            markerOptions.title(place.getName());
            markerOptions.position(Objects.requireNonNull(place.getLatLng()));
            String orario = "Gli orari possono variare";
            boolean aperto = false;

            if(place.getOpeningHours() != null) {
                List<Period> periods = Objects.requireNonNull(place.getOpeningHours()).getPeriods();
                String [] userTimeSplit;
                int hourStart, minuteStart, hourEnd, minuteEnd, hourPlaceStart,
                        minutePlaceStart, hourPlaceEnd, minutePlaceEnd;
                for(Period p : periods) {
                    if (Objects.requireNonNull(p.getOpen()).getDay() == gestioneDatiCalendario()) {
                        userTimeSplit = global.getTimeStart().split(":");
                        hourStart = Integer.parseInt(userTimeSplit[0]);
                        minuteStart = Integer.parseInt(userTimeSplit[1]);

                        userTimeSplit = global.getTimeEnd().split(":");
                        hourEnd = Integer.parseInt(userTimeSplit[0]);
                        minuteEnd = Integer.parseInt(userTimeSplit[1]);

                        hourPlaceStart = p.getOpen().getTime().getHours();
                        minutePlaceStart = p.getOpen().getTime().getMinutes();
                        hourPlaceEnd = p.getClose().getTime().getHours();
                        minutePlaceEnd = p.getClose().getTime().getMinutes();

                        if(((hourStart < hourPlaceEnd) ||
                                (hourStart == hourPlaceEnd && minuteStart < minutePlaceEnd)) &&
                                    ((hourStart == hourPlaceStart && minuteStart >= minutePlaceStart)  ||
                                        (hourStart > hourPlaceStart))) {
                            orario = "";
                            aperto = true;
                            if(hourEnd >= hourPlaceEnd && minuteEnd > minutePlaceEnd) {
                                orario = "Potrebbe chiudere prima dell'ora richiesta";
                            }
                        }
                    }
                }
            }

            if (place.getPriceLevel() == null) {
                markerOptions.snippet("Indirizzo: " + place.getAddress() +
                        "\nI prezzi possono variare" + "\n" + orario);
            } else if (place.getPriceLevel() <= gestioneDatiPrezzo()) {
                if (place.getRating() != null) {
                    markerOptions.snippet("Indirizzo: " + place.getAddress() +
                            "\nRating: " + place.getRating() + "\n" + orario);
                } else {
                    markerOptions.snippet("Indirizzo: " + place.getAddress() + "\n" + orario);
                }
            }

            if(place.getPhoneNumber() != null) {
                tag.setPhone(place.getPhoneNumber());
            }

            if (place.getPhotoMetadatas() != null) {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) ->
                        tag.setImage(fetchPhotoResponse.getBitmap())
                ).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        Log.e("PlaceNotFoundPhoto", "Place not found: " + exception.getMessage());
                    }
                });
            }

            if (place.getPriceLevel() == null || place.getPriceLevel() <= gestioneDatiPrezzo() &&
                    place.getOpeningHours() == null || aperto) {
                mMap.addMarker(markerOptions).setTag(tag);
            }

        }
    }


    /**
     * Metodo che filtra i luoghi in base al tipo
     * Vengono eliminati i luoghi precedenti per poi visualizzarne altri selezionati in base a tre tipi:
     * Musei, Cinema e Ristoranti
     */
    private void filtriMarker () {
        Button btnMusei = findViewById(R.id.btnMusei);
        Button btnCinema = findViewById(R.id.btnCinema);
        Button btnRisto = findViewById(R.id.btnRistoranti);

        btnMusei.setOnClickListener(view -> {
            Toast.makeText(this, "Caricamento dei Musei intorno a te", Toast.LENGTH_LONG)
                    .show();
            changePlaces(MUSEUM, true);
        });

        btnCinema.setOnClickListener(view -> {
            Toast.makeText(this, "Caricamento dei Cinema intorno a te", Toast.LENGTH_LONG)
                    .show();
            changePlaces(MOVIE_THEATER, true);
        });

        btnRisto.setOnClickListener(view -> {
            Toast.makeText(this, "Caricamento dei Ristoranti intorno a te", Toast.LENGTH_LONG)
                    .show();
            changePlaces(RESTAURANT, true);
        });

    }


    /**
     * Metodo per cambiare i tipi dei luoghi visulizzati
     * Vengono cancellati i dati visulizzati sulla mappa, tranne la circoscrizione, per poi visulizzare
     * quelli richiesti. Nel caso vengano visulizzati luoghi "filtrati", premendo back, potranno essere ripristinati
     * i luoghi di "interesse".
     * @param type il tipo di luogo
     * @param filtered Flag se sono stati visulizzati i luoghi "filtrati"
     */
    private void changePlaces(Place.Type type, boolean filtered) {
        mMap.clear();
        circoscrizioneTorino();
        places(type);
        isFiltered = filtered;
    }


    /**
     * Metodo per settare l'altezza della toolbar
     */
    private void treeObserve () {
        ViewTreeObserver vto = mAppBarLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mAppBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = mAppBarLayout.getMeasuredHeight();
                mToolbarArcBackground.setHeight(height);
            }
        });
    }


    /**
     * Metodo per settare la toolbar
     */
    private void toolbar () {
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                float scale = (float) Math.abs(verticalOffset) / scrollRange;
                mToolbarArcBackground.setScale(1 - scale);

            }
        });
    }


    /**
     * Metodo per la gestione del Meteo
     * Tramite OpenWeatherMap, vengono presi i dati meteo di Torino per essere poi stampati nella toolbar
     */
    private void getCurrentWeather() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.
                getCurrentWeatherData(
                        "45.070935",
                        "7.685048",
                        "INSERT_KEY_HERE"
                );
        TextView txtMeteo = findViewById(R.id.txtMeteo);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull
                    Response<WeatherResponse> response) {

                if (response.code() == 200) {
                    assert response.body() != null;
                    WeatherResponse weatherResponse = response.body();
                    @SuppressLint("DefaultLocale") String stringBuilder =
                            String.format("%.0f", kelvinToCelsius(weatherResponse.main.temp)) + "°";
                    txtMeteo.setText(stringBuilder);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                txtMeteo.setText("E°");
            }

        });

    }


    /**
     * Metodo per la conversione da gradi Kelvin a Celsius
     * @return double Il risultato della conversione
     */
    private double kelvinToCelsius(double grades) {
        return grades - 273.15;
    }


    /**
     * Metodo per la gestione del prezzo/persone
     * Vengono confrontati i dati inseriti dall'utente con i dati più pertinenti
     * @return int 0="gratis", 1="economico", 2="medio", 3="medio-alto" e 4="alto"
     */
    private int gestioneDatiPrezzo () {
        int priceS = global.getBudgetStart();
        int priceE = global.getBudgetEnd();
        String personT = global.getTypePerson();

        if (priceE == 0) {
            return 0;
        } if ((priceS >= 0 && priceE <= 20) && personT.equals("singolo")) {
            return 1;
        } else if ((priceS >= 0 && priceE <= 50) && personT.equals("singolo")) {
            return 2;
        } else if ((priceS >= 0 && priceE <= 100) && personT.equals("singolo")) {
            return 3;
        } else if ((priceS >= 0 && priceE <= 200) && personT.equals("singolo")) {
            return 4;
        } else if ((priceS >= 0 && priceE <= 30) && personT.equals("coppia")) {
            return 1;
        } else if ((priceS >= 0 && priceE <= 70) && personT.equals("coppia")) {
            return 2;
        } else if ((priceS >= 0 && priceE <= 120) && personT.equals("coppia")) {
            return 3;
        } else if ((priceS >= 0 && priceE <= 200) && personT.equals("coppia")) {
            return 4;
        } else if ((priceS >= 0 && priceE <= 50) && personT.equals("gruppo")) {
            return 1;
        } else if ((priceS >= 0 && priceE <= 100) && personT.equals("gruppo")) {
            return 2;
        } else if ((priceS >= 0 && priceE <= 150) && personT.equals("gruppo")) {
            return 3;
        } else if ((priceS >= 0 && priceE <= 200) && personT.equals("gruppo")) {
            return 4;
        }

        return 0;
    }


    /**
     * Metodo per la gestione del calendario
     * Viene prelevato il giorno della settimana in base alla data scelta dall'utente
     * @return DayOfWeek Il giorno della settimana
     */
    private DayOfWeek gestioneDatiCalendario() {
        int timeD = global.getCalendarDay();

        if(timeD == 1) {
            return DayOfWeek.SUNDAY;
        } else if(timeD == 2) {
            return DayOfWeek.MONDAY;
        } else if(timeD == 3) {
            return DayOfWeek.TUESDAY;
        } else if (timeD == 4) {
            return DayOfWeek.WEDNESDAY;
        } else if(timeD == 5) {
            return  DayOfWeek.THURSDAY;
        } else if(timeD == 6) {
            return DayOfWeek.FRIDAY;
        } else if(timeD == 7) {
            return DayOfWeek.SATURDAY;
        }

        return null;
    }


    /**
     * Metodo per la gestione degli Alert Dialog
     * @param title il titolo dell'alert
     * @param text il testo dell'alert
     * @param exit se l'alert è dedicato per il bottone back o generico
     */
    private void makeAlertDialog(String title, String text, boolean exit) {
        if (exit) {
            String[] choise = {"Reinserire i dati", "Esci"};
            new AlertDialog.Builder(this).setTitle(title)
                    .setSingleChoiceItems(choise, -1, null)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        ListView lw = ((AlertDialog)dialogInterface).getListView();
                        if(lw.getCheckedItemCount() > 0){
                            switch(lw.getCheckedItemPosition()) {
                                case 0:
                                    findViewById(R.id.frame_map).setVisibility(View.VISIBLE);
                                    FragmentTransaction fragmentTransaction =
                                            getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.frame_map, new PeopleFragment());
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                    break;
                                case 1:
                                    finish();
                                    break;
                            }
                        }
                    }).show();
        } else {
            new AlertDialog.Builder(this).setTitle(title).setMessage(text)
                    .setPositiveButton("OK", null).show();
        }

    }

}
