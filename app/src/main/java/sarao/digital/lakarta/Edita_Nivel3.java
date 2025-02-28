package sarao.digital.lakarta;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Edita_Nivel3 extends AppCompatActivity {

    Uri miPath;
    File image;
    ContentResolver resolver;

    String user,alses,alsesk;

    private long mLastClickTime = 0;

    boolean imagenDeFoto=false;

    Server_RecibeDatos serverRecibeDatos;
    Server_EnvioDatos serverEnvioDatos;
    Bundle miBundle;

    int mostrar_imagen=0;

    Kartas[] laKartaNivel3,laKartaNivel3copia;
    Kartas menuAver;

    Alergenos[] alergenos;

    ProgressBar barraProgreso;

    ImageView imagenNivel3,modificaImagen;

    String estaAgotado, alergenosElegidos;

    LinearLayout rutacontenedor,nuevaCategoria;
    LayoutInflater inflador;
    ScrollView scrollNivel3;

    TextView nombreMenu, cambiarOrden,cancelar, enviaDatosCategoria;

    private Toast mensajePop;
    private String mensajeAlerta="";

    final DecimalFormat form = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.ENGLISH));

    Restaurantes miRestaurante;

    Limitaciones misLimitaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nivel3);

        rutacontenedor=findViewById(R.id.contenedor_menus_usuario);
        scrollNivel3=findViewById(R.id.scroll_nivel3);

        nombreMenu=findViewById(R.id.nombre_menu_usuario);
        imagenNivel3=findViewById(R.id.imagen_nivel3);

        cambiarOrden=findViewById(R.id.modificar_menu_usuario);
        cancelar=findViewById(R.id.nomodificar_menu_usuario);

        nuevaCategoria=findViewById(R.id.nueva_categoria);

        inflador=(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        barraProgreso=findViewById(R.id.barraprogreso_menuuser);

        // ---------------------

        float radius = 5f;
        View decorView = getWindow().getDecorView();

        ViewGroup rootView = decorView.findViewById(android.R.id.content);

        Drawable windowBackground = decorView.getBackground();

        // --------------------------------


        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        serverRecibeDatos =new Server_RecibeDatos(this);

        miBundle=this.getIntent().getExtras();

        if(miBundle!=null) {

            menuAver = miBundle.getParcelable("NIVELAVER");

        }

        System.out.println("COD NivEL UNO "+menuAver.cod_nivel_sup);

        serverRecibeDatos.cargaNivelGuardado("nivel3");
        serverRecibeDatos.cargaMiRestGuardado();

        misLimitaciones= serverRecibeDatos.misLimitaciones;

        miRestaurante= serverRecibeDatos.miRestaurante;

//        envioDatos=new EnvioDatos(miBundle,this,miRestaurante,this,inflador,user,pass);

        cargaAlergenos();

        nuevaCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,nuevaCategoria);

                if(compruebaLimite(laKartaNivel3.length,misLimitaciones.cant_categorias_menu,"elemento")) {

                    nuevoNivel3("nivel3",nuevaCategoria,0);

                }else{

                    activaBoton(true, nuevaCategoria);
                }


            }
        });

        cambiarOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ocultaBoton(false,cambiarOrden);

                enviaCambiosOrden(cambiarOrden,"nivel3");

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cancelar);

                finish();

            }
        });

        ocultaBoton(false,cambiarOrden);
        nuevaCategoria.setVisibility(View.GONE);

        IniciaPaginaNivel inicia = new IniciaPaginaNivel();

        inicia.execute();

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

            for(int i = 0; i< serverRecibeDatos.laKartaNivel3.length; i++){

                if(serverRecibeDatos.laKartaNivel3[i].cod_nivel_sup.equals(menuAver.cod_nivel)){

                    contador++;

                }
            }

            laKartaNivel3=new Kartas[contador];

            contador=0;

            for(int i = 0; i< serverRecibeDatos.laKartaNivel3.length; i++){

                if(serverRecibeDatos.laKartaNivel3[i].cod_nivel_sup.equals(menuAver.cod_nivel)){

                    laKartaNivel3[contador]= serverRecibeDatos.laKartaNivel3[i];

                    contador++;

                }
            }

            laKartaNivel3copia=new Kartas[laKartaNivel3.length];

            for(int i=0;i<laKartaNivel3.length;i++){

                laKartaNivel3copia[i]=new Kartas(laKartaNivel3[i]);

            }

            return "ok";

        }

        protected void onPostExecute(String resultado) {

            barraProgreso.setVisibility(View.GONE);

            try {
                nombreMenu.setText(menuAver.nombre_nivel);

                Glide.with(getApplicationContext())
                        .load(menuAver.imagen_nivel)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(imagenNivel3);
            }catch (Exception e){

                nombreMenu.setText("");

            }

            ponMenus();
            nuevaCategoria.setVisibility(View.VISIBLE);

        }
    }

    public void ponMenus(){

        for (int i = 0; i < laKartaNivel3.length; i++) {  // INFLA NIVEL 3

            final int este3=i;

            final String codNivel2=laKartaNivel3[i].cod_nivel_sup;

            final LinearLayout rutaOpciones = (LinearLayout) inflador.inflate(R.layout.unidad_cat_menu_editar, null);

            final LinearLayout contendorElementos=rutaOpciones.findViewById(R.id.contenedor_elementos_user);

            final TextView nombreCriteroOp = rutaOpciones.findViewById(R.id.nombre_criterio_user);
            final TextView detalleCriteroOp = rutaOpciones.findViewById(R.id.detalle_criterio_user);
            final TextView cantidadCriteroOp = rutaOpciones.findViewById(R.id.cantidad_criterio_user);

            ImageView moverArriba=rutaOpciones.findViewById(R.id.mover_arriba);
            ImageView moverAbajo=rutaOpciones.findViewById(R.id.mover_abajo);

            ImageView imagenNivel=rutaOpciones.findViewById(R.id.imagen_nivel);
            final LinearLayout nuevoElemento=rutaOpciones.findViewById(R.id.nuevo_elemento);
            final LinearLayout fichaEditaMenu=rutaOpciones.findViewById(R.id.ficha_editamenu);

            TextView noMostrarImagenCat = rutaOpciones.findViewById(R.id.no_mostrar_imagen);

            if(laKartaNivel3[i].mostrar_imagen==1){

                noMostrarImagenCat.setVisibility(View.GONE);

            }

            nombreCriteroOp.setText(laKartaNivel3[i].nombre_nivel);
            detalleCriteroOp.setText(laKartaNivel3[i].detalle_nivel);
            cantidadCriteroOp.setText(laKartaNivel3[i].cantidad_nivel+"");

            Glide.with(getApplicationContext())
                    .load(laKartaNivel3[i].imagen_nivel)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.no_photo)
                    .into(imagenNivel);

            rutacontenedor.addView(rutaOpciones);

            moverArriba.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    moverArriba( rutacontenedor.indexOfChild(rutaOpciones), este3);

                }
            });

            moverAbajo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    moverAbajo( rutacontenedor.indexOfChild(rutaOpciones),este3);

                }
            });

            nuevoElemento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false,nuevoElemento);

                    if(compruebaLimite(laKartaNivel3[este3].nombre_subnivel.length,misLimitaciones.cant_elementos_menu,"platos")) {

                        nuevoNivel3("nivel4",nuevoElemento,este3);

                    }else{

                        activaBoton(true, nuevoElemento);
                    }

                }
            });

            fichaEditaMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false,fichaEditaMenu);

                    cambiaNivel3(laKartaNivel3[este3],fichaEditaMenu,"nivel3",0,codNivel2);


                }
            });

            for (int e = 0; e < laKartaNivel3[i].nombre_subnivel.length; e++) {  // INFLA NIVEL 4 -------------------------------

                final int este4=e;

                final LinearLayout platoNivel4 = (LinearLayout) inflador.inflate(R.layout.unidad_plato_menu_editar, null);

                final TextView nombrePlato_nivel4=platoNivel4.findViewById(R.id.nombre_plato_menu);
                final TextView detallePlato_nivel4=platoNivel4.findViewById(R.id.detalle_plato_menu);
                final TextView precioPlato_nivel4=platoNivel4.findViewById(R.id.precio_plato_menu);
                final TextView esAgotado=platoNivel4.findViewById(R.id.agotado);
                final TextView noMostrarImagen=platoNivel4.findViewById(R.id.no_mostrar_imagen);
                ImageView imagenPlato=platoNivel4.findViewById(R.id.imagen_plato);
                LinearLayout contenedorAlergenos=platoNivel4.findViewById(R.id.contenedor_alergenos);

                if(laKartaNivel3[i].visible[e]==1){

                    esAgotado.setVisibility(View.GONE);

                }

                if(laKartaNivel3[i].mostrar_imagen_subnivel[e]==1){

                    noMostrarImagen.setVisibility(View.GONE);

                }

                nombrePlato_nivel4.setText(laKartaNivel3[i].nombre_subnivel[e]);
                detallePlato_nivel4.setText(laKartaNivel3[i].detalle_subnivel[e]);
                precioPlato_nivel4.setText(String.valueOf(laKartaNivel3[i].precio_subnivel[e]));

                Glide.with(getApplicationContext())
                        .load(laKartaNivel3[i].imagen_subnivel[e])
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(imagenPlato);

                if(laKartaNivel3[i].detalle_subnivel[e].equals("")) {
                    detallePlato_nivel4.setVisibility(View.GONE);
                }

                // --- ALERGENOS ---------------

                if(!laKartaNivel3[i].alergenos_subnivel[e].equals("")) {

                    for (int a = 0; a < laKartaNivel3[i].alergenos_subnivel[e].length(); a++) {

                        for(int d=0;d<alergenos.length;d++){

                            if( laKartaNivel3[i].alergenos_subnivel[e].charAt(a)==alergenos[d].codigo_alergeno.charAt(0)){

                                View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad, null);

                                ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);

                                Glide.with(this)
                                        .load(alergenos[d].imagen_alergeno)
                                        .error(R.drawable.no_photo)
                                        .into(imagenAlergeno);

                                contenedorAlergenos.addView(unidadAlergeno);
                                break;

                            }

                        }

                    }
                }

                contendorElementos.addView(platoNivel4);

                platoNivel4.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        activaBoton(false,platoNivel4);

                        cambiaNivel3(laKartaNivel3[este3],platoNivel4,"nivel4",este4,codNivel2);

                        //cambiaDetalle("nivel4", nivel3[este3].cod_subnivel[este4],nombrePlato_nivel4,detallePlato_nivel4,visible,null);

                    }
                });

            }

        }

        scrollNivel3.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scrollNivel3.getViewTreeObserver().removeOnPreDrawListener(this);
                scrollNivel3.setScrollY(cargaPosicionScroll());
                return false;
            }
        });

        barraProgreso.setVisibility(View.GONE);

    }

    private void cambiaNivel3(final Kartas queNivel, final View boton, final String nivel, final int quesubnivel, final String codNivel2){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        estaAgotado="1";

        alergenosElegidos="";

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_imagen_datos, null);

        enviaDatosCategoria =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        imagenNivel3=alertLayout.findViewById(R.id.imagen_nivel);
        final TextView nombreNivel=alertLayout.findViewById(R.id.nombre_categoria);
        final TextView detalleNivel=alertLayout.findViewById(R.id.detalle_categoria);
        final TextView precioNivel=alertLayout.findViewById(R.id.precio_categoria);
        final TextView borraPlato=alertLayout.findViewById(R.id.elimina_nivel);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);
        TextView tituloCambio=alertLayout.findViewById(R.id.titulo_cambionivel);
        final SwitchCompat agotado=alertLayout.findViewById(R.id.switch_agotado);
        final CardView iconoNoVer=alertLayout.findViewById(R.id.icono_no_ver);

        final SwitchCompat mostrarImagen=alertLayout.findViewById(R.id.switch_sinimagen);

        final CardView fichaDestacar=alertLayout.findViewById(R.id.ficha_destacar);
        fichaDestacar.setVisibility(View.GONE);

        final LinearLayout contenedorAlergenos=alertLayout.findViewById(R.id.contenedor_alergenos);
        final TextView textoAlergenos=alertLayout.findViewById(R.id.texto_alergenos);

        LinearLayout fichaTipoComida=alertLayout.findViewById(R.id.ficha_tipo_comida);
        View lineatipoComida=alertLayout.findViewById(R.id.linea_tipo_comida);
        final TextView cantidadNivel3=alertLayout.findViewById(R.id.tipo_comida);


        final LinearLayout fichaAlergenos=alertLayout.findViewById(R.id.ficha_alergenos);
        LinearLayout fichaAgotado=alertLayout.findViewById(R.id.ficha_agotado);
        LinearLayout fichaEsmenu=alertLayout.findViewById(R.id.ficha_esmenu);
        View lineaAgotado=alertLayout.findViewById(R.id.linea_agotado);
        View lineaAlergenos=alertLayout.findViewById(R.id.linea_alergenos);
        View lineaEsmenu=alertLayout.findViewById(R.id.linea_esmenu);

        fichaEsmenu.setVisibility(View.GONE);
        lineaEsmenu.setVisibility(View.GONE);



        final String codNivelEnviar;
        final String rutaImagenEnviar;

        if(nivel.equals("nivel3")) {  // --------- nivel 3 ----------

            cantidadNivel3.setHint(getString(R.string.introduce_cuantos_platos_elegir));

            tituloCambio.setText(getString(R.string.modificar_categoria));

            borraPlato.setText(getString(R.string.borra_categoria));

            cantidadNivel3.setText(String.valueOf(queNivel.cantidad_nivel));

            cantidadNivel3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false,cantidadNivel3);

                    introduceTexto(cantidadNivel3,InputType.TYPE_CLASS_NUMBER, enviaDatosCategoria);

                }
            });

            if(queNivel.mostrar_imagen==1){

                mostrar_imagen=1;
                mostrarImagen.setChecked(true);
                iconoNoVer.setVisibility(View.GONE);

            }else{

                mostrar_imagen=0;
                mostrarImagen.setChecked(false);
                iconoNoVer.setVisibility(View.VISIBLE);
            }

            fichaAgotado.setVisibility(View.GONE);
            lineaAgotado.setVisibility(View.GONE);
            fichaAlergenos.setVisibility(View.GONE);
            lineaAlergenos.setVisibility(View.GONE);

            alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                    Glide.with(getApplicationContext())
                            .load(queNivel.imagen_nivel)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.no_photo)
                            .into(imagenNivel3);
                    return false;
                }
            });

            nombreNivel.setText(queNivel.nombre_nivel);
            detalleNivel.setText(queNivel.detalle_nivel);
            precioNivel.setText(form.format(queNivel.precio_nivel));

            codNivelEnviar=queNivel.cod_nivel;
            rutaImagenEnviar=queNivel.imagen_nivel;

        }else{  // ---------- NIVEL 4 -------------------

            tituloCambio.setText(getString(R.string.modifica_producto));

            borraPlato.setText(getString(R.string.borra_producto));

            fichaTipoComida.setVisibility(View.GONE);
            lineatipoComida.setVisibility(View.GONE);

            if(queNivel.mostrar_imagen_subnivel[quesubnivel]==1){

                mostrar_imagen=1;
                mostrarImagen.setChecked(true);
                iconoNoVer.setVisibility(View.GONE);

            }else{

                mostrar_imagen=0;
                mostrarImagen.setChecked(false);
                iconoNoVer.setVisibility(View.VISIBLE);
            }

            if(queNivel.visible[quesubnivel]==0){

                agotado.setChecked(true);
                estaAgotado="0";

            }

            //----- ALERGENOS -------------------

            alergenosElegidos=queNivel.alergenos_subnivel[quesubnivel];

            if(!alergenosElegidos.equals("")){

                int borrador=0;

                for(int i=0;i<alergenosElegidos.length();i++){

                    for(int e=0;e<alergenos.length;e++){

                        if(alergenosElegidos.charAt(i)==alergenos[e].codigo_alergeno.charAt(0)){

                            if(borrador==0){

                                borrador=1;
                                contenedorAlergenos.removeAllViews();
                            }

                            View unidadAlergeno = inflater.inflate(R.layout.alergeno_unidad, null);

                            ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);

                            Glide.with(this)
                                    .load(alergenos[e].imagen_alergeno)
                                    .error(R.drawable.no_photo)
                                    .into(imagenAlergeno);

                            contenedorAlergenos.addView(unidadAlergeno);

                            break;
                        }
                    }

                }

            }

            fichaAlergenos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false,fichaAlergenos);

                    abreFichaAlergenos(fichaAlergenos,contenedorAlergenos, enviaDatosCategoria,textoAlergenos);

                }
            });

            agotado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    if(estaAgotado.equals("0") && !isChecked){

                        estaAgotado="1";
                        agotado.setChecked(false);
                    }else{

                        estaAgotado="0";
                        agotado.setChecked(true);

                    }

                    activaBoton(true, enviaDatosCategoria);

                }
            });

            alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                    Glide.with(getApplicationContext())
                            .load(queNivel.imagen_subnivel[quesubnivel])
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.no_photo)
                            .into(imagenNivel3);
                    return false;
                }
            });


            nombreNivel.setText(queNivel.nombre_subnivel[quesubnivel]);
            detalleNivel.setText(queNivel.detalle_subnivel[quesubnivel]);
            precioNivel.setText(form.format(queNivel.precio_subnivel[quesubnivel]));

            codNivelEnviar=queNivel.cod_subnivel[quesubnivel];
            rutaImagenEnviar=queNivel.imagen_subnivel[quesubnivel];

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

        activaBoton(false, enviaDatosCategoria);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();




        nombreNivel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activaBoton(false,nombreNivel);

                introduceTexto(nombreNivel, InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        detalleNivel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activaBoton(false,detalleNivel);

                introduceTexto(detalleNivel,InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        precioNivel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,precioNivel);

                introduceTexto(precioNivel,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, enviaDatosCategoria);

            }
        });

        borraPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,borraPlato);

                preguntaSiEliminar(nivel,quesubnivel,queNivel,borraPlato,dialog);


            }
        });

        enviaDatosCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, enviaDatosCategoria);

                if(compruebaConexion()) {

                    activaBoton(false, enviaDatosCategoria);

                    String extraEnviar="";

                    if(nivel.equals("nivel3")){

                        if(cantidadNivel3.getText().toString().equals("") || cantidadNivel3.getText().toString().equals("0")){

                            extraEnviar="1";

                        }else {

                            extraEnviar = cantidadNivel3.getText().toString();
                        }
                    }

                    cargaUserEmpresa();



                    serverEnvioDatos.enviaCambiosNivel(user,alses,alsesk,scrollNivel3.getScrollY(), nombreNivel.getText().toString(), detalleNivel.getText().toString(), precioNivel.getText().toString(), extraEnviar, codNivelEnviar, queNivel.cod_nivel,codNivel2, rutaImagenEnviar, alergenosElegidos, estaAgotado, miPath,mostrar_imagen,0, nivel,menuAver.cod_nivel_sup);

                    miPath = null; // ----------- vacia la imagen para la siguiente

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

    private void preguntaSiEliminar(final String nivel,final int queBorro, final Kartas queNivel, final View boton,final AlertDialog dialogo){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        final Button cancelar=alertLayout.findViewById(R.id.pedido_guardar);
        final Button borrar=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

        if(nivel.equals("nivel3")){

            pregunta.setText(R.string.pregunta_borrar_categoria);
        }else{
            pregunta.setText(R.string.pregunta_borrar_producto);

        }



        cancelar.setText(getResources().getString(R.string.cancelar));
        borrar.setText(getResources().getString(R.string.eliminar));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,cancelar);

                activaBoton(true,boton);

                dialog.cancel();


            }
        });

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,borrar);

                JSONArray ordenNivel=new JSONArray();

                String queImagenBorro="";
                String codNivel3=queNivel.cod_nivel;
                String codNivel4=queNivel.cod_subnivel[queBorro];

                try {

                    Map<String, String> parametros = new HashMap<>();

                    int ordena=1;

                    if(nivel.equals("nivel3")) {

                        queImagenBorro=queNivel.imagen_nivel;

                        for (int i = 0; i < laKartaNivel3copia.length; i++) {

                            if (!laKartaNivel3copia[i].cod_nivel.equals(queNivel.cod_nivel)) {

                                if (laKartaNivel3copia[i].orden_nivel != ordena) {

                                    parametros.put("codNivel", laKartaNivel3copia[i].cod_nivel);
                                    parametros.put("orden", String.valueOf(ordena));

                                    JSONObject dato2 = new JSONObject(parametros);

                                    ordenNivel.put(dato2);
                                }

                                ordena++;


                            }

                        }
                    }else{

                        queImagenBorro=queNivel.imagen_subnivel[queBorro];

                        // ES NIVEL 4

                        for (int i = 0; i < laKartaNivel3copia.length; i++) {

                            if (!laKartaNivel3copia[i].cod_subnivel[queBorro].equals(queNivel.cod_subnivel[queBorro])) {

                                if (laKartaNivel3copia[i].orden_subnivel[queBorro] != ordena) {

                                    parametros.put("codNivel", laKartaNivel3copia[i].cod_subnivel[queBorro]);
                                    parametros.put("orden", String.valueOf(ordena));

                                    JSONObject dato2 = new JSONObject(parametros);

                                    ordenNivel.put(dato2);
                                }

                                ordena++;


                            }

                        }


                    }



                }catch (Exception e){

                    System.out.println("ERROR EN ORDEN: "+e.getMessage());

                }

                if(compruebaConexion()) {

                    cargaUserEmpresa();

                    serverEnvioDatos.borraNivel(user,alses,alsesk,scrollNivel3.getScrollY(), queImagenBorro, ordenNivel, nivel,0, menuAver.cod_nivel_sup, menuAver.cod_nivel ,codNivel3,codNivel4);
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

    public void nuevoNivel3(final String nivelAcrear, final View boton, final int quenivel4){

        LayoutInflater inflater = getLayoutInflater();

        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_imagen_datos, null);

        TextView titulo=alertLayout.findViewById(R.id.titulo_cambionivel);

        final String extra;

        enviaDatosCategoria =alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        imagenNivel3=alertLayout.findViewById(R.id.imagen_nivel);
        final TextView nombreNivel3=alertLayout.findViewById(R.id.nombre_categoria);
        final TextView detalleNivel3=alertLayout.findViewById(R.id.detalle_categoria);
        final TextView precioNivel3=alertLayout.findViewById(R.id.precio_categoria);
        final TextView cantidadNivel3=alertLayout.findViewById(R.id.tipo_comida);
        TextView borraCategoria=alertLayout.findViewById(R.id.elimina_nivel);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);
        final CardView iconoNoVer=alertLayout.findViewById(R.id.icono_no_ver);

        final SwitchCompat mostrarImagen=alertLayout.findViewById(R.id.switch_sinimagen);

        final CardView fichaDestacar=alertLayout.findViewById(R.id.ficha_destacar);
        fichaDestacar.setVisibility(View.GONE);

        LinearLayout fichaAgotado=alertLayout.findViewById(R.id.ficha_agotado);
        View lineaAgotado=alertLayout.findViewById(R.id.linea_agotado);

        final LinearLayout fichaAlergenos=alertLayout.findViewById(R.id.ficha_alergenos);
        final LinearLayout contenedorAlergenos=alertLayout.findViewById(R.id.contenedor_alergenos);
        final TextView textoAlergenos=alertLayout.findViewById(R.id.texto_alergenos);
        View lineaAlergenos=alertLayout.findViewById(R.id.linea_alergenos);

        LinearLayout fichaEsmenu=alertLayout.findViewById(R.id.ficha_esmenu);
        View lineaEsmenu=alertLayout.findViewById(R.id.linea_esmenu);

        LinearLayout fichaTipocomida=alertLayout.findViewById(R.id.ficha_tipo_comida);
        View lineaTipocomida=alertLayout.findViewById(R.id.linea_tipo_comida);

        fichaAgotado.setVisibility(View.GONE);
        lineaAgotado.setVisibility(View.GONE);

        fichaEsmenu.setVisibility(View.GONE);
        lineaEsmenu.setVisibility(View.GONE);

        borraCategoria.setVisibility(View.GONE);

        final String codNivelSup;

        mostrar_imagen=1;
        iconoNoVer.setVisibility(View.GONE);
        mostrarImagen.setChecked(true);

        if(nivelAcrear.equals("nivel3")){

            cantidadNivel3.setHint(getString(R.string.introduce_cuantos_platos_elegir));

            cantidadNivel3.setText("1");

            cantidadNivel3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    activaBoton(false,cantidadNivel3);

                    introduceTexto(cantidadNivel3,InputType.TYPE_CLASS_NUMBER, enviaDatosCategoria);

                }
            });

            fichaAlergenos.setVisibility(View.GONE);
            lineaAlergenos.setVisibility(View.GONE);

            enviaDatosCategoria.setText(getString(R.string.anade_categoria));
            String texto=getString(R.string.nueva_categoria_menu)+" "+menuAver.nombre_nivel;
            titulo.setText(texto);

            codNivelSup=menuAver.cod_nivel;

            extra="1";


        }else{

            fichaTipocomida.setVisibility(View.GONE);
            lineaTipocomida.setVisibility(View.GONE);

            // ES NIVEL 4

            alergenosElegidos="";

            fichaAlergenos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    activaBoton(false,fichaAlergenos);

                    abreFichaAlergenos(fichaAlergenos,contenedorAlergenos, enviaDatosCategoria,textoAlergenos);

                }
            });

            enviaDatosCategoria.setText(getString(R.string.anade_producto));

            String texto=getString(R.string.nuevo_plato_menu)+" "+laKartaNivel3[quenivel4].nombre_nivel;
            titulo.setText(texto);

            codNivelSup=laKartaNivel3[quenivel4].cod_nivel;

            extra=menuAver.cod_nivel;

        }

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

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                Glide.with(getApplicationContext())
                        .load(R.drawable.no_photo)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imagenNivel3);
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

        nombreNivel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,nombreNivel3);

                introduceTexto(nombreNivel3, InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });

        detalleNivel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,detalleNivel3);

                introduceTexto(detalleNivel3,InputType.TYPE_CLASS_TEXT, enviaDatosCategoria);

            }
        });



        precioNivel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,precioNivel3);

                introduceTexto(precioNivel3,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, enviaDatosCategoria);

            }
        });

        enviaDatosCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(compruebaConexion()) {

                    if (nombreNivel3.getText().equals("")) {

                        mensajeAlerta = getString(R.string.intro_nombre_categoria);
                        ponAlerta();

                    } else {

                        cargaUserEmpresa();

                        String extraAenviar;

                        if(nivelAcrear.equals("nivel3")){

                            if(cantidadNivel3.getText().toString().equals("") || cantidadNivel3.getText().toString().equals("0")){

                                extraAenviar="1";

                            }else {

                                extraAenviar = cantidadNivel3.getText().toString();
                            }
                        }else{

                            extraAenviar=extra;

                        }

                        activaBoton(true, boton);

                        dialog.cancel();

                        serverEnvioDatos.introduceNuevoNivel(user,alses,alsesk,scrollNivel3.getScrollY(), nivelAcrear, codNivelSup, nombreNivel3.getText().toString(), detalleNivel3.getText().toString(), precioNivel3.getText().toString(), alergenosElegidos, miPath, mostrar_imagen,0, extraAenviar,menuAver.cod_nivel_sup);

                        miPath = null; // ----------- vacia la imagen para la siguiente


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

    public void enviaCambiosOrden(View boton,String quenivel){

        JSONArray ordenNivel=new JSONArray();

        try {

            Map<String, String> parametros = new HashMap<>();


            for(int i=0;i<laKartaNivel3.length;i++){
                System.out.println("COMPARA: "+laKartaNivel3[i].orden_nivel+" Y "+laKartaNivel3copia[i].orden_nivel);

                if(laKartaNivel3[i].orden_nivel!=laKartaNivel3copia[i].orden_nivel){

                    parametros.put("codNivel", laKartaNivel3[i].cod_nivel);
                    parametros.put("orden", String.valueOf(laKartaNivel3[i].orden_nivel));

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

            serverEnvioDatos.enviaCambiosOrden(user,alses,alsesk,scrollNivel3.getScrollY(),ordenNivel,quenivel,boton);


        }else{

            System.out.println("ORDEN VACIO");

        }




    }

    private void abreFichaAlergenos(final View boton, final LinearLayout contenedorAlergenos, final TextView botonEnviar, final TextView textoAlergenos){

        View alertLayout = inflador.inflate(R.layout.emerg_alergenos, null);

        TextView aceptar=alertLayout.findViewById(R.id.aceptar);
        TextView cancelar=alertLayout.findViewById(R.id.cancelar);
        final LinearLayout listaAlergenos=alertLayout.findViewById(R.id.lista_alergenos);

        AlertDialog.Builder alert = new AlertDialog.Builder(Edita_Nivel3.this);

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

    public void onResume(){

        super.onResume();

        serverEnvioDatos =new Server_EnvioDatos(miBundle,Edita_Nivel3.this,this,inflador);

    }

    public void cargaUserEmpresa(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");

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

    private void introduceTexto(final TextView elTexto,final int tipo,final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View introTexto = inflater.inflate(R.layout.entrada_texto, null);

        final TextView entradaTexto=introTexto.findViewById(R.id.recoge_texto);
        final ImageView validaTexto=introTexto.findViewById(R.id.valida_texto);

        activaBoton(false,validaTexto);

        AlertDialog.Builder ponTexto = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        ponTexto.setView(introTexto);
        // disallow cancel of AlertDialog on click of back button and outside touch
        ponTexto.setCancelable(true);

        final AlertDialog dialogoTexto = ponTexto.create();

        dialogoTexto.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,elTexto);
            }
        });


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


                if(tipo==InputType.TYPE_CLASS_TEXT) {

                    elTexto.setText(entradaTexto.getEditableText().toString());

                }else if(tipo==InputType.TYPE_CLASS_NUMBER){

                    if(elTexto.getEditableText().toString().equals("")){

                        elTexto.setText("0");
                    }else {

                        elTexto.setText(entradaTexto.getEditableText().toString());

                    }

                }else{

                    if(entradaTexto.getEditableText().toString().equals("")){

                        elTexto.setText(form.format(Double.parseDouble("0")));

                    }else {

                        elTexto.setText(form.format(Double.parseDouble(entradaTexto.getEditableText().toString())));

                    }

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



    }

    public void moverArriba(int posNivel,int queNivel){

        if(posNivel>0) {

            View vista = rutacontenedor.getChildAt(posNivel-1);
            rutacontenedor.removeViewAt(posNivel-1);
            rutacontenedor.addView(vista, posNivel);

            int orden=laKartaNivel3[queNivel].orden_nivel;

            laKartaNivel3[queNivel].orden_nivel=orden-1;

            for(int i=0;i<laKartaNivel3.length;i++){

                if(laKartaNivel3[i].orden_nivel==orden-1 && i!=queNivel){

                    laKartaNivel3[i].orden_nivel=orden;

                }

            }

            int cuenta=0;

            for(int i=0;i<laKartaNivel3.length;i++){

                if(laKartaNivel3[i].orden_nivel!=laKartaNivel3copia[i].orden_nivel){

                    cuenta=1;
                    break;

                }

            }

            if(cuenta==1){

                ocultaBoton(true,cambiarOrden);

            }else{

                ocultaBoton(false,cambiarOrden);
            }

            int donde=rutacontenedor.getChildAt(posNivel).getHeight();
            scrollNivel3.scrollBy(0,-donde);


        }

    }

    public void moverAbajo(int posNivel,int queNivel){

        if(posNivel+1<rutacontenedor.getChildCount()) {

            View vista = rutacontenedor.getChildAt(posNivel+1);
            rutacontenedor.removeViewAt(posNivel+1);
            rutacontenedor.addView(vista, posNivel);

            int orden=laKartaNivel3[queNivel].orden_nivel;

            laKartaNivel3[queNivel].orden_nivel=orden+1;

            for(int i=0;i<laKartaNivel3.length;i++){

                if(laKartaNivel3[i].orden_nivel==orden+1 && i!=queNivel){

                    laKartaNivel3[i].orden_nivel=orden;

                }

            }

            int cuenta=0;

            for(int i=0;i<laKartaNivel3.length;i++){

                if(laKartaNivel3[i].orden_nivel!=laKartaNivel3copia[i].orden_nivel){

                    cuenta=1;
                    break;

                }

            }

            if(cuenta==1){

                ocultaBoton(true,cambiarOrden);

            }else{

                ocultaBoton(false,cambiarOrden);
            }

            int donde=rutacontenedor.getChildAt(posNivel).getHeight();
            scrollNivel3.scrollBy(0,donde);

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

                        imagenNivel3.setImageURI(miPath);
                        activaBoton(true, enviaDatosCategoria);

                        System.out.println("IMAGEN DE DIRECTORIO: "+miPath);

                    }else{

                        imagenDeFoto=true;

                        imagenNivel3.setImageURI(miPath);
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

                if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(image.getParent())) {
                    image.delete();
                    System.out.println("BORRA CAMARA :"+image.getPath());
                }else{

                    System.out.println("NO BORRA CAMARA :"+image.getPath());
                }
        }

 */
    }

    private boolean compruebaLimite(int cuantos, int limite, String elemento){

        System.out.println("CUANTOS: "+cuantos+" LIMITE: "+limite);

        if(cuantos>=limite){

            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

            Button aceptar=alertLayout.findViewById(R.id.pedido_guardar);
            Button quitar=alertLayout.findViewById(R.id.pedido_noguardar);
            ImageView icono=alertLayout.findViewById(R.id.icono_alerta);
            TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

            String texto="";

            if(elemento.equals("platos")) {

                texto = getResources().getString(R.string.limite_alcanzado) + " " + limite + " " + getResources().getString(R.string.productos_menu) + "\n\n" + getResources().getString(R.string.limite_productos);

            }else{

                texto = getResources().getString(R.string.limite_alcanzado) + " " + limite + " " + getResources().getString(R.string.categorias_menu) + "\n\n" + getResources().getString(R.string.limite_categorias);


            }
            icono.setImageResource(R.drawable.sorry);
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

