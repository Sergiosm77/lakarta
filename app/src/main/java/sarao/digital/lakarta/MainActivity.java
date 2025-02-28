package sarao.digital.lakarta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ProgressBar barraProgreso;
    Restaurantes[] losRestaurantes;
    Ciudades[] lasCiudades;

    TextView cargando, reintentar, nuevaVersion;

    Server_RecibeDatos serverRecibeDatos;

    String user, alsesUser,alseskUser;
    String userEmp, alsesEmp,alseskEmp;

    String packagename;

    Herramientas herramientas;

    int restCargados=0;

    private RequestQueue rq;
    private JsonRequest jrq;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barraProgreso=findViewById(R.id.barra_inicio);
        cargando=findViewById(R.id.cargando);
        reintentar=findViewById(R.id.reintentar);
        nuevaVersion=findViewById(R.id.nueva_version);

        reintentar.setVisibility(View.GONE);
        nuevaVersion.setVisibility(View.GONE);

        serverRecibeDatos =new Server_RecibeDatos(this);

        herramientas=new Herramientas();

        packagename=getApplicationContext().getPackageName();

        cargaUser();
        cargaEmpresa();

        reintentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                reintentar.setVisibility(View.GONE);
                barraProgreso.setVisibility(View.VISIBLE);
                cargando.setText(getString(R.string.accediendo));

                CargaDatos iniciaTodo=new CargaDatos();
                iniciaTodo.execute();

            }
        });

        nuevaVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{

                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="+packagename);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    //intent.setPackage("com.android.vending");
                    startActivity(intent);

                }catch (Exception e){

                    System.out.println("ERROR ACTUALIZACION");

                }

                finish();

            }
        });

        CargaDatos iniciaTodo=new CargaDatos();

        iniciaTodo.execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1222) {

               iniciaLakarta();

        }
    }

    private class CargaDatos extends AsyncTask<String,Integer,String> {  // carga en memoria la base de datos

        @Override
        protected String doInBackground(String... strings) {

            if (!compruebaConexion()) {


                return "nok";


            }else {

                if(!compruebaNuevaVersion()) {

                    recibeRestaurantesCiudadesSesiones();

                    while (restCargados == 0) {

                        try {
                            Thread.sleep(600);
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                    }

                    if (restCargados == 1) {

                        return "ok";

                    } else {

                        return "nok";
                    }
                }else{

                    return "nueva_version";
                }

            }

        }

        protected void onProgressUpdate(Integer... valores){


        }

        protected void onPostExecute(String resultado){

            System.out.println("RESULTADO: "+restCargados+" "+serverRecibeDatos.respuesta);

            if(resultado.equals("nok")){

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        reintentar.setVisibility(View.VISIBLE);

                        barraProgreso.setVisibility(View.GONE);

                        cargando.setText(getString(R.string.no_pudo_conectar));

                    }
                }, 2000);
            }else if(resultado.equals("ok")) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // acciones que se ejecutan tras los milisegundos
                        compruebaPermisoGPS();

                    }
                }, 2000);
            }else if(resultado.equals("nueva_version")) {

                nuevaVersion.setVisibility(View.VISIBLE);

                barraProgreso.setVisibility(View.GONE);

                cargando.setVisibility(View.GONE);
            }

        }

    }

    public void iniciaLakarta(){

        Intent miIntent = new Intent(MainActivity.this, Inicio.class);

        miIntent.putExtra("RESTAURANTES", losRestaurantes);
        miIntent.putExtra("CIUDADES",lasCiudades);

        barraProgreso.setVisibility(View.GONE);

        startActivity(miIntent);

        finish();

    }

    public void recibeRestaurantesCiudadesSesiones(){

        restCargados=0;

        String url=this.getString(R.string.servidor_getrestyciu);

        rq= Volley.newRequestQueue(this.getApplicationContext());

        System.out.println("LOS ALSES: "+alsesUser+" "+alseskUser);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("codigo_datos", this.getString(R.string.codigo_datos));
        parametros.put("user", user);
        parametros.put("alses", herramientas.codiAlses(alsesUser,alseskUser));
        parametros.put("userEmp", userEmp);
        parametros.put("alsesEmp", herramientas.codiAlses(alsesEmp,alseskEmp));

        JSONObject parametrosEnvio = new JSONObject(parametros);

        jrq = new JsonObjectRequest(Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA TOTAL SERVER "+response);


                try {


                    JSONArray jsonArray_restaurantes = response.getJSONArray("datos_restaurantes");
                    JSONArray jsonArray_ciudades = response.getJSONArray("datos_ciudades");
                    JSONArray jsonArray_alergenos = response.getJSONArray("datos_alergenos");

                    JSONArray jsonArray_user = response.getJSONArray("usuario");

                    JSONArray jsonArray_empresa = response.getJSONArray("empresa");


                    if(jsonArray_restaurantes.length()>0 && jsonArray_ciudades.length()>0 && jsonArray_ciudades.length()>0) {

                        estableceRestaurantes(jsonArray_restaurantes);
                        estableceCiudad(jsonArray_ciudades);
                        guardaDatosAlergenos(jsonArray_alergenos);

                        if(jsonArray_user.get(0).equals("error")){

                            if(!user.equals("0")){

                                herramientas.cierraSesionUsuario(getApplicationContext());
                            }
                        }else  if(jsonArray_user.get(0).equals("ok")){



                            JSONArray jsonArray_sesionUser = response.getJSONArray("datos_sesion_user");
                            JSONArray jsonArray_misLikes = response.getJSONArray("mis_likes");
                            herramientas.guardaUsuario(getApplicationContext(), jsonArray_sesionUser);
                            herramientas.guardaMisLikes(getApplicationContext(), jsonArray_misLikes);

                            JSONObject objectUser = jsonArray_sesionUser.getJSONObject(0);

                            herramientas.guardaAlsesUsuario(getApplicationContext(), herramientas.decodiAlses(objectUser.getString("nuevoalses"),alseskUser));

                            System.out.println("RESPUESTA SERVIDOR "+jsonArray_sesionUser);
                            System.out.println("MIALSES "+alsesUser);
                            System.out.println("MIS LIKES "+jsonArray_misLikes);

                        }

                        if(jsonArray_empresa.get(0).equals("error")){

                            if(!userEmp.equals("0")){

                                herramientas.cierraSesionEmpresa(getApplicationContext());
                            }
                        }else  if(jsonArray_empresa.get(0).equals("ok")){

                            JSONArray jsonAlses= response.getJSONArray("dime_alses");

                            herramientas.guardaAlsesEmpresa(getApplicationContext(), herramientas.decodiAlses(jsonAlses.get(0).toString(),alseskEmp));

                            JSONObject jsonObjsesionEmp = response.getJSONObject("datos_sesion_emp");

                            herramientas.guardaEmpresa(getApplicationContext(), jsonObjsesionEmp);


                            System.out.println("NOMBRE EMPRESA "+jsonObjsesionEmp.getString("nombre"));

                        }

                        restCargados=1;

                    }else{

                        restCargados=2;
                    }


                } catch (JSONException e) {

                    restCargados=2;

                    System.out.println("FALLO1 "+e.getMessage());

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                restCargados=2;
                System.out.println("FALLO2 "+error.getMessage());

            }
        }

        ) ;

        jrq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(jrq);

    }


    public void estableceRestaurantes(JSONArray datosRest){

        try{

            losRestaurantes=new Restaurantes[datosRest.length()];

            for(int i=0;i<losRestaurantes.length;i++) {

                losRestaurantes[i]=new Restaurantes();

                JSONObject object = datosRest.getJSONObject(i);

                losRestaurantes[i].nombre = object.getString("nombre_restaurante");
                losRestaurantes[i].poblacion = object.getString("poblacion");
                losRestaurantes[i].email = object.getString("email");
                losRestaurantes[i].codigo = object.getString("cod_restaurante");
                losRestaurantes[i].telefono = object.getInt("telefono");

                losRestaurantes[i].latitud = object.getString("latitud");
                losRestaurantes[i].longitud = object.getString("longitud");

                losRestaurantes[i].cN=Integer.parseInt(object.getString("cN"));
                losRestaurantes[i].cD=Integer.parseInt(object.getString("cD"));
                losRestaurantes[i].cP=Integer.parseInt(object.getString("cP"));
                losRestaurantes[i].fN=Integer.parseInt(object.getString("fN"));
                losRestaurantes[i].fD=Integer.parseInt(object.getString("fD"));

                losRestaurantes[i].cNP=Integer.parseInt(object.getString("cNP"));
                losRestaurantes[i].cDP=Integer.parseInt(object.getString("cDP"));
                losRestaurantes[i].fKarta=Integer.parseInt(object.getString("fkarta"));
                losRestaurantes[i].fDP=Integer.parseInt(object.getString("fDP"));

                losRestaurantes[i].tBordes=Integer.parseInt(object.getString("tBorde"));
                losRestaurantes[i].fBordes=Integer.parseInt(object.getString("fBorde"));

                losRestaurantes[i].contaComentario=Integer.parseInt(object.getString("conta_coment"));
                losRestaurantes[i].contaLike=Integer.parseInt(object.getString("conta_like"));

                losRestaurantes[i].permiteComentarios=Integer.parseInt(object.getString("puede_coment"));
                losRestaurantes[i].desdeDondeComentarios=Integer.parseInt(object.getString("dis_coment"));

                losRestaurantes[i].tipo_comida = object.getString("tipo_comida");
                losRestaurantes[i].detalle = object.getString("detalle");
                losRestaurantes[i].tags = object.getString("tags");
                losRestaurantes[i].logo = object.getString("logo");
                losRestaurantes[i].imagen_principal = object.getString("imagen_principal");

                losRestaurantes[i].online = object.getInt("online");
                losRestaurantes[i].actualizando = object.getInt("actualizando");


            }

        }catch (JSONException e){

            System.out.println("ERROR AL ESTABLECER RESTAURANTES: "+e.getMessage());

        }

    }


    public void guardaDatosAlergenos(JSONArray datos){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("ALERGENOS",datos.toString());

        System.out.println("GUARDA ALERGENOS");

        mieditor.apply();

    }


    public void estableceCiudad(JSONArray datosCiudad){

        try{

            lasCiudades=new Ciudades[datosCiudad.length()];

            for(int i=0;i<lasCiudades.length;i++) {

                lasCiudades[i]=new Ciudades();

                JSONObject object = datosCiudad.getJSONObject(i);

                lasCiudades[i].nombre_ciudad = object.getString("nombre_ciudad");

                lasCiudades[i].donde_ciudad = new Location(object.getString("nombre_ciudad"));
                lasCiudades[i].donde_ciudad.setLatitude(Double.parseDouble(object.getString("latitud")));
                lasCiudades[i].donde_ciudad.setLongitude(Double.parseDouble(object.getString("longitud")));

            }

        }catch (JSONException e){

            lasCiudades=new Ciudades[0];

        }

    }

    public void sindatos(){

        LayoutInflater inflador = this.getLayoutInflater();

        final View alertLayout = inflador.inflate(R.layout.mensaje_sindatos, null);

        Button salir=alertLayout.findViewById(R.id.salir_sindatos);
        TextView info=alertLayout.findViewById(R.id.infoSindatos);
        final ImageView icono=alertLayout.findViewById(R.id.icono_sindatos);

        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {

            float x=1.5f;
            float y=1.5f;
            @Override
            public void run() {

                icono.animate().scaleY(y);
                icono.animate().scaleX(x);

                if(x==1){x=1.5f;y=1.5f;}
                else{x=1;y=1;}

                handler.postDelayed(this,600);
            }
        },0);


        info.setText(Html.fromHtml(getResources().getString(R.string.nota_salir_sindatos)));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
                finish();


            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

    }


    public void compruebaPermisoGPS(){

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            //System.out.println("NO TIENE ACCESO A LA LOCALIZACION 2");

            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, 1222);
        }else{

            iniciaLakarta();
        }

    }

    public boolean compruebaConexion(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo estadoRed = connectivityManager.getActiveNetworkInfo();

        if (estadoRed == null || !estadoRed.isConnected()) {

            return false;

        }else{

            return true;
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

    private boolean compruebaNuevaVersion(){

        boolean resultado=false;
        String newVersion="";
        String numVersion="";

        try {
            Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packagename).timeout(3000).get();
            if (document != null) {
                //Log.d("updateAndroid", "Document: " + document);
                Elements element = document.getElementsContainingOwnText("Current Version");
                for (Element ele : element) {
                    if (ele.siblingElements() != null) {
                        Elements sibElemets = ele.siblingElements();
                        for (Element sibElemet : sibElemets) {
                            newVersion = sibElemet.text();
                        }
                    }
                }
            }

            if (!newVersion.equals("") && !newVersion.isEmpty()) {

                try {

                    numVersion=this.getPackageManager().getPackageInfo(packagename,0).versionName;

                    System.out.println("VERION ESTE: "+numVersion+" VERSION STORE: "+newVersion);

                    if(Double.parseDouble(numVersion)<Double.parseDouble(newVersion)){

                        resultado= true;

                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        return resultado;

    }

    public void cargaUser(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_USUARIO","0");
        alsesUser=guarda.getString("ALSES_USUARIO","0");
        alseskUser=guarda.getString("ALSESK_USUARIO","0");

        System.out.println("ALSES USUARIO GUARDADO "+alsesUser);

    }

    public void cargaEmpresa(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        userEmp=guarda.getString("USER_EMPRESA","0");
        alsesEmp=guarda.getString("ALSES_EMPRESA","0");
        alseskEmp=guarda.getString("ALSESK_EMPRESA","0");

        System.out.println("ALSES EMPRESA GUARDADO "+alsesEmp);

    }

}
