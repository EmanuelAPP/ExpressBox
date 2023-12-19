package com.ucaldas.appazuredb;

import android.os.Bundle;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("");

        MapView map = findViewById(R.id.map);
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        double latitude = getIntent().getDoubleExtra("latitud", 0);
        double longitude = getIntent().getDoubleExtra("longitud", 0);
        String nombreUbicacion = getIntent().getStringExtra("nombreUbicacion");

        GeoPoint packageLocation = new GeoPoint(latitude, longitude);
        IMapController mapController = map.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(packageLocation);

        Marker packageMarker = new Marker(map);
        packageMarker.setPosition(packageLocation);
        packageMarker.setTitle(nombreUbicacion);
        map.getOverlays().add(packageMarker);
    }
}