package sarao.digital.lakarta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Personaliza_Colores  extends AppCompatActivity {

    LinearLayout contenedorColores, contenedorColorGenerales, colorFondoDetallePortada, contenedorElementos;
    ScrollView scrollColores;

    ImageView imagenPlato,imagenPortada;

    private Toast mensajePop;
    private String mensajeAlerta="";

    // ELEMENTO 1 - PLATO

    LinearLayout colorFondoDetalle,colorFondoNombre;
    TextView colorNombre, colorDetalle, colorPrecio;
    int cNombre, cDetalle, cPrecio, fNombre, fDetalle;

    // ELEMENTO 2 - PORTADA

    TextView colorNombrePortada,colorDetallePortada;
    int  cNombrePortada, cDetallePortada,fDetallePortada;

    // ELEMENTO 3 - BORDES

    TextView colorNombreBorde,colorDetalleBorde, colorPrecioBorde;
    LinearLayout colorFondoBorde;
    int  cTextosBorde, cFondoBorde;

    // -----------

    TextView textoAcambiar,seleccionaDetalle,guardar,volver,previsualiza;

    int fKarta;

    String user, alses,alsesk;

    RadioButton butPrecio, butFondoNombre, butNombre, butDetalle, butFondoDetalle, butTextos, butFondo;

    int grupoSeleccionado=1;

    RadioGroup grupoElementos, grupoBarras;

    //SwitchCompat verImagen;

    View fondoAcambiar;

    Server_EnvioDatos serverEnvioDatos;

    LayoutInflater inflador;

    boolean textoSeleccionado=false;
    boolean fondoSeleccionado=false;

    int[] loscolores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambia_color);

        mensajePop = Toast.makeText(this.getApplicationContext(), mensajeAlerta, Toast.LENGTH_SHORT);

        contenedorColores=findViewById(R.id.contenedor_color_unitario);
        contenedorColorGenerales=findViewById(R.id.contenedor_colores_generales);
        scrollColores=findViewById(R.id.scroll_colores_unitarios);
        guardar=findViewById(R.id.guardar);
        previsualiza=findViewById(R.id.previsualiza);
        grupoElementos=findViewById(R.id.grupo_elementos);

        imagenPortada=findViewById(R.id.imagen_portada);
        grupoBarras=findViewById(R.id.grupo_que_barra);


        butNombre=findViewById(R.id.radioButton0);
        butDetalle=findViewById(R.id.radioButton1);
        butPrecio =findViewById(R.id.radioButton2);
        butFondoNombre =findViewById(R.id.radioButton3);
        butFondoDetalle=findViewById(R.id.radioButton4);
        butTextos=findViewById(R.id.radioButton5);
        butFondo=findViewById(R.id.radioButton6);

        activaBoton(false,previsualiza);

        volver=findViewById(R.id.volver);
        seleccionaDetalle=findViewById(R.id.selecciona_detalle);

        inflador = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        serverEnvioDatos =new Server_EnvioDatos(null, Personaliza_Colores.this,Personaliza_Colores.this,inflador);


        contenedorElementos =findViewById(R.id.contenedor_cardview);


        // ELEMENTO PLATO

        final LinearLayout elementoPlato = (LinearLayout) inflador.inflate(R.layout.cambia_elemento_1, null);

        colorFondoNombre=elementoPlato.findViewById(R.id.color_fondo_nombre);
        colorFondoDetalle=elementoPlato.findViewById(R.id.color_fondo_detalle);
        colorNombre=elementoPlato.findViewById(R.id.color_nombre);
        colorDetalle=elementoPlato.findViewById(R.id.color_detalle);
        colorPrecio=elementoPlato.findViewById(R.id.color_precio);
        imagenPlato=elementoPlato.findViewById(R.id.imagen_plato);

        if(Menu_Empresa.miRestaurante.fN!=0) {colorFondoNombre.setBackgroundColor(Menu_Empresa.miRestaurante.fN);}
        if(Menu_Empresa.miRestaurante.fD!=0) {colorFondoDetalle.setBackgroundColor(Menu_Empresa.miRestaurante.fD);}
        if(Menu_Empresa.miRestaurante.cN!=0) {colorNombre.setTextColor(Menu_Empresa.miRestaurante.cN);}
        if(Menu_Empresa.miRestaurante.cD!=0) {colorDetalle.setTextColor(Menu_Empresa.miRestaurante.cD);}
        if(Menu_Empresa.miRestaurante.cP!=0) {colorPrecio.setTextColor(Menu_Empresa.miRestaurante.cP);}

        contenedorElementos.addView(elementoPlato);

        // ELEMENTO PORTADA

        final LinearLayout elementoPortada = (LinearLayout) inflador.inflate(R.layout.cambia_elemento_2, null);

        colorNombrePortada=elementoPortada.findViewById(R.id.nombre_categoria);
        colorFondoDetallePortada=elementoPortada.findViewById(R.id.fondo_detalle_portada);
        colorDetallePortada=elementoPortada.findViewById(R.id.detalle_categoria);

        if(Menu_Empresa.miRestaurante.cNP!=0) {colorNombrePortada.setTextColor(Menu_Empresa.miRestaurante.cNP);}
        if(Menu_Empresa.miRestaurante.cDP!=0) {colorDetallePortada.setTextColor(Menu_Empresa.miRestaurante.cDP);}
        if(Menu_Empresa.miRestaurante.fDP!=0) {colorFondoDetallePortada.setBackgroundColor(Menu_Empresa.miRestaurante.fDP);}

        contenedorElementos.addView(elementoPortada);

        // ELEMENTO BORDES

        final LinearLayout elementoBordes = (LinearLayout) inflador.inflate(R.layout.cambia_elemento_3, null);

        colorNombreBorde=elementoBordes.findViewById(R.id.nombre_elemento3);
        colorDetalleBorde=elementoBordes.findViewById(R.id.detalle_elemento3);
        colorPrecioBorde=elementoBordes.findViewById(R.id.precio_elemento3);
        colorFondoBorde=elementoBordes.findViewById(R.id.fondo_elemento3);

        if(Menu_Empresa.miRestaurante.tBordes!=0) {
            colorNombreBorde.setTextColor(Menu_Empresa.miRestaurante.tBordes);
            colorDetalleBorde.setTextColor(Menu_Empresa.miRestaurante.tBordes);
            colorPrecioBorde.setTextColor(Menu_Empresa.miRestaurante.tBordes);
        }
        if(Menu_Empresa.miRestaurante.fBordes!=0) {colorFondoBorde.setBackgroundColor(Menu_Empresa.miRestaurante.fBordes);}

        contenedorElementos.addView(elementoBordes);

        // ---------- FONDO KARTA

        if(Menu_Empresa.miRestaurante.fKarta!=0) {contenedorElementos.setBackgroundColor(Menu_Empresa.miRestaurante.fKarta);}

        // ---------------------------------------

        activaBoton(false,guardar);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, guardar);

               cNombre=colorNombre.getCurrentTextColor();
               cDetalle=colorDetalle.getCurrentTextColor();
               cPrecio=colorPrecio.getCurrentTextColor();
               ColorDrawable dimeColor=(ColorDrawable)colorFondoNombre.getBackground();
               fNombre=dimeColor.getColor();
               dimeColor=(ColorDrawable)colorFondoDetalle.getBackground();
               fDetalle=dimeColor.getColor();

                cNombrePortada=colorNombrePortada.getCurrentTextColor();
                cDetallePortada=colorDetallePortada.getCurrentTextColor();

                cTextosBorde=colorNombreBorde.getCurrentTextColor();
                dimeColor=(ColorDrawable) colorFondoBorde.getBackground();
                cFondoBorde =dimeColor.getColor();

                dimeColor=(ColorDrawable) contenedorElementos.getBackground();
                fKarta =dimeColor.getColor();
                dimeColor=(ColorDrawable)colorFondoDetallePortada.getBackground();
                fDetallePortada=dimeColor.getColor();

                cargaUserEmpresa();

                serverEnvioDatos.enviaCambiosColorKarta(user, alses, alsesk, cNombre,cDetalle,cPrecio,fNombre,fDetalle,cNombrePortada,cDetallePortada, fKarta,fDetallePortada,cTextosBorde,cFondoBorde,guardar,previsualiza);

            }
        });

        previsualiza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, previsualiza);

                irAprevio();

            }
        });

        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, volver);

                if(guardar.isEnabled()){

                    preguntaSiGuardar();

                }else{

                   finish();
                }

            }
        });

        grupoElementos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.radioButton0:

                        textoSeleccionado=true;
                        fondoSeleccionado=false;
                        if(grupoSeleccionado==1) {
                            textoAcambiar = colorNombre;
                        }else if(grupoSeleccionado==2){
                            textoAcambiar = colorNombrePortada;
                        }
                        break;

                    case R.id.radioButton1:

                        textoSeleccionado=true;
                        fondoSeleccionado=false;
                        if(grupoSeleccionado==1) {
                            textoAcambiar = colorDetalle;
                        }else if(grupoSeleccionado==2){
                            textoAcambiar = colorDetallePortada;
                        }
                        break;

                    case R.id.radioButton2:

                        textoSeleccionado=true;
                        fondoSeleccionado=false;
                        textoAcambiar=colorPrecio;
                        break;

                    case R.id.radioButton3:

                        textoSeleccionado=false;
                        fondoSeleccionado=true;
                        fondoAcambiar = colorFondoNombre;

                        break;

                    case R.id.radioButton4:

                        textoSeleccionado=false;
                        fondoSeleccionado=true;
                        if(grupoSeleccionado==1) {
                            fondoAcambiar = colorFondoDetalle;
                        }else{
                            fondoAcambiar = colorFondoDetallePortada;
                        }
                        break;

                    case R.id.radioButton5:

                        textoSeleccionado=true;
                        fondoSeleccionado=false;

                        textoAcambiar= colorNombreBorde;

                        break;

                    case R.id.radioButton6:

                        textoSeleccionado=false;
                        fondoSeleccionado=true;

                        fondoAcambiar = colorFondoBorde;

                        break;

                }

            }
        });
/*
        verImagen.setChecked(true);

        grupoBarras.check(R.id.radioButton7);

        verImagen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    imagenPlato.setVisibility(View.VISIBLE);
                    imagenPortada.setVisibility(View.VISIBLE);

                }else{

                    imagenPlato.setVisibility(View.GONE);
                    imagenPortada.setVisibility(View.GONE);

                }

            }
        });



 */
        grupoBarras.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.radioButton7: // PLATO

                        elementoPortada.setVisibility(View.GONE);
                        elementoPlato.setVisibility(View.VISIBLE);
                        elementoBordes.setVisibility(View.GONE);

                        grupoSeleccionado=1;
                        grupoElementos.setVisibility(View.VISIBLE);

                        grupoElementos.check(R.id.radioButton0);

                        butNombre.setVisibility(View.VISIBLE);
                        butDetalle.setVisibility(View.VISIBLE);
                        butPrecio.setVisibility(View.VISIBLE);
                        butFondoNombre.setVisibility(View.VISIBLE);
                        butFondoDetalle.setVisibility(View.VISIBLE);
                        butTextos.setVisibility(View.GONE);
                        butFondo.setVisibility(View.GONE);
                        break;

                    case R.id.radioButton8: // PORTADA

                        elementoPortada.setVisibility(View.VISIBLE);
                        elementoPlato.setVisibility(View.GONE);
                        elementoBordes.setVisibility(View.GONE);

                        grupoSeleccionado=2;
                        grupoElementos.setVisibility(View.VISIBLE);
                        grupoElementos.check(R.id.radioButton0);

                        butNombre.setVisibility(View.VISIBLE);
                        butDetalle.setVisibility(View.VISIBLE);
                        butPrecio.setVisibility(View.GONE);
                        butFondoNombre.setVisibility(View.GONE);
                        butFondoDetalle.setVisibility(View.VISIBLE);
                        butTextos.setVisibility(View.GONE);
                        butFondo.setVisibility(View.GONE);
                        break;

                    case R.id.radioButton9: // FONDO

                        grupoSeleccionado=4;

                        grupoElementos.setVisibility(View.GONE);
                        break;

                    case R.id.radioButton10: // BORDES

                        elementoPortada.setVisibility(View.GONE);
                        elementoPlato.setVisibility(View.GONE);
                        elementoBordes.setVisibility(View.VISIBLE);

                        grupoSeleccionado=3;
                        grupoElementos.setVisibility(View.VISIBLE);
                        grupoElementos.check(R.id.radioButton5);

                        butNombre.setVisibility(View.GONE);
                        butDetalle.setVisibility(View.GONE);
                        butPrecio.setVisibility(View.GONE);
                        butFondoNombre.setVisibility(View.GONE);
                        butFondoDetalle.setVisibility(View.GONE);
                        butTextos.setVisibility(View.VISIBLE);
                        butFondo.setVisibility(View.VISIBLE);
                        break;

                }

                textoSeleccionado=true;
                fondoSeleccionado=false;
                if(grupoSeleccionado==1) {
                    textoAcambiar = colorNombre;
                }else if(grupoSeleccionado==2){
                    textoAcambiar = colorNombrePortada;
                }else if(grupoSeleccionado==3){
                    textoAcambiar = colorNombreBorde;
                }

            }
        });

        grupoBarras.check(R.id.radioButton7);
        grupoElementos.check(R.id.radioButton0);

        textoSeleccionado=true;
        fondoSeleccionado=false;
        textoAcambiar = colorNombre;


        loscolores=new int[18];

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

        for(int i=0;i<loscolores.length;i++) {

            final int cual=i;

            final LinearLayout colores = (LinearLayout) inflador.inflate(R.layout.selector_color_general, null);

            View color=colores.findViewById(R.id.color_general);

            color.setBackgroundColor(getApplicationContext().getResources().obtainTypedArray(loscolores[i]).getColor(0,0));

            contenedorColorGenerales.addView(colores);

            colores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ponColores(loscolores[cual]);

                }
            });

        }

    }

    @Override
    public void onBackPressed() {

        if(guardar.isEnabled()){

            preguntaSiGuardar();

        }else{

            super.onBackPressed();
        }

    }

    private void ponColores(final int queColor){

        contenedorColores.removeAllViews();

        final TypedArray ta= getApplicationContext().getResources().obtainTypedArray(queColor);

        for(int i=0;i<ta.length();i++) {

            final int este=i;

            final LinearLayout colores = (LinearLayout) inflador.inflate(R.layout.selector_color_unidad, null);

            View color=colores.findViewById(R.id.color_unitario);

            final int esteColor=ta.getColor(este,0);

            color.setBackgroundColor(esteColor);

            contenedorColores.addView(colores);

            colores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activaBoton(true,guardar);

                    if(grupoSeleccionado!=4 && grupoSeleccionado!=3) {

                        int chekado = grupoElementos.getCheckedRadioButtonId();

                        if (textoSeleccionado && chekado != -1) {

                            textoAcambiar.setTextColor(esteColor);

                        } else if (fondoSeleccionado && chekado != -1) {

                            fondoAcambiar.setBackgroundColor(esteColor);
                        }
                    }else if(grupoSeleccionado==3) {

                        int chekado = grupoElementos.getCheckedRadioButtonId();

                        if (textoSeleccionado && chekado != -1) {

                            textoAcambiar.setTextColor(esteColor);
                            colorDetalleBorde.setTextColor(esteColor);
                            colorPrecioBorde.setTextColor(esteColor);

                        } else if (fondoSeleccionado && chekado != -1) {

                            fondoAcambiar.setBackgroundColor(esteColor);
                        }
                    }else{

                        contenedorElementos.setBackgroundColor(esteColor);
                    }

                }
            });

        }

        ta.recycle();

        scrollColores.setScrollY(0);


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

    private void preguntaSiGuardar(){

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.emerg_pregunta_alerta, null);

        Button continuar=alertLayout.findViewById(R.id.pedido_guardar);
        Button salir=alertLayout.findViewById(R.id.pedido_noguardar);
        TextView pregunta=alertLayout.findViewById(R.id.pregunta_alerta);
        ImageView imagen=alertLayout.findViewById(R.id.icono_alerta);

        continuar.setText(getResources().getString(R.string.no_quedarme));
        salir.setText(getResources().getString(R.string.salir));
        pregunta.setText(getResources().getString(R.string.pregunta_cambios_color));
        imagen.setImageResource(R.drawable.colors);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setView(alertLayout);

        alert.setCancelable(true);


        final AlertDialog dialog = alert.create();

        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
                activaBoton(true, volver);

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

    public void cargaUserEmpresa(){


        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(this);

        user=guarda.getString("USER_EMPRESA","0");
        alses=guarda.getString("ALSES_EMPRESA","0");
        alsesk=guarda.getString("ALSESK_EMPRESA","0");


    }

    private void irAprevio(){

        if(compruebaRed()) {

            Intent miIntent = new Intent(this, Contenedor_Lakarta.class);

            miIntent.putExtra("QUERESTAURANTE", Menu_Empresa.miRestaurante);
            miIntent.putExtra("KARTA_DESDE_ADMIN", "si");
            miIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(miIntent);

        }else{

            mensajeAlerta =getResources().getString(R.string.sin_internet);
            ponAlerta();

        }


    }

    public boolean compruebaRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
}
