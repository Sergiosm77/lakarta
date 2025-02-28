package sarao.digital.lakarta;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Menu_Usuario extends AppCompatActivity {

    Context contexto;

    private InputMethodManager imm;
    ProgressBar barraProgreso;
    private LayoutInflater inflador;

    LinearLayout pantallaMenu;
    CardView fondoAvatar;

    TextView nombreUsuario, emailUsuario, salir, cambiaPass,cerrarSesion;
    Usuario miUsuario;

    String user, pass, alses, alsesk, miNuevoAvatar;

    LinearLayout cambiaAlergenos;

    private Toast mensajePop;
    private String mensajeAlerta="";
    private String misAlergenos;

    Herramientas herramientas;

    GridLayout contenedorAlergenos;

    Server_RecibeDatos serverRecibeDatos;
    Server_EnvioDatos serverEnviaDatos;

    Alergenos[] losAlergenos;

    ImageView avatar;
    int cambiaColorChico,cambiaColorChica, fondoAvatarColorChico,fondoAvatarColorChica, avatarElegido;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_usuario);

        inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        nombreUsuario = findViewById(R.id.usuario_nombre);
        emailUsuario = findViewById(R.id.usuario_email);
        avatar=findViewById(R.id.avatar);
        fondoAvatar=findViewById(R.id.fondo_avatar);
        salir=findViewById(R.id.salir_usuario);
        cambiaPass=findViewById(R.id.cambio_pass);
        contenedorAlergenos=findViewById(R.id.contenedor_misalergenos);
        cambiaAlergenos=findViewById(R.id.cambia_alergenos);
        cerrarSesion=findViewById(R.id.cerrar_ses_usuario);
        pantallaMenu=findViewById(R.id.pantalla_menu1);
        barraProgreso = findViewById(R.id.barraprogreso_usuario);

        herramientas=new Herramientas();



        pantallaMenu.setVisibility(View.GONE);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        contexto=this;

        serverRecibeDatos =new Server_RecibeDatos(this);

        cargaUsuario();

        serverEnviaDatos =new Server_EnvioDatos(null, this, this, inflador);

        if(user.equals("0")){

            Intent miIntent = new Intent(this, Login_Usuario.class);

            startActivity(miIntent);

            finish();

        }else{

            cargaPantalla();

        }

    }

    private class recibeDatosUsuario extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            barraProgreso.setVisibility(View.VISIBLE);

        }

        @Override

        protected String doInBackground(String... strings) {

            if(compruebaConexion()) {

                serverRecibeDatos.compruebaSesionUsuario(user, alses,alsesk);

                int contador = 0;

                while (serverRecibeDatos.respuesta.equals("no") && contador < 10) {

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador++;

                }

                return serverRecibeDatos.respuesta;
            }else{

                return "sinconexion";
            }

        }

        protected void onPostExecute(String resultado) {



            System.out.println("RESPUESTA USUARIO: "+resultado);

            if(resultado.equals("ok")){

                cargaPantalla();


            }else if(resultado.equals("sinconexion")){

                cargaPantalla();


                mensajeAlerta = getString(R.string.sin_internet);
                ponAlerta();


            }else if(resultado.equals("nok")){

               herramientas.cierraSesionUsuario(contexto);

               Intent miIntent = new Intent(getApplicationContext(), Login_Usuario.class);
               startActivity(miIntent);

               finish();

            }else{

                mensajeAlerta = getString(R.string.error_conexion);
                ponAlerta();

            }

        }

    }

    private void cargaPantalla(){

        barraProgreso.setVisibility(View.GONE);

        pantallaMenu.setVisibility(View.VISIBLE);

        cargaUsuarioGuardado();

        //miUsuario= serverRecibeDatos.miUsuario;

        nombreUsuario.setText(user);
        emailUsuario.setText(miUsuario.email);

        if(miUsuario.avatar.startsWith("chico")){

            avatar.setImageResource(R.drawable.boy);

            avatar.setColorFilter((Integer.parseInt(miUsuario.avatar.substring(5,14))-1000000000));
            fondoAvatar.setCardBackgroundColor((Integer.parseInt(miUsuario.avatar.substring(14))-1000000000));

        }else if(miUsuario.avatar.startsWith("chica")){

            avatar.setImageResource(R.drawable.girl);
            avatar.setColorFilter((Integer.parseInt(miUsuario.avatar.substring(5,14))-1000000000));
            fondoAvatar.setCardBackgroundColor((Integer.parseInt(miUsuario.avatar.substring(14))-1000000000));

        }

        cargaAlergenos();
        cargaMisAlergenos();
        ponAlergenos(misAlergenos);

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,salir);

                finish();
            }
        });

        cambiaPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, cambiaPass);

                //TODO
                if(compruebaConexion()){

                    veAcambioPass(cambiaPass);


                }else{

                    mensajeAlerta=getResources().getString(R.string.sin_internet);
                    ponAlerta();
                    activaBoton(true, cambiaPass);

                }

                System.out.println("ENVIAR CAMBIOS");
            }
        });

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                activaBoton(false, avatar);
                cambiaAvatar(avatar, fondoAvatar);
            }
        });

        cambiaAlergenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, cambiaAlergenos);

                abreFichaAlergenos(cambiaAlergenos);
            }
        });

        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cerrarSesion);

                herramientas.cierraSesionUsuario(contexto);

                finish();

            }
        });
    }

    private void veAcambioPass(View boton){

        Intent miIntent = new Intent(this, Cambia_Password.class);

        miIntent.putExtra("TIPOCAMBIOPASS","nueva");
        miIntent.putExtra("QUIENCAMBIAPASS","usuario");
        miIntent.putExtra("EMAILCAMBIOPASS",miUsuario.email);

        startActivity(miIntent);

        activaBoton(true, boton);

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

    public boolean compruebaConexion(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo estadoRed = connectivityManager.getActiveNetworkInfo();

        if (estadoRed == null || !estadoRed.isConnected()) {

            return false;

        }else{

            return true;
        }

    }

    private void abreFichaAlergenos(final View boton){

        View alertLayout = inflador.inflate(R.layout.emerg_alergenos, null);

        TextView aceptar=alertLayout.findViewById(R.id.aceptar);
        TextView cancelar=alertLayout.findViewById(R.id.cancelar);
        final LinearLayout listaAlergenos=alertLayout.findViewById(R.id.lista_alergenos);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setCancelable(true);

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        alert.setView(alertLayout);

        final AlertDialog dialog = alert.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if(losAlergenos!=null) {

            for (int i = 0; i < losAlergenos.length; i++) {

                View unidadAlergeno = inflador.inflate(R.layout.unidad_seleccion_alergeno, null);

                ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.imagen_alergeno);
                TextView nombreAlergeno = unidadAlergeno.findViewById(R.id.nombre_alergeno);
                TextView detalleAlergeno = unidadAlergeno.findViewById(R.id.detalle_alergeno);
                CheckBox alergenoCheck = unidadAlergeno.findViewById(R.id.checkBox_alergeno);

                if (misAlergenos.length() > 0) {
                    for (int e = 0; e < misAlergenos.length(); e++) {

                        if (losAlergenos[i].codigo_alergeno.charAt(0) == misAlergenos.charAt(e)) {

                            alergenoCheck.setChecked(true);
                        }
                    }
                }

                Glide.with(this)
                        .load(losAlergenos[i].imagen_alergeno)
                        .into(imagenAlergeno);

                nombreAlergeno.setText(losAlergenos[i].nombre_alergeno);
                detalleAlergeno.setText(losAlergenos[i].detalle_alergeno);

                listaAlergenos.addView(unidadAlergeno);

            }
        }



        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
        }

        aceptar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                misAlergenos="";

                contenedorAlergenos.removeAllViews();

                int borrador=0;


                for(int e=0;e<listaAlergenos.getChildCount();e++){

                    CheckBox chekeado=listaAlergenos.getChildAt(e).findViewById(R.id.checkBox_alergeno);

                    if(chekeado.isChecked()){

                        if(borrador==0){

                            borrador=1;
                            contenedorAlergenos.removeAllViews();
                        }

                        misAlergenos += losAlergenos[e].codigo_alergeno;

                    }
                }

                guardaMisAlergenos(misAlergenos);

                ponAlergenos(misAlergenos);

                activaBoton(true,boton);

                dialog.cancel();


            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(true,boton);
                dialog.cancel();


            }
        });


        dialog.show();



        //new Hilo1().run(listaAlergenos);




        //blurview.setVisibility(View.VISIBLE);


    }

    public void cargaAlergenos(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        try{

            JSONArray recuperaAlergenos=new JSONArray(guarda.getString("ALERGENOS","0"));

            losAlergenos=new Alergenos[recuperaAlergenos.length()];

            for(int i=0;i<losAlergenos.length;i++) {

                losAlergenos[i]=new Alergenos();

                JSONObject object = recuperaAlergenos.getJSONObject(i);

                losAlergenos[i].nombre_alergeno = object.getString("nombre");

                losAlergenos[i].detalle_alergeno = object.getString("detalle");
                losAlergenos[i].codigo_alergeno = object.getString("codigo");
                losAlergenos[i].imagen_alergeno = object.getString("imagen");

            }

        }catch (JSONException e){

            losAlergenos=new Alergenos[0];

        }

    }

    public void ponAlergenos(String queAlergeno){

        for (int e = 0; e < queAlergeno.length(); e++) {

            for(int d=0;d<losAlergenos.length;d++){

                if( queAlergeno.charAt(e)==losAlergenos[d].codigo_alergeno.charAt(0)){

                    View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad_texto, null);

                    ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);
                    TextView nombreAlergeno=unidadAlergeno.findViewById(R.id.alergeno_nombre);

                    nombreAlergeno.setText(losAlergenos[d].nombre_alergeno);


                    Glide.with(contexto)
                            .load(losAlergenos[d].imagen_alergeno)
                            .error(R.drawable.noimage)
                            .into(imagenAlergeno);

                    contenedorAlergenos.addView(unidadAlergeno);
                    break;

                }

            }

        }
    }
/*
    public void enviaCambiosUsuario(final String detalle,
                                         final String tipoComida,
                                         final int telefono,
                                         final int puesto,
                                         final BlurView blurview){

        final AlertDialog enviando=esperandoEnvio();

        enviando.show();


        Map<String, String> parametros = new HashMap<>();
        parametros.put("codigoRest", "");

        JSONObject parametrosEnvio = new JSONObject(parametros);

        String url=contexto.getString(R.string.servidor_cambia_datos_usuario);

        RequestQueue rq= Volley.newRequestQueue(contexto);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST, url,parametrosEnvio, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONArray datosJsonRest = response.getJSONArray("datos_user");

                    if (datosJsonRest.length() > 0) {

                        guardaRestaurante(datosJsonRest);

                        EnvioDatos.esperaTiempo inicia = new EnvioDatos.esperaTiempo();

                        inicia.execute(enviando);

                    } else {

                        ponAlerta(mensajePop,weakContext.get().getString(R.string.datos_no_cambiados));

                        blurview.setVisibility(View.GONE);

                        enviando.cancel();

                    }

                } catch (Exception e) {

                    ponAlerta(mensajePop,weakContext.get().getString(R.string.datos_no_cambiados));

                    blurview.setVisibility(View.GONE);

                    enviando.cancel();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                enviando.cancel();
                blurview.setVisibility(View.GONE);
                ponAlerta(mensajePop,weakContext.get().getString(R.string.datos_no_cambiados));

            }
        }

        );

        rq.add(sr);




    }

 */

    private AlertDialog esperandoEnvio(){

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

    public void guardaMisAlergenos(String  alergenosGuardar){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putString("MISALERGENOS",alergenosGuardar);

        mieditor.apply();


    }

    public void cargaMisAlergenos(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        misAlergenos=guarda.getString("MISALERGENOS","");

    }

    public void cargaUsuario(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_USUARIO","0");
        alses=guarda.getString("ALSES_USUARIO","0");
        alsesk=guarda.getString("ALSESK_USUARIO","0");



    }

    private void cambiaAvatar(final ImageView avatarActual, final CardView fondoAvatar){

        cambiaColorChica=18;
        cambiaColorChico=18;
        fondoAvatarColorChico=18;
        fondoAvatarColorChica=18;

        avatarElegido=0;

        miNuevoAvatar=miUsuario.avatar;

        LayoutInflater inflater = getLayoutInflater();
        final View alertAvatar = inflater.inflate(R.layout.emerg_cambiar_avatar, null);

        final TextView cancelar=alertAvatar.findViewById(R.id.cancelar);
        final TextView confirmar=alertAvatar.findViewById(R.id.confirmar);
        final ImageView chico=alertAvatar.findViewById(R.id.chico);
        final ImageView chica=alertAvatar.findViewById(R.id.chica);
        final CardView fondoAvatarChico=alertAvatar.findViewById(R.id.fondo_avatar_chico);
        final CardView fondoAvatarChica=alertAvatar.findViewById(R.id.fondo_avatar_chica);
        LinearLayout fondoAvatares=alertAvatar.findViewById(R.id.fondo_avatares);

        final int [] loscolores=new int[20];

        loscolores[0]=R.array.reds;
        loscolores[1]=R.array.pinks;
        loscolores[2]=R.array.purples;
        loscolores[3]=R.array.deep_purples;
        loscolores[4]=R.array.indigos;
        loscolores[5]=R.array.blues;
        loscolores[6]=R.array.light_blues;
        loscolores[7]=R.array.cyans;
        loscolores[8]=R.array.teals;
        loscolores[9]=R.array.greens;
        loscolores[10]=R.array.light_greens;
        loscolores[11]=R.array.limes;
        loscolores[12]=R.array.yellows;
        loscolores[13]=R.array.ambers;
        loscolores[14]=R.array.oranges;
        loscolores[15]=R.array.deep_oranges;
        loscolores[16]=R.array.browns;
        loscolores[17]=R.array.greys;
        loscolores[18]=R.array.blanco;
        loscolores[19]=R.array.negro;

        fondoAvatares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(avatarElegido==1) {

                    fondoAvatarColorChico++;

                    if (fondoAvatarColorChico > 19) {

                        fondoAvatarColorChico = 0;

                    }

                    int elColorFondo = getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChico]).getColor(0, 0);

                    int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChico]).getColor(0, 0);

                    fondoAvatarChico.setCardBackgroundColor(elColorFondo);

                    miNuevoAvatar="chico"+(elColor+1000000000)+(elColorFondo+1000000000);

                    //miNuevoAvatar="chico"+elColor;

                }else if(avatarElegido==2) {

                    fondoAvatarColorChica++;

                    if (fondoAvatarColorChica > 19) {

                        fondoAvatarColorChica = 0;

                    }

                    int elColorFondo = getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChica]).getColor(0, 0);

                    int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChica]).getColor(0, 0);

                    fondoAvatarChica.setCardBackgroundColor(elColorFondo);

                    miNuevoAvatar="chica"+(elColor+1000000000)+(elColorFondo+1000000000);


                }


            }
        });


        chico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                avatarElegido=1;

                if(fondoAvatarChica.getScaleX()==1.0) {

                    fondoAvatarChica.setCardBackgroundColor(getResources().getColor(R.color.colorGrisSuave, null));
                    chica.setColorFilter(getResources().getColor(R.color.colorBlanco, null));
                    fondoAvatarChica.animate().scaleX(0.7f).setDuration(200);
                    fondoAvatarChica.animate().scaleY(0.7f).setDuration(200);
                }

                if(fondoAvatarChico.getScaleX()!=1.0){

                    //chico.animate().scaleX(1.2f).setDuration(200);
                    //chico.animate().scaleY(1.2f).setDuration(200);

                    ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                            fondoAvatarChico,
                            PropertyValuesHolder.ofFloat("scaleX", 1f),
                            PropertyValuesHolder.ofFloat("scaleY", 1f));
                    scaleDown.setDuration(200);

                    //scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
                    scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

                    scaleDown.start();

                }

                cambiaColorChico++;

                if(cambiaColorChico>19) {

                    cambiaColorChico=0;

                }

                int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChico]).getColor(0, 0);

                chico.setColorFilter(elColor);

                int elColorFondo=getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChico]).getColor(0, 0);

                fondoAvatarChico.setCardBackgroundColor(elColorFondo);

                miNuevoAvatar="chico"+(elColor+1000000000)+(elColorFondo+1000000000);

            }
        });

        chica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                avatarElegido=2;

                if(fondoAvatarChico.getScaleX()==1.0) {

                    fondoAvatarChico.setCardBackgroundColor(getResources().getColor(R.color.colorGrisSuave, null));
                    chico.setColorFilter(getResources().getColor(R.color.colorBlanco, null));
                    fondoAvatarChico.animate().scaleX(0.7f).setDuration(200);
                    fondoAvatarChico.animate().scaleY(0.7f).setDuration(200);
                }

                if(fondoAvatarChica.getScaleX()!=1.0){

                    fondoAvatarChica.animate().scaleX(1.2f).setDuration(200);
                    fondoAvatarChica.animate().scaleY(1.2f).setDuration(200);
                    fondoAvatarChica.animate().scaleX(1f).setDuration(200);
                    fondoAvatarChica.animate().scaleY(1f).setDuration(200);

                }

                cambiaColorChica++;

                if(cambiaColorChica>19) {

                    cambiaColorChica=0;

                }

                int elColor=getApplicationContext().getResources().obtainTypedArray(loscolores[cambiaColorChica]).getColor(0, 0);

                chica.setColorFilter(elColor);
                int elColorFondo=getApplicationContext().getResources().obtainTypedArray(loscolores[fondoAvatarColorChica]).getColor(0, 0);

                fondoAvatarChica.setCardBackgroundColor(elColorFondo);

                miNuevoAvatar="chica"+(elColor+1000000000)+(elColorFondo+1000000000);

            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertAvatar);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(true,avatarActual);
                dialog.cancel();

            }
        });

        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(compruebaConexion()) {

                    activaBoton(false, confirmar);

                    cargaUsuario();

                    serverEnviaDatos.enviaCambiosAvatar(user, alses, alsesk,miNuevoAvatar, confirmar, avatarActual,fondoAvatar);

                    activaBoton(true, avatarActual);

                    dialog.cancel();

                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    ponAlerta();

                }

            }
        });


        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,avatarActual);


            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    public void cargaUsuarioGuardado(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        miUsuario=new Usuario();

        try {

            JSONArray usuarioCargado = new JSONArray(guarda.getString("USUARIO_GUARDADO", "0"));

            JSONObject objectUser = usuarioCargado.getJSONObject(0);

            System.out.println("CONTENIDO USUARIO: "+objectUser);

            miUsuario.email = objectUser.getString("email");
            miUsuario.avatar = objectUser.getString("avatar");
            miUsuario.puede_comentar = objectUser.getInt("puede_comentar");

        }catch (Exception e){

            System.out.println("ERROR AL CARGAR USUARIO: "+e.getMessage());

        }

    }


}

