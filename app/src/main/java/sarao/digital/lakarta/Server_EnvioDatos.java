package sarao.digital.lakarta;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadStatusDelegate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Server_EnvioDatos {

    // tiempo espera al actualizar datos

    int tiempoEspera=1;
    int milisegundos=500;

    // ------------------------------------------------------------

    private Activity actividad;
    private Toast mensajePop;
    final private Context contexto;
    final private LayoutInflater inflater;

    private String mensajeAlerta="";

    Herramientas herramientas;
    ContentResolver resolver;

    //WeakReference<Context> weakContext;

    static File file;
    Uri imageuri;
    String imagenEnvio;

    Bundle miBundle;

    public Server_EnvioDatos(Bundle miBundle, Activity actividad, Context contexto, LayoutInflater inflater){

        this.miBundle=miBundle;
        this.actividad=actividad;
        this.contexto=contexto;
        this.inflater=inflater;

        herramientas=new Herramientas();

        //weakContext = new WeakReference<>(contexto);

        mensajePop = Toast.makeText(contexto, "", Toast.LENGTH_SHORT);

    }

    public void enviaCambiosEmpresaLogo(Uri miPath, String user, final String alses, final String alsesk, final ImageView logoRest, String linkLogo){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        enviando.show();

        String url=contexto.getString(R.string.servidor_cambia_logo_empresa);

        try {

            int contador=tiempoEspera;

            while(contador>0) {

                try {
                    Thread.sleep(milisegundos);
                } catch (Exception e) {
                    System.out.println(e);
                }
                contador--;

            }

            final String uploadId = UUID.randomUUID().toString();

            String nombreLogo;

            if(linkLogo.equals("")){

                nombreLogo="nada";
            }else{

                nombreLogo=linkLogo;
            }

            reducirImagen(MediaStore.Images.Media.getBitmap(contexto.getContentResolver(), miPath));

            System.out.println("MIS ALSES "+alses+" "+alsesk);

            String alsesEnvio=herramientas.codiAlses(alses, alsesk);

            new MultipartUploadRequest(contexto.getApplicationContext(), uploadId, url)
                    .addFileToUpload(imagenEnvio, "imagen")
                    .addParameter("nombreImagen", nombreLogo)
                    .addParameter("u", user)
                    .addParameter("al", alsesEnvio)
                    .setMaxRetries(2)
                    .setUtf8Charset()
                    .setDelegate(new UploadStatusDelegate() {

                        @Override
                        public void onProgress(UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(UploadInfo uploadInfo, Exception e) {

                            System.out.println("ERROR: "+e.getMessage());

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.logo_no_cambiado);
                            ponAlerta();


                        }

                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {

                            borraTemporales();

                            try {

                                JSONArray valor = new JSONObject(serverResponse.getBodyAsString()).getJSONArray("valor");

                                if(valor.get(0).equals("ok")){

                                    String nuevoAlses = new JSONObject(serverResponse.getBodyAsString()).getJSONArray("alses").get(0).toString();

                                    herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                                    String nuevoLogo = new JSONObject(serverResponse.getBodyAsString()).getJSONArray("nuevologo").get(0).toString();

                                    if(!nuevoLogo.equals("nok")){

                                        mensajeAlerta=contexto.getString(R.string.logo_cambiado);

                                        Menu_Empresa.miRestaurante.logo=nuevoLogo;

                                        Glide.with(contexto)
                                                .load(nuevoLogo)
                                                .transition(DrawableTransitionOptions.withCrossFade())
                                                .error(R.drawable.noimage)
                                                .into(logoRest);

                                        esperaTiempoSinReinicio espera=new esperaTiempoSinReinicio();
                                        espera.execute(enviando);

                                    }else{

                                        enviando.cancel();
                                        mensajeAlerta=contexto.getString(R.string.logo_no_cambiado);
                                        ponAlerta();

                                        System.out.println("ERROR1");

                                    }

                                }else{

                                    enviando.cancel();
                                    mensajeAlerta=contexto.getString(R.string.logo_no_cambiado);
                                    ponAlerta();

                                    System.out.println("ERROR2");

                                }

                            }catch (Exception e){

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.logo_no_cambiado);
                                ponAlerta();

                                System.out.println("NO SE PUDO CARGAR JSON "+e.getMessage());

                            }

                        }

                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.logo_no_cambiado);
                            ponAlerta();

                            System.out.println("ERROR3");

                        }

                    })
                    .startUpload();

        } catch (Exception e) {

            enviando.cancel();
            mensajeAlerta=contexto.getString(R.string.logo_no_cambiado);
            ponAlerta();

            System.out.println("ERROR A: " + e.getMessage());
        }


    }

    public void enviaCambiosEmpresaImagenPrincipal(Uri miPath,
                                                   final String user,
                                                   final String alses,
                                                   final String alsesk,
                                                   final ImageView imagenPrincipal,
                                                   String linkImagen){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        enviando.show();

        String url=contexto.getString(R.string.servidor_cambia_imagen_principal_empresa);

        try {

            int contador=tiempoEspera;

            final String uploadId = UUID.randomUUID().toString();

            String nombreImagenP;

            if(linkImagen.equals("")){

                nombreImagenP="nada";
            }else{

                nombreImagenP=linkImagen;
            }

            reducirImagen(MediaStore.Images.Media.getBitmap(contexto.getContentResolver(), miPath));

            String alsesEnviar=herramientas.codiAlses(alses,alsesk);

            new MultipartUploadRequest(contexto.getApplicationContext(), uploadId, url)
                    .addFileToUpload(imagenEnvio, "imagen")
                    .addParameter("nombreImagen", nombreImagenP)
                    .addParameter("u", user)
                    .addParameter("al", alsesEnviar)
                    .setMaxRetries(2)
                    .setUtf8Charset()
                    .setDelegate(new UploadStatusDelegate() {

                        @Override
                        public void onProgress(UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(UploadInfo uploadInfo, Exception e) {

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                            ponAlerta();

                            System.out.println("ERROR: "+e.getMessage());

                        }

                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {

                            borraTemporales();

                            try {

                                JSONArray valor = new JSONObject(serverResponse.getBodyAsString()).getJSONArray("valor");

                                if(valor.get(0).equals("ok")){

                                    String nuevoAlses = new JSONObject(serverResponse.getBodyAsString()).getJSONArray("alses").get(0).toString();

                                    herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                                    String nuevaImagen = new JSONObject(serverResponse.getBodyAsString()).getJSONArray("nuevaimagen").get(0).toString();

                                    if(!nuevaImagen.equals("nok")){

                                        mensajeAlerta=contexto.getString(R.string.imagen_cambiada);

                                        Menu_Empresa.miRestaurante.imagen_principal=nuevaImagen;

                                        Glide.with(contexto)
                                                .load(nuevaImagen)
                                                .transition(DrawableTransitionOptions.withCrossFade())
                                                .error(R.drawable.noimage)
                                                .into(imagenPrincipal);

                                        esperaTiempoSinReinicio espera=new esperaTiempoSinReinicio();
                                        espera.execute(enviando);

                                    }else{

                                        enviando.cancel();
                                        mensajeAlerta=contexto.getString(R.string.imagen_no_cambiada);
                                        ponAlerta();

                                        System.out.println("ERROR1");

                                    }

                                }else{

                                    enviando.cancel();
                                    mensajeAlerta=contexto.getString(R.string.imagen_no_cambiada);
                                    ponAlerta();

                                    System.out.println("ERROR2");

                                }

                            }catch (Exception e){

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.imagen_no_cambiada);
                                ponAlerta();

                                System.out.println("NO SE PUDO CARGAR JSON "+e.getMessage());

                            }



                        }

                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.imagen_no_cambiada);
                            ponAlerta();
                            System.out.println("ERROR3");
                            borraTemporales();

                        }

                    })
                    .startUpload();

        } catch (Exception e) {

            enviando.cancel();
            borraTemporales();
            mensajeAlerta=contexto.getString(R.string.imagen_no_cambiada);
            ponAlerta();
            System.out.println("ERROR A: " + e.getMessage());
        }
    }

    public void enviaCambiosActividad(final String user,
                                    final String alses,
                                    final String alsesk,
                                    final int puesto,
                                    final int estaActualizando,
                                    final TextView estado,
                                    final TextView actualizando){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        enviando.show();

        String nuevoAlses=herramientas.codiAlses(alses,alsesk);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("online", String.valueOf(puesto));
        parametros.put("actualizando", String.valueOf(estaActualizando));
        parametros.put("u", user);
        parametros.put("al", nuevoAlses);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        String url=contexto.getString(R.string.servidor_cambia_datos_actividad);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA SERVIDOR "+response);

                try {

                    JSONArray valor = response.getJSONArray("valor");

                    if(valor.get(0).equals("ok")){

                        String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                        String hecho = response.getJSONArray("hecho").get(0).toString();


                        if(hecho.equals("ok")){

                            mensajeAlerta=contexto.getString(R.string.datos_cambiados);

                            if (puesto == 1) {
                                estado.setText("ONLINE");
                                estado.setBackgroundTintList(ColorStateList.valueOf(contexto.getResources().getColor(R.color.colorVerde, null)));
                                //estado.setBackgroundColor(Color.GREEN);
                            } else {
                                estado.setText("OFFLINE");
                                estado.setBackgroundTintList(ColorStateList.valueOf(contexto.getResources().getColor(R.color.colorRojo, null)));
                                //estado.setBackgroundColor(Color.RED);

                            }

                            if(estaActualizando==1) {
                                System.out.println("PONE ACTUALIZANDO");
                                actualizando.setText(contexto.getResources().getString(R.string.mostrando_karta));
                                actualizando.setBackgroundTintList(ColorStateList.valueOf(contexto.getResources().getColor(R.color.colorVerde,null)));
                                //estado.setBackgroundColor(Color.GREEN);
                            }else{
                                System.out.println("PONE NO ACTUALIZANDO");
                                actualizando.setText(contexto.getResources().getString(R.string.actualizando_karta));
                                actualizando.setBackgroundTintList(ColorStateList.valueOf(contexto.getResources().getColor(R.color.colorRojo,null)));
                                //estado.setBackgroundColor(Color.RED);

                            }

                            Menu_Empresa.miRestaurante.online=puesto;
                            Menu_Empresa.miRestaurante.actualizando=estaActualizando;


                            esperaTiempoSinReinicio espera=new esperaTiempoSinReinicio();
                            espera.execute(enviando);

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                            ponAlerta();

                            System.out.println("ERROR1");

                        }

                    }else{

                        enviando.cancel();
                        mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                        ponAlerta();

                        System.out.println("ERROR2");

                    }

                } catch (Exception e) {

                    enviando.cancel();
                    mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                    ponAlerta();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                enviando.cancel();
                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                ponAlerta();

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);

    }

    public void enviaCambiosEmpresa(final String user,
                                    final String alses,
                                    final String alsesk,
                                    final String detalle,
                                    final TextView detalleCaja,
                                    final String tipoComida,
                                    final TextView tipoComidaCaja,
                                    final String tags,
                                    final TextView tagsCaja,
                                    final String telefono,
                                    final TextView telefonoCaja){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        enviando.show();

        String nuevoAlses=herramientas.codiAlses(alses,alsesk);

            Map<String, String> parametros = new HashMap<>();
            parametros.put("detalleRestaurante", detalle);
            parametros.put("tipoComida", tipoComida);
        parametros.put("tags", tags);
            parametros.put("telefonoUsuario", String.valueOf(telefono));
            parametros.put("u", user);
            parametros.put("al", nuevoAlses);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            String url=contexto.getString(R.string.servidor_cambia_datos_empresa);

            RequestQueue rq= Volley.newRequestQueue(contexto);

            JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    System.out.println("RESPUESTA SERVIDOR "+response);

                    try {

                        JSONArray valor = response.getJSONArray("valor");

                        if(valor.get(0).equals("ok")){

                            String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                            herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                            String hecho = response.getJSONArray("hecho").get(0).toString();


                            if(hecho.equals("ok")){

                                mensajeAlerta=contexto.getString(R.string.datos_cambiados);

                                detalleCaja.setText(detalle);
                                tagsCaja.setText(tags);
                                tipoComidaCaja.setText(tipoComida);
                                telefonoCaja.setText(telefono);


                                    Menu_Empresa.miRestaurante.detalle=detalle;
                                Menu_Empresa.miRestaurante.tags=tags;
                                    Menu_Empresa.miRestaurante.tipo_comida=tipoComida;
                                    Menu_Empresa.miRestaurante.telefono=Integer.parseInt(telefono);

                                esperaTiempoSinReinicio espera=new esperaTiempoSinReinicio();
                                espera.execute(enviando);

                            }else{

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                ponAlerta();

                                System.out.println("ERROR1");

                            }

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                            ponAlerta();

                            System.out.println("ERROR2");

                        }

                    } catch (Exception e) {

                        enviando.cancel();
                        mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                        ponAlerta();

                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    enviando.cancel();
                    mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                    ponAlerta();

                }
            }

            );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            rq.add(sr);

    }

    public void enviaCambiosConfigUser(final String user,
                                       final String alses,
                                       final String alsesk,
                                       final int puede,
                                    final int desde){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        enviando.show();

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("puede", String.valueOf(puede));
        parametros.put("desde", String.valueOf(desde));
        parametros.put("u", user);
        parametros.put("al", alsesEnvio);



        JSONObject parametrosEnvio = new JSONObject(parametros);

        String url=contexto.getString(R.string.servidor_cambia_datos_empresa_configUser);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                     System.out.println("RESPUESTA SERVIDOR "+response);

                        JSONArray valor = response.getJSONArray("valor");

                        if(valor.get(0).equals("ok")){

                            String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                            herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                            String hecho = response.getJSONArray("hecho").get(0).toString();


                            if(hecho.equals("ok")){

                                mensajeAlerta=contexto.getString(R.string.datos_cambiados);

                                Menu_Empresa.miRestaurante.permiteComentarios=puede;
                                Menu_Empresa.miRestaurante.desdeDondeComentarios=desde;

                                esperaTiempoSinReinicio espera=new esperaTiempoSinReinicio();
                                espera.execute(enviando);

                            }else{

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                ponAlerta();

                                System.out.println("ERROR1");

                            }

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                            ponAlerta();

                            System.out.println("ERROR2");

                        }

                } catch (Exception e) {

                    enviando.cancel();

                    mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);

                    ponAlerta();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                enviando.cancel();
                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);

                ponAlerta();

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);

    }

    public void enviaCambiosAvatar(final String user, final String alses, final String alsesk, final String avatar, final View boton, final ImageView miAvatar, final CardView miAvatarFondo){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        enviando.show();

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);

        String url=contexto.getString(R.string.servidor_cambia_user_avatar);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("avatar", avatar);
        parametros.put("u", user);
        parametros.put("al", alsesEnvio);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                System.out.println("RESPUESTA AVATAR "+response);

                try {

                    String valor = response.getJSONArray("valor").get(0).toString();

                    if(valor.equals("error")){

                        activaBoton(true, boton);

                        enviando.cancel();

                        mensajeAlerta = contexto.getString(R.string.datos_no_cambiados);

                        ponAlerta();


                    }else {

                        String alses = response.getJSONArray("alses").get(0).toString();

                        herramientas.guardaAlsesUsuario(contexto, herramientas.decodiAlses(alses, alsesk));

                        String hecho = response.getJSONArray("hecho").get(0).toString();


                        if (hecho.equals("ok")) {

                            ponYguardaAvatar(avatar, miAvatar, miAvatarFondo);

                            mensajeAlerta = contexto.getString(R.string.datos_cambiados);

                            esperaTiempoSinReinicio espera = new esperaTiempoSinReinicio();

                            espera.execute(enviando);

                        } else {

                            activaBoton(true, boton);

                            enviando.cancel();

                            mensajeAlerta = contexto.getString(R.string.datos_no_cambiados);

                            ponAlerta();

                        }
                    }

                } catch (Exception e) {

                    activaBoton(true, boton);

                    enviando.cancel();

                    mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);

                    ponAlerta();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                activaBoton(true, boton);

                enviando.cancel();
                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);

                ponAlerta();

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);

    }

    public void enviaCambiosOrden(final String user,
                                  final String alses,
                                  final String alsesk,
                                  final int posicionScroll,
                                  final JSONArray orden,
                                  final String nivel,
                                  final View boton){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.modificando_orden));

        RequestQueue rq= Volley.newRequestQueue(contexto);

        String url = contexto.getString(R.string.servidor_enviaOrdenNivel);

        enviando.show();

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("orden", orden.toString());
        parametros.put("nivel", nivel);
        parametros.put("u", user);
        parametros.put("al", alsesEnvio);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    System.out.println("RESPUESTA SERVIDOR "+response);

                    JSONArray valor = response.getJSONArray("valor");

                    if(valor.get(0).equals("ok")){

                        String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                        String hecho = response.getJSONArray("hecho").get(0).toString();


                        if(hecho.equals("ok")){

                            JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                            guardaNivel(datosJsonNivel, nivel);
                            guardaPosicionScroll(posicionScroll);

                            mensajeAlerta=contexto.getString(R.string.datos_ordenados);

                            esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                            inicia.execute(enviando);

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                            ponAlerta();

                            boton.setEnabled(true);
                            boton.setAlpha(1f);

                            System.out.println("ERROR1");

                        }

                    }else{

                        enviando.cancel();
                        mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                        ponAlerta();

                        boton.setEnabled(true);
                        boton.setAlpha(1f);

                        System.out.println("ERROR2");

                    }

                } catch (Exception e) {

                    mensajeAlerta=contexto.getString(R.string.datos_no_ordenados);
                    ponAlerta();

                    boton.setEnabled(true);
                    boton.setAlpha(1f);

                    enviando.cancel();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                enviando.cancel();

                boton.setEnabled(true);
                boton.setAlpha(1f);

                mensajeAlerta=contexto.getString(R.string.datos_no_ordenados);
                ponAlerta();

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public void enviaCambiosColorKarta(final String user,
                                       final String alses,
                                       final String alsesk,
                                       final int cN,
                                  final int cD,
                                  final int cP,
                                  final int fN,
                                  final int fD,
                                       final int cNP,
                                       final int cDP,
                                       final int fKarta,
                                       final int fDP,
                                       final int tBorde,
                                       final int fBorde,
                                       final View boton,
                                       final View previsualiza){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        RequestQueue rq= Volley.newRequestQueue(contexto);

        String url = contexto.getString(R.string.servidor_color_platos);

        enviando.show();

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("cN", String.valueOf(cN));
        parametros.put("cD", String.valueOf(cD));
        parametros.put("cP", String.valueOf(cP));
        parametros.put("fN", String.valueOf(fN));
        parametros.put("fD", String.valueOf(fD));
        parametros.put("cNP", String.valueOf(cNP));
        parametros.put("cDP", String.valueOf(cDP));
        parametros.put("fkarta", String.valueOf(fKarta));
        parametros.put("fDP", String.valueOf(fDP));
        parametros.put("tborde", String.valueOf(tBorde));
        parametros.put("fborde", String.valueOf(fBorde));
        parametros.put("u", user);
        parametros.put("al", alsesEnvio);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    System.out.println("RESPUESTA SERVIDOR "+response);

                    JSONArray valor = response.getJSONArray("valor");

                    if(valor.get(0).equals("ok")){

                        String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                        String hecho = response.getJSONArray("hecho").get(0).toString();


                        if(hecho.equals("ok")){

                            mensajeAlerta=contexto.getString(R.string.datos_cambiados);

                            Menu_Empresa.miRestaurante.fN=fN;
                            Menu_Empresa.miRestaurante.fD=fD;
                            Menu_Empresa.miRestaurante.cN=cN;
                            Menu_Empresa.miRestaurante.cD=cD;
                            Menu_Empresa.miRestaurante.cP=cP;

                            Menu_Empresa.miRestaurante.cNP=cNP;
                            Menu_Empresa.miRestaurante.cDP=cDP;
                            Menu_Empresa.miRestaurante.fKarta=fKarta;
                            Menu_Empresa.miRestaurante.fDP=fDP;

                            Menu_Empresa.miRestaurante.tBordes=tBorde;
                            Menu_Empresa.miRestaurante.fBordes=fBorde;

                            previsualiza.setEnabled(true);
                            previsualiza.setAlpha(1f);


                            esperaTiempoSinReinicio espera=new esperaTiempoSinReinicio();
                            espera.execute(enviando);

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.datos_no_actualizados);
                            ponAlerta();

                            System.out.println("ERROR1");

                        }

                    }else{

                        enviando.cancel();
                        mensajeAlerta=contexto.getString(R.string.datos_no_actualizados);
                        ponAlerta();

                        System.out.println("ERROR2");

                    }

                } catch (Exception e) {

                    activaBoton(true, boton);

                    enviando.cancel();

                    mensajeAlerta=contexto.getString(R.string.datos_no_actualizados);
                    ponAlerta();

                }

            }
        }
        , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                activaBoton(true, boton);

                enviando.cancel();

                mensajeAlerta=contexto.getString(R.string.datos_no_actualizados);
                ponAlerta();

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public void borraNivel(final String user,
                           final String alses,
                           final String alsesk,
                           final int posicionScroll,
                           final String nombreImagen,
                           final JSONArray ordenNivel,
                           final String nivel,
                           final int esUnMenu,
                           final String codNivel1,
                           final String codNivel2,
                           final String codNivel3,
                           final String codNivel4){


        System.out.println(" A BORRAR: "+codNivel1+" "+codNivel2+" "+codNivel3+" "+codNivel4);
        String url=contexto.getString(R.string.servidor_borra_nivel);

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.borrando_elemento));

        RequestQueue rq= Volley.newRequestQueue(contexto);

        enviando.show();

        final String nombreImagenEnviar;

        if(nombreImagen.equals("")){

            nombreImagenEnviar="nada";

        }else {

            nombreImagenEnviar = nombreImagen;
        }

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);

        Map<String, String> parametros = new HashMap<>();
        parametros.put("nivel", nivel);
        parametros.put("esMenu", String.valueOf(esUnMenu));
        parametros.put("codNivel1", codNivel1);
        parametros.put("codNivel2", codNivel2);
        parametros.put("codNivel3", codNivel3);
        parametros.put("codNivel4", codNivel4);
        parametros.put("nombreImagen", nombreImagenEnviar);
        parametros.put("ordenNivel", ordenNivel.toString());
        parametros.put("u", user);
        parametros.put("al", alsesEnvio);

        JSONObject parametrosEnvio = new JSONObject(parametros);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    System.out.println("RESPUESTA SERVIDOR "+response);

                    JSONArray valor = response.getJSONArray("valor");

                    if(valor.get(0).equals("ok")){

                        String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                        String hecho = response.getJSONArray("hecho").get(0).toString();


                        if(hecho.equals("ok")){

                            JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                            guardaNivel(datosJsonNivel, nivel);
                            guardaPosicionScroll(posicionScroll);

                            mensajeAlerta=contexto.getString(R.string.eliminado_exito);

                            esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                            inicia.execute(enviando);

                        }else if(hecho.equals("vacio")){

                            JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                            guardaNivel(datosJsonNivel, nivel);
                            guardaPosicionScroll(posicionScroll);

                            mensajeAlerta=contexto.getString(R.string.eliminado_exito);

                            esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                            inicia.execute(enviando);

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.eliminado_sinexito);
                            ponAlerta();

                            System.out.println("ERROR1");

                        }

                    }else{

                        enviando.cancel();
                        mensajeAlerta=contexto.getString(R.string.eliminado_sinexito);
                        ponAlerta();

                        System.out.println("ERROR2");

                    }

                } catch (Exception e) {

                    enviando.cancel();

                    System.out.println("ERROR "+e.getMessage());

                    mensajeAlerta=contexto.getString(R.string.eliminado_sinexito);
                    ponAlerta();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("ERROR "+error.getMessage());
                enviando.cancel();
                mensajeAlerta=contexto.getString(R.string.eliminado_sinexito);
                ponAlerta();

            }
        }

        );

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        rq.add(sr);


    }

    public void introduceNuevoNivel(final String user,
                                    final String alses,
                                    final String alsesk,
                                    final int posicionScroll,
                                    final String nivel,
                                    final String codNivelSup,
                                    final String nombreNivel,
                                    final String detalleNivel,
                                    final String precioPlato,
                                    final String alergenos,
                                    Uri miPath,
                                    final int mostrar,
                                    final int destacar,
                                    final String extra,
                                    final String codNivel1){

        final String precioConPunto;

        String url;

        if(precioPlato.equals("")){

            precioConPunto="0";
        }else{

            precioConPunto=precioPlato.replace(",",".");

        }

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_elemento));

        enviando.show();

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);


        if(miPath==null) {  // ---------------- SIN IMAGEN ---------------------------------

            System.out.println("VA A ENVIAR NIVEL SIN  IMAGEN ");
            url=contexto.getString(R.string.servidor_nuevo_nivel);

            RequestQueue rq= Volley.newRequestQueue(contexto);


            System.out.println("MIS ALSES EMPRESA "+user+" "+alses+" "+alsesk);

            Map<String, String> parametros = new HashMap<>();
            parametros.put("codNivelSup", codNivelSup);
            parametros.put("nivel", nivel);
            parametros.put("nombreNivel", nombreNivel);
            parametros.put("detalleNivel", detalleNivel);
            parametros.put("precioNivel", precioConPunto);
            parametros.put("mostrarImagen", String.valueOf(mostrar));
            parametros.put("destacar", String.valueOf(destacar));
            parametros.put("alergenos", alergenos);

            parametros.put("extra", extra);

            parametros.put("u", user);
            parametros.put("al", alsesEnvio);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {


                @Override
                public void onResponse(JSONObject response) {

                    try {

                        System.out.println("RESPUESTA SERVIDOR "+response);

                        JSONArray valor = response.getJSONArray("valor");

                        if(valor.get(0).equals("ok")){

                            String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                            herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                            String hecho = response.getJSONArray("hecho").get(0).toString();


                            if(hecho.equals("ok")){

                                if(nivel.equals("nivel1")){

                                    mensajeAlerta=contexto.getString(R.string.anadido_categoria_exito);

                                }else{
                                    mensajeAlerta=contexto.getString(R.string.anadido_exito);
                                }

                                JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                                if (datosJsonNivel.length() > 0) {

                                    guardaNivel(datosJsonNivel, nivel);

                                    guardaPosicionScroll(posicionScroll+500);

                                }

                                esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                                inicia.execute(enviando);

                            }else{

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                                ponAlerta();

                                System.out.println("ERROR1");

                            }

                        }else{

                            enviando.cancel();
                            mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                            ponAlerta();

                            System.out.println("ERROR2");

                        }




                    } catch (Exception e) {

                        enviando.cancel();

                        mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                        ponAlerta();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    System.out.println("RESPUESTA: "+error.toString());

                    enviando.cancel();
                    mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                    ponAlerta();

                }
            }

            );

            sr.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            rq.add(sr);

        }else { // -------------------- CON IMAGEN --------------------------------

            System.out.println("VA A ENVIAR NIVEL CON IMAGEN ");

            url=contexto.getString(R.string.servidor_nuevo_nivel_imagen);

            try {

                int contador=tiempoEspera;



                final String uploadId = UUID.randomUUID().toString();

                reducirImagen(MediaStore.Images.Media.getBitmap(contexto.getContentResolver(), miPath));


                while(contador>0) {

                    try {
                        Thread.sleep(milisegundos);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador--;

                }


                new MultipartUploadRequest(contexto.getApplicationContext(), uploadId, url)
                        .addFileToUpload(imagenEnvio, "imagen")
                        .addParameter("codNivelSup", codNivelSup)
                        .addParameter("extra", extra)
                        .addParameter("codNivelUno", codNivel1)
                        .addParameter("nombreNivel", nombreNivel)
                        .addParameter("detalleNivel", detalleNivel)
                        .addParameter("precioNivel", precioConPunto)
                        .addParameter("mostrarImagen", String.valueOf(mostrar))
                        .addParameter("destacar", String.valueOf(destacar))
                        .addParameter("alergenos", alergenos)
                        .addParameter("nivel", nivel)
                        .addParameter("u", user)
                        .addParameter("al", alsesEnvio)
                        .setMaxRetries(2)
                        .setUtf8Charset()
                        .setDelegate(new UploadStatusDelegate() {

                            @Override
                            public void onProgress(UploadInfo uploadInfo) {

                            }

                            @Override
                            public void onError(UploadInfo uploadInfo, Exception e) {

                                enviando.cancel();

                                mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                                ponAlerta();

                                System.out.println("ERROR 0: "+e.getMessage());

                                borraTemporales();


                            }

                            @Override
                            public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {

                                System.out.println("RESPUESTA SERVIDOR "+serverResponse.getBodyAsString());

                                borraTemporales();

                                try {

                                    JSONObject response=new JSONObject(serverResponse.getBodyAsString());

                                    String valor = response.getJSONArray("valor").get(0).toString();

                                    if(valor.equals("ok")){

                                        String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                                        String hecho = response.getJSONArray("hecho").get(0).toString();


                                        if(hecho.equals("ok")){

                                            if(nivel.equals("nivel1") || nivel.equals("nivel3")){

                                                mensajeAlerta=contexto.getString(R.string.anadido_categoria_exito);

                                            }else{
                                                mensajeAlerta=contexto.getString(R.string.anadido_exito);
                                            }

                                            JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                                            if (datosJsonNivel.length() > 0) {

                                                guardaNivel(datosJsonNivel, nivel);

                                                guardaPosicionScroll(posicionScroll+500);

                                            }

                                            esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                                            inicia.execute(enviando);

                                        }else{

                                            enviando.cancel();
                                            mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                                            ponAlerta();

                                            System.out.println("ERROR1");

                                        }

                                    }else{

                                        enviando.cancel();
                                        mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                                        ponAlerta();

                                        System.out.println("ERROR 3");

                                    }


                                }catch (Exception e){

                                    enviando.cancel();

                                    mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                                    ponAlerta();

                                }

                            }

                            @Override
                            public void onCancelled(UploadInfo uploadInfo) {

                                enviando.cancel();

                                borraTemporales();

                                mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                                ponAlerta();

                                System.out.println("ERROR 2: "+uploadInfo.toString());

                            }

                        })
                        .startUpload();


            } catch (Exception e) {

                enviando.cancel();

                mensajeAlerta=contexto.getString(R.string.anadido_sinexito);
                ponAlerta();

                System.out.println("ERROR 4: " + e.getMessage());

                borraTemporales();

            }

        }

    }

    public void borraTemporales(){


        // borra temporales ---------------------

        if(Build.VERSION.SDK_INT<30) { // HASTA VERSION 29 ---------------------------------------------

            if(file!=null) {

                if (file.delete()) {

                    System.out.println("temporal borrado");
                } else {

                    System.out.println("temporal no borrado");
                }
            }

        }else{

            if(resolver!=null){

                try {

                    resolver.delete(imageuri, null, null);

                    System.out.println("temporal borrado");

                }catch (Exception e){

                    System.out.println("temporal no borrado "+e.getMessage());
                }



            }else{

                //imagenEnvio

                System.out.println("temporal no borrado");

            }

        }

    }

    public void enviaCambiosNivel(final String user,
                                  final String alses,
                                  final String alsesk,
                                  final int posicionScroll,
                                  final String nombre,
                                  final String detalle,
                                  final String precio,
                                  final String cantidad,
                                  final String codigoNivel,
                                  final String codigoNivelSup,
                                  final String codigoNivel2,
                                  String rutaImagen,
                                  final String alergenos,
                                  final String agotado,
                                  final Uri miPath,
                                  final int mostrar,
                                  final int destacar,
                                  final String nivel,
                                  final String codigoNivel1){

        final AlertDialog enviando=esperandoEnvio(contexto.getResources().getString(R.string.enviando_cambios));

        final String precioConPunto=precio.replace(",",".");

        enviando.show();

        String alsesEnvio=herramientas.codiAlses(alses,alsesk);

        if(miPath==null) {  // ---------------- SIN IMAGEN ---------------------------------

            String url=contexto.getString(R.string.servidor_cambia_nivel);

            RequestQueue rq= Volley.newRequestQueue(contexto);

            Map<String, String> parametros = new HashMap<>();
            parametros.put("nombreImagen", limpiaNombre(rutaImagen));
            parametros.put("nombreNivel", nombre);
            parametros.put("codigoNivel", codigoNivel);
            parametros.put("detalleNivel", detalle);
            parametros.put("precioNivel", precioConPunto);
            parametros.put("cantidadPlato", cantidad);
            parametros.put("mostrarImagen", String.valueOf(mostrar));
            parametros.put("destacar", String.valueOf(destacar));
            parametros.put("alergenos", alergenos);
            parametros.put("agotado", agotado);
            parametros.put("nivel", nivel);
            parametros.put("u", user);
            parametros.put("al", alsesEnvio);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                System.out.println("RESPUESTA SERVIDOR "+response);

                                JSONArray valor = response.getJSONArray("valor");

                                if(valor.get(0).equals("ok")){

                                    String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                                    herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                                    String hecho = response.getJSONArray("hecho").get(0).toString();


                                    if(hecho.equals("ok")){

                                        mensajeAlerta=contexto.getString(R.string.datos_cambiados);

                                        JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                                        if (datosJsonNivel.length() > 0) {

                                            guardaNivel(datosJsonNivel, nivel);

                                            guardaPosicionScroll(posicionScroll);

                                        }

                                        esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                                        inicia.execute(enviando);

                                    }else{

                                        enviando.cancel();
                                        mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                        ponAlerta();

                                        System.out.println("ERROR1");

                                    }

                                }else{

                                    enviando.cancel();
                                    mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                    ponAlerta();

                                    System.out.println("ERROR2");

                                }

                            } catch (Exception e) {

                                System.out.println("ERROR2");

                                enviando.cancel();

                                mensajeAlerta = contexto.getString(R.string.datos_no_cambiados);
                                ponAlerta();

                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            System.out.println("RESPUESTA SERVIDOR VOLLEY: " + error.getMessage());

                            enviando.cancel();

                            mensajeAlerta = contexto.getString(R.string.datos_no_cambiados);
                            ponAlerta();

                        }
                    }

                    );

            jrq.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            rq.add(jrq);

        }else { // -------------------- CON IMAGEN --------------------------------

            System.out.println("MODIFICA CON IMAGEN");

            String url=contexto.getString(R.string.servidor_cambia_nivel_imagen);

            final String uploadId = UUID.randomUUID().toString();


            try {

                reducirImagen(MediaStore.Images.Media.getBitmap(contexto.getContentResolver(), miPath));

                System.out.println("CANTIDAD PLATOS: "+cantidad);

                new MultipartUploadRequest(contexto.getApplicationContext(), uploadId, url)
                        .addFileToUpload(imagenEnvio, "imagen")
                        .addParameter("nombreImagen", limpiaNombre(rutaImagen))
                        .addParameter("codigoNivel", codigoNivel)
                        .addParameter("codigoNivelSup", codigoNivelSup)
                        .addParameter("codigoNivel1", codigoNivel1)
                        .addParameter("codigoNivel2", codigoNivel2)
                        .addParameter("nombreNivel", nombre)
                        .addParameter("alergenos", alergenos)
                        .addParameter("agotado", agotado)
                        .addParameter("nivel", nivel)
                        .addParameter("detalleNivel", detalle)
                        .addParameter("precioNivel", precioConPunto)
                        .addParameter("cantidadPlato", cantidad)
                        .addParameter("destacar", String.valueOf(destacar))
                        .addParameter("mostrarImagen", String.valueOf(mostrar))
                        .addParameter("u", user)
                        .addParameter("al", alsesEnvio)
                        .setMaxRetries(2)
                        .setUtf8Charset()
                        .setDelegate(new UploadStatusDelegate() {

                            @Override
                            public void onProgress(UploadInfo uploadInfo) {

                            }

                            @Override
                            public void onError(UploadInfo uploadInfo, Exception e) {

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                ponAlerta();

                            }

                            @Override
                            public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {

                                borraTemporales();

                                System.out.println("RESPUESTA SERVIDOR IMAGEN: "+serverResponse.getBodyAsString());

                                try {

                                    JSONObject response=new JSONObject(serverResponse.getBodyAsString());

                                    String valor = response.getJSONArray("valor").get(0).toString();

                                    if(valor.equals("ok")){

                                        String nuevoAlses = response.getJSONArray("alses").get(0).toString();

                                        herramientas.guardaAlsesEmpresa(contexto, herramientas.decodiAlses(nuevoAlses,alsesk));

                                        String hecho = response.getJSONArray("hecho").get(0).toString();

                                        if(hecho.equals("ok")){

                                            mensajeAlerta=contexto.getString(R.string.datos_cambiados);

                                            JSONArray datosJsonNivel = response.getJSONArray("datos_nivel");

                                            if (datosJsonNivel.length() > 0) {

                                                guardaNivel(datosJsonNivel, nivel);

                                                guardaPosicionScroll(posicionScroll);

                                            }

                                            esperaTiempoYreinicia inicia = new esperaTiempoYreinicia();

                                            inicia.execute(enviando);

                                        }else{

                                            enviando.cancel();
                                            mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                            ponAlerta();

                                            System.out.println("ERROR1");

                                        }

                                    }else{

                                        enviando.cancel();
                                        mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                        ponAlerta();

                                        System.out.println("ERROR2");

                                    }

                                }catch (Exception e){

                                    enviando.cancel();
                                    mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                    ponAlerta();

                                }

                            }

                            @Override
                            public void onCancelled(UploadInfo uploadInfo) {

                                enviando.cancel();
                                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                                ponAlerta();

                            }

                        })
                        .startUpload();

            } catch (Exception e) {

                enviando.cancel();
                mensajeAlerta=contexto.getString(R.string.datos_no_cambiados);
                ponAlerta();

                System.out.println("ERROR A: " + e.getMessage());

            }

        }

    }

    private void guardaNivel(JSONArray nivel, String queNivel){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString(queNivel,nivel.toString());

        System.out.println("GUARDA "+queNivel);

        mieditor.apply();

    }

    private void guardaRestaurante(JSONArray restaurante){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("MIRESTAURANTE",restaurante.toString());

        mieditor.apply();

    }


    private void guardaRestauranteLocalConfigUser(int puede, int desde){

        Menu_Empresa.miRestaurante.permiteComentarios=puede;
        Menu_Empresa.miRestaurante.desdeDondeComentarios=desde;

    }

    private void guardaPosicionScroll(int laPosicion){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        System.out.println("GUARDA POSICION: "+laPosicion);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putInt("posicionScroll",laPosicion);

        mieditor.apply();


    }

    private String limpiaNombre(String nombre){

        if(nombre.equals("")){

            return "nada";
        }else{

            String nombreLimpio="";

            for (int i = nombre.length() - 1; i > 0; i--) {

                if (nombre.charAt(i) != '/') {

                    nombreLimpio+=(nombre.charAt(i));

                } else {

                    break;
                }

            }

            StringBuilder invierte=new StringBuilder(nombreLimpio);

            //String nombreLimpio=nombre.substring(nombre.length()-16);

            return invierte.reverse().toString();

        }

    }

    class ordenaCodigos implements Comparator<OrdenaCosas> {

        @Override
        public int compare(OrdenaCosas o1, OrdenaCosas o2) {
            return o1.getCodigo().compareTo(o2.getCodigo());
        }
    }

    private class OrdenaCosas  {

        private String codigo;
        private String nombre;

        public OrdenaCosas() {
        }

        OrdenaCosas(String codigo) {
            this.codigo = codigo;

        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }


        @Override
        public String toString() {
            return this.getCodigo();
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

    private class esperaTiempoYcierra extends AsyncTask<AlertDialog,Integer,String> {

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

            ponAlerta();

            actividad.finish();

        }
    }

    private class esperaTiempoYreinicia extends AsyncTask<AlertDialog,Integer,String> {

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

            ponAlerta();

            reiniciarActivity();

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
            ponAlerta();

        }
    }

    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = contexto.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void reducirImagen(Bitmap imagen) throws IOException {


        int alto = imagen.getHeight();
        int ancho = imagen.getWidth();
        imagenEnvio="";

        int nuevaAltura = (1200 * alto) / ancho;

        if(Build.VERSION.SDK_INT<30) { // HASTA VERSION 29 ---------------------------------------------

            FileOutputStream ostream = null;


            //file = new File(Environment.getExternalStorageDirectory() + "/tmp_image.jpg");

            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES ) + File.separator + "lakarta"+ File.separator);
            //File storageDir = new File(Environment.getExternalStorageDirectory().toString(), "temp/");

            try {

                //storageDir.mkdirs(); // make sure you call mkdirs() and not mkdir()
                file = File.createTempFile(
                        "comp_image",  // prefix
                        ".jpg",         // suffix
                        storageDir      // directory
                );


                file.deleteOnExit();

                ostream = new FileOutputStream(file);
                if (ancho > 1200) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(imagen, 1200 /*Ancho*/, nuevaAltura /*Alto*/, true /* filter*/);
                    resizedBitmap.compress(Bitmap.CompressFormat.WEBP, 70, ostream);

                    resizedBitmap.recycle();

                } else {

                    imagen.compress(Bitmap.CompressFormat.WEBP, 70, ostream);
                }

                imagenEnvio=file.getPath();

                //ostream.flush();



            } catch (IOException e) {
                System.out.println("Error in writing to file: "+e.getMessage());

            } finally {

                if (ostream != null) {
                    ostream.close();
                }
            }

        }else{ // version 30+ ------------------------------------------

            OutputStream ostream;

            resolver = contexto.getContentResolver();
            ContentValues contentValues = new ContentValues();

            try {

                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "tmp_image.jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "lakarta");

                imageuri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                ostream = resolver.openOutputStream(Objects.requireNonNull(imageuri));

                if (ancho > 1200) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(imagen, 1200 /*Ancho*/, nuevaAltura /*Alto*/, true /* filter*/);
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, ostream);

                    resizedBitmap.recycle();

                } else {

                    imagen.compress(Bitmap.CompressFormat.JPEG, 70, ostream);
                }

                imagenEnvio=getRealPathFromUri(imageuri);

                System.out.println("el file path: "+imagenEnvio);


                if (ostream != null) {
                    ostream.close();
                }



            }catch (Exception e){

                System.out.println("Error comprimir: "+e.getMessage());
                resolver.delete(imageuri,null,null);
            }

        }

    }

    private AlertDialog esperandoEnvio(String aQue){

        System.out.println("ESPERANDO ENVIO");

        View alertLayout = inflater.inflate(R.layout.emerg_enviando_datos, null);

        TextView mensajeEnvio=alertLayout.findViewById(R.id.mensaje_envio);

        mensajeEnvio.setText(aQue);

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;

    }

    public void cambiaDatosJSON(JSONArray datosNivel, String nivelAcambiar, String codigoAcambiar,
                                String nombreCambiar,
                                String datosNombre){

        try {
            for (int i = 0; i < datosNivel.length(); i++) {

                System.out.println("LINEA ANTES "+i+": "+datosNivel.get(i));

                JSONObject linea=(JSONObject)datosNivel.get(i);

                if(linea.get(nivelAcambiar).equals(codigoAcambiar)){

                    linea.put(nombreCambiar, datosNombre);

                    System.out.println("LINEA DESPUES "+i+": "+datosNivel.get(i));

                }



            }
        }catch (Exception e){


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

    private void reiniciarActivity(){

        Intent intent=new Intent();
        if(miBundle!=null) {

            intent.putExtra("NIVELAVER", (Parcelable)miBundle.getParcelable("NIVELAVER"));

        }
        intent.setClass(actividad, actividad.getClass());
        System.out.println("ACTIVIDAD "+actividad.getClass());
        //llamamos a la actividad
        actividad.startActivity(intent);
        //finalizamos la actividad actual

        try {
            actividad.finish();
        }catch (Exception e){


            System.out.println("ERROR ACTIVIDAD");
        }

    }

    private void ponYguardaAvatar(String avatar, ImageView miAvatar, CardView miAvatarFondo){

        if(avatar.startsWith("chico")){

            miAvatar.setImageResource(R.drawable.boy);

            miAvatar.setColorFilter((Integer.parseInt(avatar.substring(5,14))-1000000000));
            miAvatarFondo.setCardBackgroundColor(Integer.parseInt(avatar.substring(14))-1000000000);

        }else if(avatar.startsWith("chica")){

            miAvatar.setImageResource(R.drawable.girl);
            miAvatar.setColorFilter((Integer.parseInt(avatar.substring(5,14))-1000000000));
            miAvatarFondo.setCardBackgroundColor(Integer.parseInt(avatar.substring(14))-1000000000);


        }


        herramientas.cambiaJSON(contexto, "USUARIO_GUARDADO", "avatar", avatar);


    }



}
