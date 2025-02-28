package sarao.digital.lakarta;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class politica_privacidad extends AppCompatActivity {

    private TextView textoPrivacidad, reintentarCarga, salirSinLeer;
    private Button aceptar, declinar;
    private ProgressBar barraProgreso;
    LinearLayout panel;

    JSONArray json_politica;

    private String respuesta, tipoPolitica;

    private Toast mensajePop;
    private String mensajeAlerta="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_politica_privacidad);

       textoPrivacidad=findViewById(R.id.texto_privacidad);
       reintentarCarga=findViewById(R.id.reintentar_carga);
       salirSinLeer=findViewById(R.id.salir);

       aceptar=findViewById(R.id.aceptar);
       declinar=findViewById(R.id.declinar);

       barraProgreso=findViewById(R.id.barraprogreso);

       panel=findViewById(R.id.panel_politica);

       panel.setVisibility(View.GONE);
       reintentarCarga.setVisibility(View.GONE);

       mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        Bundle miBundle=this.getIntent().getExtras();

        if(miBundle!=null) {

            tipoPolitica = miBundle.getString("POLITICA");

        }else{

            tipoPolitica="";
        }

       reintentarCarga.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               System.out.println("REINTENTA LA CARGA");

               reintentarCarga.setVisibility(View.GONE);
               barraProgreso.setVisibility(View.VISIBLE);

               cargaPanel iniciaTodo=new cargaPanel();

               iniciaTodo.execute();

           }
       });

        if(tipoPolitica.equals("menu_inicio")){

            aceptar.setVisibility(View.GONE);
            declinar.setVisibility(View.GONE);
            salirSinLeer.setText(getResources().getString(R.string.salir));
        }

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tipoPolitica.equals("politica_usuario")){

                    Registra_Usuario.iconoPolitica.setImageResource(R.drawable.ok);
                    Registra_Usuario.politicaOk=true;

                }else if(tipoPolitica.equals("politica_empresa")){

                    Registra_Empresa.iconoPolitica.setImageResource(R.drawable.ok);
                    Registra_Empresa.politicaOk=true;
                }



               finish();

            }
        });

        declinar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tipoPolitica.equals("politica_usuario")){

                    Registra_Usuario.iconoPolitica.setImageResource(R.drawable.delete);
                    Registra_Usuario.politicaOk=false;

                }else if(tipoPolitica.equals("politica_empresa")){

                    Registra_Empresa.iconoPolitica.setImageResource(R.drawable.delete);
                    Registra_Empresa.politicaOk=false;
                }

               finish();

            }
        });

        salirSinLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });


       cargaPanel iniciaTodo=new cargaPanel();

        iniciaTodo.execute();


    }

    private class cargaPanel extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            barraProgreso.setVisibility(View.VISIBLE);

        }

        @Override

        protected String doInBackground(String... strings) {

            if(compruebaConexion()) {

                recibePolitica("politica_usuario");

                int contador = 0;

                while (respuesta.equals("no") && contador < 10) {

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador++;

                }

                return respuesta;
            }else{

                return "sinconexion";
            }

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);

            System.out.println("RESPUESTA USUARIO: "+resultado);

            if(resultado.equals("ok")){

                cargaPantalla();


            }else if(resultado.equals("sinconexion")){


                mensajeAlerta = getString(R.string.sin_internet);
                ponAlerta();

                reintentarCarga.setVisibility(View.VISIBLE);


            }else if(resultado.equals("nok")){

                reintentarCarga.setVisibility(View.VISIBLE);


            }else{

                reintentarCarga.setVisibility(View.VISIBLE);

                mensajeAlerta = getString(R.string.error_conexion);
                ponAlerta();

            }

        }

    }

    public void recibePolitica(String tipo){

        respuesta="no";

        String url=this.getString(R.string.servidor_politica_privacidad);

        try {

            Map<String, String> parametros = new HashMap<>();
            parametros.put("tipo", tipo);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            RequestQueue rq = Volley.newRequestQueue(this);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (response != null) {

                                    JSONArray json_respuesta = response.getJSONArray("valor");

                                    if(json_respuesta.get(0).equals("ok")){

                                        json_politica = response.getJSONArray("politica");

                                        respuesta="ok";
                                    }else if(json_respuesta.get(0).equals("nok")){

                                        respuesta="nok";

                                    }else{

                                        respuesta="error";
                                    }


                                } else {

                                    respuesta = "error";

                                }

                            } catch (JSONException e) {

                                System.out.println("FALLO EN CONEXION: "+e.getMessage());

                                respuesta="error2";

                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("HAY UN PROBLEMA 1: " + error.getMessage());

                        }
                    });

            jrq.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            rq.add(jrq);

        }catch (Exception e){

            respuesta="error3";

        }

    }

    private void cargaPantalla(){

        if(json_politica!=null && json_politica.length()>0){

            panel.setVisibility(View.VISIBLE);

            try {

                JSONObject object = json_politica.getJSONObject(0);

                String elTexto= object.getString("contenido");

                //textoPrivacidad.setText(elTexto);

                textoPrivacidad.setText(Html.fromHtml(elTexto));


            }catch (Exception e){

                reintentarCarga.setVisibility(View.VISIBLE);
                panel.setVisibility(View.GONE);

            }


        }else{

            reintentarCarga.setVisibility(View.VISIBLE);
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

    private void ponAlerta(){


        try {
            if (!mensajePop.getView().isShown()) {

                mensajePop.setText(mensajeAlerta);

                mensajePop.setGravity(Gravity.CENTER, 0, 0);
                //TextView mensaje = mensajePop.getView().findViewById(android.R.id.message);
                //mensaje.setGravity(Gravity.CENTER);

                mensajePop.show();

            }

        }catch (Exception e){

            mensajePop.setText(mensajeAlerta);

            mensajePop.show();
        }

    }
}