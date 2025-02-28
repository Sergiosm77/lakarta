package sarao.digital.lakarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Herramientas {

    public Herramientas(){


    }


    public static void ponAlerta(Toast pop, String mensajeAlerta){


        try {
            if (!pop.getView().isShown()) {

                pop.setText(mensajeAlerta);

                pop.setGravity(Gravity.CENTER, 0, 0);
                TextView mensaje = pop.getView().findViewById(android.R.id.message);
                mensaje.setGravity(Gravity.CENTER);

                pop.show();

            }

        }catch (Exception e){

            pop.setText(mensajeAlerta);
            pop.show();
        }

    }

    public String codiAlses(String alses, String alsesk) {


        String respuesta="";

        if(alses.equals("0")){

            return "0";

        }else {

            try {


                byte[] key = alsesk.getBytes("UTF-8");

                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                cipher.doFinal(alses.getBytes(StandardCharsets.UTF_8));
                respuesta = Base64.encodeToString(cipher.doFinal(alses.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);


            } catch (Exception e) {

                System.out.println("ERROR 2: " + e.getMessage());

            }

            return respuesta;

        }

    }

    public String decodiAlses(String alses, String alsesk) {


        String respuesta="";

        if(alses.equals("0") || alsesk.equals("0") || alses.equals("") || alsesk.equals("")){

            return "0";

        }else {

            try {


                byte[] key = alsesk.getBytes("UTF-8");

                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                cipher.doFinal(alses.getBytes(StandardCharsets.UTF_8));
                //respuesta = Base64.encodeToString(cipher.doFinal(alses.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);


                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                respuesta=new String(cipher.doFinal(Base64.decode(alses,Base64.DEFAULT)));



            } catch (Exception e) {

                System.out.println("ERROR 2: " + e.getMessage());

            }

            return respuesta;

        }

    }

    public void guardaLoginUsuario(Context contexto,String user, String pass, String alses, String alsesk){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USUARIO_ANTERIOR", user);
        mieditor.putString("USER_USUARIO", user);
        mieditor.putString("PASS_USUARIO", pass);
        mieditor.putString("ALSES_USUARIO", alses);
        mieditor.putString("ALSESK_USUARIO", alsesk);


        System.out.println("USUARIO: "+user);
        System.out.println("USUARIO ALSES: "+alses);
        System.out.println("USUARIO ALSESK: "+alsesk);

        mieditor.apply();

    }

    public void guardaLoginEmpresa(Context contexto,String user, String pass, String alses, String alsesk){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("EMPRESA_ANTERIOR", user);
        mieditor.putString("USER_EMPRESA", user);
        mieditor.putString("PASS_EMPRESA", pass);
        mieditor.putString("ALSES_EMPRESA", alses);
        mieditor.putString("ALSESK_EMPRESA", alsesk);

        mieditor.apply();

    }

    public void cierraSesionUsuario(Context contexto){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USER_USUARIO", "0");
        mieditor.putString("PASS_USUARIO", "0");
        mieditor.putString("ALSES_USUARIO", "0");
        mieditor.putString("ALSESK_USUARIO", "0");
        mieditor.putString("MIS_LIKES", "0");
        Inicio.misLikes=new JSONArray();

        Inicio.sesionUserReiniciada =true;
        Inicio.likesCargados=false;

        System.out.println("CIERRA SESION USUARIO");

        mieditor.apply();

    }

    public void cierraSesionEmpresa(Context contexto){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USER_EMPRESA", "0");
        mieditor.putString("PASS_EMPRESA", "0");
        mieditor.putString("ALSES_EMPRESA", "0");
        mieditor.putString("ALSESK_EMPRESA", "0");

        System.out.println("CIERRA SESION EMPRESA");

        Inicio.sesionUserReiniciada =true;

        mieditor.apply();

    }

    public void guardaUsuario(Context contexto, JSONArray usuario){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("USUARIO_GUARDADO",usuario.toString());

        mieditor.apply();

    }

    public void guardaEmpresa(Context contexto, JSONObject empresa){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("EMPRESA_GUARDADA",empresa.toString());

        mieditor.apply();

    }

    public void guardaAlsesUsuario(Context contexto, String alses){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("ALSES_USUARIO", alses);

        System.out.println("GUARDA ALSES USUARIO "+alses);

        mieditor.apply();

    }

    public void guardaAlsesEmpresa(Context contexto, String alses){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("ALSES_EMPRESA", alses);

        System.out.println("GUARDA ALSES EMPRESA "+alses);

        mieditor.apply();

    }

    public void guardaMisLikes(Context contexto,JSONArray likes){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("MIS_LIKES",likes.toString());

        mieditor.apply();

    }

    public void cambiaJSON(Context contexto,String queJson, String queDato, String valorDato){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        JSONArray jsonCambiado=new JSONArray();

        try {

            JSONArray jsonGuardado = new JSONArray(guarda.getString(queJson, "0"));

            JSONObject objetoAcambiar = jsonGuardado.getJSONObject(0);

            objetoAcambiar.put(queDato, valorDato);

            // GUARDA EL DATO DE NUEVO

            jsonCambiado.put(objetoAcambiar);

            SharedPreferences.Editor mieditor=guarda.edit();

            mieditor.putString(queJson,jsonCambiado.toString());

            mieditor.apply();


        }catch (Exception e){

            System.out.println("ERROR AL CAMBIAR JSON: "+e.getMessage());

        }


    }





}
