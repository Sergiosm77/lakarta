package sarao.digital.lakarta;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Ver_Platos {

    LayoutInflater inflador;
    int cantidadMenusPedido;
    int cantidadPlatosPedido;
    int cantidadNivel4;
    boolean cambiosEnMenu, cambiosEnPlato;
    boolean verDetallesPlato;
    double precioFinal;
    Context contexto;
    final DecimalFormat formato = new DecimalFormat("0.00");
    int[] scrollMenuPedido;
    public Alergenos[] alergenos;
    BBDD_Helper helper;

    private String misAlergenos;

    boolean haciendoPedido=false;

    public Ver_Platos(Context contexto, LayoutInflater inflador){

        this.contexto=contexto;
        this.inflador=inflador;

        cargaAlergenos();
        cargaMisAlergenos();
        helper = new BBDD_Helper(contexto);

    }

    public void esMenu(final Kartas esteMenu, final Kartas[] elMenu, final int cual, final TextView cuantosMenus, final View boton){

        final View alertLayout = inflador.inflate(R.layout.emerg_contenedor_menu, null);

        final LinearLayout rutacontenedorMenu=alertLayout.findViewById(R.id.contenedor_menus);
        final ScrollView menusScroll=alertLayout.findViewById(R.id.contenedormenus_scroll);

        TextView nombreMenu=alertLayout.findViewById(R.id.nombre_menu);
        TextView detalleMenu=alertLayout.findViewById(R.id.detalle_menu);
        TextView precioMenu=alertLayout.findViewById(R.id.precio_menu);
        final TextView totalMenu=alertLayout.findViewById(R.id.total_menu);
        final TextView cantidadMenu=alertLayout.findViewById(R.id.cantidad_menu);
        final TextView ayuda=alertLayout.findViewById(R.id.ayuda_plato);

        final TextView masMenu=alertLayout.findViewById(R.id.menu_mas);
        TextView menosMenu=alertLayout.findViewById(R.id.menu_menos);


        // BOTONES --------------------------

        LinearLayout cajaPedido=alertLayout.findViewById(R.id.caja_hacer_pedido_menu);
        cajaPedido.setVisibility(View.GONE);

        TextView eliminaMenu=alertLayout.findViewById(R.id.elimina_menu);
        final TextView aceptaMenu=alertLayout.findViewById(R.id.acepta_menu);

        // --------------------------------

        nombreMenu.setText(esteMenu.nombre_subnivel[cual]);
        detalleMenu.setText(esteMenu.detalle_subnivel[cual]);

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                return false;
            }
        });

        int cantidad = compruebaPlatoGuardado(esteMenu.cod_subnivel[cual]);
        double suplementos=compruebaSuplementosMenu(esteMenu.cod_subnivel[cual]);

        if(cantidad>0){

            cantidadMenu.setText(String.valueOf(cantidad));
            cantidadMenusPedido=cantidad;

        }

        /*
        if(cantidad==0){

            cantidadMenu.setText(String.valueOf(1));
            cantidadMenusPedido=1;
            cantidad=1;

        }else{

            cantidadMenu.setText(String.valueOf(cantidad));
            cantidadMenusPedido=cantidad;

        }

         */

        ayuda.setVisibility(View.GONE);

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

        precioFinal=(precio*cantidad)+suplementos;
        totalMenu.setText(formato.format(precioFinal)+"€");

        precioMenu.setText(formato.format(precio)+"€");

        final int[] elegidoTotalEste=new int[elMenu.length];
        final int[] totalAelegir=new int[elMenu.length];

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);

        alert.setView(alertLayout);

        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);

            }
        });

        scrollMenuPedido=new int[elMenu.length];

        // PONE EL MENU -----------

        for (int i = 0; i < elMenu.length; i++) {  // INFLA NIVEL 3 ---------------------

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

                Glide.with(contexto)
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

                LinearLayout cajaSuplemento=platoNivel4.findViewById(R.id.caja_suplemento);
                TextView precioSuplemento=platoNivel4.findViewById(R.id.precio_suplemento);

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

                                if(misAlergenos.contains(String.valueOf(elMenu[i].alergenos_subnivel[e].charAt(a)))){

                                    alertaAlergeno.setVisibility(View.VISIBLE);

                                }



                                View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad, null);

                                ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.alergeno);

                                Glide.with(contexto)
                                        .load(alergenos[b].imagen_alergeno)
                                        .error(R.drawable.no_photo)
                                        .into(imagenAlergeno);

                                contenedorAlergenos.addView(unidadAlergeno);
                                break;

                            }

                        }

                    }
                }

                if(elMenu[este].precio_subnivel[estePlato]==0){

                    cajaSuplemento.setVisibility(View.GONE);

                }else{

                    precioSuplemento.setText(formato.format(Double.parseDouble(String.valueOf(elMenu[este].precio_subnivel[estePlato]))));
                }

                if(elMenu[este].imagen_subnivel[estePlato].equals("") || elMenu[este].mostrar_imagen_subnivel[estePlato]==0){

                    contenedorImagenPlato.setVisibility(View.GONE);

                }else{

                    Glide.with(contexto)
                            .load(elMenu[este].imagen_subnivel[estePlato])
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.noimage)
                            .into(imagenPlato);

                    contenedorImagenPlato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            activaBoton(false, contenedorImagenPlato);

                            verFoto(elMenu[este].nombre_subnivel[estePlato], elMenu[este].imagen_subnivel[estePlato], contenedorImagenPlato);

                        }
                    });

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

                            if(haciendoPedido) {

                                cantidadNivel4 = Integer.parseInt(cantidadPlato_nivel4.getText().toString());

                                if (cantidadNivel4 > 0) {

                                    precioFinal = precioFinal - elMenu[este].precio_subnivel[estePlato];

                                    totalMenu.setText(formato.format(precioFinal) + "€");

                                    elegidoTotalEste[este]--;
                                    cantidadNivel4--;
                                    TextView cambia = rutacontenedorMenu.getChildAt(este).findViewById(R.id.cantidad_criterio);
                                    cambia.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((totalAelegir[este] * cantidadMenusPedido) - elegidoTotalEste[este]) + " más)");

                                    cantidadPlato_nivel4.setText("" + cantidadNivel4);

                                    if (cantidadNivel4 == 0) {

                                        quitaEstePlato(elMenu[este].cod_subnivel[estePlato]);
                                        cantidadPlato_nivel4.setVisibility(View.GONE);
                                        cantidadPlato_nivel4.setText("");
                                        quitaPlato.setVisibility(View.GONE);

                                    } else {

                                        guardaPlato(elMenu[este].cod_subnivel[estePlato], cantidadNivel4, elMenu[este].nombre_subnivel[estePlato], elMenu[este].detalle_subnivel[estePlato], String.valueOf(elMenu[este].precio_subnivel[estePlato]), esteMenu.cod_restaurante, "4", elMenu[este].cod_nivel_sup);

                                        cambiosEnMenu = true;
                                    }

                                }

                            }

                        }
                    });

                    platoNivel4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(haciendoPedido) {

                                ayuda.setVisibility(View.GONE);

                                if (cantidadMenusPedido == 0) {

                                    ayuda.setVisibility(View.VISIBLE);
                                    ayuda.setText(contexto.getString(R.string.elige_cuantos_menus));
                                    ayuda.setScaleX(1.2f);
                                    //ayuda.setScaleY(1.2f);
                                    ayuda.animate().scaleX(1f);

                                } else {

                                    if (elegidoTotalEste[este] < (totalAelegir[este] * cantidadMenusPedido)) {

                                        if (elMenu[este].precio_subnivel[estePlato] > 0) {

                                            precioFinal = precioFinal + elMenu[este].precio_subnivel[estePlato];

                                            totalMenu.setText(formato.format(precioFinal) + "€");
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
                                        cambia.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((totalAelegir[este] * cantidadMenusPedido) - elegidoTotalEste[este]) + " más)");
                                        if ((totalAelegir[este] * cantidadMenusPedido) - elegidoTotalEste[este] > 0) {

                                            cambia.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidadMenusPedido * elMenu[este].cantidad_nivel) - elegidoTotalEste[este]) + " más)");
                                            //cambia.setTextColor(getResources().getColor(R.color.colorRosa3, getActivity().getTheme()));

                                        } else {

                                            cambia.setText("");

                                        }
                                        cantidadPlato_nivel4.setVisibility(View.VISIBLE);
                                        cantidadPlato_nivel4.setText("" + cantidadNivel4);

                                        guardaPlato(elMenu[este].cod_subnivel[estePlato], cantidadNivel4, elMenu[este].nombre_subnivel[estePlato], elMenu[este].detalle_subnivel[estePlato], String.valueOf(elMenu[este].precio_subnivel[estePlato]), esteMenu.cod_restaurante, "4", elMenu[este].cod_nivel_sup);

                                        cambiosEnMenu = true;
                                    } else {

                                        ayuda.setVisibility(View.VISIBLE);
                                        ayuda.setText("Quita platos para añadir uno nuevo");
                                        ayuda.setScaleX(1.2f);
                                        //ayuda.setScaleY(1.2f);
                                        ayuda.animate().scaleX(1f);


                                    }
                                }

                            }

                        }
                    });

                } else {

                    noDisponible.setText(contexto.getString(R.string.no_disponible));
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

                cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

            } else {

                cantidadCriterio.setText("");

            }

        }

        // -------------------------------------------

        for(int i=0;i<rutacontenedorMenu.getChildCount();i++) {
            TextView criterio =rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
            TextView cantidadCriterio =rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
            criterio.setText(elMenu[i].nombre_nivel);
            if(((cantidad*elMenu[i].cantidad_nivel)-elegidoTotalEste[i])>0) {
                cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

            }else{

                cantidadCriterio.setText("");

            }

        }

        // CONFIGURA BOTONES ------------------------------------

        eliminaMenu.setText(contexto.getResources().getString(R.string.hacer_pedido));
        aceptaMenu.setText(contexto.getResources().getString(R.string.cerrar));

        aceptaMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // AHORA ACEPTA MENU ES CERRAR ------------------------

                dialog.cancel();

            }
        });

        eliminaMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // AHORA ELIMINA MENU ES HACER PEDIDO ------------------------

                cajaPedido.setVisibility(View.VISIBLE);

                haciendoPedido=true;

                eliminaMenu.setText(contexto.getResources().getString(R.string.descarta_pedido));
                aceptaMenu.setText(contexto.getResources().getString(R.string.acepta_pedido));

                masMenu.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        precioFinal=precioFinal+precio;
                        cantidadMenusPedido++;
                        cantidadMenu.setText(String.valueOf(cantidadMenusPedido));
                        cantidadMenu.setAlpha(0.2f);
                        cantidadMenu.setScaleX(0.8f);
                        cantidadMenu.setScaleY(0.8f);
                        cantidadMenu.animate().scaleX(1f);
                        cantidadMenu.animate().scaleY(1f);
                        cantidadMenu.animate().alpha(1f);
                        totalMenu.setText(formato.format(precioFinal)+"€");
                        ayuda.setVisibility(View.GONE);
                        totalMenu.setVisibility(View.VISIBLE);

                        for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
                            TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
                            TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
                            criterio.setText(elMenu[i].nombre_nivel);

                            if ((totalAelegir[i] * cantidadMenusPedido) - elegidoTotalEste[i] > 0) {

                                cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidadMenusPedido * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

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

                        if (cantidadMenusPedido > 1) {

                            boolean quitaPlato = false;

                            for (int i = 0; i < elegidoTotalEste.length; i++) {

                                if (elegidoTotalEste[i] == cantidadMenusPedido * totalAelegir[i]) {

                                    quitaPlato = true;

                                    break;
                                }

                            }

                            if (quitaPlato) {

                                ayuda.setVisibility(View.VISIBLE);
                                ayuda.setText(contexto.getResources().getString(R.string.elimine_platos));
                                ayuda.setScaleX(1.2f);
                                //ayuda.setScaleY(1.2f);
                                ayuda.animate().scaleX(1f);

                            } else {

                                cantidadMenusPedido--;

                                cantidadMenu.setText(String.valueOf(cantidadMenusPedido));
                                cantidadMenu.setAlpha(0.2f);
                                cantidadMenu.animate().alpha(1f);
                                cantidadMenu.setScaleX(1.2f);
                                cantidadMenu.setScaleY(1.2f);
                                cantidadMenu.animate().scaleX(1f);
                                cantidadMenu.animate().scaleY(1f);


                                if (precio > 0) {

                                    if (precioFinal > 0) {

                                        precioFinal = precioFinal - precio;

                                        totalMenu.setText(formato.format(precioFinal) + "€");

                                        for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
                                            TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
                                            TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
                                            criterio.setText(elMenu[i].nombre_nivel);

                                            if ((totalAelegir[i] * cantidadMenusPedido) - elegidoTotalEste[i] > 0) {

                                                cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidadMenusPedido * elMenu[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

                                            } else {

                                                cantidadCriterio.setText("");

                                            }

                                        }

                                    }

                                }
                            }

                        }
                    }
                });

                aceptaMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        activaBoton(true, boton);

                        if (cantidadMenusPedido != 0) {

                            int elegido = 0;
                            int total = 0;

                            for (int i = 0; i < elegidoTotalEste.length; i++) {

                                elegido = elegido + (elegidoTotalEste[i]);
                                total = total + (totalAelegir[i] * cantidadMenusPedido);
                            }

                            if (elegido < total) {

                                ayuda.setVisibility(View.VISIBLE);
                                ayuda.setText(contexto.getResources().getString(R.string.faltan_platos));
                                ayuda.setScaleX(1.2f);
                                //ayuda.setScaleY(1.2f);
                                ayuda.animate().scaleX(1f);

                                scrollMenuPedido[0] = 0;

                                for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {

                                    View child = rutacontenedorMenu.getChildAt(i);

                                    TextView cuantos = child.findViewById(R.id.cantidad_criterio);

                                    if (i > 0) {
                                        scrollMenuPedido[i] = child.getHeight() + scrollMenuPedido[i - 1];
                                    }

                                    if (!cuantos.getText().equals("")) {

                                        menusScroll.scrollTo(0, scrollMenuPedido[i]);
                                        break;
                                    }

                                }

                            } else {

                                if (cuantosMenus != null) {

                                    float alturaBarra = cuantosMenus.getHeight();

                                    cuantosMenus.setAlpha(1f);

                                    int contenidoContador;

                                    if (cuantosMenus.getText().toString().equals("")) {

                                        contenidoContador = 0;
                                    } else {

                                        contenidoContador = Integer.parseInt(cuantosMenus.getText().toString());
                                    }

                                    cuantosMenus.setText(String.valueOf(cantidadMenusPedido));
/*
                                    if (cuantosMenus.getY() != Math.round(px) || contenidoContador == 0) {

                                        cuantosMenus.setY(-alturaBarra + Math.round(px));
                                        cuantosMenus.animate().yBy(alturaBarra);
                                    }

 */
                                    if (cuantosMenus.getY() != 0 || contenidoContador == 0) {

                                        cuantosMenus.setY(-alturaBarra);
                                        cuantosMenus.animate().yBy(alturaBarra);
                                    }
                                }

                                guardaPlato(esteMenu.cod_subnivel[cual], cantidadMenusPedido, esteMenu.nombre_subnivel[cual], esteMenu.detalle_subnivel[cual], String.valueOf(precio), esteMenu.cod_restaurante, "2", "tiene");
                                ponGastoTotal(esteMenu.cod_restaurante);
                                Contenedor_Lakarta.hayCambios = true;

                        /*
                        for(int m=0; m<rutacontenedorMenu.getChildCount();m++){

                            LinearLayout contendorPlatos=rutacontenedorMenu.getChildAt(m).findViewById(R.id.contenedor_elementos);

                            for(int e=0;e<contendorPlatos.getChildCount();e++){

                                TextView cantidadPlato=contendorPlatos.getChildAt(e).findViewById(R.id.cantidadPlato_nivel4);

                                if(Integer.parseInt(cantidadPlato.getText().toString())>0){

                                    guardaPlato("","","","","","","","");
                                }
                            }

                        }

                         */


                                if (cambiosEnMenu && cuantosMenus == null) {

                                    ((Contenedor_Lakarta) contexto).compruebaCambiosPedido();

                                    ((Contenedor_Lakarta) contexto).onResume();

                                }

                                System.out.println("ACEPTA EL MENU");

                                dialog.cancel();

                            }

                        }else{

                            ayuda.setVisibility(View.VISIBLE);
                            ayuda.setText(contexto.getResources().getString(R.string.elige_cuantos_menus));
                            ayuda.setScaleX(1.2f);
                            ayuda.animate().scaleX(1f);

                        }

                    }
                });

                eliminaMenu.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if(cuantosMenus!=null){

                            float alturaBarra=cuantosMenus.getHeight();

                            Animation move = new TranslateAnimation(0f,0f,0f,-alturaBarra);
                            move.setDuration(300);

                            move.setAnimationListener(new Animation.AnimationListener(){

                                @Override
                                public void onAnimationStart(Animation animation){}

                                @Override
                                public void onAnimationRepeat(Animation animation){}

                                @Override
                                public void onAnimationEnd(Animation animation){

                                    cuantosMenus.setText("");
                                    cuantosMenus.setAlpha(0f);

                                }
                            });
                            cuantosMenus.startAnimation(move);

                        }

                        quitaEstePlato(esteMenu.cod_subnivel[cual]);
                        for(int i=0;i<elMenu.length;i++){

                            for(int e=0;e<elMenu[i].cod_subnivel.length;e++) {

                                quitaEstePlato(elMenu[i].cod_subnivel[e]);
                            }
                        }

                        ponGastoTotal(esteMenu.cod_restaurante);

                        if(cuantosMenus==null) {

                            ((Contenedor_Lakarta) contexto).compruebaCambiosPedido();

                            ((Contenedor_Lakarta) contexto).onResume();

                        }

                        cantidadMenu.setText("0");
                        cantidadMenusPedido=0;
                        haciendoPedido=false;

                        activaBoton(true,boton);

                        dialog.cancel();

                    }
                });

            }
        });

        // ---------------------

        if(cantidad>0){

            eliminaMenu.callOnClick();
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public void esPlato(final Kartas queplato, final int cual, final TextView cuantosPlatos, final View boton, boolean alerta,String queAlergenos){

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

        verDetallesPlato =false;

        if(alerta){

            String ponAlergias=contexto.getResources().getString(R.string.eres_alergico)+queAlergenos;

            alertaAlergeno.setVisibility(View.VISIBLE);
            alertaAlergeno.setText(ponAlergias);
        }else{

            alertaAlergeno.setVisibility(View.GONE);
        }

        if(Contenedor_Lakarta.queRestaurante.fN!=0){
/*
            int color=Contenedor_Lakarta.queRestaurante.fN;

            if(!queplato.imagen_subnivel[cual].equals("null") && queplato.mostrar_imagen_subnivel[cual]==1) {

                color = (color & 0x00FFFFFF) | 0x99000000;
            }

 */

            fichaNombre.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fN);
        }

        if(Contenedor_Lakarta.queRestaurante.cN!=0){

            nombrePlato.setTextColor(Contenedor_Lakarta.queRestaurante.cN);
        }

        if(Contenedor_Lakarta.queRestaurante.cP!=0){

            precioPlato.setTextColor(Contenedor_Lakarta.queRestaurante.cP);
        }
        if(Contenedor_Lakarta.queRestaurante.fD!=0){

            int color=Contenedor_Lakarta.queRestaurante.fD;

            if(!queplato.imagen_subnivel[cual].equals("") && queplato.mostrar_imagen_subnivel[cual]==1) {

                color = (color & 0x00FFFFFF) | 0x99000000;
            }


            contenedorDetalle.setBackgroundColor(color);
        }

        if(Contenedor_Lakarta.queRestaurante.cD!=0){

            detallePlato.setTextColor(Contenedor_Lakarta.queRestaurante.cD);
        }



        //final double precio = Double.parseDouble(queplato.precio_subnivel[cual]);

        final double precio;

        if(queplato.precio_subnivel[cual]==0){
            if(queplato.precio_nivel!=0){

                precio = queplato.precio_nivel;

            }else{

                precio = 0;
            }
        }else{

            precio = queplato.precio_subnivel[cual];
        }

        cantidadPlatosPedido=(compruebaPlatoGuardado(queplato.cod_subnivel[cual]));

        cantidadPlatos.setText(String.valueOf(cantidadPlatosPedido));

        if(cantidadPlatosPedido>0 && precio>0){

            precioFinal=precio*cantidadPlatosPedido;
            if(precioFinal==0){
                precioTotal.setText("");
            }else {
                precioTotal.setText(formato.format(precioFinal) + "€");
            }

        }else{

            precioTotal.setText("");

            precioFinal=0;
        }

        nombrePlato.setText(queplato.nombre_subnivel[cual]);

        if(queplato.detalle_subnivel[cual].equals("")){

            detallePlato.setVisibility(View.GONE);
        }else {
            detallePlato.setText(queplato.detalle_subnivel[cual]);
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

                if(precio>0) {

                    precioFinal = ((double) Math.round(precioFinal * 100d) / 100d) + precio;

                    precioTotal.setText(formato.format(precioFinal) + "€");
                }

                if(cantidadPlatosPedido==0 && precio>0) {

                    float alto=foto.getHeight();
                    float alturaPrecio=precioTotal.getHeight();


                    /*
                    precioTotal.setY(alto);
                    precioTotal.animate().yBy(-alturaPrecio).setDuration(100);

                     */

                }

                cantidadPlatosPedido=cantidadPlatosPedido+1;
                cantidadPlatos.setText(String.valueOf(cantidadPlatosPedido));
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

                if(precio>0){

                    if(precioFinal>0){

                        precioFinal=((double)Math.round(precioFinal * 100d) / 100d)-precio;

                    }

                    precioTotal.setText(formato.format(precioFinal)+"€");

                }else{

                    precioTotal.setText("");
                }

                if(cantidadPlatosPedido>0){

                    cantidadPlatosPedido=cantidadPlatosPedido-1;
                    cantidadPlatos.setText(String.valueOf(cantidadPlatosPedido));
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

                if(cantidadPlatosPedido==0){

                    precioTotal.setText("");

                }

            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true,boton);

            }
        });

        aceptaPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, aceptaPlato);

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true,boton);

                int contenidoContador;

                if(cantidadPlatosPedido>0) {

                    if(cuantosPlatos!=null) {

                        float alturaBarra=cuantosPlatos.getHeight();

                        cuantosPlatos.setAlpha(1f);


                        if (cuantosPlatos.getText().toString().equals("")) {

                            contenidoContador = 0;
                        } else {

                            contenidoContador = Integer.parseInt(cuantosPlatos.getText().toString());
                        }

                        cuantosPlatos.setText(String.valueOf(cantidadPlatosPedido));

                        if (cuantosPlatos.getY() != 0 || contenidoContador == 0) {

                            cuantosPlatos.setY(-alturaBarra);
                            cuantosPlatos.animate().yBy(alturaBarra);
                        }
                    }

                    guardaPlato(queplato.cod_subnivel[cual],cantidadPlatosPedido,queplato.nombre_subnivel[cual],queplato.detalle_subnivel[cual],String.valueOf(precio), queplato.cod_restaurante,"2",null);


                }else{
                    quitaEstePlato(queplato.cod_subnivel[cual]);


                    //float alturaContenedor=contenedorPlatos.getHeight();

                    if(cuantosPlatos!=null) {

                        float alturaBarra=cuantosPlatos.getHeight();

                        cuantosPlatos.setAlpha(0f);

                        cuantosPlatos.setText(String.valueOf(cantidadPlatosPedido));

                        if (cuantosPlatos.getY() != -alturaBarra) {

                            cuantosPlatos.setY(0);
                            cuantosPlatos.animate().yBy(-alturaBarra);

                        }
                    }


                }

                Contenedor_Lakarta.hayCambios=true;

                ponGastoTotal(queplato.cod_restaurante);

                if(cuantosPlatos==null) {

                    ((Contenedor_Lakarta) contexto).compruebaCambiosPedido();

                    ((Contenedor_Lakarta) contexto).onResume();

                }

                dialog.cancel();

            }
        });

        cancelaPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(false, cancelaPlato);

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true,boton);
                dialog.cancel();

            }
        });

        eliminaPlato.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                activaBoton(false,eliminaPlato);

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true,boton);

                if(cuantosPlatos!=null) {

                    float alturaBarra = cuantosPlatos.getHeight();

                    Animation move = new TranslateAnimation(0f, 0f, 0f, -alturaBarra);
                    move.setDuration(300);

                    System.out.println("ELIMINA BARRA");

                    move.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            cuantosPlatos.setText("");
                            cuantosPlatos.setAlpha(0f);



                        }
                    });
                    cuantosPlatos.startAnimation(move);

                }

                quitaEstePlato(queplato.cod_subnivel[cual]);
                ponGastoTotal(queplato.cod_restaurante);

                if(cuantosPlatos==null) {

                    ((Contenedor_Lakarta) contexto).compruebaCambiosPedido();

                    ((Contenedor_Lakarta) contexto).onResume();

                }

                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {


                if(!queplato.imagen_subnivel[cual].equals("") && queplato.mostrar_imagen_subnivel[cual]==1) {

                    Glide.with(contexto)
                            .load(queplato.imagen_subnivel[cual])
                            .error(R.drawable.no_photo)
                            .into(imagenPlato);

                }else{

                    imagenPlato.setVisibility(View.GONE);
                    vacio.setVisibility(View.GONE);

                    detallePlato.setMaxLines(10);
                    detallePlato.setMinLines(4);
                    verMas.setVisibility(View.GONE);
                }

                if(!queplato.alergenos_subnivel[cual].equals("")){

                    ponAlergenos(contenedorAlergenos, queplato.alergenos_subnivel[cual]);
                }

                Contenedor_Lakarta.blurview.setVisibility(View.VISIBLE);

                if(detallePlato.getLineCount()>3) {

                    verMas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            if (detallePlato.getMaxLines() == 3 && !verDetallesPlato) {


                                params.height = 0;

                                contenedorDetalle.setLayoutParams(params);

                                verDetallesPlato =true;

                                detallePlato.setMaxLines(20);

                                verMas.setRotation(180f);

                                detallePlato.scrollTo(0,0);

                                //constraintSet.connect(R.id.emerg_detalle_plato, ConstraintSet.TOP, R.id.ficha_nombre, ConstraintSet.BOTTOM, 0);
                                //constraintSet.applyTo(foto);

                            } else {


                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                                contenedorDetalle.setLayoutParams(params);

                                verDetallesPlato =false;

                                //constraintSet.connect(R.id.emerg_detalle_plato, ConstraintSet.TOP, R.id.vacio, ConstraintSet.BOTTOM, 0);
                                //constraintSet.applyTo(foto);

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

    public void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

    public void cargaAlergenos(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

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

    private int compruebaPlatoGuardado(String cod_plato){

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

    private double compruebaSuplementosMenu(String cod_menu){

        System.out.println("COD SUPERIOR SUPLEMENTOS: "+cod_menu);

        SQLiteDatabase db = helper.getReadableDatabase(); // Hace que la BBDD sea de lectura

        String[] projection = {  // dice qué columnas nos debe devolver la consulta
                // (no ponemos la primera porque es la que usaremos para buscar)
                //MiBaseDatos.NOMBRE_COLUMNA2,
                //MiBaseDatos.NOMBRE_COLUMNA3
        };

        // El valor que queremos buscar con WHERE
        String selection = BBDDmiskartas.NOMBRE_COLUMNA9 + " = ?";
        String[] selectionArgs = {cod_menu};  // metemos (convertido a String) el contenido de

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

            double totalPrecioMenu=0;

            if(cursor.getCount()>0){

                for(int i=0;i<cursor.getCount();i++){

                    cursor.moveToPosition(i);

                    totalPrecioMenu=totalPrecioMenu+(Integer.parseInt(cursor.getString(2))*Double.parseDouble(cursor.getString(5)));

                }

                db.close();

                return totalPrecioMenu;

            }else{

                db.close();

                return 0;
            }

        }catch (Exception e){

            db.close();
            return 0;

        }

    }

    private void guardaPlato(String cod_plato, int cantidad, String nombre, String detalle, String precio, String cod_restaurante,String nivel, String codigoNivel4){

        if(buscaPlato(cod_plato)) {

            insertarPlato(cod_plato, cantidad, nombre, detalle, precio, cod_restaurante,nivel,codigoNivel4);

        } else {

            actualizaPlato(cod_plato, cantidad, nombre, detalle, precio, cod_restaurante,nivel,codigoNivel4);
        }

    }

    private void quitaEstePlato(String cod_plato){

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

    public void actualizaPlato(String cod_plato,int cantidad, String nombre, String detalle, String precio,String cod_restaurante,String nivel, String codigoNivel4){

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

    private void ponGastoTotal(String cod_restaurante){

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

                    System.out.println("DATOS PRODUCTO "+cursor.getString(3));

                    System.out.println("PRECIOS DE ESTE PRODUCTO "+cursor.getString(5)+" "+cursor.getString(2));

                    total=total+(Double.parseDouble(cursor.getString(5))*Integer.parseInt(cursor.getString(2)));

                }

                Contenedor_Lakarta.botonPedido.setAlpha(1f);
                mover(Contenedor_Lakarta.botonPedido,false, true);
                System.out.println("PONE BOTON PEDIDO");

                System.out.println("TOTAL MENU "+total);

                if(total>0) {
                    Contenedor_Lakarta.contador_total.setText("Total: " + formato.format(total) + "€");
                    Contenedor_Lakarta.contador_total.setVisibility(View.VISIBLE);
                }else{

                    Contenedor_Lakarta.contador_total.setVisibility(View.GONE);
                }


            }else{

                System.out.println("QUITA BOTON PEDIDO");

                mover(Contenedor_Lakarta.botonPedido,false, false);

            }


        }catch (Exception e){


        }

        db.close();

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

    public void ponAlergenos(GridLayout contenedor, String queAlergeno){

        for (int e = 0; e < queAlergeno.length(); e++) {

            for(int d=0;d<alergenos.length;d++){

                if( queAlergeno.charAt(e)==alergenos[d].codigo_alergeno.charAt(0)){

                    View unidadAlergeno = inflador.inflate(R.layout.alergeno_unidad_texto, null);

                    ImageView imagenAlergeno=unidadAlergeno.findViewById(R.id.alergeno);
                    TextView nombreAlergeno=unidadAlergeno.findViewById(R.id.alergeno_nombre);

                    nombreAlergeno.setText(alergenos[d].nombre_alergeno);


                    Glide.with(contexto)
                            .load(alergenos[d].imagen_alergeno)
                            .error(R.drawable.noimage)
                            .into(imagenAlergeno);

                    contenedor.addView(unidadAlergeno);
                    break;

                }

            }

        }
    }

    private void cargaMisAlergenos(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        misAlergenos=guarda.getString("MISALERGENOS","");

    }

    private void verFoto(String nombrePlato, String fotoPlato, final View boton){

        final View alertLayout = inflador.inflate(R.layout.ver_foto, null);

        final ImageView queFoto = alertLayout.findViewById(R.id.ver_foto);

        TextView nombreCriterio=alertLayout.findViewById(R.id.nombre_categoria);
        TextView detalleCriterio=alertLayout.findViewById(R.id.detalle_categoria);

        detalleCriterio.setVisibility(View.GONE);

        nombreCriterio.setText(nombrePlato);

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true, boton);

            }
        });

        queFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activaBoton(true, boton);
                dialog.cancel();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                if (!fotoPlato.equals("null")) {

                    Glide.with(contexto)
                            .load(fotoPlato)
                            .error(R.drawable.no_photo)
                            .into(queFoto);

                } else {

                    queFoto.setImageResource(R.drawable.no_photo);
                }



            }
        });

        dialog.show();


    }

}
