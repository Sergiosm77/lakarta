package sarao.digital.lakarta;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

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

public class NuevoUsuario {

    // tiempo espera al actualizar datos

    int tiempoEspera=1;
    int milisegundos=500;

    // ---------------------------

    Activity actividad;
    private Toast mensajePop;
    String mensajeAlerta="";
    final private Context contexto;

    boolean reiniciar=false;

    boolean enviarCodigo=false;

    final private LayoutInflater inflater;

    public NuevoUsuario(Activity actividad, Context contexto, LayoutInflater inflater){

        this.actividad=actividad;
        this.contexto=contexto;
        this.inflater=inflater;

        mensajePop = Toast.makeText(contexto, mensajeAlerta, Toast.LENGTH_SHORT);

    }

    private AlertDialog esperandoEnvio(){

        View alertLayout = inflater.inflate(R.layout.emerg_enviando_datos, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;

    }

    public void enviaNuevaEmpresa(final String nombreUsu,
                                  final String nombreRest,
                                  final String contacto,
                                  final String poblacion,
                                  final String provincia,
                                  final String email,
                                  final String direccion,
                                  final String telefono,
                                  final String latitud,
                                  final String longitud,
                                  final ImageView iconoUsuario,
                                  final ImageView iconoTelefono,
                                  final ImageView iconoEmail,
                                  final ScrollView scroll,
                                  final TextView boton,
                                  final View cajaDatos,
                                  final View cajaConfirmar){

        final AlertDialog enviando=esperandoEnvio();

        System.out.println("ENVIA DATOS A NUEVA EMPRESA");

        reiniciar=false;
        enviarCodigo=false;

        enviando.show();

        String url=contexto.getString(R.string.servidor_comprueba_nueva_empresa);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("nombreUsu", nombreUsu);
        parametros.put("nombreRest", nombreRest);
        parametros.put("contacto", contacto);
        parametros.put("poblacion", poblacion);
        parametros.put("provincia", provincia);
        parametros.put("email", email);
        parametros.put("direccion", direccion);
        parametros.put("telefono", telefono);
        parametros.put("latitud", latitud);
        parametros.put("longitud", longitud);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RECIBIDO: "+response);

                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("ok")) {

                        activaEnviacodigo(cajaConfirmar, cajaDatos, boton);
                        enviarCodigo=true;
                        mensajeAlerta="";
                        esperaTiempo inicia = new esperaTiempo();

                        inicia.execute(enviando);

                    } else if(jsonRespuesta.get(0).equals("existe_usuario")){

                        scroll.scrollTo(0,0);

                        mensajeAlerta = contexto.getString(R.string.usuario_existe);
                        iconoUsuario.setImageResource(R.drawable.delete);
                        activaBoton(true, boton);
                        esperaTiempo inicia = new esperaTiempo();

                        inicia.execute(enviando);

                    } else if(jsonRespuesta.get(0).equals("existe_email")){

                        scroll.scrollTo(0,0);

                        mensajeAlerta = contexto.getString(R.string.email_existe);
                        iconoEmail.setImageResource(R.drawable.delete);
                        activaBoton(true, boton);
                        esperaTiempo inicia = new esperaTiempo();

                        inicia.execute(enviando);

                    }else if(jsonRespuesta.get(0).equals("existe_telefono")){

                        scroll.scrollTo(0,0);

                        mensajeAlerta = contexto.getString(R.string.telefono_existe);
                        iconoTelefono.setImageResource(R.drawable.delete);
                        activaBoton(true, boton);
                        esperaTiempo inicia = new esperaTiempo();

                        inicia.execute(enviando);

                    }else{

                        mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                        activaBoton(true, boton);
                        esperaTiempo inicia = new esperaTiempo();

                        inicia.execute(enviando);
                    }

                } catch (Exception e) {

                    System.out.println("ERROR RESPUESTA: "+e.getMessage());
                    mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                   ponAlerta();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR VOLEY: "+error.getMessage());

                enviando.cancel();

                mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                ponAlerta();
                activaBoton(true, boton);

            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);

    }

    public void activaEmpresa(final String email,
                              final String nombreUsu,
                                      final String codigo,
                                      final TextView boton){

        final AlertDialog enviando=esperandoEnvio();

        System.out.println("PARA ACTIVAR: "+codigo+" "+email);

        reiniciar=false;

        enviando.show();

        String url=contexto.getString(R.string.servidor_activa_empresa);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("email", email);
        parametros.put("nombreUser", nombreUsu);
        parametros.put("codigo", codigo);

        JSONObject parametrosEnvio = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RECIBIDO: "+response);

                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("ok")) {

                        enviando.cancel();
                       altaCompletada();

                    } else {

                        mensajeAlerta = contexto.getString(R.string.codigo_incorrecto);
                        activaBoton(true, boton);
                        esperaTiempo inicia = new esperaTiempo();

                        inicia.execute(enviando);
                    }

                } catch (Exception e) {

                    System.out.println("ERROR RESPUESTA: "+e.getMessage());
                    mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                    activaBoton(true, boton);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR VOLEY");

                enviando.cancel();

                mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                ponAlerta();
                activaBoton(true, boton);

            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);

    }

    public void enviaNuevoUsuario(final String nombre,
                                  final String email,
                                  final String avatar,
                                  final String contra,
                                  final ImageView iconoNombre,
                                  final ImageView iconoEmail,
                                  final ScrollView scroll,
                                  final TextView boton,
                                  final View cajaCodigo,
                                  final View cajaRegistro,
                                  final LinearLayout fondoAvatares,
                                  final ImageView chica,
                                  final ImageView chico){

        final AlertDialog enviando=esperandoEnvio();

        reiniciar=false;

        enviando.show();

        String url=contexto.getString(R.string.servidor_verifica_usuario);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("nombre", nombre);
        parametros.put("email", email);
        parametros.put("avatar", avatar);
        parametros.put("contra", contra);

        JSONObject parametrosEnvio = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RECIBIDO: "+response);

                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("ok")) {

                        mensajeAlerta = "";
                        activaEnviacodigo(cajaCodigo, cajaRegistro, boton);
                        fondoAvatares.setEnabled(false);
                        chico.setEnabled(false);
                        chica.setEnabled(false);

                    } else if(jsonRespuesta.get(0).equals("existe_user")){

                        scroll.scrollTo(0,0);

                        mensajeAlerta = contexto.getString(R.string.usuario_existe);
                        iconoNombre.setImageResource(R.drawable.delete);

                    }else if(jsonRespuesta.get(0).equals("existe_email")){

                        scroll.scrollTo(0,0);

                        mensajeAlerta = contexto.getString(R.string.email_existe);
                        iconoEmail.setImageResource(R.drawable.delete);

                    }else{

                        mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                    }

                } catch (Exception e) {

                    System.out.println("ERROR RESPUESTA: "+e.getMessage());
                    mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);

                }

                activaBoton(true, boton);
                esperaTiempo inicia = new esperaTiempo();

                inicia.execute(enviando);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR VOLEY "+error.getMessage());

                enviando.cancel();

                mensajeAlerta = contexto.getString(R.string.no_pudo_usuario);
                ponAlerta();
                activaBoton(true, boton);

            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);

    }

    public void activaUsuario(final String nombre,
                              final String codigo,
                              final View boton){

        final AlertDialog enviando=esperandoEnvio();

        enviando.show();

        reiniciar=false;

        String url=contexto.getString(R.string.servidor_activa_usuario);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        System.out.println("ENVIANDO: "+nombre+" "+codigo);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("nombre", nombre);
        parametros.put("codigo", codigo);

        JSONObject parametrosEnviar = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnviar, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONArray jsonRespuesta = response.getJSONArray("valor");

                    if (jsonRespuesta.get(0).equals("ok")) {

                        mensajeAlerta = contexto.getString(R.string.alta_completada);

                        reiniciar=true;

                    } else {

                        mensajeAlerta = contexto.getString(R.string.codigo_incorrecto);

                    }

                    activaBoton(true, boton);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                } catch (Exception e) {


                    mensajeAlerta = contexto.getString(R.string.codigo_incorrecto);
                    activaBoton(true, boton);
                    esperaTiempo inicia = new esperaTiempo();

                    inicia.execute(enviando);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR DEL VOLEY: "+error.getMessage());

                mensajeAlerta = contexto.getString(R.string.codigo_incorrecto);
                activaBoton(true, boton);
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

    public void descartaUsuario(final String nombre,
                              final View boton){


        String url=contexto.getString(R.string.servidor_descarta_usuario);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("nombre", nombre);

        JSONObject parametrosEnviar = new JSONObject(parametros);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,parametrosEnviar, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                    actividad.finish();
                    activaBoton(true, boton);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR DEL VOLEY: "+error.getMessage());
                actividad.finish();

                activaBoton(true, boton);


            }
        }

        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(request);
    }

    public void activaEnviacodigo(View cajaCodigo, View cajaRegistro, TextView registrar){

        cajaCodigo.setVisibility(View.VISIBLE);
        cajaRegistro.setVisibility(View.GONE);
        registrar.setText(contexto.getResources().getString(R.string.enviar_codigo));
        enviarCodigo=true;


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

                cerrarActivity();
            }

        }
    }

    private class esperaTiempoSinReinicio extends AsyncTask<AlertDialog,Integer,String> {

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

            mensajeAlerta=contexto.getString(R.string.datos_cambiados);
            ponAlerta();


        }
    }

    private void ponAlerta(){

        if(!mensajeAlerta.equals("")) {

            try{

            if (!mensajePop.getView().isShown()) {

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

    public void cerrarActivity(){

        try {
            actividad.finish();
        }catch (Exception e){


        }

    }

    public void guardaLoginUsuario(String user, String pass, String avatar){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USER_USUARIO", user);
        mieditor.putString("PASS_USUARIO", pass);
        mieditor.putString("AVATAR_USUARIO", avatar);

        mieditor.apply();


    }

    private void altaCompletada(){

        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        final Button aceptar=alertLayout.findViewById(R.id.pedido_guardar);
        final Button cancelar=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView info=alertLayout.findViewById(R.id.pregunta_alerta);
        ImageView icono=alertLayout.findViewById(R.id.icono_alerta);

        aceptar.setText(contexto.getResources().getString(R.string.aceptar));
        info.setText(contexto.getResources().getString(R.string.alta_empresa_completada));

        icono.setImageResource(R.drawable.registro_ok);

        cancelar.setVisibility(View.GONE);

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);

        alert.setView(alertLayout);

        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, aceptar);

                dialog.cancel();

                actividad.finish();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

}
