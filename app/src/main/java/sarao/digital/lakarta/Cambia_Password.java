package sarao.digital.lakarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cambia_Password extends AppCompatActivity {

    int tiempoEspera=1;
    int milisegundos=500;

    LocationManager mlocManager;
    String mensajeAlerta="";
    Toast mensajePop;
    EditText emailUsr,contraUsrAntigua,contraUsrNueva,contraUsrNuevaRpt,codigoUsr;
    TextView cambiar, tituloCambioPass, textoEmail, volver;
    ImageView iconoEmail, iconoContraAnterior, iconoContraNueva, iconoContraNuevaRepite;

    String contraNuevaEnviar,codigoEnviar;

    LinearLayout cajaCodigo, cajaRegistro, cajaEmail, cajaPassAnterior;

    boolean contraAntiguaOk=false,emailOk=false,contraNuevaOk=false;

    boolean enviarCodigo=false;

    boolean reiniciar;

    private String tipoCambio, quienCambia, emailQuien;

    private String user, pass, alses, alsesk;

    LayoutInflater inflador;

    Herramientas herramientas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cambia_password);

        mlocManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        emailUsr=findViewById(R.id.email);
        contraUsrAntigua=findViewById(R.id.contra_anterior);
        contraUsrNueva=findViewById(R.id.contra_nueva);
        contraUsrNuevaRpt=findViewById(R.id.contra_nueva_repite);
        codigoUsr=findViewById(R.id.codigo);
        tituloCambioPass=findViewById(R.id.titulo_cambio_pass);

        textoEmail=findViewById(R.id.texto_email);
        cajaEmail=findViewById(R.id.caja_email);

        cambiar=findViewById(R.id.cambiar);
        volver=findViewById(R.id.volver);

        iconoContraAnterior=findViewById(R.id.icono_contra_anterior);
        iconoContraNueva=findViewById(R.id.icono_contra_nueva);
        iconoContraNuevaRepite=findViewById(R.id.icono_contra_nueva_repite);
        iconoEmail=findViewById(R.id.icono_email);

        cajaCodigo=findViewById(R.id.caja_codigo);
        cajaRegistro=findViewById(R.id.caja_registro);
        cajaPassAnterior=findViewById(R.id.caja_pass_anterior);

        inflador=(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        herramientas=new Herramientas();


        Bundle miBundle=this.getIntent().getExtras();

        if(miBundle!=null) {

            tipoCambio = miBundle.getString("TIPOCAMBIOPASS");
            quienCambia = miBundle.getString("QUIENCAMBIAPASS");
            emailQuien = miBundle.getString("EMAILCAMBIOPASS");

        }else{

            tipoCambio="";
            quienCambia="";
            emailQuien="";
        }

        if(quienCambia.equals("usuario")){

            cargaUsuario();
        }else{

            cargaUsuarioEmpresa();
        }

        if(tipoCambio.equals("nueva")){

            cajaEmail.setVisibility(View.GONE);
            textoEmail.setVisibility(View.GONE);

            cambiar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false, cambiar);

                    if(contraUsrAntigua.getText().toString().equals(pass)) {

                        if (contraAntiguaOk && contraNuevaOk) {

                            contraNuevaEnviar = contraUsrNueva.getText().toString();


                            enviaNuevoPassword();

                        } else {

                            mensajeAlerta = "Datos incompletos";
                            ponAlerta();
                            activaBoton(true, cambiar);
                        }
                    }else{

                        mensajeAlerta = getResources().getString(R.string.contrasena_antigua_error);
                        iconoContraAnterior. setImageResource(R.drawable.delete);
                        ponAlerta();
                        activaBoton(true, cambiar);
                    }

                }
            });

        }else if(tipoCambio.equals("olvidado")){

            tituloCambioPass.setText(getResources().getString(R.string.olvidado_contrasena));
            cambiar.setText(getResources().getString(R.string.solicita_cambio));

            cajaPassAnterior.setVisibility(View.GONE);

            cambiar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false, cambiar);

                    if (!quienCambia.equals("")) {

                        if (!enviarCodigo) {

                            if (emailOk && contraNuevaOk) {

                                contraNuevaEnviar = contraUsrNueva.getText().toString();
                                emailQuien = emailUsr.getText().toString();

                                pideRecuperarPassword();

                            } else {

                                mensajeAlerta = "Datos incompletos";
                                ponAlerta();
                                activaBoton(true, cambiar);
                            }
                        } else {

                            if (codigoUsr.getText().toString().length() > 0) {

                                codigoEnviar = codigoUsr.getText().toString();
                                enviaRecuperaPassword();

                            } else {

                                mensajeAlerta = getResources().getString(R.string.codigo_incorrecto);
                                ponAlerta();
                                activaBoton(true, cambiar);

                            }

                        }

                    }else{

                        mensajeAlerta = getResources().getString(R.string.error_conexion);
                        ponAlerta();
                        activaBoton(true, cambiar);

                    }
                }

            });


        }else{

            finish();
        }

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activaBoton(false, volver);
                finish();

            }
        });

        cajaCodigo.setVisibility(View.GONE);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);



        emailUsr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>5){

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
                    iconoEmail.setImageResource(R.drawable.delete);
                    emailOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contraUsrAntigua.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length()>5){

                    iconoContraAnterior.setImageResource(R.drawable.ok);
                    contraAntiguaOk=true;


                }else{
                    iconoContraAnterior.setImageResource(R.drawable.delete);
                    contraAntiguaOk=false;

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contraUsrNueva.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                contraUsrNuevaRpt.setText("");
                contraNuevaOk=false;

                if(s.length()>5){

                    iconoContraNueva.setImageResource(R.drawable.ok);


                }else{
                    iconoContraNueva.setImageResource(R.drawable.delete);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contraUsrNuevaRpt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(contraUsrNueva.getText().toString().contentEquals(s) && s.length()>5){

                    iconoContraNuevaRepite.setImageResource(R.drawable.ok);
                    contraNuevaOk=true;

                }else{
                    iconoContraNuevaRepite.setImageResource(R.drawable.delete);
                    contraNuevaOk=false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onBackPressed() {

        if(enviarCodigo){

            preguntaSiSalir();
        }else{

            super.onBackPressed();
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

    private void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

    private void preguntaSiSalir(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        final Button salir=alertLayout.findViewById(R.id.pedido_guardar);
        final Button cancelar=alertLayout.findViewById(R.id.pedido_noguardar);

        salir.setText(getResources().getString(R.string.salir));
        cancelar.setText(getResources().getString(R.string.cancelar));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, salir);
                dialog.cancel();

                finish();

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, cancelar);

                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    public void activaEnviacodigo(){

        cajaCodigo.setVisibility(View.VISIBLE);
        cajaRegistro.setVisibility(View.GONE);
        cambiar.setText("ENVIAR CODIGO");
        enviarCodigo=true;

    }


    public void pideRecuperarPassword(){

        reiniciar=false;

        final AlertDialog enviando=esperandoEnvio();

        enviando.show();

        String url=this.getString(R.string.servidor_pide_recuperar_pass);

        RequestQueue rq= Volley.newRequestQueue(this);

        System.out.println("A ENVIAR: "+emailQuien+' '+quienCambia);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("email", emailQuien);
        parametros.put("quien", quienCambia);


        JSONObject parametrosEnvio = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RECIBIDO: "+response);

                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("ok")) {

                        mensajeAlerta="";
                        activaEnviacodigo();

                    }else if(jsonRespuesta.get(0).equals("no_email")){

                        mensajeAlerta = getResources().getString(R.string.email_no_existe);
                        iconoEmail.setImageResource(R.drawable.delete);

                    }else{

                        mensajeAlerta = getResources().getString(R.string.no_pudo_verificar_email);
                    }

                } catch (Exception e) {

                    System.out.println("ERROR RESPUESTA: "+e.getMessage());
                    mensajeAlerta = getResources().getString(R.string.no_pudo_verificar_email);

                }

                activaBoton(true, cambiar);
                esperaTiempo inicia = new esperaTiempo();

                inicia.execute(enviando);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR VOLEY "+error.getMessage());

                enviando.cancel();

                mensajeAlerta = getResources().getString(R.string.no_pudo_verificar_email);
                ponAlerta();
                activaBoton(true, cambiar);

            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);

    }

    public void enviaNuevoPassword(){

        final AlertDialog enviando=esperandoEnvio();

        enviando.show();

        reiniciar=false;

        String url=getResources().getString(R.string.servidor_cambia_password);

        RequestQueue rq= Volley.newRequestQueue(this);


        Map<String, String> parametros = new HashMap<>();
        parametros.put("user", user);
        parametros.put("al", herramientas.codiAlses(alses,alsesk));
        parametros.put("passNuevo", contraNuevaEnviar);
        parametros.put("quienCambia", quienCambia);

        JSONObject parametrosEnviar = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnviar, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA SERVIDOR "+response);

                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("error")) {

                        mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);

                    }else{

                        JSONArray jsonAlses = response.getJSONArray("alses");

                        if(quienCambia.equals("usuario")) {
                            herramientas.guardaAlsesUsuario(getApplicationContext(), herramientas.decodiAlses(jsonAlses.get(0).toString(), alsesk));
                        }else if(quienCambia.equals("empresa")){
                            herramientas.guardaAlsesEmpresa(getApplicationContext(), herramientas.decodiAlses(jsonAlses.get(0).toString(), alsesk));
                        }


                        if (jsonRespuesta.get(0).equals("ok")) {

                            mensajeAlerta = getResources().getString(R.string.contrasena_cambiada);

                            reiniciar=true;

                        } else if(jsonRespuesta.get(0).equals("nok")){

                            mensajeAlerta = getResources().getString(R.string.contrasena_error);

                        }else if(jsonRespuesta.get(0).equals("no_email")){

                            mensajeAlerta = getResources().getString(R.string.email_no_existe);

                        }else {

                            mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);

                        }

                    }



                    activaBoton(true, cambiar);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                } catch (Exception e) {


                    mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);
                    activaBoton(true, cambiar);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR DEL VOLEY: "+error.getMessage());

                mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);
                activaBoton(true, cambiar);
                ponAlerta();

            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);
    }

    public void enviaRecuperaPassword(){

        final AlertDialog enviando=esperandoEnvio();

        enviando.show();

        reiniciar=false;

        String url=getResources().getString(R.string.servidor_recupera_password);

        RequestQueue rq= Volley.newRequestQueue(this);


        Map<String, String> parametros = new HashMap<>();
        parametros.put("email", emailQuien);
        parametros.put("codigo", codigoEnviar);
        parametros.put("passNuevo", contraNuevaEnviar);
        parametros.put("quien", quienCambia);

        JSONObject parametrosEnviar = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnviar, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA SERVIDOR: "+response);
                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("ok")) {

                        mensajeAlerta = getResources().getString(R.string.contrasena_cambiada);

                        reiniciar=true;

                    } else if(jsonRespuesta.get(0).equals("nok")){

                        mensajeAlerta = getResources().getString(R.string.codigo_incorrecto);

                    }else {

                        mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);

                    }

                    activaBoton(true, cambiar);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                } catch (Exception e) {


                    mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);
                    activaBoton(true, cambiar);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR DEL VOLEY: "+error.getMessage());

                enviando.cancel();
                mensajeAlerta = getResources().getString(R.string.contrasena_no_cambiada);
                activaBoton(true, cambiar);
                ponAlerta();

            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);
    }

    private AlertDialog esperandoEnvio(){

        View alertLayout = inflador.inflate(R.layout.emerg_enviando_datos, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;

    }

    private class esperaTiempo extends AsyncTask<AlertDialog,Integer,String> {

        AlertDialog alerta;

        @Override
        protected String doInBackground(AlertDialog... alerta) {

            this.alerta=alerta[0];
            int contador=tiempoEspera;

            while(contador>0) {

                try {
                    Thread.sleep(milisegundos);
                } catch (Exception e) {
                    System.out.println(e);
                }
                contador--;

            }

            return "ok";
        }

        protected void onPostExecute(String resultado) {

            alerta.cancel();

            if(!mensajeAlerta.equals("")) {
                ponAlerta();
            }

            if(reiniciar){

                finish();
            }

        }
    }

    public void cargaUsuario(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_USUARIO","0");
        pass=guarda.getString("PASS_USUARIO","0");
        alses=guarda.getString("ALSES_USUARIO","0");
        alsesk=guarda.getString("ALSESK_USUARIO","0");

    }

    public void cargaUsuarioEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        pass=guarda.getString("PASS_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");


    }

}

