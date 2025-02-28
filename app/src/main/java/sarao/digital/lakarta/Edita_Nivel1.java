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
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;



public class Edita_Nivel1 extends AppCompatActivity {


    // pantalla ------

    LinearLayout pantallaMenu, botonesMenu;

    // --------------

    Context contexto;

    Uri miPath;
    File image;
    ContentResolver resolver;

    int mostrar_imagen=0;

    private long mLastClickTime = 0;

    String user,alses,alsesk;

    boolean imagenDeFoto=false;

    Kartas[] laKartaNivel1,laKartaNivel1copia;

    ProgressBar barraProgreso;

    ImageView imagenNivel1,modificaImagen;

    LinearLayout rutacontenedorCategorias,contenedorEmergente,nueva_categoria;
    LayoutInflater inflador;
    ScrollView scrollNivel1;

    TextView enviaDatosCategoria,salirUsuario,cambiarOrden;

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
        setContentView(R.layout.activity_nivel1);

        // ---------------------

        float radius = 5f;
        View decorView = getWindow().getDecorView();

        ViewGroup rootView = decorView.findViewById(android.R.id.content);

        Drawable windowBackground = decorView.getBackground();

        pantallaMenu=findViewById(R.id.pantalla_menu1);
        botonesMenu=findViewById(R.id.botones_menu1);

        pantallaMenu.setAlpha(0f);
        botonesMenu.setAlpha(0f);

        rutacontenedorCategorias = findViewById(R.id.contenedor_categorias);
        contenedorEmergente=findViewById(R.id.layout_emergente1);
        scrollNivel1=findViewById(R.id.scroll_menu_usuario);

        nueva_categoria=findViewById(R.id.nueva_categoria);


        salirUsuario=findViewById(R.id.salir_usuario);
        cambiarOrden=findViewById(R.id.enviar_cambio_orden1);
        //estado=findViewById(R.id.estado);

        inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        barraProgreso = findViewById(R.id.barraprogreso_usuario);

        barraProgreso.setVisibility(View.GONE);

        ocultaBoton(false,cambiarOrden);

        contexto=this;

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        serverRecibeDatos =new Server_RecibeDatos(this);

        serverRecibeDatos.cargaNivelGuardado("nivel1");
        serverRecibeDatos.cargaMiRestGuardado();

        miRestaurante= serverRecibeDatos.miRestaurante;
        misLimitaciones= serverRecibeDatos.misLimitaciones;
        laKartaNivel1= serverRecibeDatos.laKartaNivel1;

        // copia de nivel1 -----------------

        laKartaNivel1copia=new Kartas[laKartaNivel1.length];

        for(int i=0;i<laKartaNivel1.length;i++){

            laKartaNivel1copia[i]=new Kartas(laKartaNivel1[i]);

        }

        // ---------------------------------------


        //envioDatos=new EnvioDatos(null,Menu_Usuario.this,miRestaurante,Menu_Usuario.this,inflador,user,pass);

        cargaPantalla();

    }

    @Override
    public void onBackPressed() {

        if(cambiarOrden.isEnabled()){

            preguntaSiGuardar();
        }else {

            super.onBackPressed();
        }

    }

    private void cargaPantalla(){

        pantallaMenu.setAlpha(1f);
        botonesMenu.setAlpha(1f);

        if (laKartaNivel1.length > 0) {

            ponCategorias();

        }

        salirUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,salirUsuario);

                if(cambiarOrden.isEnabled()){

                    preguntaSiGuardar();

                }else {

                    finish();
                }


            }
        });

        nueva_categoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false, nueva_categoria);

                if(compruebaLimite(laKartaNivel1.length,misLimitaciones.cant_categorias)) {

                    nuevaCategoria(nueva_categoria);

                }else{

                    activaBoton(true, nueva_categoria);
                }

            }
        });

        cambiarOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                ocultaBoton(false,cambiarOrden);
                enviaCambiosOrden(cambiarOrden);

            }
        });


    }

    public void ponCategorias(){

        rutacontenedorCategorias.removeAllViews();

        for(int i=0;i<laKartaNivel1.length;i++) {

            final int cual=i;

            final ConstraintLayout laCategoria = (ConstraintLayout) inflador.inflate(R.layout.barra_categorias_edit, null);

            TextView nombreCat = laCategoria.findViewById(R.id.nombre_categoria);
            TextView precioCat = laCategoria.findViewById(R.id.precio_categoria);
            TextView detalleCat = laCategoria.findViewById(R.id.detalle_categoria);
            LinearLayout fondoDetalle=laCategoria.findViewById(R.id.fondo_detalle);
            final ImageView imagenPlato = laCategoria.findViewById(R.id.imagen_categoria);
            ImageView moverArriba=laCategoria.findViewById(R.id.mover_arriba);
            ImageView moverAbajo=laCategoria.findViewById(R.id.mover_abajo);
            final ImageView editaCategoria=laCategoria.findViewById(R.id.edita_categoria);

            CardView noVerImagen = laCategoria.findViewById(R.id.no_mostrar_imagen);
            noVerImagen.setVisibility(View.GONE);

            // COLORES ------------

            if(Menu_Empresa.miRestaurante.cNP!=0) {
                nombreCat.setTextColor(Menu_Empresa.miRestaurante.cNP);
            }
            if(Menu_Empresa.miRestaurante.cDP!=0) {
                detalleCat.setTextColor(Menu_Empresa.miRestaurante.cDP);
            }
            if(Menu_Empresa.miRestaurante.fDP!=0) {
                fondoDetalle.setBackgroundColor(Menu_Empresa.miRestaurante.fDP);
            }

            // ----------------------

            nombreCat.setText(laKartaNivel1[i].nombre_nivel);
            precioCat.setText(form.format(laKartaNivel1[i].precio_nivel) + " €");
            detalleCat.setText(laKartaNivel1[i].detalle_nivel);

            if(laKartaNivel1[i].mostrar_imagen==0){

                noVerImagen.setVisibility(View.VISIBLE);
            }

            Glide.with(getApplicationContext())
                    .load(laKartaNivel1[i].imagen_nivel)
                    .error(R.drawable.no_photo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imagenPlato);

            //rutacontenedor.addView(losplatos, cuantos + i);
            rutacontenedorCategorias.addView(laCategoria);



            moverArriba.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    moverArriba( rutacontenedorCategorias.indexOfChild(laCategoria), cual);

                }
            });

            moverAbajo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    moverAbajo( rutacontenedorCategorias.indexOfChild(laCategoria),cual);

                }
            });

            laCategoria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(false,laCategoria);
                    veAcategoria(laKartaNivel1[cual],laCategoria);


                }
            });


            editaCategoria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    System.out.println("EDITA");

                    activaBoton(false,editaCategoria);
                    cambiaCategoria(laKartaNivel1[cual],editaCategoria, cual);


                }
            });


        }

        scrollNivel1.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scrollNivel1.getViewTreeObserver().removeOnPreDrawListener(this);
                scrollNivel1.setScrollY(cargaPosicionScroll());
                return false;
            }
        });

    }

    public void nuevaCategoria(final View boton){

        LayoutInflater inflater = getLayoutInflater();

        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_imagen_datos, null);

        TextView titulo=alertLayout.findViewById(R.id.titulo_cambionivel);

        enviaDatosCategoria=alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        imagenNivel1 =alertLayout.findViewById(R.id.imagen_nivel);
        final TextView nombreCategoria=alertLayout.findViewById(R.id.nombre_categoria);
        final TextView detalleCategoria=alertLayout.findViewById(R.id.detalle_categoria);
        final TextView precioCategoria=alertLayout.findViewById(R.id.precio_categoria);
        TextView borraCategoria=alertLayout.findViewById(R.id.elimina_nivel);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);
        final CardView iconoNoVer=alertLayout.findViewById(R.id.icono_no_ver);

        alertLayout.findViewById(R.id.cantidad_platos).setVisibility(View.GONE);

        final SwitchCompat mostrarImagen=alertLayout.findViewById(R.id.switch_sinimagen);

        final CardView fichaDestacar=alertLayout.findViewById(R.id.ficha_destacar);
        fichaDestacar.setVisibility(View.GONE);

        LinearLayout fichaTipoComida=alertLayout.findViewById(R.id.ficha_tipo_comida);
        View lineatipoComida=alertLayout.findViewById(R.id.linea_tipo_comida);
        fichaTipoComida.setVisibility(View.GONE);
        lineatipoComida.setVisibility(View.GONE);

        LinearLayout fichaEsmenu=alertLayout.findViewById(R.id.ficha_esmenu);
        LinearLayout fichaAgotado=alertLayout.findViewById(R.id.ficha_agotado);
        final LinearLayout fichaAlergenos=alertLayout.findViewById(R.id.ficha_alergenos);
        View lineaAgotado=alertLayout.findViewById(R.id.linea_agotado);
        View lineaEsmenu=alertLayout.findViewById(R.id.linea_esmenu);
        View lineaAlergenos=alertLayout.findViewById(R.id.linea_alergenos);

        fichaAgotado.setVisibility(View.GONE);
        lineaAgotado.setVisibility(View.GONE);
        fichaEsmenu.setVisibility(View.GONE);
        fichaAlergenos.setVisibility(View.GONE);
        lineaEsmenu.setVisibility(View.GONE);
        lineaAlergenos.setVisibility(View.GONE);
        borraCategoria.setVisibility(View.GONE);
        iconoNoVer.setVisibility((View.GONE));

        mostrarImagen.setChecked(true);

        mostrar_imagen=1;

        enviaDatosCategoria.setText(getString(R.string.anade_categoria));
        titulo.setText(getString(R.string.nueva_categoria));

        Glide.with(getApplicationContext())
                .load(R.drawable.no_photo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imagenNivel1);

        activaBoton(false, enviaDatosCategoria);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });

        nombreCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,nombreCategoria);

                introduceTexto(nombreCategoria,InputType.TYPE_CLASS_TEXT,enviaDatosCategoria);

            }
        });

        detalleCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,detalleCategoria);

                introduceTexto(detalleCategoria,InputType.TYPE_CLASS_TEXT,enviaDatosCategoria);

            }
        });

        precioCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,precioCategoria);

                introduceTexto(precioCategoria,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,enviaDatosCategoria);

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

        enviaDatosCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,enviaDatosCategoria);

                    if (compruebaConexion()) {

                        if (nombreCategoria.getText().equals("")) {

                            mensajeAlerta = getString(R.string.intro_nombre_categoria);
                            activaBoton(true, enviaDatosCategoria);
                            ponAlerta();

                        } else {

                            cargaUserEmpresa();

                            serverEnvioDatos.introduceNuevoNivel(user, alses, alsesk, scrollNivel1.getScrollY(), "nivel1", "", nombreCategoria.getText().toString(), detalleCategoria.getText().toString(), precioCategoria.getText().toString(), "", miPath, mostrar_imagen,0, "","");

                            miPath = null; // ----------- vacia la imagen para la siguiente

                            activaBoton(true, boton);

                            dialog.cancel();
                        }
                    } else {

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

        //activaBoton(true,boton);



    }

    public void veAcategoria(Kartas queCategoria,View boton){

        Intent miIntent = new Intent(this, Edita_Nivel2.class);

        miIntent.putExtra("NIVELAVER",queCategoria);

        startActivity(miIntent);

        activaBoton(true,boton);

    }

    private void preguntaSiGuardar(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        Button continuar=alertLayout.findViewById(R.id.pedido_guardar);
        Button salir=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);
        ImageView imagen=alertLayout.findViewById(R.id.icono_alerta);

        continuar.setText(getResources().getString(R.string.no_quedarme));
        salir.setText(getResources().getString(R.string.salir));
        pregunta.setText(getResources().getString(R.string.pregunta_cambios_orden));
        imagen.setImageResource(R.drawable.cambios_karta);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
                activaBoton(true, salirUsuario);

            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dialog.cancel();
                finish();


            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    private void cambiaCategoria(final Kartas queCategoria, final View boton, final int idCategoria){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.emerg_cambia_imagen_datos, null);

        enviaDatosCategoria=alertLayout.findViewById(R.id.modificar);
        final TextView cancelar=alertLayout.findViewById(R.id.nomodificar);
        imagenNivel1 =alertLayout.findViewById(R.id.imagen_nivel);
        final TextView nombreCategoria=alertLayout.findViewById(R.id.nombre_categoria);
        final TextView detalleCategoria=alertLayout.findViewById(R.id.detalle_categoria);
        final TextView precioCategoria=alertLayout.findViewById(R.id.precio_categoria);
        final TextView borraCategoria=alertLayout.findViewById(R.id.elimina_nivel);
        modificaImagen=alertLayout.findViewById(R.id.cambia_imagen);
        final CardView iconoNoVer=alertLayout.findViewById(R.id.icono_no_ver);

        alertLayout.findViewById(R.id.cantidad_platos).setVisibility(View.GONE);

        final SwitchCompat mostrarImagen=alertLayout.findViewById(R.id.switch_sinimagen);

        final CardView fichaDestacar=alertLayout.findViewById(R.id.ficha_destacar);
        fichaDestacar.setVisibility(View.GONE);

        LinearLayout fichaTipoComida=alertLayout.findViewById(R.id.ficha_tipo_comida);
        View lineatipoComida=alertLayout.findViewById(R.id.linea_tipo_comida);
        fichaTipoComida.setVisibility(View.GONE);
        lineatipoComida.setVisibility(View.GONE);

        LinearLayout fichaAgotado=alertLayout.findViewById(R.id.ficha_agotado);
        View lineaAgotado=alertLayout.findViewById(R.id.linea_agotado);


        LinearLayout fichaEsmenu=alertLayout.findViewById(R.id.ficha_esmenu);
        View lineaEsmenu=alertLayout.findViewById(R.id.linea_esmenu);

        LinearLayout fichaAlergenos=alertLayout.findViewById(R.id.ficha_alergenos);
        View lineaAlergenos=alertLayout.findViewById(R.id.linea_alergenos);

        fichaAgotado.setVisibility(View.GONE);
        lineaAgotado.setVisibility(View.GONE);

        fichaEsmenu.setVisibility(View.GONE);
        fichaAlergenos.setVisibility(View.GONE);
        lineaEsmenu.setVisibility(View.GONE);
        lineaAlergenos.setVisibility(View.GONE);

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                Glide.with(getApplicationContext())
                        .load(queCategoria.imagen_nivel)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.no_photo)
                        .into(imagenNivel1);
                return false;
            }
        });

        nombreCategoria.setText(queCategoria.nombre_nivel);
        detalleCategoria.setText(queCategoria.detalle_nivel);
        precioCategoria.setText(form.format(queCategoria.precio_nivel));

        if(queCategoria.mostrar_imagen==1){

            mostrar_imagen=1;
            mostrarImagen.setChecked(true);
            iconoNoVer.setVisibility(View.GONE);

        }else{

            mostrar_imagen=0;
            mostrarImagen.setChecked(false);
            iconoNoVer.setVisibility(View.VISIBLE);
        }

        activaBoton(false, enviaDatosCategoria);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

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


        nombreCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,nombreCategoria);

                introduceTexto(nombreCategoria,InputType.TYPE_CLASS_TEXT,enviaDatosCategoria);

            }
        });

        detalleCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,detalleCategoria);

                introduceTexto(detalleCategoria,InputType.TYPE_CLASS_TEXT,enviaDatosCategoria);

            }
        });

        precioCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,precioCategoria);

                introduceTexto(precioCategoria,InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,enviaDatosCategoria);

            }
        });

        borraCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                activaBoton(false,borraCategoria);

                preguntaSiEliminar(queCategoria,borraCategoria,dialog,idCategoria);


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

                    activaBoton(false, enviaDatosCategoria);

                    cargaUserEmpresa();

                    serverEnvioDatos.enviaCambiosNivel(user, alses, alsesk, scrollNivel1.getScrollY(), nombreCategoria.getText().toString(), detalleCategoria.getText().toString(), precioCategoria.getText().toString(), "",queCategoria.cod_nivel, "","",queCategoria.imagen_nivel, "", "", miPath, mostrar_imagen, 0,"nivel1","");

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

        //activaBoton(true,boton);

    }

    private void preguntaSiEliminar(final Kartas queCategoria, final View boton, final AlertDialog dialogo, final int idCat){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        Button cancelar=alertLayout.findViewById(R.id.pedido_guardar);
        final Button borrar=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

        pregunta.setText(R.string.pregunta_borrar_categoria);

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

                activaBoton(false,borrar);

                JSONArray ordenNivel=new JSONArray();

                try {

                    Map<String, String> parametros = new HashMap<>();

                    int ordena=1;

                    for(int i=0;i<laKartaNivel1copia.length;i++){

                        if(!laKartaNivel1copia[i].cod_nivel.equals(queCategoria.cod_nivel)){

                            if(laKartaNivel1copia[i].orden_nivel!=ordena) {

                                parametros.put("codNivel", laKartaNivel1copia[i].cod_nivel);
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

                    serverEnvioDatos.borraNivel(user,alses,alsesk,scrollNivel1.getScrollY(), queCategoria.imagen_nivel, ordenNivel, "nivel1",0, queCategoria.cod_nivel, "","","");
                    dialog.cancel();
                    dialogo.cancel();
                }else{

                    activaBoton(true,boton);
                    mensajeAlerta = getString(R.string.sin_internet);
                    ponAlerta();
                    dialog.cancel();

                }


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

            System.out.println("ACTIVA");

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            System.out.println("DESACTIVA");

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

    public void onResume(){

        super.onResume();

        serverEnvioDatos =new Server_EnvioDatos(null, Edita_Nivel1.this, Edita_Nivel1.this,inflador);
        System.out.println("RESUME");
    }
    public void cargaUserEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");


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

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

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

                        imagenNivel1.setImageURI(miPath);
                        activaBoton(true, enviaDatosCategoria);

                        System.out.println("IMAGEN DE DIRECTORIO: "+miPath);

                    }else{

                        imagenDeFoto=true;

                        imagenNivel1.setImageURI(miPath);
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

    public void moverArriba(int posCategoria,int queCategoria){


        if(posCategoria>0) {

            View vista = rutacontenedorCategorias.getChildAt(posCategoria-1);
            rutacontenedorCategorias.removeViewAt(posCategoria-1);
            rutacontenedorCategorias.addView(vista, posCategoria);

            int orden=laKartaNivel1[queCategoria].orden_nivel;

            laKartaNivel1[queCategoria].orden_nivel=orden-1;

            for(int i=0;i<laKartaNivel1.length;i++){

                if(laKartaNivel1[i].orden_nivel==orden-1 && i!=queCategoria){

                    laKartaNivel1[i].orden_nivel=orden;

                }

            }

            int cuenta=0;

            for(int i=0;i<laKartaNivel1.length;i++){

                if(laKartaNivel1[i].orden_nivel!=laKartaNivel1copia[i].orden_nivel){

                    cuenta=1;
                    break;

                }

            }

            if(cuenta==1){

                ocultaBoton(true,cambiarOrden);

            }else{

                ocultaBoton(false,cambiarOrden);
            }

            int donde=rutacontenedorCategorias.getChildAt(posCategoria).getHeight();
            scrollNivel1.scrollBy(0,-donde);

        }

    }

    public void moverAbajo(int posCategoria,int queCategoria){

        if(posCategoria+1<rutacontenedorCategorias.getChildCount()) {

            View vista = rutacontenedorCategorias.getChildAt(posCategoria+1);
            rutacontenedorCategorias.removeViewAt(posCategoria+1);
            rutacontenedorCategorias.addView(vista, posCategoria);

            int orden=laKartaNivel1[queCategoria].orden_nivel;

            laKartaNivel1[queCategoria].orden_nivel=orden+1;

            for(int i=0;i<laKartaNivel1.length;i++){

                if(laKartaNivel1[i].orden_nivel==orden+1 && i!=queCategoria){

                    laKartaNivel1[i].orden_nivel=orden;

                }

            }

            int cuenta=0;

            for(int i=0;i<laKartaNivel1.length;i++){

                if(laKartaNivel1[i].orden_nivel!=laKartaNivel1copia[i].orden_nivel){

                    cuenta=1;
                    break;

                }

            }

            if(cuenta==1){

                ocultaBoton(true,cambiarOrden);

            }else{

                ocultaBoton(false,cambiarOrden);
            }

            int donde=rutacontenedorCategorias.getChildAt(posCategoria).getHeight();
            scrollNivel1.scrollBy(0,donde);

        }

    }

    public void enviaCambiosOrden(View boton){

        JSONArray ordenNivel=new JSONArray();

        try {

            Map<String, String> parametros = new HashMap<>();


            for(int i=0;i<laKartaNivel1.length;i++){

                if(laKartaNivel1[i].orden_nivel!=laKartaNivel1copia[i].orden_nivel){

                    parametros.put("codNivel", laKartaNivel1[i].cod_nivel);
                    parametros.put("orden", String.valueOf(laKartaNivel1[i].orden_nivel));

                    JSONObject dato2=new JSONObject(parametros);

                    ordenNivel.put(dato2);

                }

            }


        }catch (Exception e){

            System.out.println("ERROR EN ORDEN: "+e.getMessage());

        }

        if(ordenNivel.length()>0) {

            cargaUserEmpresa();

            serverEnvioDatos.enviaCambiosOrden(user, alses, alsesk, scrollNivel1.getScrollY(),ordenNivel,"nivel1", boton);

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

                    if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(Server_EnvioDatos.file.getParent())) {
                        Server_EnvioDatos.file.delete();
                        System.out.println("BORRA EL FILE COMPRIMIDO :"+ Server_EnvioDatos.file.getPath());
                    }else{

                        System.out.println("NO BORRA EL FILE COMPRIMIDO:"+ Server_EnvioDatos.file.getPath());
                    }

            }
        }

        if(miPath!=null && imagenDeFoto) {
            if (miPath.getPath() != null) {
                File fi = new File(miPath.getPath());
                if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(fi.getParent())) {
                    fi.delete();
                    System.out.println("BORRA EL FILE TEMPORAL :"+fi.getPath());
                }else{

                    System.out.println("NO BORRA EL FILE TEMPORAL:"+fi.getPath());
                }
                miPath = null;
            }
        }

        if(image!=null) {

            if((Environment.getExternalStorageDirectory().toString()+"/temp").equals(image.getParent())) {
                image.delete();
                System.out.println("BORRA IMAGEN DE CAMARA: "+image.getPath());
            }else{

                System.out.println("NO BORRA IMAGEN DE CAMARA: "+image.getPath());
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
            ImageView icono=alertLayout.findViewById(R.id.icono_alerta);
            TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);

            String texto=getResources().getString(R.string.limite_alcanzado)+" "+misLimitaciones.cant_categorias+" "+getResources().getString(R.string.categorias)+"\n\n"+getResources().getString(R.string.limite_categorias);

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

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

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

