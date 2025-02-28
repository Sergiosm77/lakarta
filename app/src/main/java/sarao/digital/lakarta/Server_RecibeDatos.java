package sarao.digital.lakarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

public class Server_RecibeDatos {

    JSONArray jsonArray_datosRest,jsonArray_datosNivel1,jsonArray_datosNivel2,jsonArray_datosNivel3,jsonArray_datosNivel4;
    JSONObject jsonObject_limitaciones;

    Limitaciones misLimitaciones;

    Restaurantes miRestaurante;

    Kartas[] laKartaNivel3,laKartaNivel1,laKartaNivel2,laKartaNivel1con2;
    Usuario miUsuario;

    String respuesta;

    String[] loginUsuario;

    Herramientas herramientas;


    Context contexto;

    public Server_RecibeDatos(Context contexto){

        this.contexto=contexto;

        herramientas=new Herramientas();

    }

    public void compruebaLoginEmpresa(final String user, final String pass){

        respuesta="no";

        String url=contexto.getString(R.string.servidor_login_empresa);

        try {

            Map<String, String> parametros = new HashMap<>();
            parametros.put("u", user);
            parametros.put("c", pass);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            RequestQueue rq = Volley.newRequestQueue(contexto);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (response != null) {

                                    JSONArray json_respuesta = response.getJSONArray("valor");

                                    if(json_respuesta.get(0).equals("nok")){

                                        cierraSesionEmpresa();
                                        respuesta="nok";

                                    }else if(json_respuesta.get(0).equals("ok")){

                                        JSONObject objetoAlses = response.getJSONObject("dime_alses");

                                        herramientas.guardaLoginEmpresa(contexto,user,pass,objetoAlses.getString("alses"),herramientas.decodiAlses(objetoAlses.getString("alsesk"),objetoAlses.getString("alses")));

                                        JSONObject jsonObjSesionEmpresa = response.getJSONObject("datos_sesion");

                                            if(!jsonObjSesionEmpresa.toString().equals("nok")){

                                                herramientas.guardaEmpresa(contexto, jsonObjSesionEmpresa);

                                                respuesta="ok";

                                            }else{

                                                respuesta="nok";
                                            }



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

    public void recibeDatosUserEmpresa(final String user, final String alses, final String alsesk){

        respuesta="no";

        String url=contexto.getString(R.string.servidor_recibe_datos_empresa);

        try {

            Map<String, String> parametros = new HashMap<>();
            parametros.put("u", user);
            parametros.put("al", herramientas.codiAlses(alses,alsesk));
            parametros.put("nivel", "todos");
            parametros.put("codigoNivel", "");

            JSONObject parametrosEnvio = new JSONObject(parametros);

            RequestQueue rq = Volley.newRequestQueue(contexto);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            System.out.println("RESPUESTA SERVIDOR: "+response);

                            try {



                                if (response != null) {

                                    String valor=response.getString("valor");

                                    if(valor.equals("error")) {

                                        respuesta=valor;
                                        herramientas.cierraSesionEmpresa(contexto);

                                    }else {

                                        herramientas.guardaAlsesEmpresa(contexto,herramientas.decodiAlses(response.getString("alses"),alsesk));


                                        if (response.getString("cargado").equals("ok")) {

                                            jsonObject_limitaciones = response.getJSONObject("limitaciones");
                                            jsonArray_datosNivel1 = response.getJSONArray("datos_nivel1");
                                            jsonArray_datosNivel2 = response.getJSONArray("datos_nivel2");
                                            jsonArray_datosNivel3 = response.getJSONArray("datos_nivel3");
                                            jsonArray_datosNivel4 = response.getJSONArray("datos_nivel4");
                                            jsonArray_datosRest = response.getJSONArray("datos_querest");

                                            if (jsonArray_datosRest.length() == 0) {

                                                respuesta = "error1";
                                                //laKartaNivel1 = new Kartas[0];
                                                //laKartaNivel2 = new Kartas[0];
                                                //laKartaNivel3 = new Kartas[0];

                                            } else {

                                                guardaNivel(jsonArray_datosNivel1, "nivel1");
                                                guardaNivel(jsonArray_datosNivel2, "nivel2");
                                                guardaNivel(jsonArray_datosNivel3, "nivel3");
                                                guardaNivel(jsonArray_datosNivel4, "nivel4");
                                                guardaLimitaciones(jsonObject_limitaciones);

                                                //cargaNivel1(jsonArray_datosNivel1);
                                                //cargaNivel2(jsonArray_datosNivel2);
                                                //cargaNivel3(jsonArray_datosNivel3,jsonArray_datosNivel4);


                                                guardaMiRest(jsonArray_datosRest);
                                                //miRest(jsonArray_datosRest);

                                                respuesta = "ok";

                                            }
                                        } else if (response.getJSONObject("cargado").toString().equals("nok")) {

                                            respuesta = "nok";

                                        } else {

                                            respuesta = "error1";
                                        }


                                    }

                                } else {

                                    respuesta = "error1";
                                    //laKartaNivel1 = new Kartas[0];
                                    //laKartaNivel2 = new Kartas[0];
                                    //laKartaNivel3 = new Kartas[0];

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

    public void compruebaLoginUsuario(final String user, final String pass){

        respuesta="no";


        String url=contexto.getString(R.string.servidor_login_usuario);

        try {

            Map<String, String> parametros = new HashMap<>();
            parametros.put("u", user);
            parametros.put("c", pass);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            RequestQueue rq = Volley.newRequestQueue(contexto);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            System.out.println("RESPUESTA USUARIO servidor: "+response);

                            try {

                                if (response != null) {

                                    JSONArray respuestaJson = response.getJSONArray("usuario");

                                    if(respuestaJson.get(0).equals("ok")){

                                        JSONArray jsonArray_sesionUser = response.getJSONArray("datos_sesion");
                                        JSONArray jsonArray_misLikes = response.getJSONArray("mis_likes");
                                        herramientas.guardaUsuario(contexto, jsonArray_sesionUser);
                                        herramientas.guardaMisLikes(contexto, jsonArray_misLikes);

                                        JSONArray datosUsuarioJson = response.getJSONArray("datos_sesion");

                                        JSONObject extraeAlses = datosUsuarioJson.getJSONObject(0);

                                        herramientas.guardaLoginUsuario(contexto,user,pass,extraeAlses.getString("alses"),herramientas.decodiAlses(extraeAlses.getString("alsesk"),extraeAlses.getString("alses")));

                                        respuesta = "ok";


                                    }else if(respuestaJson.get(0).equals("nok")){

                                        respuesta = "nok";

                                    }else{

                                        respuesta="error";
                                    }


                                } else {

                                    respuesta = "error1";

                                }

                            } catch (JSONException e) {

                                System.out.println("FALLO EN CONEXION: "+e.getMessage());

                                respuesta="error2";

                            }



                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {

                            respuesta = "error3";
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

    public void compruebaSesionUsuario(final String user, final String alses, final String alsesk){

        respuesta="no";

        String url=contexto.getString(R.string.servidor_sesion_usuario);

        try {

            Map<String, String> parametros = new HashMap<>();
            parametros.put("u", user);
            parametros.put("user_alses", herramientas.codiAlses(alses,alsesk));

            JSONObject parametrosEnvio = new JSONObject(parametros);

            RequestQueue rq = Volley.newRequestQueue(contexto);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            System.out.println("RESPUESTA USUARIO: "+response);

                            try {

                                if (response != null) {

                                    JSONArray respuestaJson = response.getJSONArray("valor");

                                    if(respuestaJson.get(0).equals("error")){

                                        cierraSesionUsuario();
                                        respuesta = "error";

                                    }else {

                                        JSONArray jsonArray_sesionUser = response.getJSONArray("datos_sesion");

                                        JSONObject objectUser = jsonArray_sesionUser.getJSONObject(0);

                                        herramientas.guardaAlsesUsuario(contexto, herramientas.decodiAlses(objectUser.getString("nuevoalses"), alsesk));

                                        if (respuestaJson.get(0).equals("ok")) {

                                            JSONArray jsonArray_misLikes = response.getJSONArray("mis_likes");
                                            herramientas.guardaUsuario(contexto, jsonArray_sesionUser);
                                            herramientas.guardaMisLikes(contexto, jsonArray_misLikes);



                                            System.out.println("RESPUESTA SERVIDOR " + jsonArray_sesionUser);

                                            respuesta = "ok";

                                        } else {

                                            respuesta = "nok";

                                        }

                                    }


                                } else {

                                    respuesta = "error1";

                                }

                            } catch (JSONException e) {

                                System.out.println("FALLO EN CONEXION: "+e.getMessage());

                                respuesta="error2";

                            }



                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {

                            respuesta = "error3";
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

    public void cargaNivelGuardado(String queNivel){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        try {

            JSONArray nivelCargado = new JSONArray(guarda.getString(queNivel, "0"));

            if(queNivel.equals("nivel1")){

                cargaNivel1(nivelCargado);

            }

            if(queNivel.equals("nivel2")){

                cargaNivel2(nivelCargado);
            }

            if(queNivel.equals("nivel3")){

                JSONArray nivel4 = new JSONArray(guarda.getString("nivel4", "0"));

                cargaNivel3(nivelCargado,nivel4);
            }

            cargaLimitaciones();



        }catch (Exception e){


        }

    }

    public void cargaUsuarioGuardado00000(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        miUsuario=new Usuario();

        try {

            JSONArray usuarioCargado = new JSONArray(guarda.getString("USUARIO_GUARDADO", "0"));

            JSONObject objectUser = usuarioCargado.getJSONObject(0);

            miUsuario.nombre = objectUser.getString("nombre");
            miUsuario.email = objectUser.getString("email");
            miUsuario.avatar = objectUser.getString("avatar");
            miUsuario.puede_comentar = objectUser.getInt("puede_comentar");

        }catch (Exception e){

            System.out.println("ERROR AL CARGAR USUARIO: "+e.getMessage());

        }

    }

    public void cargaTodosNiveles(String codigoRest){

        respuesta="no";

        String url=contexto.getString(R.string.servidor_cargadatos);

        try {

            Map<String, String> parametros = new HashMap<>();
            parametros.put("codigo", contexto.getString(R.string.codigo_datos));
            parametros.put("codigoRest", codigoRest);

            JSONObject parametrosEnvio = new JSONObject(parametros);

            RequestQueue rq = Volley.newRequestQueue(contexto);

            JsonObjectRequest jrq = new JsonObjectRequest
                    (Request.Method.POST, url, parametrosEnvio, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                JSONArray valor=response.getJSONArray("valor");

                                if(valor.get(0).equals("ok")){

                                    jsonArray_datosNivel1 = response.getJSONArray("datos_nivel1");
                                    jsonArray_datosNivel2 = response.getJSONArray("datos_nivel2");
                                    jsonArray_datosNivel3 = response.getJSONArray("datos_nivel3");
                                    jsonArray_datosNivel4 = response.getJSONArray("datos_nivel4");

                                    if (jsonArray_datosNivel1.length() == 0) {

                                        laKartaNivel1con2 = new Kartas[0];
                                        laKartaNivel3 = new Kartas[0];

                                        respuesta = "error1";

                                    } else {


                                        cargaNivel1con2(jsonArray_datosNivel1,jsonArray_datosNivel2);
                                        cargaNivel3(jsonArray_datosNivel3,jsonArray_datosNivel4);

                                        respuesta = "ok";

                                    }


                                }else {

                                    laKartaNivel1con2 = new Kartas[0];
                                    laKartaNivel3 = new Kartas[0];
                                    respuesta = "error1";

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

            respuesta="error3 "+e.getMessage();

        }

    }

    public void cargaMiRestGuardado(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        try {

            JSONArray JSONRest = new JSONArray(guarda.getString("MIRESTAURANTE", "0"));

            cargamiRest(JSONRest);

        }catch (Exception e){


        }

    }

    public void cargaLimitaciones(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        try {

            JSONObject JSONLimit = new JSONObject(guarda.getString("LIMITACIONES", "0"));

            cargaLimit(JSONLimit);

        }catch (Exception e){


        }

        System.out.println("LIMITACIONES");

    }

    public void cargaNivel1(JSONArray datosNivel){


        try{

            laKartaNivel1=new Kartas[datosNivel.length()];

            for(int i=0;i<datosNivel.length();i++) {

                laKartaNivel1[i] = new Kartas();

                JSONObject objectNivel1 = datosNivel.getJSONObject(i);

                laKartaNivel1[i].cod_restaurante = objectNivel1.getString("cod_restaurante");

                laKartaNivel1[i].cod_nivel = objectNivel1.getString("cod_nivel1");
                laKartaNivel1[i].nombre_nivel = objectNivel1.getString("nivel1_nombre");
                laKartaNivel1[i].imagen_nivel = objectNivel1.getString("nivel1_imagen");
                laKartaNivel1[i].mostrar_imagen = objectNivel1.getInt("mostrar_imagen");
                laKartaNivel1[i].detalle_nivel = objectNivel1.getString("nivel1_detalle");
                laKartaNivel1[i].orden_nivel = objectNivel1.getInt("nivel1_orden");
                if (!objectNivel1.getString("nivel1_precio").equals("null")) {
                    laKartaNivel1[i].precio_nivel = objectNivel1.getDouble("nivel1_precio");

                }

            }

        }catch (JSONException e){

            System.out.println("ERROR AL PONER PLATOS: "+e.getMessage());

        }

    }

    public void cargaNivel1con2(JSONArray datosNivel1,JSONArray datosNivel2){


        try{

            laKartaNivel1con2=new Kartas[datosNivel1.length()];

            for(int i=0;i<datosNivel1.length();i++) {

                laKartaNivel1con2[i] = new Kartas();

                JSONObject objectNivel1 = datosNivel1.getJSONObject(i);

                laKartaNivel1con2[i].cod_restaurante = objectNivel1.getString("cod_restaurante");

                laKartaNivel1con2[i].cod_nivel = objectNivel1.getString("cod_nivel1");
                laKartaNivel1con2[i].nombre_nivel = objectNivel1.getString("nivel1_nombre");
                laKartaNivel1con2[i].imagen_nivel = objectNivel1.getString("nivel1_imagen");
                laKartaNivel1con2[i].mostrar_imagen = objectNivel1.getInt("mostrar_imagen");
                laKartaNivel1con2[i].detalle_nivel = objectNivel1.getString("nivel1_detalle");
                laKartaNivel1con2[i].tipoMenu = Integer.parseInt(objectNivel1.getString("tipo_menu"));
                if (!objectNivel1.getString("nivel1_precio").equals("null")) {
                    laKartaNivel1con2[i].precio_nivel = objectNivel1.getDouble("nivel1_precio");

                }

                int contador = 0;

                for (int e = 0; e < datosNivel2.length(); e++) {

                    JSONObject objectNivel2 = datosNivel2.getJSONObject(e);

                    if (laKartaNivel1con2[i].cod_nivel.equals(objectNivel2.getString("cod_nivel1"))) {

                        contador++;
                    }

                }

                laKartaNivel1con2[i].cod_subnivel=new String[contador];
                laKartaNivel1con2[i].nombre_subnivel=new String[contador];
                laKartaNivel1con2[i].imagen_subnivel=new String[contador];
                laKartaNivel1con2[i].mostrar_imagen_subnivel=new int[contador];
                laKartaNivel1con2[i].detalle_subnivel=new String[contador];
                laKartaNivel1con2[i].alergenos_subnivel=new String[contador];
                laKartaNivel1con2[i].precio_subnivel=new double[contador];
                laKartaNivel1con2[i].conOpciones=new int[contador];
                laKartaNivel1con2[i].destacado_subnivel=new int[contador];
                laKartaNivel1con2[i].agotado_subnivel=new int[contador];


                contador=0;

                for (int e = 0; e < datosNivel2.length(); e++) {

                    JSONObject objectNivel2 = datosNivel2.getJSONObject(e);

                    if (laKartaNivel1con2[i].cod_nivel.equals(objectNivel2.getString("cod_nivel1"))) {

                        laKartaNivel1con2[i].cod_subnivel[contador]=objectNivel2.getString("cod_nivel2");
                        laKartaNivel1con2[i].nombre_subnivel[contador]=objectNivel2.getString("nivel2_nombre");
                        laKartaNivel1con2[i].imagen_subnivel[contador]=objectNivel2.getString("nivel2_imagen");
                        laKartaNivel1con2[i].mostrar_imagen_subnivel[contador]=objectNivel2.getInt("mostrar_imagen");
                        laKartaNivel1con2[i].detalle_subnivel[contador]=objectNivel2.getString("nivel2_detalle");
                        laKartaNivel1con2[i].alergenos_subnivel[contador]=objectNivel2.getString("nivel2_alergenos");
                        if(objectNivel2.getDouble("nivel2_precio")!=0) {
                            laKartaNivel1con2[i].precio_subnivel[contador]=objectNivel2.getDouble("nivel2_precio");

                        }
                        laKartaNivel1con2[i].conOpciones[contador]=objectNivel2.getInt("esmenu");
                        laKartaNivel1con2[i].destacado_subnivel[contador]=objectNivel2.getInt("destacado");
                        laKartaNivel1con2[i].agotado_subnivel[contador]=objectNivel2.getInt("agotado");
                        contador++;

                    }

                }

            }


        }catch (JSONException e){

            System.out.println("ERROR AL PONER PLATOS: "+e.getMessage());

        }

    }

    public void cargaNivel2(JSONArray datosNivel){



        try{

            laKartaNivel2=new Kartas[datosNivel.length()];

            for(int i=0;i<datosNivel.length();i++) {

                laKartaNivel2[i] = new Kartas();

                JSONObject objectNivel1 = datosNivel.getJSONObject(i);

                laKartaNivel2[i].cod_restaurante = objectNivel1.getString("cod_restaurante");
                laKartaNivel2[i].cod_nivel_sup = objectNivel1.getString("cod_nivel1");
                laKartaNivel2[i].cod_nivel = objectNivel1.getString("cod_nivel2");
                laKartaNivel2[i].nombre_nivel = objectNivel1.getString("nivel2_nombre");
                laKartaNivel2[i].imagen_nivel = objectNivel1.getString("nivel2_imagen");
                laKartaNivel2[i].mostrar_imagen = objectNivel1.getInt("mostrar_imagen");
                laKartaNivel2[i].detalle_nivel = objectNivel1.getString("nivel2_detalle");
                laKartaNivel2[i].alergenos = objectNivel1.getString("nivel2_alergenos");
                laKartaNivel2[i].orden_nivel = objectNivel1.getInt("nivel2_orden");
                laKartaNivel2[i].esmenu = objectNivel1.getInt("esmenu");
                laKartaNivel2[i].destacado = objectNivel1.getInt("destacado");
                laKartaNivel2[i].agotado = objectNivel1.getInt("agotado");
                if (!objectNivel1.getString("nivel2_precio").equals("null")) {
                    laKartaNivel2[i].precio_nivel = objectNivel1.getDouble("nivel2_precio");

                }

            }

        }catch (JSONException e){

            System.out.println("ERROR AL PONER PLATOS: "+e.getMessage());

        }

    }

    public void cargaNivel3(JSONArray datosNivel3, JSONArray datosNivel4){

        if(datosNivel3.length()>0) {

            try {

                laKartaNivel3 = new Kartas[datosNivel3.length()];

                JSONObject objectNivel3;

                for (int i = 0; i < datosNivel3.length(); i++) {

                    laKartaNivel3[i] = new Kartas();

                    objectNivel3 = datosNivel3.getJSONObject(i);

                    laKartaNivel3[i].cod_restaurante = objectNivel3.getString("cod_restaurante");

                    laKartaNivel3[i].cod_nivel = objectNivel3.getString("cod_nivel3");
                    laKartaNivel3[i].cod_nivel_sup = objectNivel3.getString("cod_nivel2");
                    laKartaNivel3[i].nombre_nivel = objectNivel3.getString("nivel3_nombre");
                    laKartaNivel3[i].imagen_nivel = objectNivel3.getString("nivel3_imagen");
                    laKartaNivel3[i].mostrar_imagen = objectNivel3.getInt("mostrar_imagen");
                    laKartaNivel3[i].detalle_nivel = objectNivel3.getString("nivel3_detalle");
                    laKartaNivel3[i].cantidad_nivel = objectNivel3.getInt("nivel3_cantidad");
                    laKartaNivel3[i].orden_nivel = objectNivel3.getInt("nivel3_orden");
                    if (!objectNivel3.getString("nivel3_precio").equals("null")) {
                        laKartaNivel3[i].precio_nivel = objectNivel3.getDouble("nivel3_precio");

                    }

                    int contador = 0;



                    for (int e = 0; e < datosNivel4.length(); e++) {

                        JSONObject objectNivel4 = datosNivel4.getJSONObject(e);

                        if (laKartaNivel3[i].cod_nivel.equals(objectNivel4.getString("cod_nivel3"))) {

                            contador++;
                        }

                    }

                    laKartaNivel3[i].cod_subnivel=new String[contador];
                    laKartaNivel3[i].nombre_subnivel=new String[contador];
                    laKartaNivel3[i].imagen_subnivel=new String[contador];
                    laKartaNivel3[i].mostrar_imagen_subnivel=new int[contador];
                    laKartaNivel3[i].detalle_subnivel=new String[contador];
                    laKartaNivel3[i].alergenos_subnivel=new String[contador];
                    laKartaNivel3[i].precio_subnivel=new double[contador];
                    laKartaNivel3[i].orden_subnivel=new int[contador];
                    laKartaNivel3[i].visible=new int[contador];

                    contador=0;

                    for (int e = 0; e < datosNivel4.length(); e++) {

                        JSONObject objectNivel4 = datosNivel4.getJSONObject(e);

                        if (laKartaNivel3[i].cod_nivel.equals(objectNivel4.getString("cod_nivel3"))) {

                            laKartaNivel3[i].cod_subnivel[contador]=objectNivel4.getString("cod_nivel4");
                            laKartaNivel3[i].nombre_subnivel[contador]=objectNivel4.getString("nivel4_nombre");
                            laKartaNivel3[i].imagen_subnivel[contador]=objectNivel4.getString("nivel4_imagen");
                            laKartaNivel3[i].mostrar_imagen_subnivel[contador]=objectNivel4.getInt("mostrar_imagen");
                            laKartaNivel3[i].detalle_subnivel[contador]=objectNivel4.getString("nivel4_detalle");
                            laKartaNivel3[i].alergenos_subnivel[contador]=objectNivel4.getString("nivel4_alergenos");
                            if(!objectNivel4.getString("nivel4_precio").equals("null")) {
                                laKartaNivel3[i].precio_subnivel[contador]=objectNivel4.getDouble("nivel4_precio");

                            }
                            laKartaNivel3[i].orden_subnivel[contador]=objectNivel4.getInt("nivel4_orden");
                            laKartaNivel3[i].visible[contador]=objectNivel4.getInt("visible");
                            contador++;

                        }

                    }

                }

            } catch (JSONException e) {

                System.out.println("ERROR AL PONER PLATOS OP: " + e.getMessage());

            }
        }else{
            laKartaNivel3 = new Kartas[0];

        }

    }

    public void cargamiRest(JSONArray datosRest){

        System.out.println("CARGA RESTAURANTE");

        try{

            miRestaurante=new Restaurantes();

            JSONObject objectMiRest = datosRest.getJSONObject(0);

            miRestaurante.nombre = objectMiRest.getString("nombre_restaurante");
            miRestaurante.poblacion = objectMiRest.getString("poblacion");
            miRestaurante.email = objectMiRest.getString("email");
            miRestaurante.codigo = objectMiRest.getString("cod_restaurante");
            miRestaurante.telefono = objectMiRest.getInt("telefono");

            miRestaurante.latitud = objectMiRest.getString("latitud");
            miRestaurante.longitud = objectMiRest.getString("longitud");

            miRestaurante.cN=Integer.parseInt(objectMiRest.getString("cN"));
            miRestaurante.cD=Integer.parseInt(objectMiRest.getString("cD"));
            miRestaurante.cP=Integer.parseInt(objectMiRest.getString("cP"));
            miRestaurante.fN=Integer.parseInt(objectMiRest.getString("fN"));
            miRestaurante.fD=Integer.parseInt(objectMiRest.getString("fD"));

            miRestaurante.cNP=Integer.parseInt(objectMiRest.getString("cNP"));
            miRestaurante.cDP=Integer.parseInt(objectMiRest.getString("cDP"));
            miRestaurante.fKarta=Integer.parseInt(objectMiRest.getString("fkarta"));
            miRestaurante.fDP=Integer.parseInt(objectMiRest.getString("fDP"));

            miRestaurante.tBordes=Integer.parseInt(objectMiRest.getString("tBorde"));
            miRestaurante.fBordes=Integer.parseInt(objectMiRest.getString("fBorde"));

            miRestaurante.contaComentario=Integer.parseInt(objectMiRest.getString("conta_coment"));
            miRestaurante.contaLike=Integer.parseInt(objectMiRest.getString("conta_like"));

            miRestaurante.permiteComentarios=Integer.parseInt(objectMiRest.getString("puede_coment"));
            miRestaurante.desdeDondeComentarios=Integer.parseInt(objectMiRest.getString("dis_coment"));


            miRestaurante.tipo_comida = objectMiRest.getString("tipo_comida");
            miRestaurante.detalle = objectMiRest.getString("detalle");
            miRestaurante.tags = objectMiRest.getString("tags");
            miRestaurante.logo = objectMiRest.getString("logo");
            miRestaurante.imagen_principal = objectMiRest.getString("imagen_principal");
            miRestaurante.online = objectMiRest.getInt("online");
            miRestaurante.actualizando = objectMiRest.getInt("actualizando");

        }catch (JSONException e){

            System.out.println("ERROR EN MIRESTAURANTE: "+e.getMessage());

        }

    }

    public void cargaLimit(JSONObject datosLimit){

        try{

            misLimitaciones=new Limitaciones();


            misLimitaciones.cant_categorias = datosLimit.getInt("cant_categorias");
            misLimitaciones.cant_elementos = datosLimit.getInt("cant_elementos");
            misLimitaciones.cant_categorias_menu = datosLimit.getInt("cant_categorias_menu");
            misLimitaciones.cant_elementos_menu = datosLimit.getInt("cant_elementos_menu");

            misLimitaciones.fechaAlta = datosLimit.getString("fecha_alta");
            misLimitaciones.fechaFin = datosLimit.getString("fecha_fin");
            misLimitaciones.activo = datosLimit.getInt("activo");

        }catch (JSONException e){

            System.out.println("ERROR EN LIMITACIONES: "+e.getMessage());

        }

    }

    public void cargaUser(){

        loginUsuario=new String[2];

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        loginUsuario[0]=guarda.getString("USER_USUARIO","0");
        loginUsuario[1]=guarda.getString("PASS_USUARIO","0");

    }


    public void guardaNivel(JSONArray nivel, String queNivel){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString(queNivel,nivel.toString());

        mieditor.apply();

    }

    public void guardaMiRest(JSONArray miRest){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("MIRESTAURANTE",miRest.toString());

        System.out.println("GUARDA MI RESTAURANTE");

        mieditor.apply();


    }

    public void guardaLimitaciones(JSONObject limitaciones){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("LIMITACIONES",limitaciones.toString());

        System.out.println("GUARDA LIMITACIONES");

        mieditor.apply();


    }



    public void cierraSesionUsuario(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USER_USUARIO", "0");
        mieditor.putString("PASS_USUARIO", "0");

        System.out.println("CIERRA SESION USUARIO");

        mieditor.apply();

    }

    public void cierraSesionEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USER_EMPRESA", "0");
        mieditor.putString("PASS_EMPRESA", "0");

        System.out.println("CIERRA SESION EMPRESA");

        mieditor.apply();

    }






}
