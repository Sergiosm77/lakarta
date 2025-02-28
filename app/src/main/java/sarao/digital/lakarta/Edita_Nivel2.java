package sarao.digital.lakarta;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Edita_Nivel2 extends AppCompatActivity {

    Server_RecibeDatos serverRecibeDatos;

    Server_EnvioDatos serverEnvioDatos;

    TextView enviaDatosCategoria;

    ImageView imagenNivel2;
    ImageView modificaImagen;

    private long mLastClickTime = 0;

    boolean imagenDeFoto=false;

    Uri miPath;
    File image;
    ContentResolver resolver;

    int puesto=0;
    int mostrar_imagen=0;
    int destacar_plato=0;
    int plato_agotado=0;

    String user,alses,alsesk;
    String alergenosElegidos;

    Kartas[] laKartaNivel2,laKartaNivel2copia;

    Kartas queCategoria;

    Limitaciones misLimitaciones;

    ProgressBar barraProgreso;

    LinearLayout rutacontenedorPlatos,nuevo_plato;
    LayoutInflater inflador;

    ScrollView scrollNivel2;

    TextView cancelar,nombreCategoria, cambiarOrden;

    Alergenos[] alergenos;

    private Toast mensajePop;
    private String mensajeAlerta="";

    final DecimalFormat form = new DecimalFormat("0.00");

    Restaurantes miRestaurante;
    Bundle miBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nivel2);

        rutacontenedorPlatos = findViewById(R.id.contenedor_platos);
        scrollNivel2=findViewById(R.id.scroll_nivel2);

        nombreCategoria=findViewById(R.id.nombre_nivel2);
        imagenNivel2 =findViewById(R.id.imagen_nivel2);
        cancelar=findViewById(R.id.cancelar_nivel2);
        nuevo_plato=findViewById(R.id.nueva_categoria);
        TextView textoNuevoPlato=findViewById(R.id.texto_nuevoplato);
        cambiarOrden=findViewById(R.id.modificar_orden_nivel2);
        barraProgreso = findViewById(R.id.barraprogreso_categorias);

        textoNuevoPlato.setText(getString(R.string.nuevo_producto));

        inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        barraProgreso.setVisibility(View.GONE);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        serverRecibeDatos =new Server_RecibeDatos(this);

        miBundle = this.getIntent().getExtras();

        if (miBundle != null) {

            queCategoria = miBundle.getParcelable("NIVELAVER");

        }else{

            System.out.println("BUNDLE VACIO");
        }

        serverRecibeDatos.cargaNivelGuardado("nivel2");
        serverRecibeDatos.cargaMiRestGuardado();
        miRestaurante= serverRecibeDatos.miRestaurante;
        misLimitaciones= serverRecibeDatos.misLimitaciones;
        //envioDatos=new EnvioDatos(miBundle,Edita_Nivel2.this,miRestaurante,this,inflador,user,pass);

        cargaAlergenos();

        ocultaBoton(false, cambiarOrden);

        //envioDatos=new EnvioDatos(this,this,inflador,user,pass);

        nuevo_plato.setVisibility(View.GONE);

        nuevo_plato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,nuevo_plato);

                if(compruebaLimite(laKartaNivel2.length,misLimitaciones.cant_elementos)) {
                    nuevoPlato(nuevo_plato);
                }else{

                    activaBoton(true,nuevo_plato);
                }

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        cambiarOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ocultaBoton(false,cambiarOrden);
                enviaCambiosOrden(cambiarOrden);

            }
        });


        IniciaPaginaNivel inicia = new IniciaPaginaNivel();

        inicia.execute();

    }

    @Override
    public void onResume(){

        super.onResume();

        serverEnvioDatos =new Server_EnvioDatos(miBundle,Edita_Nivel2.this,this,inflador);
    }

    public class IniciaPaginaNivel extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {

            barraProgreso.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            int contador=0;

            for(int i = 0; i< serverRecibeDatos.laKartaNivel2.length; i++){

                if(serverRecibeDatos.laKartaNivel2[i].cod_nivel_sup.equals(queCategoria.cod_nivel)){

                    contador++;

                }
            }

            laKartaNivel2=new Kartas[contador];

            contador=0;

            for(int i = 0; i< serverRecibeDatos.laKartaNivel2.length; i++){

                if(serverRecibeDatos.laKartaNivel2[i].cod_nivel_sup.equals(queCategoria.cod_nivel)){

                    laKartaNivel2[contador]= serverRecibeDatos.laKartaNivel2[i];
                    contador++;

                }
            }

            laKartaNivel2copia=new Kartas[laKartaNivel2.length];

            for(int i=0;i<laKartaNivel2.length;i++){

                laKartaNivel2copia[i]=new Kartas(laKartaNivel2[i]);

            }

            return "ok";

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);

            if(queCategoria!=null) {

                nombreCategoria.setText(queCategoria.nombre_nivel);
                Glide.with(getApplicationContext())
                        .load(queCategoria.imagen_nivel)
                        .error(R.drawable.no_photo)
                        .into(imagenNivel2);

            }

            ponContenidoCategoria();

            nuevo_plato.setVisibility(View.VISIBLE);

        }
    }

    private void veAmenu(Kartas menuAver){

        Intent miIntent = new Intent(this, Edita_Nivel3.class);

        miIntent.putExtra("NIVELAVER", menuAver);

        startActivity(miIntent);

    }

    private void ponContenidoCategoria(){

        for(int i=0;i<laKartaNivel2.length;i++){

            final int cual=i;

            final ConstraintLayout losplatos = (ConstraintLayout)inflador.inflate(R.layout.barra_platos_edit, null);

            TextView plato = losplatos.findViewById(R.id.nombre_plato);
            TextView precio = losplatos.findViewById(R.id.precio_plato);
            TextView detalle = losplatos.findViewById(R.id.detalle_plato);
            LinearLayout fondoNombre=losplatos.findViewById(R.id.fondo_nombre);
            LinearLayout fondoDetalle=losplatos.findViewById(R.id.fondo_detalle);
            ImageView imagenPlato = losplatos.findViewById(R.id.imagen_plato);
            ImageView moverArriba=losplatos.findViewById(R.id.mover_arriba);
            ImageView moverAbajo=losplatos.findViewById(R.id.mover_abajo);

            TextView noVerImagen=losplatos.findViewById(R.id.no_mostrar_imagen);
            TextView esDestacado=losplatos.findViewById(R.id.es_destacado);
            TextView esAgotado=losplatos.findViewById(R.id.es_agotado);
            TextView esMenu=losplatos.findViewById(R.id.es_menu);

            noVerImagen.setVisibility(View.GONE);
            esDestacado.setVisibility(View.GONE);
            esAgotado.setVisibility(View.GONE);
            esMenu.setVisibility(View.GONE);

            // COLORES ---------------

            if(Menu_Empresa.miRestaurante.cN!=0){

                plato.setTextColor(Menu_Empresa.miRestaurante.cN);
            }

            if(Menu_Empresa.miRestaurante.cP!=0){

                precio.setTextColor(Menu_Empresa.miRestaurante.cP);
            }


            if(Menu_Empresa.miRestaurante.cD!=0){

                detalle.setTextColor(Menu_Empresa.miRestaurante.cD);
            }

            if(Menu_Empresa.miRestaurante.fD!=0){

                fondoDetalle.setBackgroundColor(Menu_Empresa.miRestaurante.fD);

            }

            if(Menu_Empresa.miRestaurante.fN!=0){

                fondoNombre.setBackgroundColor(Menu_Empresa.miRestaurante.fN);

            }

            // -------------------------

            LinearLayout contenedorAlergenos=losplatos.findViewById(R.id.contenedor_alergenos);

            final ImageView editaNivel2=losplatos.findViewById(R.id.edita_nivel2);

            plato.setText(laKartaNivel2[i].nombre_nivel);
            precio.setText(form.format(laKartaNivel2[i].precio_nivel) + " €");
            detalle.setText(laKartaNivel2[i].detalle_nivel);

            // OPCIONES -------------------------

            if(laKartaNivel2[i].mostrar_imagen==0){

                noVerImagen.setVisibility(View.VISIBLE);
            }

            if(laKartaNivel2[i].destacado==1){

                esDestacado.setVisibility(View.VISIBLE);
            }

            if(laKartaNivel2[i].agotado==1){

                esAgotado.setVisibility(View.VISIBLE);
            }

            if(laKartaNivel2[i].esmenu==1){

                esMenu.setVisibility(View.VISIBLE);
            }

            // --- ALERGENOS ---------------

            if(!laKartaNivel2[i].alergenos.equals("")) {

                ponAlergenos(contenedorAlergenos,laKartaNivel2[i].alergenos);

                /*
                for (int e = 0; e < laKartaNivel2[i].alergenos.length(); e++) {

                    for(int d=0;d<alergenos.length;d++){

                        if( laKartaNivel2[i].alergenos.charAt(e)==alergenos[d].codigo_alergeno.charAt(0)){

                            View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad, null);

                            ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);

                            Glide.with(this)
                                    .load(alergenos[d].imagen_alergeno)
                                    .error(R.drawable.noimage)
                                    .into(imagenAlergeno);

                            contenedorAlergenos.addView(unidadAlergeno);
                            break;

                        }

                    }

                }

                 */
            }

            Glide.with(getApplicationContext())
                    .load(laKartaNivel2[i].imagen_nivel)
                    .error(R.drawable.no_photo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imagenPlato);



            rutacontenedorPlatos.addView(losplatos);

            moverArriba.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    moverArriba( rutacontenedorPlatos.indexOfChild(losplatos), cual);

                }
            });

            moverAbajo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    moverAbajo( rutacontenedorPlatos.indexOfChild(losplatos),cual);

                }
            });

            editaNivel2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false,editaNivel2);

                    cambiaNivel2(laKartaNivel2[cual],editaNivel2);

                }
            });

            losplatos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(laKartaNivel2[cual].esmenu==1) {

                        veAmenu(laKartaNivel2[cual]);
                    }else{


                    }

                }
            });


        }

        scrollNivel2.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scrollNivel2.getViewTreeObserver().removeOnPreDrawListener(this);
                scrollNivel2.setScrollY(cargaPosicionScroll());
                return false;
            }
        });

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

    private void cambiaNivel2(final Kartas queNivel, final View boton){

        alergenosElegidos="";

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_imagen_datos, null);

        enviaDatosCategoria =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        imagenNivel2 =alertLayout.findViewById(R.id.imagen_nivel);
        final TextView nombreCategoria=alertLayout.findViewById(R.id.nombre_categoria);
        final TextView detalleCategoria=alertLayout.findViewById(R.id.detalle_categoria);
        final TextView precioCategoria=alertLayout.findViewById(R.id.precio_categoria);
        final TextView borraPlato=alertLayout.findViewById(R.id.elimina_nivel);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);
        final CardView iconoNoVer=alertLayout.findViewById(R.id.icono_no_ver);

        alertLayout.findViewById(R.id.cantidad_platos).setVisibility(View.GONE);

        final SwitchCompat mostrarImagen=alertLayout.findViewById(R.id.switch_sinimagen);

        final SwitchCompat destacar=alertLayout.findViewById(R.id.switch_destacar);

        final SwitchCompat productoAgotado=alertLayout.findViewById(R.id.switch_agotado);

        final LinearLayout contenedorAlergenos=alertLayout.findViewById(R.id.contenedor_alergenos);
        final LinearLayout fichaAlergenos=alertLayout.findViewById(R.id.ficha_alergenos);
        View lineaAlergenos=alertLayout.findViewById(R.id.linea_alergenos);
        final TextView textoAlergenos=alertLayout.findViewById(R.id.texto_alergenos);

        TextView tituloCambio=alertLayout.findViewById(R.id.titulo_cambionivel);
        LinearLayout fichaEsmenu=alertLayout.findViewById(R.id.ficha_esmenu);
        View lineaEsmenu=alertLayout.findViewById(R.id.linea_esmenu);

        LinearLayout fichaTipoComida=alertLayout.findViewById(R.id.ficha_tipo_comida);
        View lineatipoComida=alertLayout.findViewById(R.id.linea_tipo_comida);
        fichaTipoComida.setVisibility(View.GONE);
        lineatipoComida.setVisibility(View.GONE);

        fichaEsmenu.setVisibility(View.GONE);
        lineaEsmenu.setVisibility(View.GONE);

        tituloCambio.setText(getString(R.string.modifica_producto));

        borraPlato.setText(getString(R.string.borra_producto));

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                Glide.with(getApplicationContext())
                        .load(queNivel.imagen_nivel)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(imagenNivel2);
                return false;
            }
        });


        nombreCategoria.setText(queNivel.nombre_nivel);
        detalleCategoria.setText(queNivel.detalle_nivel);
        precioCategoria.setText(form.format(queNivel.precio_nivel));

        if(queNivel.mostrar_imagen==1){

            mostrar_imagen=1;
            mostrarImagen.setChecked(true);
            iconoNoVer.setVisibility(View.GONE);

        }else{

            mostrar_imagen=0;
            mostrarImagen.setChecked(false);
            iconoNoVer.setVisibility(View.VISIBLE);
        }

        if(queNivel.destacado==1){

            destacar_plato=1;
            destacar.setChecked(true);

        }else{

            destacar_plato=0;
            destacar.setChecked(false);
        }

        if(queNivel.agotado==1){

            plato_agotado=1;
            productoAgotado.setChecked(true);

        }else{

            plato_agotado=0;
            productoAgotado.setChecked(false);
        }

        //----- ALERGENOS -------------------

        if(queNivel.esmenu!=1) {

            alergenosElegidos = queNivel.alergenos;

            if (!alergenosElegidos.equals("")) {

                int borrador = 0;

                for (int i = 0; i < alergenosElegidos.length(); i++) {

                    for (int e = 0; e < alergenos.length; e++) {

                        if (alergenosElegidos.charAt(i) == alergenos[e].codigo_alergeno.charAt(0)) {

                            if (borrador == 0) {

                                borrador = 1;
                                contenedorAlergenos.removeAllViews();
                            }

                            View unidadAlergeno = inflater.inflate(R.layout.alergeno_unidad, null);

                            ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.alergeno);

                            Glide.with(this)
                                    .load(alergenos[e].imagen_alergeno)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.no_photo)
                                    .into(imagenAlergeno);

                            contenedorAlergenos.addView(unidadAlergeno);

                            break;
                        }
                    }

                }

            }

        }else{



            fichaAlergenos.setVisibility(View.GONE);
            lineaAlergenos.setVisibility(View.GONE);
        }

        activaBoton(false, enviaDatosCategoria);

        AlertDialog.Builder alert = new AlertDialog.Builder(Edita_Nivel2.this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
        }

        mostrarImagen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                activaBoton(true, enviaDatosCategoria);

                if(mostrar_imagen==0 && isChecked){

                    iconoNoVer.setVisibility(View.GONE);
                    mostrar_imagen=1;

                }else if(mostrar_imagen==1 && !isChecked){

                    iconoNoVer.setVisibility(View.VISIBLE);
                    mostrar_imagen=0;

                }

            }
        });

        destacar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                activaBoton(true, enviaDatosCategoria);

                if(destacar_plato==0 && isChecked){

                    destacar_plato=1;

                }else if(destacar_plato==1 && !isChecked){

                    destacar_plato=0;

                }

            }
        });

        productoAgotado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                activaBoton(true, enviaDatosCategoria);

                if(plato_agotado==0 && isChecked){

                    plato_agotado=1;

                }else if(plato_agotado==1 && !isChecked){

                    plato_agotado=0;

                }

            }
        });

        fichaAlergenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,fichaAlergenos);

                abreFichaAlergenos(fichaAlergenos,contenedorAlergenos, enviaDatosCategoria,textoAlergenos);

            }
        });

        nombreCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,nombreCategoria);

                introduceTexto(nombreCategoria, InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        detalleCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,detalleCategoria);

                introduceTexto(detalleCategoria,InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        precioCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,precioCategoria);

                introduceTexto(precioCategoria,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, enviaDatosCategoria);

            }
        });

        borraPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,borraPlato);

                preguntaSiEliminar(queNivel,borraPlato,dialog);


            }
        });

        enviaDatosCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, enviaDatosCategoria);

                if(compruebaConexion()) {

                    cargaUserEmpresa();

                    serverEnvioDatos.enviaCambiosNivel(user,alses,alsesk,scrollNivel2.getScrollY(), nombreCategoria.getText().toString(), detalleCategoria.getText().toString(), precioCategoria.getText().toString(),"", queNivel.cod_nivel,queNivel.cod_nivel_sup, "",queNivel.imagen_nivel, alergenosElegidos, String.valueOf(plato_agotado), miPath,mostrar_imagen, destacar_plato,"nivel2","");

                    activaBoton(true, boton);

                    dialog.cancel();
                }else{

                    mensajeAlerta = getString(R.string.sin_internet);

                    activaBoton(true, enviaDatosCategoria);
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

    private void abreFichaAlergenos(final View boton, final LinearLayout contenedorAlergenos, final TextView botonEnviar, final TextView textoAlergenos){

        View alertLayout = inflador.inflate(R.layout.emerg_alergenos, null);

        TextView aceptar=alertLayout.findViewById(R.id.aceptar);
        TextView cancelar=alertLayout.findViewById(R.id.cancelar);
        final LinearLayout listaAlergenos=alertLayout.findViewById(R.id.lista_alergenos);

        AlertDialog.Builder alert = new AlertDialog.Builder(Edita_Nivel2.this);

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

        for (int i = 0; i < alergenos.length; i++) {

            View unidadAlergeno = inflador.inflate(R.layout.unidad_seleccion_alergeno, null);

            ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.imagen_alergeno);
            TextView nombreAlergeno = unidadAlergeno.findViewById(R.id.nombre_alergeno);
            TextView detalleAlergeno = unidadAlergeno.findViewById(R.id.detalle_alergeno);
            CheckBox alergenoCheck = unidadAlergeno.findViewById(R.id.checkBox_alergeno);

            for (int e = 0; e < alergenosElegidos.length(); e++) {

                if (alergenos[i].codigo_alergeno.charAt(0) == alergenosElegidos.charAt(e)) {

                    alergenoCheck.setChecked(true);
                }
            }

            Glide.with(this)
                    .load(alergenos[i].imagen_alergeno)
                    .into(imagenAlergeno);

            nombreAlergeno.setText(alergenos[i].nombre_alergeno);
            detalleAlergeno.setText(alergenos[i].detalle_alergeno);

            listaAlergenos.addView(unidadAlergeno);

        }



        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
        }

        aceptar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String alergenosAnteriores=alergenosElegidos;

                alergenosElegidos="";

                contenedorAlergenos.removeAllViews();
                contenedorAlergenos.addView(textoAlergenos);

                int borrador=0;


                for(int e=0;e<listaAlergenos.getChildCount();e++){

                    CheckBox chekeado=listaAlergenos.getChildAt(e).findViewById(R.id.checkBox_alergeno);

                    if(chekeado.isChecked()){

                        if(borrador==0){

                            borrador=1;
                            contenedorAlergenos.removeAllViews();
                        }

                        View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad, null);

                        ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);

                        Glide.with(getApplicationContext())
                                .load(alergenos[e].imagen_alergeno)
                                .error(R.drawable.noimage)
                                .into(imagenAlergeno);

                        contenedorAlergenos.addView(unidadAlergeno);

                        alergenosElegidos += alergenos[e].codigo_alergeno;

                    }
                }

                if(!alergenosAnteriores.equals(alergenosElegidos)){

                    activaBoton(true,botonEnviar);
                }

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

    }

    class Hilo1 extends Thread {

        public void run(LinearLayout listaAlergenos) {
            for (int i = 0; i < alergenos.length; i++) {

                View unidadAlergeno = inflador.inflate(R.layout.unidad_seleccion_alergeno, null);

                ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.imagen_alergeno);
                TextView nombreAlergeno = unidadAlergeno.findViewById(R.id.nombre_alergeno);
                TextView detalleAlergeno = unidadAlergeno.findViewById(R.id.detalle_alergeno);
                CheckBox alergenoCheck = unidadAlergeno.findViewById(R.id.checkBox_alergeno);

                for (int e = 0; e < alergenosElegidos.length(); e++) {

                    if (alergenos[i].codigo_alergeno.charAt(0) == alergenosElegidos.charAt(e)) {

                        alergenoCheck.setChecked(true);
                    }
                }

                Glide.with(Edita_Nivel2.this)
                        .load(alergenos[i].imagen_alergeno)
                        .error(R.drawable.no_photo)
                        .into(imagenAlergeno);

                nombreAlergeno.setText(alergenos[i].nombre_alergeno);
                detalleAlergeno.setText(alergenos[i].detalle_alergeno);

                listaAlergenos.addView(unidadAlergeno);

            }
        }
    }

    public void nuevoPlato(final View boton){

        alergenosElegidos="";

        final View alertLayout = inflador.inflate(R.layout.emerg_cambia_imagen_datos, null);

        TextView titulo=alertLayout.findViewById(R.id.titulo_cambionivel);

        enviaDatosCategoria =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        imagenNivel2 =alertLayout.findViewById(R.id.imagen_nivel);
        final TextView nombreCategoria=alertLayout.findViewById(R.id.nombre_categoria);
        final TextView detalleCategoria=alertLayout.findViewById(R.id.detalle_categoria);
        final TextView precioCategoria=alertLayout.findViewById(R.id.precio_categoria);
        TextView borraCategoria=alertLayout.findViewById(R.id.elimina_nivel);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);
        final CardView iconoNoVer=alertLayout.findViewById(R.id.icono_no_ver);

        alertLayout.findViewById(R.id.cantidad_platos).setVisibility(View.GONE);

        final SwitchCompat destacar=alertLayout.findViewById(R.id.switch_destacar);

        final LinearLayout fichaAlergenos=alertLayout.findViewById(R.id.ficha_alergenos);
        final View lineaAlergenos=alertLayout.findViewById(R.id.linea_alergenos);
        final LinearLayout contenedorAlergenos=alertLayout.findViewById(R.id.contenedor_alergenos);
        final TextView textoAlergenos=alertLayout.findViewById(R.id.texto_alergenos);

        final SwitchCompat mostrarImagen=alertLayout.findViewById(R.id.switch_sinimagen);

        SwitchCompat esmenu=alertLayout.findViewById(R.id.switch_esmenu);

        LinearLayout fichaAgotado=alertLayout.findViewById(R.id.ficha_agotado);
        View lineaAgotado=alertLayout.findViewById(R.id.linea_agotado);
        fichaAgotado.setVisibility(View.GONE);
        lineaAgotado.setVisibility(View.GONE);

        LinearLayout fichaTipoComida=alertLayout.findViewById(R.id.ficha_tipo_comida);
        View lineatipoComida=alertLayout.findViewById(R.id.linea_tipo_comida);
        fichaTipoComida.setVisibility(View.GONE);
        lineatipoComida.setVisibility(View.GONE);

        borraCategoria.setVisibility(View.GONE);
        iconoNoVer.setVisibility(View.GONE);

        mostrarImagen.setChecked(true);
        destacar.setChecked(false);

        mostrar_imagen=1;

        enviaDatosCategoria.setText(getString(R.string.anade_producto));
        String texto=getString(R.string.nuevo_producto_de)+" "+queCategoria.nombre_nivel;
        titulo.setText(texto);

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                Glide.with(getApplicationContext())
                        .load(R.drawable.no_photo)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(imagenNivel2);
                return false;
            }
        });


        activaBoton(false, enviaDatosCategoria);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        esmenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(puesto==0 && isChecked){

                    fichaAlergenos.setVisibility(View.GONE);
                    lineaAlergenos.setVisibility(View.GONE);
                    contenedorAlergenos.removeAllViews();
                    contenedorAlergenos.addView(textoAlergenos);
                    alergenosElegidos="";

                    puesto=1;

                }else if(puesto==1 && !isChecked){

                    fichaAlergenos.setVisibility(View.VISIBLE);
                    lineaAlergenos.setVisibility(View.VISIBLE);


                    puesto=0;

                }

            }
        });

        mostrarImagen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(mostrar_imagen==0 && isChecked){

                    iconoNoVer.setVisibility(View.GONE);
                    mostrar_imagen=1;

                }else if(mostrar_imagen==1 && !isChecked){

                    iconoNoVer.setVisibility(View.VISIBLE);
                    mostrar_imagen=0;

                }

            }
        });

        destacar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(destacar_plato==0 && isChecked){

                    destacar_plato=1;

                }else if(destacar_plato==1 && !isChecked){

                    destacar_plato=0;

                }

            }
        });




        fichaAlergenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,fichaAlergenos);

                abreFichaAlergenos(fichaAlergenos,contenedorAlergenos, enviaDatosCategoria,textoAlergenos);

            }
        });

        nombreCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,nombreCategoria);

                introduceTexto(nombreCategoria,InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        detalleCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,detalleCategoria);

                introduceTexto(detalleCategoria,InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        precioCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,precioCategoria);

                introduceTexto(precioCategoria,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, enviaDatosCategoria);

            }
        });

        enviaDatosCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(compruebaConexion()) {

                    if (nombreCategoria.getText().equals("")) {

                        mensajeAlerta = getString(R.string.intro_nombre_categoria);
                        ponAlerta();

                    } else {

                        cargaUserEmpresa();

                        serverEnvioDatos.introduceNuevoNivel(user,alses,alsesk,scrollNivel2.getScrollY(), "nivel2", queCategoria.cod_nivel, nombreCategoria.getText().toString(), detalleCategoria.getText().toString(), precioCategoria.getText().toString(), alergenosElegidos, miPath,mostrar_imagen,destacar_plato, String.valueOf(puesto),"");

                        miPath = null; // ----------- vacia la imagen para la siguiente

                        activaBoton(true, boton);

                        dialog.cancel();
                    }
                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    activaBoton(true, enviaDatosCategoria);
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

    private void preguntaSiEliminar(final Kartas queNivel, final View boton, final AlertDialog dialogo){

        View alertLayout = inflador.inflate(R.layout.emerg_pregunta_alerta, null);

        Button cancelar=alertLayout.findViewById(R.id.pedido_guardar);
        Button borrar=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

        pregunta.setText(R.string.pregunta_borrar_producto);

        cancelar.setText(getResources().getString(R.string.cancelar));
        borrar.setText(getResources().getString(R.string.eliminar));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
                activaBoton(true,boton);

            }
        });

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONArray ordenNivel=new JSONArray();

                try {

                    Map<String, String> parametros = new HashMap<>();

                    int ordena=1;

                    for(int i=0;i<laKartaNivel2copia.length;i++){

                        if(!laKartaNivel2copia[i].cod_nivel.equals(queNivel.cod_nivel)){

                            if(laKartaNivel2copia[i].orden_nivel!=ordena) {

                                parametros.put("codNivel", laKartaNivel2copia[i].cod_nivel);
                                parametros.put("orden", String.valueOf(ordena));

                                JSONObject dato2 = new JSONObject(parametros);

                                ordenNivel.put(dato2);
                            }

                            ordena++;


                        }

                    }


                }catch (Exception e){

                    System.out.println("ERROR EN ORDEN: "+e.getMessage());

                }

                if(compruebaConexion()) {

                    cargaUserEmpresa();

                    serverEnvioDatos.borraNivel(user,alses,alsesk,scrollNivel2.getScrollY(), queNivel.imagen_nivel, ordenNivel,"nivel2",queNivel.esmenu, queNivel.cod_nivel_sup,queNivel.cod_nivel,"","");
                    //envioDatos.borraNivel2(scrollNivel2.getScrollY(), queNivel.imagen_nivel, ordenNivel, queNivel.cod_nivel, "nivel2",rutacontenedorPlatos,cualBorrar);
                    dialog.cancel();
                    dialogo.cancel();
                }else{

                    mensajeAlerta = getString(R.string.sin_internet);
                    activaBoton(true, boton);
                    ponAlerta();
                    dialog.cancel();
                }


            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    private void introduceTexto(final TextView elTexto,final int tipo,final View botonAceptar){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        //LayoutInflater inflater = getLayoutInflater();
        final View introTexto = inflador.inflate(R.layout.entrada_texto, null);

        final TextView entradaTexto=introTexto.findViewById(R.id.recoge_texto);
        final ImageView validaTexto=introTexto.findViewById(R.id.valida_texto);

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

                //dialogoTexto.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


                if(tipo!=InputType.TYPE_CLASS_TEXT) {

                    if(entradaTexto.getEditableText().toString().equals("")){

                        elTexto.setText(form.format(Double.parseDouble("0")));

                    }else {

                        elTexto.setText(form.format(Double.parseDouble(entradaTexto.getEditableText().toString())));

                    }

                }else{

                    elTexto.setText(entradaTexto.getEditableText().toString());

                }

                activaBoton(true,botonAceptar);
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


        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);



        dialogoTexto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogoTexto.show();



    }

    public void cargaAlergenos(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        try{

            JSONArray recuperaAlergenos=new JSONArray(guarda.getString("ALERGENOS","0"));

            alergenos=new Alergenos[recuperaAlergenos.length()];

            for(int i=0;i<alergenos.length;i++) {

                alergenos[i]=new Alergenos();

                JSONObject object = recuperaAlergenos.getJSONObject(i);

                alergenos[i].nombre_alergeno = object.getString("nombre");

                alergenos[i].detalle_alergeno = object.getString("detalle");
                alergenos[i].codigo_alergeno = object.getString("codigo");
                alergenos[i].imagen_alergeno = object.getString("imagen");

            }

        }catch (JSONException e){

            alergenos=new Alergenos[0];

        }

    }

    public void cargaUserEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");


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

    public void moverArriba(int posCategoria, int queCategoria){

        if(posCategoria>0) {

            View vista = rutacontenedorPlatos.getChildAt(posCategoria-1);
            rutacontenedorPlatos.removeViewAt(posCategoria-1);
            rutacontenedorPlatos.addView(vista, posCategoria);

            int orden=laKartaNivel2[queCategoria].orden_nivel;

            laKartaNivel2[queCategoria].orden_nivel=orden-1;

            for(int i=0;i<laKartaNivel2.length;i++){

                if(laKartaNivel2[i].orden_nivel==orden-1 && i!=queCategoria){

                    laKartaNivel2[i].orden_nivel=orden;

                }

            }

            int cuenta=0;

            for(int i=0;i<laKartaNivel2.length;i++){

                if(laKartaNivel2[i].orden_nivel!=laKartaNivel2copia[i].orden_nivel){

                    cuenta=1;
                    break;

                }

            }

            if(cuenta==1){

                ocultaBoton(true,cambiarOrden);

            }else{

                ocultaBoton(false,cambiarOrden);
            }

            int donde=rutacontenedorPlatos.getChildAt(posCategoria).getHeight();
            scrollNivel2.scrollBy(0,-donde);


        }

    }

    public void moverAbajo(int posCategoria,int queCategoria){

        if(posCategoria+1<rutacontenedorPlatos.getChildCount()) {

            View vista = rutacontenedorPlatos.getChildAt(posCategoria+1);
            rutacontenedorPlatos.removeViewAt(posCategoria+1);
            rutacontenedorPlatos.addView(vista, posCategoria);

            int orden=laKartaNivel2[queCategoria].orden_nivel;

            laKartaNivel2[queCategoria].orden_nivel=orden+1;

            for(int i=0;i<laKartaNivel2.length;i++){

                if(laKartaNivel2[i].orden_nivel==orden+1 && i!=queCategoria){

                    laKartaNivel2[i].orden_nivel=orden;

                }

            }

            int cuenta=0;

            for(int i=0;i<laKartaNivel2.length;i++){

                if(laKartaNivel2[i].orden_nivel!=laKartaNivel2copia[i].orden_nivel){

                    cuenta=1;
                    break;

                }

            }

            if(cuenta==1){

                ocultaBoton(true,cambiarOrden);

            }else{

                ocultaBoton(false,cambiarOrden);
            }

            int donde=rutacontenedorPlatos.getChildAt(posCategoria).getHeight();
            scrollNivel2.scrollBy(0,donde);

        }

    }

    public void enviaCambiosOrden(View boton){

        JSONArray ordenNivel=new JSONArray();

        try {

            Map<String, String> parametros = new HashMap<>();


            for(int i=0;i<laKartaNivel2.length;i++){

                if(laKartaNivel2[i].orden_nivel!=laKartaNivel2copia[i].orden_nivel){

                    parametros.put("codNivel", laKartaNivel2[i].cod_nivel);
                    parametros.put("orden", String.valueOf(laKartaNivel2[i].orden_nivel));

                    System.out.println("PONE: "+laKartaNivel2[i].cod_nivel);

                    JSONObject dato2=new JSONObject(parametros);

                    ordenNivel.put(dato2);

                }

            }


        }catch (Exception e){

            System.out.println("ERROR EN ORDEN: "+e.getMessage());

        }

        if(ordenNivel.length()>0) {

            System.out.println("ENVIA ORDEN: "+ordenNivel);

            cargaUserEmpresa();

            serverEnvioDatos.enviaCambiosOrden(user,alses,alsesk,scrollNivel2.getScrollY(),ordenNivel,"nivel2",boton);


        }else{

            System.out.println("ORDEN VACIO");

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

    public void mover(final View vista,Boolean arriba,final Boolean donde){

        float alturaBarra=0;
        if(arriba){

            alturaBarra= vista.getHeight();

        }else{

            alturaBarra= vista.getWidth();
        }

        System.out.println("LO VA A MOVER "+alturaBarra);

        Animation move;

        if(donde){

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

                if(!donde) {
                    vista.setAlpha(0f);
                    vista.setVisibility(View.GONE);
                }else{


                }

            }
        });

        vista.startAnimation(move);






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

    private void ocultaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0f);

        }

    }

    public int cargaPosicionScroll(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        int posicion;

        posicion =guarda.getInt("posicionScroll",0);

        System.out.println("CARGA POSICION: "+posicion);

        SharedPreferences.Editor mieditor=guarda.edit();

        mieditor.putInt("posicionScroll",0);

        mieditor.apply();

        return posicion;

    }

    public void ponAlergenos(LinearLayout contenedor, String queAlergeno){

        for (int e = 0; e < queAlergeno.length(); e++) {

            for(int d=0;d<alergenos.length;d++){

                if( queAlergeno.charAt(e)==alergenos[d].codigo_alergeno.charAt(0)){

                    View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad, null);

                    ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);

                    Glide.with(this)
                            .load(alergenos[d].imagen_alergeno)
                            .error(R.drawable.noimage)
                            .into(imagenAlergeno);

                    contenedor.addView(unidadAlergeno);
                    break;

                }

            }

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

                        imagenNivel2.setImageURI(miPath);
                        activaBoton(true, enviaDatosCategoria);

                        System.out.println("IMAGEN DE DIRECTORIO: "+miPath);

                    }else{

                        imagenDeFoto=true;

                        imagenNivel2.setImageURI(miPath);
                        activaBoton(true, enviaDatosCategoria);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*

        if(Server_EnvioDatos.file!=null) {
            if (Server_EnvioDatos.file.exists()) {
                if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(Server_EnvioDatos.file.getParent())) {
                    Server_EnvioDatos.file.delete();
                    System.out.println("BORRA EL FILE COMPRIMIDO :"+ Server_EnvioDatos.file.getPath());
                }else{

                    System.out.println("NO BORRA EL FILE COMPRIMIDO :"+ Server_EnvioDatos.file.getPath());
                }

            }
        }

        if(miPath!=null && imagenDeFoto) {
            if (miPath.getPath() != null) {

                File fi = new File(miPath.getPath());

                if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(fi.getParent())) {
                    fi.delete();
                    System.out.println("BORRA EL TEMPORAL:"+fi.getPath());
                }else{

                    System.out.println("NO BORRA EL TEMPORAL:"+fi.getPath());
                }

                miPath = null;
            }
        }

        if(image!=null) {

            if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(image.getParent())) {
                image.delete();
                System.out.println("BORRA IMAGEN DE CAMARA :"+image.getPath());
            }else{

                System.out.println("NO BORRA IMAGEN DE CAMARA :"+image.getPath());
            }

        }

         */
    }


    private boolean compruebaLimite(int cuantos, int limite){

        if(cuantos>=limite){

            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

            Button aceptar=alertLayout.findViewById(R.id.pedido_guardar);
            Button quitar=alertLayout.findViewById(R.id.pedido_noguardar);
            TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);
            ImageView icono=alertLayout.findViewById(R.id.icono_alerta);

            icono.setImageResource(R.drawable.sorry);

            String texto=getResources().getString(R.string.limite_alcanzado)+" "+misLimitaciones.cant_elementos+" "+getResources().getString(R.string.productos)+"\n\n"+getResources().getString(R.string.limite_productos);

            pregunta.setText(texto);

            aceptar.setText(getResources().getString(R.string.aceptar));
            quitar.setVisibility(View.GONE);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setView(alertLayout);

            alert.setCancelable(true);


            final AlertDialog dialog = alert.create();

            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.cancel();

                }
            });


            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            return false;
        }else{

            return true;
        }

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



}

