package com.ucaldas.appazuredb;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
public class MainActivity extends AppCompatActivity{

    private EditText idPaquete;
    private MapView map;
    private double latitud=0;
    private double longitud=0;
    private String nombreUbicacion="";
    private String primerNombre="";
    private String otrosNombres="";
    private String primerApellido="";
    private String tipoCalle="";
    private String numeroCalle="";
    private double precioEnvio=0;
    private String nombreMunicipio="";
    private String nombreDepartamento="";
    private TextView nombreRemitente;
    private TextView direccionDestino;
    private TextView precio;
    private TextView ciudadDestino;
    private TextView departamentoDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idPaquete = findViewById(R.id.idPaquete);
        //OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button btnMapa = findViewById(R.id.btnMapa);
        Button btnPaquete = findViewById(R.id.btnPaquete);

         nombreRemitente = findViewById(R.id.nombreRemitente);
         direccionDestino = findViewById(R.id.direccionDestino);
         precio = findViewById(R.id.precio);
         ciudadDestino = findViewById(R.id.ciudadDestino);
         departamentoDestino = findViewById(R.id.departamentoDestino);

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = "Select nombre,latitud,longitud from bodegas\n" +
                        "where id in (select TOP 1 id_bodega from historials\n" +
                        "where id_paquete ="+idPaquete.getText().toString()+ "\n" +
                        "ORDER BY fechaingreso DESC)";
                new ExecuteMapa().execute(query);

            }
        });
        btnPaquete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query ="SELECT Personas.PrimerNombre, Personas.OtrosNombres, Personas.PrimerApellido,\n" +
                        "       Direccions.TipoCalle,Direccions.Numero,\n" +
                        "       Envios.precio, Municipios.Nombre as NombreMunicipio, Departamentoes.Nombre as NombreDepartamento\n" +
                        "FROM envios\n" +
                        "JOIN Personas ON envios.id_remitente = Personas.id\n" +
                        "JOIN Direccions ON envios.id_direcciondestino = direccions.id\n" +
                        "JOIN Municipios ON direccions.id_municipio = municipios.id\n" +
                        "JOIN Departamentoes on municipios.id_depto = departamentoes.id\n" +
                        "WHERE Envios.id_paquete ="+idPaquete.getText().toString()+";";
                new ExecuteInformacionPaquete().execute(query);
            }
        });
    }

    private class ExecuteInformacionPaquete extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... queries) {
            String result = "";

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String connectionString = "jdbc:jtds:sqlserver://mensajeriaservidor.database.windows.net:1433;DatabaseName=MensajeriaBD;user=alejandromedina2026@mensajeriaservidor;password=Sansimon1822;ssl=request";
                Connection connection = DriverManager.getConnection(connectionString);

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(queries[0]);

                while (resultSet.next()) {
                    // Procesar resultados según tu necesidad

                    primerNombre = resultSet.getString("PrimerNombre") != null ? resultSet.getString("PrimerNombre") : "";
                    otrosNombres = resultSet.getString("OtrosNombres") != null ? resultSet.getString("OtrosNombres") : "";
                    primerApellido = resultSet.getString("PrimerApellido") != null ? resultSet.getString("PrimerApellido") : "";
                    tipoCalle = resultSet.getString("TipoCalle") != null ? resultSet.getString("TipoCalle") : "";
                    numeroCalle = resultSet.getString("Numero") != null ? resultSet.getString("Numero") : "";
                    precioEnvio = resultSet.getString("precio") != null ? Double.parseDouble(resultSet.getString("precio")) : 0.0;
                    nombreMunicipio = resultSet.getString("NombreMunicipio") != null ? resultSet.getString("NombreMunicipio") : "";
                    nombreDepartamento = resultSet.getString("NombreDepartamento") != null ? resultSet.getString("NombreDepartamento") : "";

                }

                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("Error")) {
                Toast.makeText(MainActivity.this, "Error en la consulta: " + result, Toast.LENGTH_SHORT).show();
            } else {
                if (primerNombre.equals("")) {
                    //el paquete no está en camino
                    showAlertDialog("No hay Informacion del paquete", "Aceptar");
                    nombreRemitente.setText("Nombre: ");
                    direccionDestino.setText("Direccion Destino: ");
                    precio.setText("Precio Envio: ");
                    ciudadDestino.setText("Ciudad Destino: ");
                    departamentoDestino.setText("Departamento Destino: ");
                } else {
                    nombreRemitente.setText("Nombre: "+primerNombre +" "+otrosNombres+" "+primerApellido);
                    direccionDestino.setText("Direccion Destino: "+tipoCalle +" #"+numeroCalle);
                    precio.setText("Precio Envio: "+precioEnvio);
                    ciudadDestino.setText("Ciudad Destino: "+nombreMunicipio);
                    departamentoDestino.setText("Departamento Destino: "+nombreDepartamento);
                    primerNombre="";
                }
            }
        }
    }

    private class ExecuteMapa extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... queries) {
            String result = "";

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String connectionString = "jdbc:jtds:sqlserver://mensajeriaservidor.database.windows.net:1433;DatabaseName=MensajeriaBD;user=alejandromedina2026@mensajeriaservidor;password=Sansimon1822;ssl=request";
                Connection connection = DriverManager.getConnection(connectionString);

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(queries[0]);

                while (resultSet.next()) {
                    // Procesar resultados según tu necesidad

                    latitud = Double.parseDouble(resultSet.getString("latitud"));
                    longitud = Double.parseDouble(resultSet.getString("longitud"));
                    nombreUbicacion = resultSet.getString("nombre");
                }

                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("Error")) {
                Toast.makeText(MainActivity.this, "Error en la consulta: " + result, Toast.LENGTH_SHORT).show();
            } else {
                if (nombreUbicacion.isEmpty()) {
                    showAlertDialog("No hay Informacion del paquete", "Aceptar");
                } else {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("latitud", latitud);
                    intent.putExtra("longitud", longitud);
                    intent.putExtra("nombreUbicacion",nombreUbicacion);
                    startActivity(intent);

                    nombreUbicacion = "";
                }
            }
        }
    }

    private void showAlertDialog(String message, String positiveButtonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}