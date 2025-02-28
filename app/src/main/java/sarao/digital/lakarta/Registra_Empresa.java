package sarao.digital.lakarta;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registra_Empresa extends AppCompatActivity {

    LocationManager mlocManager;
    String mensajeAlerta;
    Toast mensajePop;
    EditText nombreUsu,nombreEmp,poblacionEmp,provinciaEmp,emailEmp,direccionEmp,telefonoEmp,contactoEmp, codigoUsr;
    TextView migpsPuesto,getGPS,registrar,verificaGps,leePolitica, volver;
    ImageView iconoUsuario,iconoNombre, iconoPoblacion, iconoProvincia,iconoEmail, iconoDireccion, iconoTlf, iconoContacto,gpsObtenido;

    private long mLastClickTime = 0;

    LinearLayout cajaDatos, cajaConfirmar;

    boolean usuarioOk=false, nombreOk=false,poblacionOk=false,provinciaOk=false,emailOk=false,direccionOk=false,tlfOk=false,contactoOk=false,gpsOk=false;

    public static ImageView iconoPolitica;
    public static Boolean politicaOk=false;

    ProgressBar progresoGps;

    public Location migps=new Location("");
    Localizacion miLoc;

    NuevoUsuario nuevoUsuario;

    ScrollView scroll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_empresa);

        mlocManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        nombreUsu=findViewById(R.id.usuario);
        nombreEmp=findViewById(R.id.nombre);
        poblacionEmp=findViewById(R.id.poblacion);
        provinciaEmp=findViewById(R.id.provincia);
        emailEmp=findViewById(R.id.email);
        direccionEmp=findViewById(R.id.direccion);
        telefonoEmp=findViewById(R.id.telefono);
        contactoEmp=findViewById(R.id.contacto);
        getGPS=findViewById(R.id.get_gps);
        registrar=findViewById(R.id.registrar);
        migpsPuesto=findViewById(R.id.gps);
        gpsObtenido=findViewById(R.id.gps_obtenido);
        verificaGps=findViewById(R.id.verifica_gps);
        progresoGps=findViewById(R.id.barraprogreso_gps);
        scroll=findViewById(R.id.scroll_alta_empresa);
        volver=findViewById(R.id.volver);

        leePolitica=findViewById(R.id.leer_politica);

        iconoUsuario=findViewById(R.id.icono_usuario);
        iconoNombre=findViewById(R.id.icono_nombre);
        iconoPoblacion=findViewById(R.id.icono_poblacion);
        iconoProvincia=findViewById(R.id.icono_provincia);
        iconoEmail=findViewById(R.id.icono_email);
        iconoDireccion=findViewById(R.id.icono_direccion);
        iconoTlf=findViewById(R.id.icono_tlf);
        iconoContacto=findViewById(R.id.icono_contacto);
        iconoPolitica=findViewById(R.id.icono_politica);

        codigoUsr=findViewById(R.id.codigo);

        cajaDatos=findViewById(R.id.caja_datos);
        cajaConfirmar=findViewById(R.id.caja_confirmar);

        verificaGps.setVisibility(View.GONE);
        progresoGps.setVisibility(View.GONE);

        cajaConfirmar.setVisibility(View.GONE);

        nuevoUsuario=new NuevoUsuario(this,this,getLayoutInflater());

        miLoc=new Localizacion();

        iniciaGPS();

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(!nuevoUsuario.enviarCodigo) {

                    if(usuarioOk && nombreOk && poblacionOk && provinciaOk && emailOk && direccionOk && tlfOk && contactoOk && politicaOk) {

                        if(gpsOk) {

                            nuevoUsuario.enviaNuevaEmpresa(nombreUsu.getText().toString(),nombreEmp.getText().toString(), contactoEmp.getText().toString(),poblacionEmp.getText().toString(),
                                    provinciaEmp.getText().toString(),emailEmp.getText().toString(), direccionEmp.getText().toString(),telefonoEmp.getText().toString(),
                                    String.valueOf(migps.getLatitude()),String.valueOf(migps.getLongitude()),
                                    iconoUsuario,iconoTlf,iconoEmail,scroll,registrar,cajaDatos,cajaConfirmar);

                        }else{

                            preguntaPorGps();
                        }
                    }else{

                        mensajeAlerta="Datos incompletos";
                        ponAlerta();
                    }

                }else{

                    if(codigoUsr.getText().toString().length()>0) {

                        nuevoUsuario.activaEmpresa(emailEmp.getText().toString(),nombreUsu.getText().toString(),codigoUsr.getText().toString(),registrar);

                    }else{

                        mensajeAlerta = getResources().getString(R.string.codigo_incorrecto);
                        ponAlerta();
                        activaBoton(true, registrar);

                    }

                }



            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activaBoton(false, volver);

                if(nuevoUsuario.enviarCodigo){

                    preguntaSiSalir();
                }else {

                    finish();

                }



            }
        });

        leePolitica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activaBoton(false, leePolitica);

                irApolitica(leePolitica, "politica_empresa");

            }
        });

        verificaGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

               if(gpsOk){

                   verificaGps();
               }
            }
        });

        nombreUsu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>5){

                    iconoUsuario.setImageResource(R.drawable.ok);
                    usuarioOk=true;

                }else{
                    iconoUsuario.setImageResource(R.drawable.delete);
                    usuarioOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        nombreEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    iconoNombre.setImageResource(R.drawable.ok);
                    nombreOk=true;

                }else{
                    iconoNombre.setImageResource(R.drawable.delete);
                    nombreOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        poblacionEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    iconoPoblacion.setImageResource(R.drawable.ok);
                    poblacionOk=true;
                }else{
                    iconoPoblacion.setImageResource(R.drawable.delete);
                    poblacionOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        provinciaEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    iconoProvincia.setImageResource(R.drawable.ok);
                    provinciaOk=true;
                }else{
                    iconoProvincia.setImageResource(R.drawable.delete);
                    provinciaOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    // Patrón para validar el email
                    Pattern pattern = Pattern
                            .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

                    // El email a validar

                    Matcher mather = pattern.matcher(s.toString());

                    if (mather.find()) {
                        iconoEmail.setImageResource(R.drawable.ok);

                        emailOk=true;

                    } else {
                        iconoEmail.setImageResource(R.drawable.delete);
                        emailOk=false;

                    }
                }else{
                    iconoNombre.setImageResource(R.drawable.delete);
                    emailOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        direccionEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    iconoDireccion.setImageResource(R.drawable.ok);
                    direccionOk=true;

                }else{
                    iconoDireccion.setImageResource(R.drawable.delete);
                    direccionOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        telefonoEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    iconoTlf.setImageResource(R.drawable.ok);
                    tlfOk=true;

                }else{
                    iconoTlf.setImageResource(R.drawable.delete);
                    tlfOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contactoEmp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>0){

                    iconoContacto.setImageResource(R.drawable.ok);
                    contactoOk=true;

                }else{
                    iconoContacto.setImageResource(R.drawable.delete);
                    contactoOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        getGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, getGPS);

                if(migps.getLatitude()!=0){

                    migpsPuesto.setText(getResources().getString(R.string.gps_ok));
                    verificaGps.setVisibility(View.VISIBLE);
                    gpsObtenido.setImageResource(R.drawable.ok);
                    gpsOk=true;

                }else{

                    iniciaCuandoGps inicia=new iniciaCuandoGps();

                    inicia.execute();
                }

                activaBoton(true, getGPS);
            }
        });



    }

    @Override
    public void onBackPressed() {

        if(nuevoUsuario.enviarCodigo){

            preguntaSiSalir();
        }else {

            super.onBackPressed();

        }
    }

    public void preguntaPorGps(){


    }

    public void verificaGps(){

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="+migps.getLatitude()+","+migps.getLongitude())); //o la direccion/consulta que quiera "http://maps.google.com/maps?q="+ myLatitude  +"," + myLongitude +"("+ labLocation + ")&iwloc=A&hl=es"
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    public void iniciaGPS(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //System.out.println("NO TIENE ACCESO A LA LOCALIZACION 1");
            //ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION }, 1222);

        }

        try {
            mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, miLoc);
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, miLoc);


        }catch (Exception e){


        }
    }

    private class iniciaCuandoGps extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {

            progresoGps.setVisibility(View.VISIBLE);
            migpsPuesto.setText(getResources().getString(R.string.buscando));
            gpsObtenido.setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(String... strings) {

            int contador=0;

            while (migps.getLatitude() == 0 && contador<20) {



                try {
                    Thread.sleep(300);
                    contador++;
                } catch (Exception e) {
                    System.out.println("ERROR GPS "+e.getMessage());

                }

            }

            if(contador==20){


                return "nok";
            }


            return "ok";
        }

        protected void onPostExecute(String resultado) {

            progresoGps.setVisibility(View.GONE);
            gpsObtenido.setVisibility(View.VISIBLE);

            if(resultado.equals("ok")){
                migpsPuesto.setText(getResources().getString(R.string.gps_ok));
                gpsObtenido.setImageResource(R.drawable.ok);
                verificaGps.setVisibility(View.VISIBLE);
                gpsOk=true;

            }else{
                migpsPuesto.setText(getResources().getString(R.string.gps_nok));
                gpsObtenido.setImageResource(R.drawable.delete);

            }

        }
    }

    @Override
    public void onStop() {

        mlocManager.removeUpdates(miLoc);  // ------------- DETIENE EL GPS ----------------------

        super.onStop();
    }

    public class Localizacion implements LocationListener {


        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            migps.setLatitude(loc.getLatitude());
            migps.setLongitude(loc.getLongitude());

            //System.out.println("Ha puesto el gps");
            //System.out.println("POSICION GPS: "+migps.getLatitude()+" "+migps.getLongitude());
        }
        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado

        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado

        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    System.out.println("LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    System.out.println( "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    System.out.println( "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    private void enviaRegistro(){

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",emailEmp.getText().toString(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PETICION DE EMPRESA: "+nombreEmp.getText().toString());
        emailIntent.putExtra(Intent.EXTRA_TEXT, poblacionEmp.getText().toString());
        startActivity(Intent.createChooser(emailIntent,  getResources().getString(R.string.selecciona_tu_correo)));
    }



    private void ponAlerta(){
        
        try {
        if(!mensajePop.getView().isShown()) {

            mensajePop.setText(mensajeAlerta);

            mensajePop.setGravity(Gravity.CENTER, 0, 0);
            TextView mensaje = mensajePop.getView().findViewById(android.R.id.message);
            mensaje.setGravity(Gravity.CENTER);

            mensajePop.show();

        }
        }catch (Exception e){

            mensajePop.setText(mensajeAlerta);
            mensajePop.show();
        }

    }


    private void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

    private void irApolitica(TextView boton, String tipoPolitica){

        Intent miIntent = new Intent(this, politica_privacidad.class);

        miIntent.putExtra("POLITICA",tipoPolitica);

        startActivity(miIntent);

        activaBoton(true, boton);
    }


    private void preguntaSiSalir(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        final Button seguir=alertLayout.findViewById(R.id.pedido_guardar);
        final Button salir=alertLayout.findViewById(R.id.pedido_noguardar);

        TextView mensaje=alertLayout.findViewById(R.id.pregunta_alerta);

        mensaje.setText(getResources().getString(R.string.salir_sin_alta));

        seguir.setText(getResources().getString(R.string.continuar));
        salir.setText(getResources().getString(R.string.salir));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true, volver);

            }
        });

        seguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, seguir);

                activaBoton(true, volver);

                dialog.cancel();


            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, salir);

                dialog.cancel();
                finish();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

}

