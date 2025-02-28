package sarao.digital.lakarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Server_ComentLikes {

    Context contexto;
    LayoutInflater inflador;
    int tiempoEspera=1;
    int milisegundos=500;
    private final Toast mensajePop;
    String mensajeAlerta="";
    String recibido="no";
    String alses, alsesEmp;
    String alsesk,alseskEmp, nombreEmpresa;

    Herramientas herramientas;

    JSONArray losComentarios, lasRespuestas;


    public Server_ComentLikes(Context contexto, LayoutInflater inflador){

        this.contexto=contexto;
        this.inflador=inflador;

        mensajePop = Toast.makeText(contexto, mensajeAlerta, Toast.LENGTH_SHORT);

        herramientas=new Herramientas();

        cargaEmpresaGuardado();

    }

    public void enviaComentarioEmpresa(final String user,
                                final String comentario,
                                final String tipoComent,
                                String codRest,
                                String codCom,
                                final EditText cajaComentario,
                                final LinearLayout contenedorComentarios,
                                final LinearLayout contenedorRespuestas,
                                final TextView botonCancelar,
                                final NestedScrollView scroll,
                                final int positionScroll
                                ){

        final AlertDialog enviando=esperandoEnvio();

        RequestQueue rq= Volley.newRequestQueue(contexto);

        String url = contexto.getString(R.string.servidor_envia_comentario_empresa);

        enviando.show();

        cargaAlsesEmpresa();

        String alsescod=herramientas.codiAlses(alsesEmp,alseskEmp);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("comentario", comentario);
        parametros.put("tipoComent", tipoComent);
        parametros.put("codRest", codRest);
        parametros.put("codCom", codCom);
        parametros.put("u", user);
        parametros.put("al", alsescod);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA ENVIO COMENTARIO: "+response);

                try {

                    JSONArray respuestaServer = response.getJSONArray("valor");

                    if(respuestaServer.get(0).equals("error")){

                        ponAlerta(mensajePop,contexto.getString(R.string.datos_no_actualizados));

                        enviando.cancel();

                    }else {

                        JSONArray jsonAlses = response.getJSONArray("alses");
                        JSONArray jsonLogo = response.getJSONArray("logo");


                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(jsonAlses.get(0).toString(),alseskEmp));

                        System.out.println("GUARDA ALSES: "+herramientas.decodiAlses(jsonAlses.get(0).toString(),alseskEmp));

                        if ( response.getJSONArray("hecho").get(0).equals("ok")) {

                            esperaTiempoSinReinicio inicia = new esperaTiempoSinReinicio();

                            cajaComentario.setText("");
                            String textoRespuesta = contexto.getResources().getString(R.string.escribe_comentario);
                            cajaComentario.setHint(textoRespuesta);
                            botonCancelar.setVisibility(View.GONE);
                            if (tipoComent.equals("normal")) {

                                View comentarios = inflador.inflate(R.layout.unidad_comentario, null);

                                TextView usuario = comentarios.findViewById(R.id.nombre_usuario);
                                TextView elcomentario = comentarios.findViewById(R.id.comentario_usuario);
                                TextView tituloRespuestas = comentarios.findViewById(R.id.titulo_respuestas);
                                ImageView avatarImagen = comentarios.findViewById(R.id.avatar);

                                    Glide.with(contexto)
                                            .load(jsonLogo.get(0).toString())
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .error(R.drawable.user)
                                            .into(avatarImagen);

                                usuario.setText(nombreEmpresa);
                                usuario.setTextColor(Color.CYAN);
                                elcomentario.setText(comentario);
                                tituloRespuestas.setVisibility(View.GONE);

                                contenedorComentarios.addView(comentarios, 0);

                                scroll.scrollTo(0, 0);

                            } else {

                                View cajaComentario = inflador.inflate(R.layout.unidad_comentario_respuesta, null);

                                TextView usuario = cajaComentario.findViewById(R.id.nombre_usuario);
                                TextView elcomentario = cajaComentario.findViewById(R.id.comentario_usuario);
                                ImageView avatarImagen = cajaComentario.findViewById(R.id.avatar);

                                Glide.with(contexto)
                                        .load(jsonLogo.get(0).toString())
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .error(R.drawable.user)
                                        .into(avatarImagen);

                                usuario.setText(nombreEmpresa);
                                usuario.setTextColor(Color.CYAN);
                                elcomentario.setText(comentario);

                                contenedorRespuestas.addView(cajaComentario);

                                scroll.scrollTo(0, positionScroll);

                            }

                            inicia.execute(enviando);

                        } else if ( response.getJSONArray("hecho").get(0).equals("nopuede")) {

                            ponAlerta(mensajePop, contexto.getString(R.string.sin_permiso_comentar));

                            enviando.cancel();

                        }else{

                            ponAlerta(mensajePop, contexto.getString(R.string.datos_no_actualizados));

                            enviando.cancel();

                        }
                    }

                } catch (Exception e) {

                    ponAlerta(mensajePop,contexto.getString(R.string.datos_no_actualizados));

                    System.out.println("ERROR "+e.getMessage());

                    enviando.cancel();

                }

            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                
                enviando.cancel();

                ponAlerta(mensajePop,contexto.getString(R.string.error_conexion));

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public void enviaComentario(final String user,
                                final String avatar,
                                final String comentario,
                                final String tipoComent,
                                String codRest,
                                String codCom,
                                final EditText cajaComentario,
                                final LinearLayout contenedorComentarios,
                                final LinearLayout contenedorRespuestas,
                                final TextView botonCancelar,
                                final NestedScrollView scroll,
                                final int positionScroll
    ){

        final AlertDialog enviando=esperandoEnvio();

        RequestQueue rq= Volley.newRequestQueue(contexto);

        String url = contexto.getString(R.string.servidor_envia_comentario);

        enviando.show();

        cargaAlsesUser();

        String alsescod=herramientas.codiAlses(alses,alsesk);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("comentario", comentario);
        parametros.put("tipoComent", tipoComent);
        parametros.put("codRest", codRest);
        parametros.put("codCom", codCom);
        parametros.put("u", user);
        parametros.put("al", alsescod);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA ENVIO COMENTARIO: "+response);

                try {

                    JSONArray respuestaServer = response.getJSONArray("valor");

                    if(respuestaServer.get(0).equals("error")){

                        ponAlerta(mensajePop,contexto.getString(R.string.datos_no_actualizados));

                        enviando.cancel();

                    }else {

                        JSONArray jsonAlses = response.getJSONArray("alses");

                        guardaAlsesUser(herramientas.decodiAlses(jsonAlses.get(0).toString(), alsesk));

                        if (respuestaServer.get(0).equals("ok")) {

                            esperaTiempoSinReinicio inicia = new esperaTiempoSinReinicio();

                            cajaComentario.setText("");
                            String textoRespuesta = contexto.getResources().getString(R.string.escribe_comentario);
                            cajaComentario.setHint(textoRespuesta);
                            botonCancelar.setVisibility(View.GONE);
                            if (tipoComent.equals("normal")) {

                                View comentarios = inflador.inflate(R.layout.unidad_comentario, null);

                                TextView usuario = comentarios.findViewById(R.id.nombre_usuario);
                                TextView elcomentario = comentarios.findViewById(R.id.comentario_usuario);
                                TextView tituloRespuestas = comentarios.findViewById(R.id.titulo_respuestas);
                                ImageView avatarImagen = comentarios.findViewById(R.id.avatar);
                                CardView fondoAvatar = comentarios.findViewById(R.id.fondo_avatar);

                                if (avatar.startsWith("chico")) {

                                    avatarImagen.setImageResource(R.drawable.boy);

                                    avatarImagen.setColorFilter((Integer.parseInt(avatar.substring(5, 14)) - 1000000000));
                                    fondoAvatar.setCardBackgroundColor(Integer.parseInt(avatar.substring(14)) - 1000000000);

                                } else if (avatar.startsWith("chica")) {

                                    avatarImagen.setImageResource(R.drawable.girl);
                                    avatarImagen.setColorFilter((Integer.parseInt(avatar.substring(5, 14)) - 1000000000));
                                    fondoAvatar.setCardBackgroundColor(Integer.parseInt(avatar.substring(14)) - 1000000000);

                                }

                                usuario.setText(user);
                                elcomentario.setText(comentario);
                                tituloRespuestas.setVisibility(View.GONE);

                                contenedorComentarios.addView(comentarios, 0);

                                scroll.scrollTo(0, 0);

                            } else {

                                View cajaComentario = inflador.inflate(R.layout.unidad_comentario_respuesta, null);

                                TextView usuario = cajaComentario.findViewById(R.id.nombre_usuario);
                                TextView elcomentario = cajaComentario.findViewById(R.id.comentario_usuario);
                                ImageView avatarImagen = cajaComentario.findViewById(R.id.avatar);
                                CardView fondoAvatarResp = cajaComentario.findViewById(R.id.fondo_avatar);

                                if (avatar.startsWith("chico")) {

                                    avatarImagen.setImageResource(R.drawable.boy);

                                    avatarImagen.setColorFilter((Integer.parseInt(avatar.substring(5, 14)) - 1000000000));
                                    fondoAvatarResp.setCardBackgroundColor(Integer.parseInt(avatar.substring(14)) - 1000000000);


                                } else if (avatar.startsWith("chica")) {

                                    avatarImagen.setImageResource(R.drawable.girl);
                                    avatarImagen.setColorFilter((Integer.parseInt(avatar.substring(5, 14)) - 1000000000));
                                    fondoAvatarResp.setCardBackgroundColor(Integer.parseInt(avatar.substring(14)) - 1000000000);

                                }

                                usuario.setText(user);
                                elcomentario.setText(comentario);

                                contenedorRespuestas.addView(cajaComentario);

                                scroll.scrollTo(0, positionScroll);

                            }

                            inicia.execute(enviando);

                        } else if (respuestaServer.get(0).equals("nopuede")) {

                            ponAlerta(mensajePop, contexto.getString(R.string.sin_permiso_comentar));

                            enviando.cancel();

                        }else{

                            ponAlerta(mensajePop, contexto.getString(R.string.datos_no_actualizados));

                            enviando.cancel();

                        }
                    }

                } catch (Exception e) {

                    ponAlerta(mensajePop,contexto.getString(R.string.datos_no_actualizados));

                    enviando.cancel();

                }

            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                enviando.cancel();

                ponAlerta(mensajePop,contexto.getString(R.string.error_conexion));

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public void enviaLike(final String user,
                          String codRest,
                          final ImageView botonLike,
                          final String ponQuita

    ){

        RequestQueue rq= Volley.newRequestQueue(contexto);

        System.out.println("ENVIANDO PARA LIKE: "+user+" "+codRest+" "+ponQuita);

        String url = contexto.getString(R.string.servidor_pon_like);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("codRest", codRest);
        parametros.put("u", user);
        parametros.put("ponQuita", ponQuita);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA ENVIO LIKE: "+response);

                try {

                    JSONArray respuestaServer = response.getJSONArray("valor");

                    if (!respuestaServer.get(0).equals("ok")) {



                    }

                    activaBoton(true, botonLike);

                } catch (Exception e) {

                    ponAlerta(mensajePop,contexto.getString(R.string.datos_no_actualizados));


                    activaBoton(true, botonLike);


                }

            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                ponAlerta(mensajePop,contexto.getString(R.string.error_conexion));

                activaBoton(true, botonLike);

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public void recibeComentarios(String codRest){

        recibido="no";

        System.out.println("RESTAURANTE A RECIBIR: "+codRest);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        String url = contexto.getString(R.string.servidor_recibe_comentarios);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("codRest", codRest);


        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONArray respuestaServer = response.getJSONArray("valor");

                    if (respuestaServer.get(0).equals("ok")) {

                        //cargaComentarios(response.getJSONArray("comentarios"));

                        losComentarios=response.getJSONArray("comentarios");
                        lasRespuestas=response.getJSONArray("respuestas");

                        recibido="ok";

                    } else if(respuestaServer.get(0).equals("vacio")){

                        recibido="vacio";


                    }else{

                        recibido="error";
                    }

                } catch (Exception e) {

                  recibido="error";

                }

            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

               recibido="error";

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public AlertDialog esperandoEnvio(){

        View alertLayout = inflador.inflate(R.layout.emerg_enviando_datos, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;

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

        }
    }

    private void ponAlerta(Toast mensajePop, String mensajeAlerta){


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

    public void guardaComentarios(JSONArray comentarios){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("COMENTARIOS",comentarios.toString());

        System.out.println("GUARDA COMENTARIOS");

        mieditor.apply();


    }

    public void guardaAlsesUser(String alses){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("ALSES_USUARIO", alses);

        mieditor.apply();

    }

    public void cargaAlsesUser(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        alses=guarda.getString("ALSES_USUARIO","0");
        alsesk=guarda.getString("ALSESK_USUARIO","0");

    }

    public void cargaAlsesEmpresa(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        alsesEmp=guarda.getString("ALSES_EMPRESA","0");
        alseskEmp=guarda.getString("ALSESK_EMPRESA","0");

    }

    public void cargaEmpresaGuardado(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        try {

            String valor=guarda.getString("EMPRESA_GUARDADA", "0");

            if(valor!=null) {
                JSONObject objectUser = new JSONObject(valor);

                nombreEmpresa = objectUser.getString("nombre");
            }else{

                nombreEmpresa="";
            }


        }catch (Exception e){

            System.out.println("ERROR AL CARGAR USUARIO: "+e.getMessage());

        }

    }



}
