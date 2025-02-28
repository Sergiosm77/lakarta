package sarao.digital.lakarta;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.File;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Menu_Empresa extends AppCompatActivity {


    // pantalla ------

    LinearLayout pantallaMenu;

    // --------------

    Herramientas herramientas;

    private long mLastClickTime = 0;

    TextView textocontrato, cuentaCaducada, contratoHasta, verMasDetalle, cerrarSesion;

    LinearLayout persColores, modificaKarta, contactoKarta, previoKarta, editaRestaurante, crearPDF, confUsuario, cambiaPass;

    Context contexto;

    int viendo=0;

    Uri miPath;
    File image;
    ContentResolver resolver;

    int puesto=0;
    int estaActualizando=0;

    String user, alses, alsesk;

    boolean imagenDeFoto=false;

    Kartas[] laKartaNivel1,laKartaNivel1copia;

    ProgressBar barraProgreso;

    ImageView imagenRestaurante,cambiaImagen,modificaImagen,logoRestaurante;

    LinearLayout rutacontenedorCategorias,contenedorEmergente,nueva_categoria, estadoEmpresa;
    LayoutInflater inflador;
    ScrollView scrollNivel1;

    private TextView estado, panelActualizando, enviaDatosEmpresa,nombreUsuario,
            nombreRestaurante,tipoComida, losTags, telefonoRestaurante,detalleRestaurante,salirUsuario;

    private Toast mensajePop;
    private String mensajeAlerta="";

    final DecimalFormat form = new DecimalFormat("0.00");

    public static Restaurantes miRestaurante;
    Limitaciones misLimitaciones;

    Server_EnvioDatos serverEnvioDatos;
    Server_RecibeDatos serverRecibeDatos;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_empresa);

        pantallaMenu=findViewById(R.id.pantalla_menu1);

        pantallaMenu.setVisibility(View.GONE);

        rutacontenedorCategorias = findViewById(R.id.contenedor_categorias);
        contenedorEmergente=findViewById(R.id.layout_emergente1);
        scrollNivel1=findViewById(R.id.scroll_menu_usuario);
        estadoEmpresa=findViewById(R.id.estado_empresa);

        nueva_categoria=findViewById(R.id.nueva_categoria);

        persColores=findViewById(R.id.personaliza_colores);
        modificaKarta=findViewById(R.id.modificar_karta);

        nombreUsuario = findViewById(R.id.usuario_nom_usuario);
        nombreRestaurante = findViewById(R.id.usuario_nom_restaurante);
        detalleRestaurante = findViewById(R.id.usuario_detalle);
        tipoComida=findViewById(R.id.usuario_tipo_comida);
        losTags=findViewById(R.id.usuario_tags);
        imagenRestaurante = findViewById(R.id.imagen_usuario);
        logoRestaurante=findViewById(R.id.logo_usuario);
        telefonoRestaurante = findViewById(R.id.usuario_telefono);
        editaRestaurante = findViewById(R.id.edita_datos);
        confUsuario=findViewById(R.id.configura_usuarios);
        salirUsuario=findViewById(R.id.salir_usuario);
        estado=findViewById(R.id.estado);
        panelActualizando =findViewById(R.id.actualizando);
        cerrarSesion=findViewById(R.id.cerrar_ses_empresa);

        textocontrato=findViewById(R.id.contrato_hasta);
        contratoHasta=findViewById(R.id.fecha_fin);
        cuentaCaducada=findViewById(R.id.cuenta_caducada);
        contactoKarta=findViewById(R.id.contacto_lakarta);

        verMasDetalle=findViewById(R.id.ver_mas_detalle);
        previoKarta=findViewById(R.id.ver_previo);

        cambiaPass=findViewById(R.id.cambio_pass);

        crearPDF=findViewById(R.id.crear_pdf);

        inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        barraProgreso = findViewById(R.id.barraprogreso_usuario);

        barraProgreso.setVisibility(View.GONE);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        contexto=this;

        herramientas=new Herramientas();

        cargaUsuarioEmpresa();

        salirUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,salirUsuario);

                finish();

            }
        });

        if(user.equals("0")){

            Intent miIntent = new Intent(this, Login_Empresa.class);

            startActivity(miIntent);

            finish();

        }else {

            //envioDatos=new EnvioDatos(null,Menu_Usuario.this,miRestaurante,Menu_Usuario.this,inflador,user,pass);

            serverRecibeDatos = new Server_RecibeDatos(this);

            recibeDatos inicia=new recibeDatos();
            inicia.execute();

        }

    }

    private class recibeDatos extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            barraProgreso.setVisibility(View.VISIBLE);

        }

        @Override

        protected String doInBackground(String... strings) {

            if(compruebaConexion()) {

                serverRecibeDatos.recibeDatosUserEmpresa(user, alses, alsesk);

                int contador = 0;

                while (serverRecibeDatos.respuesta.equals("no") && contador < 20) {

                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    contador++;

                }

                System.out.println("RESPUESTA PARA LOGIN: " + serverRecibeDatos.respuesta);

                return serverRecibeDatos.respuesta;
            }else{

                return "sinconexion";
            }

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);

            if(resultado.equals("ok")){

                cargaPantalla();


            }else if(resultado.equals("sinconexion")){

                mensajeAlerta = getString(R.string.sin_internet);
                ponAlerta();

            }else if(resultado.equals("error")){

                herramientas.cierraSesionEmpresa(getApplicationContext());
                Intent miIntent = new Intent(getApplicationContext(), Login_Empresa.class);
                startActivity(miIntent);
                finish();

            }else{

                herramientas.cierraSesionEmpresa(contexto);

                finish();

            }

        }
    }

    private class IniciaPagina extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {

            barraProgreso.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            int contador=0;

            while(serverRecibeDatos.respuesta.equals("no") && contador<10) {

                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    System.out.println(e);
                }
                contador++;

            }

            return serverRecibeDatos.respuesta;

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);

            if(resultado.equals("ok")) {

                miRestaurante= serverRecibeDatos.miRestaurante;
                laKartaNivel1= serverRecibeDatos.laKartaNivel1;

                // copia de nivel1 -----------------

                laKartaNivel1copia=new Kartas[laKartaNivel1.length];

                for(int i=0;i<laKartaNivel1.length;i++){

                    laKartaNivel1copia[i]=new Kartas(laKartaNivel1[i]);

                }

                // ---------------------------------------


                serverEnvioDatos =new Server_EnvioDatos(null, Menu_Empresa.this, Menu_Empresa.this,inflador);

                cargaPantalla();


            }else{


                System.out.println("RESULTADO: "+resultado);


            }

        }
    }

    private void cargaPantalla(){

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        SimpleDateFormat formato = new SimpleDateFormat(

                "EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");



        pantallaMenu.setVisibility(View.VISIBLE);

        serverRecibeDatos.cargaNivelGuardado("nivel1");
        serverRecibeDatos.cargaMiRestGuardado();

        miRestaurante = serverRecibeDatos.miRestaurante;
        misLimitaciones = serverRecibeDatos.misLimitaciones;
        laKartaNivel1 = serverRecibeDatos.laKartaNivel1;

        if(misLimitaciones.activo==0){

            cuentaCaducada.setText(getResources().getString(R.string.cuenta_desactivada));

        }else{

            cuentaCaducada.setVisibility(View.GONE);
        }

        if(misLimitaciones.fechaFin!=null && !misLimitaciones.fechaFin.equals("")){

                try {

                    String formateaFecha = formato.format(myFormat.parse(misLimitaciones.fechaFin));

                    if(dimeCuando(misLimitaciones.fechaFin)<0){

                        textocontrato.setVisibility(View.GONE);

                        String haCaducado=getString(R.string.cuenta_caducada)+" "+formateaFecha;

                        contratoHasta.setText(haCaducado);
                        contratoHasta.setTextColor(Color.RED);

                    }else {

                        contratoHasta.setText(formateaFecha);

                    }

                } catch (Exception e) {

                    contratoHasta.setVisibility(View.GONE);
                    e.printStackTrace();
                }





        }else{

            contratoHasta.setVisibility(View.GONE);
        }

        serverEnvioDatos =new Server_EnvioDatos(null,this,this,inflador);

        // copia de nivel1 -----------------

        laKartaNivel1copia = new Kartas[laKartaNivel1.length];

        for (int i = 0; i < laKartaNivel1.length; i++) {

            laKartaNivel1copia[i] = new Kartas(laKartaNivel1[i]);

        }

        // ---------------------------------------

        nombreUsuario.setText(user);

        nombreRestaurante.setText(miRestaurante.nombre);
        detalleRestaurante.setText(miRestaurante.detalle);
        losTags.setText(miRestaurante.tags);

        System.out.println("LINEAS: "+detalleRestaurante.getLineCount());

        detalleRestaurante.post(new Runnable() {
            @Override
            public void run() {
                if (detalleRestaurante.getLineCount()>4) {

                    verMasDetalle.setVisibility(View.VISIBLE);

                    verMasDetalle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(viendo==0) {

                                detalleRestaurante.setMaxLines(500);

                                verMasDetalle.setText(getResources().getString(R.string.ver_menos_info));

                                viendo=1;

                            }else{

                                detalleRestaurante.setMaxLines(4);

                                verMasDetalle.setText(getResources().getString(R.string.ver_mas_info));

                                viendo=0;

                            }
                        }
                    });

                }else{

                    verMasDetalle.setVisibility(View.GONE);

                }
            }
        });



        tipoComida.setText(miRestaurante.tipo_comida);
        telefonoRestaurante.setText(miRestaurante.telefono + "");
        if(miRestaurante.online==1) {
            estado.setText("ONLINE");
            estado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorVerde,null)));
            //estado.setBackgroundColor(Color.GREEN);
        }else{
            estado.setText("OFFLINE");
            estado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRojo,null)));
            //estado.setBackgroundColor(Color.RED);

        }

        if(miRestaurante.actualizando==1) {
            panelActualizando.setText(getResources().getString(R.string.mostrando_karta));
            panelActualizando.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorVerde,null)));
            //estado.setBackgroundColor(Color.GREEN);
        }else{
            panelActualizando.setText(getResources().getString(R.string.actualizando_karta));
            panelActualizando.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRojo,null)));
            //estado.setBackgroundColor(Color.RED);

        }

        Glide.with(getApplicationContext())
                .load(miRestaurante.imagen_principal)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.noimage)
                .into(imagenRestaurante);

        Glide.with(getApplicationContext())
                .load(miRestaurante.logo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.noimage)
                .into(logoRestaurante);

        cambiaPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(compruebaConexion()){

                    veAcambioPass();


                }else{

                    mensajeAlerta=getResources().getString(R.string.sin_internet);
                    ponAlerta();
                    activaBoton(true, cambiaPass);

                }

                System.out.println("ENVIAR CAMBIOS");
            }
        });

        estadoEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,estadoEmpresa);

                cambiaEstadoEmpresa(estadoEmpresa);

            }
        });

        editaRestaurante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,editaRestaurante);

                cambiaDatosEmpresa(editaRestaurante);

            }
        });

        confUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                configuraUsuario();

            }
        });

        previoKarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                irAprevio();

            }
        });

        crearPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                veAcrearPDF();

            }
        });

        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cerrarSesion);

                herramientas.cierraSesionEmpresa(contexto);

                finish();

            }
        });

        logoRestaurante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,logoRestaurante);

                cambiaLogoEmpresa(logoRestaurante);

            }
        });

        imagenRestaurante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,imagenRestaurante);

                cambiaImagenPrincipalUsuario(imagenRestaurante);

            }
        });

        persColores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                personalizaColores();

            }
        });

        modificaKarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                personalizaKarta();

            }
        });

        contactoKarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","lakarta.app@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Cliente "+miRestaurante.nombre);
                startActivity(Intent.createChooser(emailIntent,  contexto.getString(R.string.contacto_mediante)));

            }
        });

    }

    private void veAcambioPass(){

        Intent miIntent = new Intent(this, Cambia_Password.class);

        miIntent.putExtra("TIPOCAMBIOPASS","nueva");
        miIntent.putExtra("QUIENCAMBIAPASS","empresa");
        miIntent.putExtra("EMAILCAMBIOPASS",miRestaurante.email);
        startActivity(miIntent);


    }

    private void veAcrearPDF(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){

            Intent miIntent = new Intent(this, Crear_PDF.class);

            startActivity(miIntent);
        } else{
            mensajeAlerta="TU VERSION ANDROID NO ADMINTE CREAR PDF";
            ponAlerta();
        }



    }

    private void personalizaColores(){

        Intent miIntent = new Intent(this, Personaliza_Colores.class);

        startActivity(miIntent);

    }

    private void personalizaKarta(){

        Intent miIntent = new Intent(this, Edita_Nivel1.class);

        startActivity(miIntent);

    }

    private void configuraUsuario(){

        Intent miIntent = new Intent(this, Configura_Usuarios.class);

        startActivity(miIntent);

    }

    private void cambiaEstadoEmpresa(final View boton){

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_solo_actividad, null);

        enviaDatosEmpresa =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);

        final TextView online=alertLayout.findViewById(R.id.estado);
        final TextView actualizando=alertLayout.findViewById(R.id.actualizando);

        if(miRestaurante.online==1){

            online.setText("ONLINE");
            online.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorVerde,null)));
            puesto=1;
            //estado.setBackgroundColor(Color.GREEN);
        }else{
            online.setText("OFFLINE");
            online.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRojo,null)));
            //estado.setBackgroundColor(Color.RED);
            puesto=0;

        }

        if(miRestaurante.actualizando==1){

            actualizando.setText(getResources().getString(R.string.mostrando_karta));
            actualizando.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorVerde,null)));
            estaActualizando=1;
            //estado.setBackgroundColor(Color.GREEN);
        }else{
            actualizando.setText(getResources().getString(R.string.actualizando_karta));
            actualizando.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRojo,null)));
            //estado.setBackgroundColor(Color.RED);
            estaActualizando=0;

        }

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                return false;
            }
        });

        activaBoton(false, enviaDatosEmpresa);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(puesto==0){

                    online.setText("ONLINE");
                    online.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorVerde,null)));

                    puesto=1;

                    activaBoton(true, enviaDatosEmpresa);

                }else if(puesto==1){

                    online.setText("OFFLINE");
                    online.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRojo,null)));

                    puesto=0;

                    activaBoton(true, enviaDatosEmpresa);
                }
            }
        });

        actualizando.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(estaActualizando==0){

                    actualizando.setText(getResources().getString(R.string.mostrando_karta));
                    actualizando.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorVerde,null)));

                    estaActualizando=1;

                    activaBoton(true, enviaDatosEmpresa);

                }else if(estaActualizando==1){

                    actualizando.setText(getResources().getString(R.string.actualizando_karta));
                    actualizando.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRojo,null)));

                    estaActualizando=0;

                    activaBoton(true, enviaDatosEmpresa);
                }
            }
        });


        enviaDatosEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(compruebaConexion()) {

                    activaBoton(false, enviaDatosEmpresa);

                    cargaAlses();

                    serverEnvioDatos.enviaCambiosActividad(user, alses, alsesk, puesto, estaActualizando, estado, panelActualizando);

                    activaBoton(true, boton);

                    dialog.cancel();

                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    activaBoton(true, enviaDatosEmpresa);
                    ponAlerta();

                }

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cancelar);

                dialog.cancel();

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void cambiaDatosEmpresa(final View boton){

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_solo_datos, null);

        enviaDatosEmpresa =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        final TextView detalleUsuario=alertLayout.findViewById(R.id.detalle_usuario);
        final TextView tipoComidaEdit=alertLayout.findViewById(R.id.tipo_comida);
        final TextView tagsUsuario=alertLayout.findViewById(R.id.tags_usuario);
        final EditText telefonoUsuario=alertLayout.findViewById(R.id.usuario_telefono);


        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                return false;
            }
        });

        detalleUsuario.setText(miRestaurante.detalle);


        tipoComidaEdit.setText(miRestaurante.tipo_comida);
        telefonoUsuario.setText(String.valueOf(miRestaurante.telefono));
        tagsUsuario.setText(String.valueOf(miRestaurante.tags));

        activaBoton(false, enviaDatosEmpresa);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        telefonoUsuario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                activaBoton(true, enviaDatosEmpresa);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*

        tipoComidaEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                activaBoton(true, enviaDatosEmpresa);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

         */

        tipoComidaEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,tipoComidaEdit);

                introduceTexto(tipoComidaEdit,InputType.TYPE_CLASS_TEXT,enviaDatosEmpresa,0);

            }
        });

        tagsUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,tagsUsuario);

                introduceTexto(tagsUsuario,InputType.TYPE_CLASS_TEXT,enviaDatosEmpresa,100);

            }
        });

        detalleUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,detalleUsuario);

                introduceTexto(detalleUsuario,InputType.TYPE_CLASS_TEXT,enviaDatosEmpresa,0);

            }
        });
/*
        detalleUsuario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                activaBoton(true, enviaDatosEmpresa);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

 */
/*
        detalleUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,detalleUsuario);

                introduceTexto(detalleUsuario,InputType.TYPE_CLASS_TEXT,enviaDatosCategoria);

            }
        });

        tipoComida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,tipoComida);

                introduceTexto(tipoComida,InputType.TYPE_CLASS_TEXT,enviaDatosCategoria);

            }
        });

        telefonoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,telefonoUsuario);

                introduceTexto(telefonoUsuario,InputType.TYPE_CLASS_NUMBER,enviaDatosCategoria);

            }
        });

 */

        enviaDatosEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(compruebaConexion()) {

                    activaBoton(false, enviaDatosEmpresa);

                    cargaAlses();

                    serverEnvioDatos.enviaCambiosEmpresa(user, alses, alsesk, detalleUsuario.getText().toString(), detalleRestaurante, tipoComidaEdit.getText().toString(), tipoComida,  tagsUsuario.getText().toString(), losTags,telefonoUsuario.getText().toString(), telefonoRestaurante);

                    activaBoton(true, boton);

                    dialog.cancel();

                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    activaBoton(true, enviaDatosEmpresa);
                    ponAlerta();

                }

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cancelar);

                dialog.cancel();

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void cambiaLogoEmpresa(final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_solo_imagen, null);

        TextView titulo=alertLayout.findViewById(R.id.titulo);
        enviaDatosEmpresa =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        cambiaImagen=alertLayout.findViewById(R.id.imagen);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);

        titulo.setText(getString(R.string.modificar_logo));


        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                Glide.with(getApplicationContext())
                        .load(miRestaurante.logo)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(cambiaImagen);
                return false;
            }
        });



        activaBoton(false, enviaDatosEmpresa);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();


        enviaDatosEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(compruebaConexion()) {

                    activaBoton(false, enviaDatosEmpresa);

                    cargaAlses();

                    serverEnvioDatos.enviaCambiosEmpresaLogo(miPath,user,alses,alsesk,logoRestaurante,miRestaurante.logo);

                    activaBoton(true, boton);

                    dialog.cancel();

                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    activaBoton(true, enviaDatosEmpresa);
                    ponAlerta();

                }

            }
        });

        modificaImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                buscaFoto();


            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cancelar);

                dialog.cancel();

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    private void cambiaImagenPrincipalUsuario(final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_solo_imagen, null);

        TextView titulo=alertLayout.findViewById(R.id.titulo);
        enviaDatosEmpresa =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        cambiaImagen=alertLayout.findViewById(R.id.imagen);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);

        titulo.setText(getString(R.string.modificar_imagen_principal));


        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                Glide.with(getApplicationContext())
                        .load(miRestaurante.imagen_principal)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(cambiaImagen);
                return false;
            }
        });



        activaBoton(false, enviaDatosEmpresa);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();


        enviaDatosEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(compruebaConexion()) {

                    activaBoton(false, enviaDatosEmpresa);

                    cargaAlses();

                    serverEnvioDatos.enviaCambiosEmpresaImagenPrincipal(miPath,user,alses,alsesk,imagenRestaurante,miRestaurante.imagen_principal);

                    activaBoton(true, boton);

                    dialog.cancel();

                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    activaBoton(true, enviaDatosEmpresa);
                    ponAlerta();

                }

            }
        });

        modificaImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                buscaFoto();


            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cancelar);

                dialog.cancel();

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public static String damePath(Context context, Uri contentUri) {

        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
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



    public void onResume(){

        super.onResume();

        serverEnvioDatos =new Server_EnvioDatos(null, Menu_Empresa.this, Menu_Empresa.this,inflador);
        System.out.println("RESUME");
    }
    public void cargaUsuarioEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");


    }

    public void cargaAlses(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        alses=guarda.getString("ALSES_EMPRESA","0");



    }



    public int cargaPosicionScroll(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        int posicion;

        posicion =guarda.getInt("posicionScroll",0);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putInt("posicionScroll",0);

        mieditor.apply();

        return posicion;

    }

    private void introduceTexto(final TextView elTexto,final int tipo,final View boton, final int maxCaracteres){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View introTexto = inflater.inflate(R.layout.entrada_texto, null);

        final TextView entradaTexto=introTexto.findViewById(R.id.recoge_texto);
        final ImageView validaTexto=introTexto.findViewById(R.id.valida_texto);

        if(maxCaracteres!=0){

            entradaTexto.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxCaracteres) });
        }

        activaBoton(false,validaTexto);

        AlertDialog.Builder ponTexto = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        ponTexto.setView(introTexto);
        // disallow cancel of AlertDialog on click of back button and outside touch
        ponTexto.setCancelable(true);

        final AlertDialog dialogoTexto = ponTexto.create();

        if(tipo!=InputType.TYPE_CLASS_TEXT) {

            entradaTexto.setInputType(tipo);

            String precio;

            try {
                precio=elTexto.getText().toString().replace(",", ".");
            }catch (Exception e){

                precio=elTexto.getText().toString();

            }

            if(precio.equals("")){

                precio="0";
            }


            if(Double.parseDouble(precio)!=0) {

                entradaTexto.setText(precio);

            }else{

                entradaTexto.setText("");
                entradaTexto.setHint(getString(R.string.intro_precio));
            }
        }else{

            entradaTexto.setText(elTexto.getText().toString());
        }



/*
        entradaTexto.setOnFocusChangeListener(new View.OnFocusChangeListener() { // ABRE EL TECLADO -----------------
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

 */



        dialogoTexto.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //imm.hideSoftInputFromWindow(entradaTexto.getWindowToken(), 0);

                activaBoton(true,elTexto);

            }
        });

        entradaTexto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                if(!validaTexto.isEnabled()) {

                    activaBoton(true, validaTexto);
                }

            }
        });

        validaTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(tipo!=InputType.TYPE_CLASS_TEXT) {

                    if(entradaTexto.getEditableText().toString().equals("")){

                        elTexto.setText(form.format(Double.parseDouble("0")));

                    }else {

                        elTexto.setText(form.format(Double.parseDouble(entradaTexto.getEditableText().toString())));

                    }

                }else{

                    elTexto.setText(entradaTexto.getEditableText().toString());
                }

                activaBoton(true,boton);

                activaBoton(true,elTexto);

                dialogoTexto.cancel();


            }
        });

        entradaTexto.setOnFocusChangeListener(new View.OnFocusChangeListener() { // ABRE EL TECLADO -----------------
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialogoTexto.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        entradaTexto.requestFocus(); //Asegurar que editText tiene focus


        dialogoTexto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogoTexto.show();


        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);



    }

    private void ponAlerta(){


        if(!mensajePop.getView().isShown()) {

            mensajePop.setText(mensajeAlerta);

            mensajePop.setGravity(Gravity.CENTER, 0, 0);
            TextView mensaje = mensajePop.getView().findViewById(android.R.id.message);
            mensaje.setGravity(Gravity.CENTER);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 110) {


            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                System.out.println("PERMISO CONCEDIDO");

                if (checkSelfPermission(
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 120);

                }else{

                    buscaFoto();
                }

            }else{

                avisoPermisos();
            }

        }

        if (requestCode == 120) {

            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                System.out.println("PERMISO CONCEDIDO");

             buscaFoto();

            }else{

                avisoPermisos();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void buscaFoto(){

        imagenDeFoto=false;

        if (checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);


        }else{

            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA}, 120);


            }else {

                Intent galleryintent = new Intent(Intent.ACTION_GET_CONTENT, null);
                galleryintent.setType("image/*");

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INTENT, galleryintent);
                chooser.putExtra(Intent.EXTRA_TITLE, "Seleccionar desde");

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // HASTA VERSION 29 ---------------------------------------------

                    System.out.println("SDK menor 30");

                    try{

                        //File storageDir = new File(Environment.getExternalStorageDirectory().toString(), Environment.DIRECTORY_PICTURES);
                        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "lakarta");
                        if (!storageDir.exists()) {

                            storageDir.mkdirs();
                            System.out.println("intenta crear directorio");

                        }
                        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                        image = new File(storageDir+ File.separator+"image"+(new Random()).nextInt(999999999)+".jpg");

                        miPath = Uri.fromFile(image);

                        System.out.println("DONDE MIPATH1: " + miPath.getPath());

                        //takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, miPath);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, miPath);


                        Intent[] intentArray = {cameraIntent};
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                        startActivityForResult(Intent.createChooser(chooser, "Seleccione"), 10);

                    }catch (Exception e){

                        System.out.println("ERROR "+e.getMessage());

                    }


                }else{  // VERSION 30+ ---------------------------------------------

                    System.out.println("SDK 30");

                    resolver = getContentResolver();
                    ContentValues contentValues = new ContentValues();

                    try{

                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"imagen"+(new Random()).nextInt(999999999)+".jpg");
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+File.separator+"lakarta");

                        miPath = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

                        //takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, miPath);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, miPath);

                        Intent[] intentArray = {cameraIntent};
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                        startActivityForResult(Intent.createChooser(chooser, "Seleccione"), 10);

                    }catch (Exception e){

                        System.out.println("ERROR "+e.getMessage());
                        borraTemporales();

                    }

                }

            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("resultado camara :"+requestCode+" "+resultCode);

        if (requestCode == 10) {

            activaBoton(true,modificaImagen);

            try {

                if(resultCode==0) {

                    System.out.println("CANCELADO/ERROR CAPTURA");

                    borraTemporales();

                    miPath = null;

                }else{

                    if(data!=null && data.getData()!=null) {

                        imagenDeFoto=false;

                        borraTemporales();

                        miPath = data.getData();

                        cambiaImagen.setImageURI(miPath);
                        activaBoton(true, enviaDatosEmpresa);

                        System.out.println("IMAGEN DE DIRECTORIO: "+miPath);

                    }else{

                        imagenDeFoto=true;

                        cambiaImagen.setImageURI(miPath);
                        activaBoton(true, enviaDatosEmpresa);

                        System.out.println("IMAGEN DE CAMARA: "+miPath);

                    }

                }

            }catch (Exception e){

                System.out.println("ERROR RESULTADO "+e.getCause()+ e.getMessage());

                borraTemporales();

            }
        }

    }

    public void borraTemporales(){

        if(Build.VERSION.SDK_INT<30) { // HASTA VERSION 29 -----------------------

            String dirImagen=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "lakarta";

            if (image != null && image.length()==0) {

                if (dirImagen.equals(image.getParent())) {
                    image.delete();
                    System.out.println("BORRA TEMPORAL :" + image.getPath());
                } else {

                    System.out.println("NO BORRA TEMPORAL :" + image.getPath());
                }
            }


        }else{ //  VERSION 30+ ---------------------------------

            if (!imagenDeFoto && resolver!=null){

                resolver.delete(miPath,null,null);

            }

        }

    }

    public boolean comparaKartas(Kartas uno, Kartas dos){

            if(!uno.nombre_nivel.equals(dos.nombre_nivel)){

                return false;

            }else  if(!uno.detalle_nivel.equals(dos.detalle_nivel)) {

                return false;

            }else  if(uno.precio_nivel!=dos.precio_nivel) {

                return false;

            }else  if(uno.cantidad_nivel!=dos.cantidad_nivel) {

                return false;

            }else {

                return true;
            }

    }

    public void mover(final View vista,Boolean arriba,final Boolean poner){

        float alturaBarra=0;
        if(arriba){

            //alturaBarra= vista.getHeight();
            alturaBarra=1080;

        }else{

            alturaBarra= vista.getWidth();
        }

        Animation move;

        if(poner){

            move = new TranslateAnimation(0,0,alturaBarra,0f);

        }else{

            move = new TranslateAnimation(0,0,0f,alturaBarra);
        }

        move.setDuration(800);

        move.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation){

                vista.setAlpha(1f);
                vista.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation){

                if(!poner) {
                    vista.setAlpha(0f);
                    vista.setVisibility(View.GONE);
                }else{


                }

            }
        });

        vista.startAnimation(move);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*

        if(Server_EnvioDatos.file!=null) {
            if (Server_EnvioDatos.file.exists()) {
                System.out.println("BORRA EL FILE COMPRIMIDO: "+ Server_EnvioDatos.file.getPath());
                Server_EnvioDatos.file.delete();
            }
        }

        if(miPath!=null && imagenDeFoto) {
            if (miPath.getPath() != null) {
                System.out.println("BORRA TEMPORAL: "+miPath.getPath());
                File fi = new File(miPath.getPath());
                fi.delete();
                miPath = null;
            }
        }

        if(image!=null) {
            System.out.println("BORRA IMAGEN DE CAMARA :"+image.getPath());
            image.delete();
        }

         */
    }

    private void avisoPermisos(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        Button cancelar=alertLayout.findViewById(R.id.pedido_guardar);
        Button ver_permisos=alertLayout.findViewById(R.id.pedido_noguardar);
        ImageView icono=alertLayout.findViewById(R.id.icono_alerta);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

        String texto=getResources().getString(R.string.sin_permisos_archivos);

        icono.setImageResource(R.drawable.image_permission);
        pregunta.setText(texto);

        cancelar.setText(getResources().getString(R.string.cancelar));
        ver_permisos.setText(R.string.ver_permisos);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        ver_permisos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.cancel();

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             dialog.cancel();

            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    private void irAprevio(){

        if(compruebaRed()) {

            Intent miIntent = new Intent(contexto, Contenedor_Lakarta.class);

            miIntent.putExtra("QUERESTAURANTE", miRestaurante);
            miIntent.putExtra("KARTA_DESDE_ADMIN", "si");
            miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(miIntent);

        }else{

            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

        }

    }

    public boolean compruebaRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) contexto
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private int dimeCuando(String fechaFinal){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date fechaHoy=new Date();

        String diaHoy=sdf.format(fechaHoy);

        int cuando=0;

        try {

            String fechaFinFormat = sdf.format(myFormat.parse(fechaFinal));

            //Timestamp laFechaComentario=Timestamp.valueOf(fechaComentario);
            //Timestamp elDiaHoy=Timestamp.valueOf(diaHoy);

            long laFechaFinal= Timestamp.valueOf(fechaFinFormat).getTime();
            long elDiaHoy=Timestamp.valueOf(diaHoy).getTime();

            cuando=(int)TimeUnit.MILLISECONDS.toDays(laFechaFinal-elDiaHoy);

        }catch (Exception e){
            System.out.println("ERROR FECHA "+e.getMessage());

        }

        return cuando;

    }


}

