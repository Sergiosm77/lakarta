package sarao.digital.lakarta;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Contenedor_Lakarta extends AppCompatActivity {

    public ViewPager2 viewPager;
    public FragmentStateAdapter pagerAdapter;

    final DecimalFormat formato = new DecimalFormat("0.00");

    private long mLastClickTime = 0;

    int cantidad;
    double precioFinal;
    int pon;

    private Ver_Platos verLosPlatos;

    ImageView salirKarta;

    // VARIABLES PEDIDO -----------
    int cantidadMenusPedido;
    int[] scrollMenuPedido;
    int cantidadNivel4;
    AlertDialog alertaPedido;

    private View ventanaPedido;
    private LinearLayout contenedorPedidos;

    TextView nombreRestaurante;

    boolean portadaPuesta;
    boolean cambiosEnplato;
    boolean cambiosEnMenu;

    LinearLayout contenedorPortada;

    private Toast mensajePop;
    private String mensajeAlerta="";

    static BlurView blurview;

    BBDD_Helper helper;

    TabLayout tabLayout;

    Kartas[] laKartaNivel1,laKartaNivel3;

    public static Restaurantes queRestaurante;
    public static Alergenos[] alergenos;

    LinearLayout portada;

    LayoutInflater inflador;

    ProgressBar barraProgreso;

    Server_RecibeDatos serverRecibeDatos;

    // Accesible desde fragmentos ----------

    public static TextView contador_total;
    public static LinearLayout botonPedido;
    public static boolean hayCambios;

    private String verDesdeAdmin="";

    // ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contenedor_lakarta);

        Bundle miBundle=this.getIntent().getExtras();

        helper = new BBDD_Helper(this);

        if(miBundle!=null) {

            queRestaurante = miBundle.getParcelable("QUERESTAURANTE");

            verDesdeAdmin = miBundle.getString("KARTA_DESDE_ADMIN");

        }

        cargaAlergenos();

        hayCambios=false;

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        //foto=findViewById(R.id.fotoKarta);
        salirKarta=findViewById(R.id.salir_karta);
        portada=findViewById(R.id.portada);
        contenedorPortada=findViewById(R.id.contenedor_portada);
        botonPedido=findViewById(R.id.boton_pedido);
        //nombreRest=findViewById(R.id.nombreKarta);
        //verPedido=findViewById(R.id.ver_pedido);

        nombreRestaurante=findViewById(R.id.nombre_restaurante);

        nombreRestaurante.setText("Karta de "+queRestaurante.nombre);

        contador_total=findViewById(R.id.contador_total);

        botonPedido.setAlpha(0f);

        barraProgreso=findViewById(R.id.progressBar2);

        if(queRestaurante.fKarta!=0){

            portada.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fKarta);
        }


        // ---------------------

        blurview=findViewById(R.id.blurView);

        float radius = 5f;
        View decorView = getWindow().getDecorView();

        ViewGroup rootView = decorView.findViewById(android.R.id.content);

        Drawable windowBackground = decorView.getBackground();

        blurview.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)

        ;

        portadaPuesta=true;

        blurview.setVisibility(View.GONE);

        // --------------------------------

        salirKarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!portadaPuesta){

                    moverPortada(portada,true);

                }else{

                    activaBoton(false, salirKarta);

                    if(hayCambios && !compruebaSiPedidoVacio(queRestaurante.codigo)) {
                        preguntaSiGuardar();
                    }else{

                        finish();
                    }

                }

            }
        });


        botonPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,botonPedido);

                if(cargaAyudaPedido()) {
                    ponAyuda();
                }else {

                    iraVerPedido(botonPedido);
                }

            }
        });

        viewPager = findViewById(R.id.pager_karta);
        tabLayout = findViewById(R.id.tab_layout);

        if(queRestaurante.tBordes!=0){

            tabLayout.setTabTextColors(Contenedor_Lakarta.queRestaurante.tBordes,Contenedor_Lakarta.queRestaurante.tBordes);

        }

        if(queRestaurante.fBordes!=0) {
            tabLayout.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fBordes);

        }

        serverRecibeDatos =new Server_RecibeDatos(this);

        inflador=(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        //titulo=findViewById(R.id.titulo_lakarta);
        //titulo.setText(queRestaurante.nombre);


        if(queRestaurante.actualizando!=0 || verDesdeAdmin.equals("si")) {

            CargaKarta poncarta = new CargaKarta();

            poncarta.execute();
        }else{

            ponActualizando();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        blurview.setVisibility(View.GONE);

        if(viewPager!=null && viewPager.getAdapter()!=null) {

            int actual=viewPager.getCurrentItem();

            viewPager.setAdapter(pagerAdapter);

            viewPager.setCurrentItem(actual);

            System.out.println("CAMBIA ADAPTADOR");
        }

    }

    @Override
    public void onBackPressed() {

        if(!portadaPuesta){

            moverPortada(portada,true);

        }else{

            if(hayCambios && !compruebaSiPedidoVacio(queRestaurante.codigo)) {
                preguntaSiGuardar();
            }else{

                super.onBackPressed();
            }

        }


        /*
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }

         */
    }

    private class CargaKarta extends AsyncTask<String,Integer,String> {  // carga en memoria la base de datos

        @Override
        protected String doInBackground(String... strings) {

            serverRecibeDatos.cargaTodosNiveles(queRestaurante.codigo);

            int contador=0;

            while(serverRecibeDatos.respuesta.equals("no") && contador<10) {

                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    System.out.println(e);
                }
                contador++;

            }

            laKartaNivel1= serverRecibeDatos.laKartaNivel1con2;
            laKartaNivel3= serverRecibeDatos.laKartaNivel3;

            return serverRecibeDatos.respuesta;

        }

        protected void onPostExecute(String respuesta){

            if(respuesta.equals("ok")) {

                barraProgreso.setVisibility(View.GONE);

                ponGastoTotal(queRestaurante.codigo);

                ponPortada();
                ponPager();
                verLosPlatos=new Ver_Platos(Contenedor_Lakarta.this, inflador);


            }else{

                barraProgreso.setVisibility(View.GONE);

                mensajeAlerta=getString(R.string.sin_datos);
                ponAlerta();
            }

        }

    }

    public void ponPortada(){

        for(int i=0;i<laKartaNivel1.length;i++) {

            final int donde=i;

            LinearLayout unidadPortada= (LinearLayout) inflador.inflate(R.layout.barra_categoria_portada, null);

            TextView nombreCat = unidadPortada.findViewById(R.id.nombre_categoria);
            TextView detalleCat = unidadPortada.findViewById(R.id.detalle_categoria);
            ImageView imagenCat = unidadPortada.findViewById(R.id.imagen_categoria);
            LinearLayout fondoDetalle=unidadPortada.findViewById(R.id.fondo_detalle);

            if(Contenedor_Lakarta.queRestaurante.cNP!=0) {
                //nombreCat.setTextColor(Contenedor_Lakarta.queRestaurante.cNP);
            }
            if(Contenedor_Lakarta.queRestaurante.cDP!=0) {
                detalleCat.setTextColor(Contenedor_Lakarta.queRestaurante.cDP);
            }
            if(Contenedor_Lakarta.queRestaurante.fKarta!=0) {
                //nombreCat.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fNP);
            }
            if(Contenedor_Lakarta.queRestaurante.fDP!=0) {
                fondoDetalle.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fDP);
            }

            if(laKartaNivel1[i].mostrar_imagen==0 || laKartaNivel1[i].imagen_nivel.equals("")) {

                imagenCat.setVisibility(View.GONE);

            }else{

                Glide.with(getApplicationContext())
                        .load(laKartaNivel1[i].imagen_nivel)
                        .error(R.drawable.no_photo)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imagenCat);
            }

            nombreCat.setText(laKartaNivel1[i].nombre_nivel);
            if (laKartaNivel1[i].detalle_nivel.equals("")) {

                detalleCat.setVisibility(View.GONE);
            } else {
                detalleCat.setText(laKartaNivel1[i].detalle_nivel);
            }

            unidadPortada.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    viewPager.setCurrentItem(donde, false);

                    moverPortada(portada, false);

                }
            });

            contenedorPortada.addView(unidadPortada);
        }

    }

    public void ponPager(){

        pagerAdapter = new ScreenSlidePagerAdapter(this);

        viewPager.setAdapter(pagerAdapter);

        viewPager.setOffscreenPageLimit(2);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(laKartaNivel1[position].nombre_nivel);
                    }
                }).attach();


    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {


        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }



        @Override
        public Fragment createFragment(int position) {

            Kartas[] niveles3karta;

            int contador=0;

            for(int a=0;a<laKartaNivel1[position].cod_subnivel.length;a++){

                for(int b=0;b<laKartaNivel3.length;b++) {

                    if (laKartaNivel1[position].cod_subnivel[a].equals(laKartaNivel3[b].cod_nivel_sup)) {

                        contador++;

                    }
                }

            }

            niveles3karta=new Kartas[contador];

            contador=0;

            for(int a=0;a<laKartaNivel1[position].cod_subnivel.length;a++){

                for(int b=0;b<laKartaNivel3.length;b++) {

                    if (laKartaNivel1[position].cod_subnivel[a].equals(laKartaNivel3[b].cod_nivel_sup)) {

                        niveles3karta[contador]=laKartaNivel3[b];
                        contador++;

                    }
                }

            }


            return new fragment_karta(laKartaNivel1[position],niveles3karta);
        }

        @Override
        public int getItemCount() {
            return laKartaNivel1.length;
        }


    }

    private void iraVerPedido(final View boton){

        SQLiteDatabase db = helper.getReadableDatabase();

        ventanaPedido = inflador.inflate(R.layout.pedido, null);

        contenedorPedidos=ventanaPedido.findViewById(R.id.contenedor_pedido);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final TextView eliminaPedido=ventanaPedido.findViewById(R.id.boton_borra_pedido);
        final TextView guardaPedido=ventanaPedido.findViewById(R.id.boton_guarda_pedido);
        final TextView salirPedido=ventanaPedido.findViewById(R.id.salir_pedido);
        TextView nombreRestaurante=ventanaPedido.findViewById(R.id.nombre_restaurante);
        ImageView logoPedido=ventanaPedido.findViewById(R.id.logo_empresa_pedido);

        nombreRestaurante.setText(getString(R.string.mi_pedido_en)+" "+queRestaurante.nombre);
        Glide.with(this)
                .load(queRestaurante.logo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.cambios_karta)
                .into(logoPedido);

        try {  // Ponemos un trycatch por si el registro buscado no existiera y no nos de error

            String selection = BBDDmiskartas.NOMBRE_COLUMNA7 + " = ? AND "+ BBDDmiskartas.NOMBRE_COLUMNA8+" = ?" ;
            String[] selectionArgs = {queRestaurante.codigo,"2"};  // metemos (convertido a String) el contenido de

            Cursor cursor = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    null,             // array con las columnas a devolver creado antes
                    selection,              // el criterio WHERE
                    selectionArgs,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );

            String selection_nivel4 = BBDDmiskartas.NOMBRE_COLUMNA8+" = ?" ;
            String[] selectionArgs_nivel4 = {"4"};  // metemos (convertido a String) el contenido de

            Cursor cursor_nivel4 = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    null,             // array con las columnas a devolver creado antes
                    selection_nivel4,              // el criterio WHERE
                    selectionArgs_nivel4,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );


            if (cursor.getCount() > 0) {

                ponContenidoPedido(cursor, cursor_nivel4, db);

                alert.setView(ventanaPedido);

                alert.setCancelable(true);

                alertaPedido = alert.create();

                alertaPedido.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        activaBoton(true,boton);
                        blurview.setVisibility(View.GONE);

                    }
                });

                eliminaPedido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        activaBoton(false,eliminaPedido);

                        eliminaTodoMenu(queRestaurante.codigo);
                        alertaPedido.cancel();
                        finish();
                        startActivity(getIntent());


                    }
                });

                guardaPedido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        activaBoton(false, guardaPedido);

                        hayCambios=false;
                        blurview.setVisibility(View.GONE);
                        activaBoton(true,boton);
                        alertaPedido.cancel();

                    }
                });

                salirPedido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        activaBoton(false, salirPedido);

                        blurview.setVisibility(View.GONE);
                        activaBoton(true,boton);
                        alertaPedido.cancel();


                    }
                });

                alertaPedido.requestWindowFeature(Window.FEATURE_NO_TITLE);

                alertaPedido.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                alertaPedido.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {

                        blurview.setVisibility(View.VISIBLE);

                    }
                });

                alertaPedido.show();

            }else{

                mensajeAlerta=getResources().getString(R.string.pedido_vacio);
                activaBoton(true,boton);
                ponAlerta();

            }

        }catch (Exception e){

            System.out.println("ERROR EN PEDIDO "+e.getMessage());
            mensajeAlerta=getResources().getString(R.string.pedido_vacio);
            activaBoton(true,boton);
            ponAlerta();
        }

    }

    private void ponContenidoPedido(Cursor cursor, final Cursor cursor_nivel4, SQLiteDatabase db){

        int cantidad_total=0;
        double total_pedido=0;

        if(ventanaPedido!=null && contenedorPedidos!=null) {

            contenedorPedidos.removeAllViews();

            final TextView cantidadTotalPedido = ventanaPedido.findViewById(R.id.cantidad_total);
            final TextView precioTotalPedido = ventanaPedido.findViewById(R.id.total_pedido);

            for (int i = 0; i < cursor.getCount(); i++) { // ------- PONE PLATOS --------------

                cursor.moveToPosition(i);

                double total;

                final LinearLayout contenidoPedido = (LinearLayout) inflador.inflate(R.layout.unidad_pedido, null);
                LinearLayout contenedorPedidoN4 = contenidoPedido.findViewById(R.id.contenedor_pedidos_nivel4);

                TextView nombrePlato = contenidoPedido.findViewById(R.id.pedido_nombre);
                TextView precioPlato = contenidoPedido.findViewById(R.id.pedido_precio);
                final TextView cantidadPlato = contenidoPedido.findViewById(R.id.pedido_cantidad);
                final TextView precioTotalPlato = contenidoPedido.findViewById(R.id.pedido_total_plato);

                total = Double.parseDouble(cursor.getString(5)) * Integer.parseInt(cursor.getString(2));

                String precio = formato.format(Double.parseDouble(cursor.getString(5))) + "€";
                String eltotal = formato.format(total) + "€";

                final String codigoGuardado = cursor.getString(1);

                nombrePlato.setText(cursor.getString(3));

                precioPlato.setText(precio);
                cantidadPlato.setText(cursor.getString(2));
                precioTotalPlato.setText(eltotal);

                contenedorPedidos.addView(contenidoPedido, i);

                nombrePlato.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        System.out.println("MIRA PLATO");

                        for (int i = 0; i < laKartaNivel1.length; i++) {

                            for (int e = 0; e < laKartaNivel1[i].cod_subnivel.length; e++) {

                                if (codigoGuardado.equals(laKartaNivel1[i].cod_subnivel[e])) {

                                    //esPlato(laKartaNivel1[i], e, false, contenidoPedido, cantidadPlato);
                                    verLosPlatos.esPlato(laKartaNivel1[i], e, null, contenidoPedido, false,"");
                                }
                            }

                        }


                    }
                });

                cantidad_total = cantidad_total + Integer.parseInt(cursor.getString(2));
                total_pedido = total_pedido + total;

                if (cursor.getString(8) != null && cursor.getString(8).equals("tiene")) { // ------- SI PLATO ES MENU -----------

                    int contador = 0;

                    for (int p = 0; p < cursor_nivel4.getCount(); p++) {

                        cursor_nivel4.moveToPosition(p);

                        if (cursor_nivel4.getString(8).equals(cursor.getString(1))) { // -------- PONE PLATOS DE MENU -------------

                            LinearLayout contenidoPedido_nivel4 = (LinearLayout) inflador.inflate(R.layout.unidad_pedido_menu, null);

                            TextView nombrePlato_menu = contenidoPedido_nivel4.findViewById(R.id.pedido_plato_menu);
                            TextView suplementoPlato_menu = contenidoPedido_nivel4.findViewById(R.id.pedido_suplemento_plato);
                            TextView totalPlato_menu = contenidoPedido_nivel4.findViewById(R.id.pedido_total_plato_menu);

                            nombrePlato_menu.setText(cursor_nivel4.getString(3) + " x " + cursor_nivel4.getString(2));
                            if(!cursor_nivel4.getString(5).equals("0.0")) {
                                suplementoPlato_menu.setText(formato.format(Double.parseDouble(cursor_nivel4.getString(5))) + "€");
                                totalPlato_menu.setText(formato.format(Double.valueOf(cursor_nivel4.getString(5))*Integer.parseInt(cursor_nivel4.getString(2)))+"€");

                                System.out.println("TOTAL ANTES "+total_pedido);
                                total = Double.parseDouble(cursor_nivel4.getString(5)) * Integer.parseInt(cursor_nivel4.getString(2));
                                total_pedido = total_pedido + total;

                                System.out.println("TOTAL DESPUES "+total_pedido);
                            }else{
                                suplementoPlato_menu.setText("");
                                totalPlato_menu.setText("");
                            }

                            final String codigoNivel3 = cursor.getString(1);
                            contenedorPedidoN4.addView(contenidoPedido_nivel4, contador);
                            contador++;

                            nombrePlato.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();

                                    for (int i = 0; i < laKartaNivel1.length; i++) {

                                        for (int e = 0; e < laKartaNivel1[i].cod_subnivel.length; e++) {

                                            if (codigoNivel3.equals(laKartaNivel1[i].cod_subnivel[e])) {

                                                //esMenu(laKartaNivel1[i], laKartaNivel3, cursor_nivel4.getInt(2),e,contenidoPedido);
                                                verLosPlatos.esMenu(laKartaNivel1[i], laKartaNivel3,e,null,contenidoPedido);

                                                System.out.println("COINCIDE: "+codigoNivel3);

                                            }
                                        }

                                    }

                                }
                            });

                        }

                    }

                }

            }

            db.close();

            String textoPrecio=formato.format(total_pedido) + "€";

            precioTotalPedido.setText(textoPrecio);
            cantidadTotalPedido.setText(String.valueOf(cantidad_total));
        }

    }

    private void preguntaSiGuardar(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        final Button guardar=alertLayout.findViewById(R.id.pedido_guardar);
        final Button noGuardar=alertLayout.findViewById(R.id.pedido_noguardar);

        guardar.setText(getResources().getString(R.string.guardar_pedido));
        noGuardar.setText(getResources().getString(R.string.noguardar_pedido));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true, salirKarta);

            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, guardar);

                dialog.cancel();
                finish();

            }
        });

        noGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false,noGuardar);

                eliminaTodoMenu(queRestaurante.codigo);

                dialog.cancel();
                finish();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    private void eliminaTodoMenu(String cod_restaurante){

        SQLiteDatabase db = helper.getWritableDatabase();

        String selection = BBDDmiskartas.NOMBRE_COLUMNA7 + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {cod_restaurante};
        // Issue SQL statement.
        db.delete(BBDDmiskartas.TABLE_NAME, selection, selectionArgs);

        //System.out.println("REGISTRO BORRADO");

        db.close();

    }

    public void ponGastoTotal(String cod_restaurante){

        SQLiteDatabase db = helper.getReadableDatabase();

        double total=0;

        try {  // Ponemos un trycatch por si el registro buscado no existiera y no nos de error

            String selection = BBDDmiskartas.NOMBRE_COLUMNA7 + " = ?";
            String[] selectionArgs = {cod_restaurante};  // metemos (convertido a String) el contenido de

            Cursor cursor = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    null,             // array con las columnas a devolver creado antes
                    selection,              // el criterio WHERE
                    selectionArgs,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );

            if (cursor.getCount() > 0) {

                for (int i = 0;i<cursor.getCount(); i++) {

                    cursor.moveToPosition(i);

                    System.out.println("DATOS PEDIDO "+cursor.getString(3));

                    total=total+(Double.parseDouble(cursor.getString(5))*Integer.parseInt(cursor.getString(2)));

                }

                botonPedido.setAlpha(1f);
                mover(botonPedido,false, true);
                System.out.println("PONE BOTON PEDIDO");

                if(total>0) {
                    contador_total.setText("Total: " + formato.format(total) + "€");
                    contador_total.setVisibility(View.VISIBLE);
                }else{

                    contador_total.setVisibility(View.GONE);
                }


            }else{

                System.out.println("QUITA BOTON PEDIDO");

                mover(botonPedido,false, false);

            }


        }catch (Exception e){


        }

        db.close();

    }

    public void moverPortada(final View vista, final Boolean activar){


        float alturaBarra;

        alturaBarra= vista.getHeight();

        System.out.println("TAMAÑO BARRA: "+alturaBarra);

        ObjectAnimator move;

        if(activar){

            move = ObjectAnimator.ofFloat(vista, "translationY", alturaBarra, 0f);

            System.out.println("PONE PORTADA: "+vista.getY());

            portadaPuesta=true;


        }else{

            move = ObjectAnimator.ofFloat(vista, "translationY", 0f,alturaBarra);

            System.out.println("QUITA PORTADA: "+vista.getY());

            portadaPuesta=false;
        }

        move.setDuration(300);
        move.start();

    }

    public void mover(final View vista,Boolean arriba,final Boolean donde){

        final float alturaBarra;
        if(arriba){

            alturaBarra= vista.getHeight();

        }else{

            alturaBarra= vista.getWidth();

            System.out.println("TAMAÑO BARRA: "+alturaBarra);
        }

        ObjectAnimator move;

        if(donde){


            vista.setAlpha(1f);
            vista.setX(-alturaBarra);
            move = ObjectAnimator.ofFloat(vista, "translationX", -alturaBarra, 0f);


        }else{

            move = ObjectAnimator.ofFloat(vista, "translationX", 0f,-alturaBarra);
        }

        move.setDuration(300);
        move.start();


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

    private void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);

        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

    public boolean compruebaSiPedidoVacio(String cod_rest){

        SQLiteDatabase db = helper.getReadableDatabase(); // Hace que la BBDD sea de lectura

        String[] projection = {  // dice qué columnas nos debe devolver la consulta
                // (no ponemos la primera porque es la que usaremos para buscar)
                //MiBaseDatos.NOMBRE_COLUMNA2,
                //MiBaseDatos.NOMBRE_COLUMNA3
        };

        // El valor que queremos buscar con WHERE
        String selection = BBDDmiskartas.NOMBRE_COLUMNA7 + " = ?";
        String[] selectionArgs = {cod_rest};  // metemos (convertido a String) el contenido de

        try {  // Ponemos un trycatch por si el registro buscado no existiera y no nos de error

            Cursor cursor = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    projection,             // array con las columnas a devolver creado antes
                    selection,              // el criterio WHERE
                    selectionArgs,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );

            cursor.moveToFirst();

            if(cursor.getCount()>0){

                db.close();

                return false;

            }else{

                db.close();

                return true;
            }

        }catch (Exception e){

            db.close();
            return false;

        }

    }

    public void compruebaCambiosPedido(){

        System.out.println("CAMBIA PEDIDO");

        SQLiteDatabase db = helper.getReadableDatabase();

        try {  // Ponemos un trycatch por si el registro buscado no existiera y no nos de error

            String selection = BBDDmiskartas.NOMBRE_COLUMNA7 + " = ? AND " + BBDDmiskartas.NOMBRE_COLUMNA8 + " = ?";
            String[] selectionArgs = {queRestaurante.codigo, "2"};  // metemos (convertido a String) el contenido de

            Cursor cursor = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    null,             // array con las columnas a devolver creado antes
                    selection,              // el criterio WHERE
                    selectionArgs,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );

            String selection_nivel4 = BBDDmiskartas.NOMBRE_COLUMNA8 + " = ?";
            String[] selectionArgs_nivel4 = {"4"};  // metemos (convertido a String) el contenido de

            Cursor cursor_nivel4 = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    null,             // array con las columnas a devolver creado antes
                    selection_nivel4,              // el criterio WHERE
                    selectionArgs_nivel4,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );


            if (cursor.getCount() > 0) {

                ponContenidoPedido(cursor, cursor_nivel4, db);

            }else{

                if(alertaPedido!=null && alertaPedido.isShowing()){

                    alertaPedido.cancel();
                    mensajeAlerta=getResources().getString(R.string.pedido_vacio);
                    ponAlerta();
                    mover(botonPedido,false, false);
                    botonPedido.setAlpha(0f);
                }

                db.close();

            }
        }catch (Exception e){


        }
    }

    private void ponAyuda(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_info_una_vez, null);

        Button aceptar=alertLayout.findViewById(R.id.aceptar);
        TextView textoAyuda=alertLayout.findViewById(R.id.texto_ayuda);
        final CheckBox noVermas=alertLayout.findViewById(R.id.checkBox);

        textoAyuda.setText(R.string.ayuda_pedido);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(noVermas.isChecked()){

                    quitaAyudaPedido();
                }

                iraVerPedido(botonPedido);

                dialog.cancel();


            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void ponActualizando(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_info_una_vez, null);

        ImageView logo=alertLayout.findViewById(R.id.icono_alerta);
        Button aceptar=alertLayout.findViewById(R.id.aceptar);
        TextView textoAyuda=alertLayout.findViewById(R.id.texto_ayuda);
        final CheckBox noVermas=alertLayout.findViewById(R.id.checkBox);

        String texto=queRestaurante.nombre+"\n\n\n"+getResources().getString(R.string.msg_actualizando_karta);

        textoAyuda.setText(texto);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(false);
        noVermas.setVisibility(View.GONE);

        Glide.with(getApplicationContext())
                .load(queRestaurante.logo)
                .error(R.drawable.no_photo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(logo);


        final AlertDialog dialog = alert.create();

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dialog.cancel();

                finish();


            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private boolean cargaAyudaPedido(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        String valor=guarda.getString("AYUDA_PEDIDO","si");

        return valor.equals("si");

    }

    public void quitaAyudaPedido() {

        SharedPreferences guarda = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor mieditor = guarda.edit();

        mieditor.putString("AYUDA_PEDIDO", "no");

        mieditor.apply();
    }


    // ----------- DESCARTAR ----------------------

    private void esPlato(final Kartas estaKarta, final int cual, boolean alerta, final View boton, final TextView cuantosPlato){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);

        final View alertLayout = inflador.inflate(R.layout.emerg_cuantos_platos, null);

        TextView nombrePlato=alertLayout.findViewById(R.id.emerg_nombre_plato);
        final TextView detallePlato=alertLayout.findViewById(R.id.emerg_detalle_plato);
        TextView precioPlato=alertLayout.findViewById(R.id.emerg_precio_plato);
        final TextView cantidadPlatos=alertLayout.findViewById(R.id.emerg_cantidad);
        final TextView alertaAlergeno=alertLayout.findViewById(R.id.alerta_alergeno);
        final TextView precioTotal=alertLayout.findViewById(R.id.emerg_precio_total);
        final ImageView imagenPlato=alertLayout.findViewById(R.id.emerg_imagen_plato);
        final ImageView eliminaPlato=alertLayout.findViewById(R.id.elimina_plato);
        ImageView masPlato=alertLayout.findViewById(R.id.plato_mas);
        ImageView menosPlato=alertLayout.findViewById(R.id.plato_menos);
        final TextView aceptaPlato=alertLayout.findViewById(R.id.acepta_plato);
        final TextView cancelaPlato=alertLayout.findViewById(R.id.cancela_plato);
        final ConstraintLayout foto=alertLayout.findViewById(R.id.contenedor_foto_plato);
        final GridLayout contenedorAlergenos=alertLayout.findViewById(R.id.contenedor_alergenos);
        final ImageView verMas=alertLayout.findViewById(R.id.ver_mas);
        final LinearLayout contenedorDetalle=alertLayout.findViewById(R.id.contenedor_detalle);
        final LinearLayout fichaNombre=alertLayout.findViewById(R.id.ficha_nombre);
        final LinearLayout vacio=alertLayout.findViewById(R.id.vacio);

        final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) contenedorDetalle.getLayoutParams();

        pon=0;

        cambiosEnplato=false;

        if(alerta){

            //String ponAlergias=getResources().getString(R.string.eres_alergico)+queAlergenos;

            alertaAlergeno.setVisibility(View.VISIBLE);
            //alertaAlergeno.setText(ponAlergias);
        }else{

            alertaAlergeno.setVisibility(View.GONE);
        }

        if(queRestaurante.fN!=0){
/*
            int color=Contenedor_Lakarta.queRestaurante.fN;

            if(!queplato.imagen_subnivel[cual].equals("null") && queplato.mostrar_imagen_subnivel[cual]==1) {

                color = (color & 0x00FFFFFF) | 0x99000000;
            }

 */

            fichaNombre.setBackgroundColor(queRestaurante.fN);
        }

        if(queRestaurante.cN!=0){

            nombrePlato.setTextColor(queRestaurante.cN);
        }

        if(queRestaurante.cP!=0){

            precioPlato.setTextColor(queRestaurante.cP);
        }
        if(queRestaurante.fD!=0){

            int color=queRestaurante.fD;
/*
            if(!queplato.imagen_subnivel[cual].equals("") && queplato.mostrar_imagen_subnivel[cual]==1) {

                color = (color & 0x00FFFFFF) | 0x99000000;
            }


 */

            contenedorDetalle.setBackgroundColor(color);
        }

        if(queRestaurante.cD!=0){

            detallePlato.setTextColor(queRestaurante.cD);
        }



        //final double precio = Double.parseDouble(queplato.precio_subnivel[cual]);

        final double precio;


        precio = estaKarta.precio_subnivel[cual];
        cantidad=Integer.parseInt(cuantosPlato.getText().toString());


        cantidadPlatos.setText(String.valueOf(cantidad));

        if(cantidad>0 && precio>0){

            precioFinal=precio*cantidad;
            if(precioFinal==0){
                precioTotal.setText("");
            }else {
                precioTotal.setText(formato.format(precioFinal) + "€");
            }

        }else{

            precioTotal.setText("");

            precioFinal=0;
        }

        nombrePlato.setText(estaKarta.nombre_subnivel[cual]);

        if(estaKarta.detalle_subnivel[cual].equals("")){

            detallePlato.setVisibility(View.GONE);
        }else {
            detallePlato.setText(estaKarta.detalle_subnivel[cual]);
        }
        detallePlato.setMovementMethod(new ScrollingMovementMethod());
        if(precio==0){
            precioPlato.setVisibility(View.GONE);
        }else {
            precioPlato.setText(formato.format(precio) + "€");
        }

        masPlato.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                cambiosEnplato=true;

                if(precio>0) {

                    precioFinal = ((double) Math.round(precioFinal * 100d) / 100d) + precio;

                    precioTotal.setText(formato.format(precioFinal) + "€");
                }

                if(cantidad==0 && precio>0) {

                    float alto=foto.getHeight();
                    float alturaPrecio=precioTotal.getHeight();


                    /*
                    precioTotal.setY(alto);
                    precioTotal.animate().yBy(-alturaPrecio).setDuration(100);

                     */

                }

                cantidad=cantidad+1;
                cantidadPlatos.setText(String.valueOf(cantidad));
                cantidadPlatos.setAlpha(0.2f);
                cantidadPlatos.setScaleX(0.8f);
                cantidadPlatos.setScaleY(0.8f);
                cantidadPlatos.animate().scaleX(1f);
                cantidadPlatos.animate().scaleY(1f);
                cantidadPlatos.animate().alpha(1f);

            }
        });

        menosPlato.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                cambiosEnplato=true;

                if(precio>0){

                    if(precioFinal>0){

                        precioFinal=((double)Math.round(precioFinal * 100d) / 100d)-precio;

                    }

                    precioTotal.setText(formato.format(precioFinal)+"€");

                }else{

                    precioTotal.setText("");
                }

                if(cantidad>0){

                    cantidad=cantidad-1;
                    cantidadPlatos.setText(String.valueOf(cantidad));
                    cantidadPlatos.setAlpha(0.2f);
                    cantidadPlatos.animate().alpha(1f);
                    cantidadPlatos.setScaleX(1.2f);
                    cantidadPlatos.setScaleY(1.2f);
                    cantidadPlatos.animate().scaleX(1f);
                    cantidadPlatos.animate().scaleY(1f);


                    /*
                    float alto=foto.getHeight();
                    float alturaPrecio=precioTotal.getHeight();
                    precioTotal.setY(alto-alturaPrecio);
                    precioTotal.animate().yBy(alturaPrecio).setDuration(100);

                     */

                }

                if(cantidad==0){

                    precioTotal.setText("");

                }

            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {


                activaBoton(true,boton);

            }
        });

        aceptaPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("NOTIFICA CAMBIOS");

                activaBoton(false, aceptaPlato);

                if(cambiosEnplato) {

                    int actual = viewPager.getCurrentItem();

                    viewPager.setAdapter(pagerAdapter);

                    viewPager.setCurrentItem(actual);

                    if(cantidad>0) {

                        guardaPlato(estaKarta.cod_subnivel[cual],cantidad, estaKarta.nombre_subnivel[cual], estaKarta.detalle_subnivel[cual],String.valueOf(precio), queRestaurante.codigo,"2",null);
                        hayCambios=true;

                    }else{
                        quitaPlato(estaKarta.cod_subnivel[cual]);

                    }
                    ponGastoTotal(queRestaurante.codigo);



                }

                if(cambiosEnplato) {

                    int actual=viewPager.getCurrentItem();

                    viewPager.setAdapter(pagerAdapter);

                    viewPager.setCurrentItem(actual);

                    compruebaCambiosPedido();

                }

                activaBoton(true,boton);

                dialog.cancel();


            }
        });

        cancelaPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, cancelaPlato);

                activaBoton(true,boton);
                dialog.cancel();

            }
        });

        eliminaPlato.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                activaBoton(false,eliminaPlato);

                activaBoton(true,boton);

                quitaPlato(estaKarta.cod_subnivel[cual]);
                ponGastoTotal(queRestaurante.codigo);

                int actual=viewPager.getCurrentItem();

                viewPager.setAdapter(pagerAdapter);

                viewPager.setCurrentItem(actual);

                compruebaCambiosPedido();

                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                if(!estaKarta.imagen_subnivel[cual].equals("") && estaKarta.mostrar_imagen_subnivel[cual]==1) {

                    Glide.with(getApplicationContext())
                            .load(estaKarta.imagen_subnivel[cual])
                            .error(R.drawable.no_photo)
                            .into(imagenPlato);

                }else{

                    imagenPlato.setVisibility(View.GONE);
                    vacio.setVisibility(View.GONE);

                    detallePlato.setMaxLines(10);
                    detallePlato.setMinLines(4);
                    verMas.setVisibility(View.GONE);
                }

                if(!estaKarta.alergenos_subnivel[cual].equals("")){

                    ponAlergenos(contenedorAlergenos, estaKarta.alergenos_subnivel[cual]);
                }


                if(detallePlato.getLineCount()>3) {

                    verMas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            if (detallePlato.getMaxLines() == 3 && pon == 0) {

                                params.height = 0;

                                contenedorDetalle.setLayoutParams(params);

                                pon = 1;

                                detallePlato.setMaxLines(20);

                                verMas.setRotation(180f);

                                detallePlato.scrollTo(0,0);

                                //constraintSet.connect(R.id.emerg_detalle_plato, ConstraintSet.TOP, R.id.ficha_nombre, ConstraintSet.BOTTOM, 0);
                                //constraintSet.applyTo(foto);

                            } else {


                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                                contenedorDetalle.setLayoutParams(params);

                                pon = 0;

                                detallePlato.setMaxLines(3);

                                verMas.setRotation(180f);

                            }

                        }
                    });
                }else{

                    verMas.setVisibility(View.GONE);
                }
            }
        });

        dialog.show();


    }

    private void esMenu(final Kartas esteMenu, final Kartas[] elMenu, int cantidad, final int cual, final View boton){

        final View alertLayout = inflador.inflate(R.layout.emerg_contenedor_menu, null);

        final LinearLayout rutacontenedorMenu=alertLayout.findViewById(R.id.contenedor_menus);
        final ScrollView menusScroll=alertLayout.findViewById(R.id.contenedormenus_scroll);

        TextView nombreCriterio=alertLayout.findViewById(R.id.nombre_menu);
        TextView detalleCriterio=alertLayout.findViewById(R.id.detalle_menu);
        TextView precioMenu=alertLayout.findViewById(R.id.precio_menu);
        final TextView totalMenu=alertLayout.findViewById(R.id.total_menu);
        final TextView cantidadMenu=alertLayout.findViewById(R.id.cantidad_menu);
        TextView eliminaMenu=alertLayout.findViewById(R.id.elimina_menu);
        final TextView ayuda=alertLayout.findViewById(R.id.ayuda_plato);
        final TextView masMenu=alertLayout.findViewById(R.id.menu_mas);
        TextView menosMenu=alertLayout.findViewById(R.id.menu_menos);
        final TextView aceptaMenu=alertLayout.findViewById(R.id.acepta_menu);

        nombreCriterio.setText(esteMenu.nombre_subnivel[cual]);
        detalleCriterio.setText(esteMenu.detalle_subnivel[cual]);

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                return false;
            }
        });

        cantidadMenu.setText(String.valueOf(cantidad));

        cantidadMenusPedido=cantidad;

        cambiosEnMenu=false;

        final double precio;

        if(esteMenu.precio_subnivel[cual]==0){
            if(esteMenu.precio_nivel!=0){

                precio = esteMenu.precio_nivel;

            }else{

                precio = 0;
            }
        }else{

            precio = esteMenu.precio_subnivel[cual];
        }

        if(cantidad>0){

            precioFinal=precio*cantidad;
            totalMenu.setText(getResources().getString(R.string.total)+" "+formato.format(precioFinal)+"€");

        }else{

            totalMenu.setVisibility(View.GONE);

            precioFinal=0;
        }

        precioMenu.setText(formato.format(precio)+"€");

        final int[] elegidoTotalEste=new int[elMenu.length];
        final int[] totalAelegir=new int[elMenu.length];

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        scrollMenuPedido=new int[elMenu.length];

        for (int i = 0; i < elMenu.length; i++) {  // INFLA NIVEL 3

            LinearLayout rutaOpciones = (LinearLayout) inflador.inflate(R.layout.unidad_cat_menu, null);

            final LinearLayout contendorElementos = rutaOpciones.findViewById(R.id.contenedor_elementos);

            TextView nombreCriteroOp = rutaOpciones.findViewById(R.id.nombre_criterio);
            TextView detalleCriterioOp = rutaOpciones.findViewById(R.id.detalle_criterio);
            ImageView imagenCriterio = rutaOpciones.findViewById(R.id.imagen_nivel);

            nombreCriteroOp.setText(elMenu[i].nombre_nivel);
            if (!elMenu[i].detalle_nivel.equals("")) {
                detalleCriterioOp.setText(elMenu[i].detalle_nivel);
            } else {
                detalleCriterioOp.setVisibility(View.GONE);
            }

            if(elMenu[i].imagen_nivel.equals("") || elMenu[i].mostrar_imagen==0){

                imagenCriterio.setVisibility(View.GONE);

            }else{

                Glide.with(this)
                        .load(elMenu[i].imagen_nivel)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.noimage)
                        .into(imagenCriterio);
            }

            totalAelegir[i] = elMenu[i].cantidad_nivel;

            final int este = i;

            for (int e = 0; e < elMenu[i].nombre_subnivel.length; e++) {  // INFLA NIVEL 4

                final int estePlato = e;
                final LinearLayout platoNivel4 = (LinearLayout) inflador.inflate(R.layout.unidad_plato_menu, null);
                TextView nombrePlato_nivel4 = platoNivel4.findViewById(R.id.nombrePlato_nivel4);
                TextView detallePlato_nivel4 = platoNivel4.findViewById(R.id.detallePlato_nivel4);
                final TextView cantidadPlato_nivel4 = platoNivel4.findViewById(R.id.cantidadPlato_nivel4);
                TextView noDisponible = platoNivel4.findViewById(R.id.nodisponible);
                final ImageView quitaPlato = platoNivel4.findViewById(R.id.quita_plato);
                ImageView alertaAlergeno = platoNivel4.findViewById(R.id.alerta_alergeno);
                LinearLayout contenedorAlergenos=platoNivel4.findViewById(R.id.contenedor_alergenos);

                CardView contenedorImagenPlato=platoNivel4.findViewById(R.id.contenedor_plato);
                final ImageView imagenPlato=platoNivel4.findViewById(R.id.imagen_plato);

                noDisponible.setVisibility(View.GONE);
                quitaPlato.setVisibility(View.GONE);

                nombrePlato_nivel4.setText(elMenu[este].nombre_subnivel[estePlato]);

                alertaAlergeno.setVisibility(View.GONE);

                // -------- ALERGENOS -----------


                if (!elMenu[i].alergenos_subnivel[e].equals("")) {

                    for (int a = 0; a < elMenu[i].alergenos_subnivel[e].length(); a++) {

                        for (int b = 0; b < Contenedor_Lakarta.alergenos.length; b++) {

                            if (elMenu[i].alergenos_subnivel[e].charAt(a) == alergenos[b].codigo_alergeno.charAt(0)) {
/*
                                if(misAlergenos.contains(String.valueOf(elMenu[i].alergenos_subnivel[e].charAt(a)))){

                                    alertaAlergeno.setVisibility(View.VISIBLE);

                                }
*/

                                View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad, null);

                                ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.alergeno);

                                Glide.with(this)
                                        .load(alergenos[b].imagen_alergeno)
                                        .error(R.drawable.no_photo)
                                        .into(imagenAlergeno);

                                contenedorAlergenos.addView(unidadAlergeno);
                                break;

                            }

                        }

                    }
                }

                if(elMenu[este].imagen_subnivel[estePlato].equals("") || elMenu[este].mostrar_imagen_subnivel[estePlato]==0){

                    contenedorImagenPlato.setVisibility(View.GONE);

                }else{

                    Glide.with(this)
                            .load(elMenu[este].imagen_subnivel[estePlato])
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.noimage)
                            .into(imagenPlato);
                }

                if (!elMenu[i].detalle_subnivel[e].equals("")) {
                    detallePlato_nivel4.setText(elMenu[i].detalle_subnivel[e]);
                } else {
                    detallePlato_nivel4.setVisibility(View.GONE);
                }
                //cantidadPlato_nivel4.setText("");
                cantidadPlato_nivel4.setVisibility(View.GONE);

                if (elMenu[este].visible[estePlato] == 1) {

                    // pon platos guardados

                    int cuantos = compruebaPlatoGuardado(elMenu[i].cod_subnivel[estePlato]);

                    if (cuantos > 0) {

                        elegidoTotalEste[este] = elegidoTotalEste[este] + cuantos;

                        cantidadPlato_nivel4.setText("" + cuantos);
                        cantidadPlato_nivel4.setVisibility(View.VISIBLE);
                        quitaPlato.setVisibility(View.VISIBLE);

                    }

                    //--------------------------

                    quitaPlato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            cantidadNivel4 = Integer.parseInt(cantidadPlato_nivel4.getText().toString());

                            if (cantidadNivel4 > 0) {

                                elegidoTotalEste[este]--;
                                cantidadNivel4--;
                                TextView cambia = rutacontenedorMenu.getChildAt(este).findViewById(R.id.cantidad_criterio);
                                cambia.setText("(" + getResources().getString(R.string.elige) + " " + ((totalAelegir[este] * cantidadMenusPedido) - elegidoTotalEste[este]) + " más)");

                                cantidadPlato_nivel4.setText("" + cantidadNivel4);

                                if (cantidadNivel4 == 0) {

                                    quitaPlato(elMenu[este].cod_subnivel[estePlato]);
                                    cantidadPlato_nivel4.setVisibility(View.GONE);
                                    cantidadPlato_nivel4.setText("");
                                    quitaPlato.setVisibility(View.GONE);

                                } else {

                                    guardaPlato(elMenu[este].cod_subnivel[estePlato], cantidadNivel4, elMenu[este].nombre_subnivel[estePlato], elMenu[este].detalle_subnivel[estePlato], String.valueOf(elMenu[este].precio_subnivel[estePlato]), esteMenu.cod_restaurante, "4", elMenu[este].cod_nivel_sup);

                                    cambiosEnMenu=true;
                                }

                            }

                        }
                    });

                    platoNivel4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ayuda.setVisibility(View.GONE);

                            System.out.println("PULSA1");

                            if (cantidadMenusPedido == 0) {

                                ayuda.setVisibility(View.VISIBLE);
                                ayuda.setText(getString(R.string.elige_cuantos_menus));
                                ayuda.setScaleX(1.2f);
                                //ayuda.setScaleY(1.2f);
                                ayuda.animate().scaleX(1f);

                            } else {

                                System.out.println("PULSA2");

                                if (elegidoTotalEste[este] < (totalAelegir[este] * cantidadMenusPedido)) {

                                    System.out.println("PRECIO PLATO MENU"+elMenu[este].precio_subnivel[estePlato]);

                                    if(elMenu[este].precio_subnivel[estePlato]>0){


                                        precioFinal=precioFinal+elMenu[este].precio_subnivel[estePlato];

                                        totalMenu.setText(getResources().getString(R.string.total)+" "+formato.format(precioFinal)+"€");
                                    }

                                    if (cantidadPlato_nivel4.getText().toString().equals("")) {

                                        cantidadNivel4 = 0;
                                        quitaPlato.setVisibility(View.VISIBLE);

                                    } else {

                                        cantidadNivel4 = Integer.parseInt(cantidadPlato_nivel4.getText().toString());

                                    }

                                    cantidadNivel4++;
                                    elegidoTotalEste[este]++;
                                    TextView cambia = rutacontenedorMenu.getChildAt(este).findViewById(R.id.cantidad_criterio);
                                    cambia.setText("(" + getResources().getString(R.string.elige) + " " + ((totalAelegir[este] * cantidadMenusPedido) - elegidoTotalEste[este]) + " más)");
                                    if ((totalAelegir[este] * cantidadMenusPedido) - elegidoTotalEste[este] > 0) {

                                        cambia.setText("(" + getResources().getString(R.string.elige) + " " + ((cantidadMenusPedido * elMenu[este].cantidad_nivel) - elegidoTotalEste[este]) + " más)");
                                        //cambia.setTextColor(getResources().getColor(R.color.colorRosa3, getActivity().getTheme()));

                                    } else {

                                        cambia.setText("");

                                    }
                                    cantidadPlato_nivel4.setVisibility(View.VISIBLE);
                                    cantidadPlato_nivel4.setText("" + cantidadNivel4);

                                    guardaPlato(elMenu[este].cod_subnivel[estePlato], cantidadNivel4, elMenu[este].nombre_subnivel[estePlato], elMenu[este].detalle_subnivel[estePlato], String.valueOf(elMenu[este].precio_subnivel[estePlato]), esteMenu.cod_restaurante, "4", elMenu[este].cod_nivel_sup);

                                    cambiosEnMenu=true;
                                } else {

                                    ayuda.setVisibility(View.VISIBLE);
                                    ayuda.setText("Quita platos para añadir uno nuevo");
                                    ayuda.setScaleX(1.2f);
                                    //ayuda.setScaleY(1.2f);
                                    ayuda.animate().scaleX(1f);


                                }
                            }

                        }
                    });

                } else {

                    noDisponible.setText(getString(R.string.no_disponible));
                    noDisponible.setVisibility(View.VISIBLE);

                }

                contendorElementos.addView(platoNivel4);

            }

            rutacontenedorMenu.addView(rutaOpciones);

        }


        // PONE LA CANTIDAD DE MENUS ELEGIDOS -----------------

        for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
            TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
            TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
            criterio.setText(elMenu[i].nombre_nivel);

            if ((totalAelegir[i] * cantidad) - elegidoTotalEste[i] > 0) {

                cantidadCriterio.setText("(" + getResources().getString(R.string.elige) + " " + ((cantidad * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

            } else {

                cantidadCriterio.setText("");

            }

        }

        // -------------------------------------------

        aceptaMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(true,boton);

                int elegido = 0;
                int total = 0;

                for (int i = 0; i < elegidoTotalEste.length; i++) {

                    elegido = elegido + (elegidoTotalEste[i]);
                    total = total + (totalAelegir[i] * cantidadMenusPedido);
                }

                if (elegido < total) {

                    ayuda.setVisibility(View.VISIBLE);
                    ayuda.setText(getResources().getString(R.string.faltan_platos));
                    ayuda.setScaleX(1.2f);
                    //ayuda.setScaleY(1.2f);
                    ayuda.animate().scaleX(1f);

                    scrollMenuPedido[0]=0;

                    for(int i=0;i<rutacontenedorMenu.getChildCount();i++){

                        View child = rutacontenedorMenu.getChildAt(i);

                        TextView cuantos=child.findViewById(R.id.cantidad_criterio);

                        if(i>0) {
                            scrollMenuPedido[i] = child.getHeight() + scrollMenuPedido[i - 1];
                        }

                        if(!cuantos.getText().equals("")){

                            menusScroll.scrollTo(0,scrollMenuPedido[i]);
                            break;
                        }

                    }

                } else {

                    if (cantidadMenusPedido > 0) {

                        guardaPlato(esteMenu.cod_subnivel[cual], cantidadMenusPedido, esteMenu.nombre_subnivel[cual], esteMenu.detalle_subnivel[cual], String.valueOf(esteMenu.precio_subnivel[cual]), esteMenu.cod_restaurante, "2", "tiene");
                        ponGastoTotal(esteMenu.cod_restaurante);
                        hayCambios = true;


                    } else {
                        quitaPlato(esteMenu.cod_subnivel[cual]);
                        ponGastoTotal(esteMenu.cod_restaurante);
                    }

                    if(cambiosEnMenu) {

                        int actual=viewPager.getCurrentItem();

                        viewPager.setAdapter(pagerAdapter);

                        viewPager.setCurrentItem(actual);

                        compruebaCambiosPedido();

                    }

                    System.out.println("ACEPTA EL MENU");

                    dialog.cancel();

                }

            }
        });

        for(int i=0;i<rutacontenedorMenu.getChildCount();i++) {
            TextView criterio =rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
            TextView cantidadCriterio =rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
            criterio.setText(elMenu[i].nombre_nivel);
            if(((cantidad*elMenu[i].cantidad_nivel)-elegidoTotalEste[i])>0) {
                cantidadCriterio.setText("(" + getResources().getString(R.string.elige) + " " + ((cantidad * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

            }else{

                cantidadCriterio.setText("");

            }

        }

        masMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                precioFinal=precioFinal+precio;
                cantidadMenusPedido=cantidadMenusPedido+1;
                cantidadMenu.setText(""+cantidadMenusPedido);
                cantidadMenu.setAlpha(0.2f);
                cantidadMenu.setScaleX(0.8f);
                cantidadMenu.setScaleY(0.8f);
                cantidadMenu.animate().scaleX(1f);
                cantidadMenu.animate().scaleY(1f);
                cantidadMenu.animate().alpha(1f);
                totalMenu.setText(getResources().getString(R.string.total)+" "+formato.format(precioFinal)+"€");
                ayuda.setVisibility(View.GONE);
                totalMenu.setVisibility(View.VISIBLE);

                for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
                    TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
                    TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
                    criterio.setText(elMenu[i].nombre_nivel);

                    if ((totalAelegir[i] * cantidadMenusPedido) - elegidoTotalEste[i] > 0) {

                        cantidadCriterio.setText("(" + getResources().getString(R.string.elige) + " " + ((cantidadMenusPedido * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

                    } else {

                        cantidadCriterio.setText("");

                    }

                    //cantidadCriterio.setText("("+getResources().getString(R.string.elige)+" "+cantidad*niveles3poner[i].cantidad_nivel+")");

                }

            }
        });

        menosMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (cantidadMenusPedido == 1) {

                    totalMenu.setVisibility(View.GONE);
                }

                boolean quitaPlato = false;

                for (int i = 0; i < elegidoTotalEste.length; i++) {

                    if (elegidoTotalEste[i] == cantidadMenusPedido * totalAelegir[i] && cantidadMenusPedido > 0) {

                        quitaPlato = true;

                        break;
                    }

                }

                if (quitaPlato) {

                    ayuda.setVisibility(View.VISIBLE);
                    ayuda.setText(getResources().getString(R.string.elimine_platos));
                    ayuda.setScaleX(1.2f);
                    //ayuda.setScaleY(1.2f);
                    ayuda.animate().scaleX(1f);

                } else {

                    if (cantidadMenusPedido > 0) {
                        cantidadMenusPedido = cantidadMenusPedido - 1;

                        cantidadMenu.setText(String.valueOf(cantidadMenusPedido));
                        cantidadMenu.setAlpha(0.2f);
                        cantidadMenu.animate().alpha(1f);
                        cantidadMenu.setScaleX(1.2f);
                        cantidadMenu.setScaleY(1.2f);
                        cantidadMenu.animate().scaleX(1f);
                        cantidadMenu.animate().scaleY(1f);
                    }

                    if (precio > 0) {

                        if(precioFinal>0){

                            precioFinal = precioFinal - precio;

                            totalMenu.setText(getResources().getString(R.string.total) + " " + formato.format(precioFinal) + "€");

                            for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
                                TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
                                TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
                                criterio.setText(elMenu[i].nombre_nivel);

                                if ((totalAelegir[i] * cantidadMenusPedido) - elegidoTotalEste[i] > 0) {

                                    cantidadCriterio.setText("(" + getResources().getString(R.string.elige) + " " + ((cantidadMenusPedido * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

                                } else {

                                    cantidadCriterio.setText("");

                                }

                            }

                        }

                    }
                }

            }
        });

        eliminaMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                quitaPlato(esteMenu.cod_subnivel[cual]);
                cambiosEnMenu=true;
                for(int i=0;i<elMenu.length;i++){

                    for(int e=0;e<elMenu[i].cod_subnivel.length;e++) {

                        quitaPlato(elMenu[i].cod_subnivel[e]);
                    }
                }

                ponGastoTotal(esteMenu.cod_restaurante);

                int actual=viewPager.getCurrentItem();

                viewPager.setAdapter(pagerAdapter);

                viewPager.setCurrentItem(actual);

                compruebaCambiosPedido();

                activaBoton(true,boton);

                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void guardaPlato(String cod_plato, int cantidad, String nombre, String detalle, String precio, String cod_restaurante,String nivel, String codigoNivel4){

        if(buscaPlato(cod_plato)) {

            insertarPlato(cod_plato, cantidad, nombre, detalle, precio, cod_restaurante,nivel,codigoNivel4);

        } else {

            actualizaPlato(cod_plato, cantidad, nombre, detalle, precio, cod_restaurante,nivel,codigoNivel4);
        }

    }

    private void quitaPlato(String cod_plato){

        if(!buscaPlato(cod_plato)) {

            eliminaPlato(cod_plato);

        }

    }

    private void insertarPlato(String cod_plato, int cantidad, String nombre, String detalle, String precio,String cod_restaurante,String nivel,String codigoNivel4){

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BBDDmiskartas.NOMBRE_COLUMNA2, cod_plato);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA3, cantidad);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA4, nombre);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA5, detalle);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA6, precio);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA7, cod_restaurante);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA8, nivel);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA9, codigoNivel4);

// Insert the new row, returning the primary key value of the new row
        long datos=db.insert(BBDDmiskartas.TABLE_NAME, null, values);

        //System.out.println("REGISTRO INSERTADO");

        db.close();

    }

    private void eliminaPlato(String cod_plato){

        SQLiteDatabase db = helper.getWritableDatabase();

        String selection = BBDDmiskartas.NOMBRE_COLUMNA2 + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {cod_plato};
        // Issue SQL statement.
        db.delete(BBDDmiskartas.TABLE_NAME, selection, selectionArgs);

        //System.out.println("REGISTRO BORRADO");

        db.close();

    }

    private void actualizaPlato(String cod_plato,int cantidad, String nombre, String detalle, String precio,String cod_restaurante,String nivel, String codigoNivel4){

        SQLiteDatabase db = helper.getWritableDatabase();

        // Nuevo valor de la(s) columna(s)
        ContentValues values = new ContentValues();
        values.put(BBDDmiskartas.NOMBRE_COLUMNA2, cod_plato);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA3, cantidad);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA4, nombre);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA5, detalle);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA6, precio);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA7, cod_restaurante);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA8, nivel);
        values.put(BBDDmiskartas.NOMBRE_COLUMNA9, codigoNivel4);

        // Columna a buscar donde hacer los cambios
        String selection = BBDDmiskartas.NOMBRE_COLUMNA2 + " LIKE ?";
        String[] selectionArgs = {""+cod_plato};

        int count = db.update(
                BBDDmiskartas.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        //System.out.println("DATOS ACTUALIZADOS: "+count);

        db.close();

    }

    private boolean buscaPlato(String cod_plato){

        // DEVUELVE TRUE SI NO EXISTE EL PLATO ------------

        SQLiteDatabase db = helper.getReadableDatabase(); // Hace que la BBDD sea de lectura

        String[] projection = {  // dice qué columnas nos debe devolver la consulta
                // (no ponemos la primera porque es la que usaremos para buscar)
                //MiBaseDatos.NOMBRE_COLUMNA2,
                //MiBaseDatos.NOMBRE_COLUMNA3
        };

        // El valor que queremos buscar con WHERE
        String selection = BBDDmiskartas.NOMBRE_COLUMNA2 + " = ?";
        String[] selectionArgs = {cod_plato};  // metemos (convertido a String) el contenido de

        try {  // Ponemos un trycatch por si el registro buscado no existiera y no nos de error

            Cursor cursor = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    projection,             // array con las columnas a devolver creado antes
                    selection,              // el criterio WHERE
                    selectionArgs,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );

            cursor.moveToFirst();

            if(cursor.getCount()==0){

                //System.out.println("NO EXISTE ESTE NOMBRE: "+cursor.getCount()+" "+nombre);
                db.close();
                return true;
            }else{

                db.close();
                return false;
            }

        }catch (Exception e){

            //System.out.println("NO EXISTE ESTE NOMBRE - ERROR");
            db.close();
            return true;

        }

    }

    public void ponAlergenos(GridLayout contenedor, String queAlergeno){

        for (int e = 0; e < queAlergeno.length(); e++) {

            for(int d=0;d<alergenos.length;d++){

                if( queAlergeno.charAt(e)==alergenos[d].codigo_alergeno.charAt(0)){

                    View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad_texto, null);

                    ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);
                    TextView nombreAlergeno=unidadAlergeno.findViewById(R.id.alergeno_nombre);

                    nombreAlergeno.setText(alergenos[d].nombre_alergeno);


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

    public int compruebaPlatoGuardado(String cod_plato){

        // DEVUELVE TRUE SI NO EXISTE EL PLATO ------------

        SQLiteDatabase db = helper.getReadableDatabase(); // Hace que la BBDD sea de lectura

        String[] projection = {  // dice qué columnas nos debe devolver la consulta
                // (no ponemos la primera porque es la que usaremos para buscar)
                //MiBaseDatos.NOMBRE_COLUMNA2,
                //MiBaseDatos.NOMBRE_COLUMNA3
        };

        // El valor que queremos buscar con WHERE
        String selection = BBDDmiskartas.NOMBRE_COLUMNA2 + " = ?";
        String[] selectionArgs = {cod_plato};  // metemos (convertido a String) el contenido de

        try {  // Ponemos un trycatch por si el registro buscado no existiera y no nos de error

            Cursor cursor = db.query(
                    BBDDmiskartas.TABLE_NAME,   // Tabla a consultar
                    projection,             // array con las columnas a devolver creado antes
                    selection,              // el criterio WHERE
                    selectionArgs,          // argumentos del criterio
                    null,                   // agrupar o no los registros
                    null,                   // filtrar o no por columnas
                    null               // ordenamiento (sortOrder)
            );

            cursor.moveToFirst();

            if(cursor.getCount()>0){

                db.close();

                return Integer.parseInt(cursor.getString(2));

            }else{

                db.close();

                return 0;
            }

        }catch (Exception e){

            db.close();
            return 0;

        }

    }

    }
