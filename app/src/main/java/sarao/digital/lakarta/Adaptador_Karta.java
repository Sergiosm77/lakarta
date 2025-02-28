package sarao.digital.lakarta;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Adaptador_Karta extends RecyclerView.Adapter<Adaptador_Karta.ViewHolder> {

    int cantidadNivel4;
    int[] elegidoTotalEste,totalAelegir;
    boolean menuCargado;

    int[] scroll;
    Alergenos[] alergenos;
    //static float px;

    String misAlergenos;

    TextView ayuda;

    int pon;

    LayoutInflater inflador;

    Kartas[] niveles3karta;

    private final Kartas kartaPoner;
    private final Context contexto;
    private final View v;
    private final DecimalFormat formato=new DecimalFormat("0.00");

    Ver_Platos verLosPlatos;

    int cantidad;
    double precioFinal;

    BBDD_Helper helper;

    public Adaptador_Karta(Kartas[] niveles3karta, Kartas kartaPoner, Context contexto, View v) {

        this.contexto = contexto;
        this.kartaPoner=kartaPoner;
        this.niveles3karta=niveles3karta;



        this.v=v;

        helper = new BBDD_Helper(contexto);

        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        verLosPlatos=new Ver_Platos(contexto, inflador);

    }

    @NonNull
    @Override
    public Adaptador_Karta.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.barra_platos, parent, false);



        cargaAlergenos();

        cargaMisAlergenos();

        return new Adaptador_Karta.ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final Adaptador_Karta.ViewHolder holder, final int position) {

        holder.itemView.setAlpha(1f);
        holder.itemView.setEnabled(true);

        holder.contenedorAlergenos.removeAllViews();

        holder.alertaAlergeno.setVisibility(View.GONE);
        holder.productoAgotado.setVisibility(View.GONE);

        holder.ponImagen=true;

        LayoutInflater inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


/*
        px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                contexto.getResources().getDisplayMetrics()
        );

 */

        if(position+1!=kartaPoner.cod_subnivel.length+1) {

            //holder.itemView = LayoutInflater.from(contexto).inflate(R.layout.barra_platos, null, false);


            if (!kartaPoner.imagen_subnivel[position].equals("") && kartaPoner.mostrar_imagen_subnivel[position]==1) {

                if(kartaPoner.agotado_subnivel[position]==1){

                    holder.productoAgotado.setVisibility(View.VISIBLE);
                }

                if(kartaPoner.destacado_subnivel[position]==0) {

                    holder.imagenPlatoDestacada.setVisibility(View.GONE);

                    holder.imagenPlatoNormal.setVisibility(View.VISIBLE);

                    holder.plato.setTextSize(0,contexto.getResources().getDimensionPixelSize(R.dimen._11sdp));
                    holder.plato.setTypeface(null, Typeface.NORMAL);

                    Glide.with(v.getContext())
                            .load(kartaPoner.imagen_subnivel[position])
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.no_photo)
                            .into(holder.imagenPlatoNormal);

                }else{

                    holder.imagenPlatoDestacada.setVisibility(View.VISIBLE);
                    holder.imagenPlatoNormal.setVisibility(View.GONE);

                    holder.plato.setTextSize(0,contexto.getResources().getDimensionPixelSize(R.dimen._14sdp));
                    holder.plato.setTypeface(null, Typeface.BOLD);

                    Glide.with(v.getContext())
                            .load(kartaPoner.imagen_subnivel[position])
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.no_photo)
                            .into(holder.imagenPlatoDestacada);

                }

            } else {

                if(kartaPoner.destacado_subnivel[position]==0) {

                    holder.plato.setTextSize(0,contexto.getResources().getDimensionPixelSize(R.dimen._11sdp));

                }else{

                    holder.plato.setTextSize(0,contexto.getResources().getDimensionPixelSize(R.dimen._14sdp));
                }

                holder.imagenPlatoNormal.setVisibility(View.GONE);
                holder.imagenPlatoDestacada.setVisibility(View.GONE);

                holder.ponImagen=false;
            }

            // -------- ALERGENOS -----------

            holder.alerta=false;
            holder.queAlergenos="";

            if (!kartaPoner.alergenos_subnivel[position].equals("")) {

                for (int i = 0; i < kartaPoner.alergenos_subnivel[position].length(); i++) {

                    for (int e = 0; e < Contenedor_Lakarta.alergenos.length; e++) {

                        if (kartaPoner.alergenos_subnivel[position].charAt(i) == Contenedor_Lakarta.alergenos[e].codigo_alergeno.charAt(0)) {

                            if(misAlergenos.contains(String.valueOf(kartaPoner.alergenos_subnivel[position].charAt(i)))){

                                holder.alertaAlergeno.setVisibility(View.VISIBLE);
                                holder.alerta=true;
                                holder.queAlergenos += " "+Contenedor_Lakarta.alergenos[e].nombre_alergeno;

                            }

                            View unidadAlergeno = inflater.inflate(R.layout.alergeno_unidad, null);

                            ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.alergeno);

                            Glide.with(v.getContext())
                                    .load(Contenedor_Lakarta.alergenos[e].imagen_alergeno)
                                    .error(R.drawable.no_photo)
                                    .into(imagenAlergeno);

                            holder.contenedorAlergenos.addView(unidadAlergeno);
                            break;

                        }

                    }

                }
            }

            if(Contenedor_Lakarta.queRestaurante.fN!=0) {
                holder.fondoNombre.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fN);
            }
            if(Contenedor_Lakarta.queRestaurante.fD!=0) {
                holder.fondoDetalle.setBackgroundColor(Contenedor_Lakarta.queRestaurante.fD);
            }

            if(Contenedor_Lakarta.queRestaurante.cN!=0) {
                holder.plato.setTextColor(Contenedor_Lakarta.queRestaurante.cN);
            }

            if(Contenedor_Lakarta.queRestaurante.cD!=0) {
                holder.detalle.setTextColor(Contenedor_Lakarta.queRestaurante.cD);
            }

            if(Contenedor_Lakarta.queRestaurante.cP!=0) {
                holder.precio.setTextColor(Contenedor_Lakarta.queRestaurante.cP);
            }

            holder.plato.setText(kartaPoner.nombre_subnivel[position]);

            if (kartaPoner.precio_subnivel[position] == 0) {

                holder.precio.setText("");

            } else {
                holder.precio.setText(formato.format(kartaPoner.precio_subnivel[position]) + " €");
            }


            holder.detalle.setText(kartaPoner.detalle_subnivel[position]);

            final int cuantos = compruebaPlatoGuardado(kartaPoner.cod_subnivel[position]);

            if (cuantos > 0) {

                holder.cantidadPlatos.setAlpha(1f);

                holder.cantidadPlatos.setText("" + cuantos);

            } else {

                holder.cantidadPlatos.setAlpha(0f);
                holder.cantidadPlatos.setText("");

            }

            if(kartaPoner.agotado_subnivel[position]==0) {

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        activaBoton(false, holder.itemView);

                        if (kartaPoner.conOpciones[position] == 0) {

                            //esPlato(kartaPoner, position, holder.cantidadPlatos,holder.itemView,holder.alerta,holder.queAlergenos,holder.ponImagen);
                            verLosPlatos.esPlato(kartaPoner, position, holder.cantidadPlatos, holder.itemView, holder.alerta, holder.queAlergenos);

                        } else {

                            Kartas[] niveles3poner;

                            int contador = 0;

                            for (int i = 0; i < niveles3karta.length; i++) {

                                if (kartaPoner.cod_subnivel[position].equals(niveles3karta[i].cod_nivel_sup)) {

                                    contador++;
                                }
                            }

                            niveles3poner = new Kartas[contador];

                            contador = 0;

                            for (int i = 0; i < niveles3karta.length; i++) {

                                if (kartaPoner.cod_subnivel[position].equals(niveles3karta[i].cod_nivel_sup)) {

                                    niveles3poner[contador] = niveles3karta[i];

                                    contador++;
                                }
                            }

                            //esMenu(niveles3poner, position, holder.cantidadPlatos,holder.itemView);
                            verLosPlatos.esMenu(kartaPoner, niveles3poner, position, holder.cantidadPlatos, holder.itemView);

                        }


                    }
                });
            }

        }else{

            holder.itemView.setEnabled(false);
            holder.imagenPlatoDestacada.setVisibility(View.GONE);

            holder.itemView.setAlpha(0f);

        }


    }


    @Override
    public int getItemCount() {
        return kartaPoner.cod_subnivel.length+1;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imagenPlatoNormal,imagenPlatoDestacada,alertaAlergeno;
        TextView plato, precio,detalle,cantidadPlatos;
        TextView productoAgotado;
        LinearLayout fondoNombre, fondoDetalle;
        CardView cardPlato;
        LinearLayout contenedorAlergenos;
        boolean alerta;
        boolean ponImagen;
        String queAlergenos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            plato = itemView.findViewById(R.id.nombre_plato);
            precio = itemView.findViewById(R.id.precio_plato);
            detalle = itemView.findViewById(R.id.detalle_plato);
            cantidadPlatos = itemView.findViewById(R.id.cantidad_platos);
            imagenPlatoNormal = itemView.findViewById(R.id.imagen_plato_normal);
            imagenPlatoDestacada = itemView.findViewById(R.id.imagen_plato_destacada);
            cardPlato=itemView.findViewById(R.id.contenedor_cardview);
            contenedorAlergenos=itemView.findViewById(R.id.contenedor_alergenos);
            fondoNombre=itemView.findViewById(R.id.fondo_nombre);
            fondoDetalle=itemView.findViewById(R.id.fondo_detalle);
            alertaAlergeno=itemView.findViewById(R.id.alerta_alergeno);
            productoAgotado=itemView.findViewById(R.id.producto_agotado);

        }
    }

    private void cargaMisAlergenos(){

        SharedPreferences guarda= PreferenceManager.getDefaultSharedPreferences(contexto);

        misAlergenos=guarda.getString("MISALERGENOS","");

    }

    private void esPlato(final Kartas queplato, final int cual, final TextView cuantosPlatos, final View boton, boolean alerta,String queAlergenos,boolean ponerImagen){

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


        if(!cuantosPlatos.getText().toString().equals("")){
            cantidad=Integer.parseInt(cuantosPlatos.getText().toString());
        }else{

            cantidad=0;
        }

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

                float alturaBarra=cuantosPlatos.getHeight();

                int contenidoContador=0;

                if(cantidad>0) {

                    cuantosPlatos.setAlpha(1f);


                    if(cuantosPlatos.getText().toString().equals("")){

                        contenidoContador=0;
                    }else{

                        contenidoContador=Integer.parseInt(cuantosPlatos.getText().toString());
                    }

                    cuantosPlatos.setText(String.valueOf(cantidad));

                    if(cuantosPlatos.getY()!=0 || contenidoContador==0) {

                        cuantosPlatos.setY(-alturaBarra);
                        cuantosPlatos.animate().yBy(alturaBarra);
                    }



                    guardaPlato(queplato.cod_subnivel[cual],cantidad,queplato.nombre_subnivel[cual],queplato.detalle_subnivel[cual],String.valueOf(precio), kartaPoner.cod_restaurante,"2",null);
                    Contenedor_Lakarta.hayCambios=true;

                }else{
                    quitaPlato(queplato.cod_subnivel[cual]);


                    //float alturaContenedor=contenedorPlatos.getHeight();

                    cuantosPlatos.setAlpha(0f);

                    cuantosPlatos.setText(cantidad + "");

                    if(cuantosPlatos.getY()!=-alturaBarra) {

                        cuantosPlatos.setY(0);
                        cuantosPlatos.animate().yBy(-alturaBarra);

                    }


                }
                dialog.cancel();

                ponGastoTotal(kartaPoner.cod_restaurante);



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

                float alturaBarra=cuantosPlatos.getHeight();

                Animation move = new TranslateAnimation(0f,0f,0f,-alturaBarra);
                move.setDuration(300);

                dialog.cancel();

                System.out.println("ELIMINA BARRA");

                move.setAnimationListener(new Animation.AnimationListener(){

                    @Override
                    public void onAnimationStart(Animation animation){}

                    @Override
                    public void onAnimationRepeat(Animation animation){}

                    @Override
                    public void onAnimationEnd(Animation animation){

                        cuantosPlatos.setText("");
                        cuantosPlatos.setAlpha(0f);

                        quitaPlato(queplato.cod_subnivel[cual]);
                        ponGastoTotal(kartaPoner.cod_restaurante);

                    }
                });
                cuantosPlatos.startAnimation(move);

/*

                float alturaBarra=cuantosPlatos.getHeight();

                if(cuantosPlatos.getY()!=-alturaBarra) {

                    cuantosPlatos.setY(0);
                    cuantosPlatos.animate().yBy(-alturaBarra);

                }

                cuantosPlatos.setText("");

                quitaPlato(queplato.cod_subnivel[cual]);
                ponPlatos(kartaPoner.cod_restaurante);
                dialog.cancel();



 */
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




/*
        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                Contenedor_Lakarta.blurview.setVisibility(View.VISIBLE);



                return false;
            }
        });

 */
/*
        alertLayout.getViewTreeObserver().addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
            @Override
            public void onWindowAttached() {


                if(!queplato.imagen_subnivel[cual].equals("null")) {

                    Glide.with(contexto)
                            .load(queplato.imagen_subnivel[cual])
                            .error(R.drawable.plato_standard)
                            .into(imagenPlato);

                }else{

                    imagenPlato.setImageResource(R.drawable.plato_standard);
                }

                if(!queplato.alergenos_subnivel[cual].equals("")){

                    ponAlergenos(contenedorAlergenos, queplato.alergenos_subnivel[cual]);
                }

                Contenedor_Lakarta.blurview.setVisibility(View.VISIBLE);

                System.out.println("LINEAS: "+detallePlato.getLineCount());

                if(detallePlato.getLineCount()>4) {

                    verMas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            if (detallePlato.getMaxLines() == 4 && pon == 0) {

                                System.out.println("ARRIBA TEXTO");

                                params.height = 0;

                                contenedorDetalle.setLayoutParams(params);

                                pon = 1;

                                detallePlato.setMaxLines(20);

                                verMas.setRotation(180f);

                                //constraintSet.connect(R.id.emerg_detalle_plato, ConstraintSet.TOP, R.id.ficha_nombre, ConstraintSet.BOTTOM, 0);
                                //constraintSet.applyTo(foto);

                            } else {

                                System.out.println("ABAJO TEXTO");

                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                                contenedorDetalle.setLayoutParams(params);

                                pon = 0;

                                //constraintSet.connect(R.id.emerg_detalle_plato, ConstraintSet.TOP, R.id.vacio, ConstraintSet.BOTTOM, 0);
                                //constraintSet.applyTo(foto);

                                detallePlato.setMaxLines(4);

                                verMas.setRotation(180f);

                            }

                        }
                    });
                }else{

                    verMas.setVisibility(View.GONE);
                }

            }

            @Override
            public void onWindowDetached() {

                System.out.println("LINEAS: "+detallePlato.getLineCount());

            }


        });

 */

    }

    private void esMenu(final Kartas[] niveles3poner, final int cualNivel2, final TextView cuantosMenus,final View boton){

        final LayoutInflater inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View alertLayout = inflater.inflate(R.layout.emerg_contenedor_menu, null);

        final LinearLayout rutacontenedorMenu=alertLayout.findViewById(R.id.contenedor_menus);
        final ScrollView menusScroll=alertLayout.findViewById(R.id.contenedormenus_scroll);

        TextView nombreCriterio=alertLayout.findViewById(R.id.nombre_menu);
        TextView detalleCriterio=alertLayout.findViewById(R.id.detalle_menu);
        TextView precioMenu=alertLayout.findViewById(R.id.precio_menu);
        final TextView totalMenu=alertLayout.findViewById(R.id.total_menu);
        final TextView cantidadMenu=alertLayout.findViewById(R.id.cantidad_menu);
        TextView eliminaMenu=alertLayout.findViewById(R.id.elimina_menu);
        ayuda=alertLayout.findViewById(R.id.ayuda_plato);
        final TextView masMenu=alertLayout.findViewById(R.id.menu_mas);
        TextView menosMenu=alertLayout.findViewById(R.id.menu_menos);
        final TextView aceptaMenu=alertLayout.findViewById(R.id.acepta_menu);

        nombreCriterio.setText(kartaPoner.nombre_subnivel[cualNivel2]);
        if(kartaPoner.detalle_subnivel[cualNivel2].equals("")){

            detalleCriterio.setVisibility(View.GONE);

        }else {
            detalleCriterio.setText(kartaPoner.detalle_subnivel[cualNivel2]);
        }

        rutacontenedorMenu.setVisibility(View.GONE);

        alertLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                alertLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                Contenedor_Lakarta.blurview.setVisibility(View.VISIBLE);


                return false;
            }
        });

        if(!cuantosMenus.getText().toString().equals("")){
            cantidad=Integer.parseInt(cuantosMenus.getText().toString());
        }else{

            cantidad=0;
        }

        cantidadMenu.setText(""+cantidad);

        final double precio;

        if(kartaPoner.precio_subnivel[cualNivel2]==0){
            if(kartaPoner.precio_nivel!=0){

                precio = kartaPoner.precio_nivel;

            }else{

                precio = 0;
            }
        }else{

            precio = kartaPoner.precio_subnivel[cualNivel2];
        }

        if(cantidad>0){

            precioFinal=precio*cantidad;
            totalMenu.setText(formato.format(precioFinal)+"€");

        }else{

            totalMenu.setVisibility(View.GONE);

            precioFinal=0;
        }



        precioMenu.setText(formato.format(precio)+"€");

        elegidoTotalEste=new int[niveles3poner.length];
        totalAelegir=new int[niveles3poner.length];

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);

        alert.setView(alertLayout);

        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                activaBoton(true,boton);
                Contenedor_Lakarta.blurview.setVisibility(View.GONE);

            }
        });

        menuCargado=false;

        scroll=new int[niveles3poner.length];

        aceptaMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cantidad > 0) {

                    rutacontenedorMenu.setVisibility(View.VISIBLE);

                    menuCargado=true;

                    for (int i = 0; i < niveles3poner.length; i++) {  // INFLA NIVEL 3

                        LinearLayout rutaOpciones = (LinearLayout) inflater.inflate(R.layout.unidad_cat_menu, null);

                        final LinearLayout contendorElementos = rutaOpciones.findViewById(R.id.contenedor_elementos);

                        TextView nombreCriteroOp = rutaOpciones.findViewById(R.id.nombre_criterio);
                        TextView detalleCriterioOp = rutaOpciones.findViewById(R.id.detalle_criterio);
                        ImageView imagenCriterio = rutaOpciones.findViewById(R.id.imagen_nivel);

                        nombreCriteroOp.setText(niveles3poner[i].nombre_nivel);
                        if (!niveles3poner[i].detalle_nivel.equals("")) {
                            detalleCriterioOp.setText(niveles3poner[i].detalle_nivel);
                        } else {
                            detalleCriterioOp.setVisibility(View.GONE);
                        }

                        if(niveles3poner[i].imagen_nivel.equals("") || niveles3poner[i].mostrar_imagen==0){

                            imagenCriterio.setVisibility(View.GONE);

                        }else{

                            Glide.with(v.getContext())
                                    .load(niveles3poner[i].imagen_nivel)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.noimage)
                                    .into(imagenCriterio);
                        }

                        totalAelegir[i] = niveles3poner[i].cantidad_nivel;

                        final int este = i;

                        for (int e = 0; e < niveles3poner[i].nombre_subnivel.length; e++) {  // INFLA NIVEL 4

                            final int estePlato = e;
                            final LinearLayout platoNivel4 = (LinearLayout) inflater.inflate(R.layout.unidad_plato_menu, null);
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

                            nombrePlato_nivel4.setText(niveles3poner[este].nombre_subnivel[estePlato]);

                            alertaAlergeno.setVisibility(View.GONE);

                            // -------- ALERGENOS -----------

                            if (!niveles3poner[i].alergenos_subnivel[e].equals("")) {


                                for (int a = 0; a < niveles3poner[i].alergenos_subnivel[e].length(); a++) {

                                    for (int b = 0; b < Contenedor_Lakarta.alergenos.length; b++) {

                                        if (niveles3poner[i].alergenos_subnivel[e].charAt(a) == Contenedor_Lakarta.alergenos[b].codigo_alergeno.charAt(0)) {

                                            if(misAlergenos.contains(String.valueOf(niveles3poner[i].alergenos_subnivel[e].charAt(a)))){

                                                alertaAlergeno.setVisibility(View.VISIBLE);

                                            }

                                            View unidadAlergeno = inflater.inflate(R.layout.alergeno_unidad, null);

                                            ImageView imagenAlergeno = unidadAlergeno.findViewById(R.id.alergeno);

                                            Glide.with(v.getContext())
                                                    .load(Contenedor_Lakarta.alergenos[b].imagen_alergeno)
                                                    .error(R.drawable.no_photo)
                                                    .into(imagenAlergeno);

                                            contenedorAlergenos.addView(unidadAlergeno);
                                            break;

                                        }

                                    }

                                }
                            }

                            // -------- ALERGENOS -----------
/*
                            if (!niveles3poner[i].alergenos_subnivel[e].equals("")) {

                                for (int a = 0; a < niveles3poner[i].alergenos_subnivel.length; a++) {

                                            if(misAlergenos.contains(String.valueOf(niveles3poner[i].alergenos_subnivel[e].charAt(a)))){

                                                alertaAlergeno.setVisibility(View.VISIBLE);

                                                break;

                                            }

                                }
                            }

 */
                            if(niveles3poner[este].precio_subnivel[estePlato]==0){

                                cajaSuplemento.setVisibility(View.GONE);

                            }else{

                                precioSuplemento.setText(String.valueOf(niveles3poner[este].precio_subnivel[estePlato]));
                            }

                            if(niveles3poner[este].imagen_subnivel[estePlato].equals("") || niveles3poner[este].mostrar_imagen_subnivel[estePlato]==0){

                                contenedorImagenPlato.setVisibility(View.GONE);

                            }else{

                                Glide.with(v.getContext())
                                        .load(niveles3poner[este].imagen_subnivel[estePlato])
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .error(R.drawable.noimage)
                                        .into(imagenPlato);
                            }

                            if (!niveles3poner[i].detalle_subnivel[e].equals("")) {
                                detallePlato_nivel4.setText(niveles3poner[i].detalle_subnivel[e]);
                            } else {
                                detallePlato_nivel4.setVisibility(View.GONE);
                            }
                            //cantidadPlato_nivel4.setText("");
                            cantidadPlato_nivel4.setVisibility(View.GONE);

                            if (niveles3poner[este].visible[estePlato] == 1) {


                                // pon platos guardados

                                int cuantos = compruebaPlatoGuardado(niveles3poner[i].cod_subnivel[estePlato]);

                                if (cuantos > 0) {

                                    elegidoTotalEste[este] = elegidoTotalEste[este] + cuantos;

                                    cantidadPlato_nivel4.setText("" + cuantos);
                                    cantidadPlato_nivel4.setVisibility(View.VISIBLE);
                                    quitaPlato.setVisibility(View.VISIBLE);

                                }

                                //--------------------------

                                imagenPlato.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        verFoto(niveles3poner[este],estePlato,imagenPlato);

                                    }
                                });

                                quitaPlato.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        cantidadNivel4 = Integer.parseInt(cantidadPlato_nivel4.getText().toString());

                                        if (cantidadNivel4 > 0) {

                                            elegidoTotalEste[este]--;
                                            cantidadNivel4--;
                                            TextView cambia = rutacontenedorMenu.getChildAt(este).findViewById(R.id.cantidad_criterio);
                                            cambia.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((totalAelegir[este] * cantidad) - elegidoTotalEste[este]) + " más)");

                                            cantidadPlato_nivel4.setText("" + cantidadNivel4);

                                            if (cantidadNivel4 == 0) {

                                                quitaPlato(niveles3poner[este].cod_subnivel[estePlato]);
                                                cantidadPlato_nivel4.setVisibility(View.GONE);
                                                cantidadPlato_nivel4.setText("");
                                                quitaPlato.setVisibility(View.GONE);

                                            } else {

                                                guardaPlato(niveles3poner[este].cod_subnivel[estePlato], cantidadNivel4, niveles3poner[este].nombre_subnivel[estePlato], niveles3poner[este].detalle_subnivel[estePlato], String.valueOf(niveles3poner[este].precio_subnivel[estePlato]), kartaPoner.cod_restaurante, "4", niveles3poner[este].cod_nivel_sup);

                                            }

                                        }

                                    }
                                });

                                platoNivel4.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        ayuda.setVisibility(View.GONE);

                                        if (cantidad == 0) {

                                            ponAyuda(contexto.getString(R.string.elige_cuantos_menus));

                                        } else {

                                            if (elegidoTotalEste[este] < (totalAelegir[este] * cantidad)) {

                                                if (cantidadPlato_nivel4.getText().toString().equals("")) {

                                                    cantidadNivel4 = 0;
                                                    quitaPlato.setVisibility(View.VISIBLE);

                                                } else {

                                                    cantidadNivel4 = Integer.parseInt(cantidadPlato_nivel4.getText().toString());

                                                }

                                                cantidadNivel4++;
                                                elegidoTotalEste[este]++;
                                                TextView cambia = rutacontenedorMenu.getChildAt(este).findViewById(R.id.cantidad_criterio);
                                                cambia.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((totalAelegir[este] * cantidad) - elegidoTotalEste[este]) + " más)");
                                                if ((totalAelegir[este] * cantidad) - elegidoTotalEste[este] > 0) {

                                                    cambia.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * niveles3poner[este].cantidad_nivel) - elegidoTotalEste[este]) + " más)");
                                                    //cambia.setTextColor(getResources().getColor(R.color.colorRosa3, getActivity().getTheme()));

                                                } else {

                                                    cambia.setText("");

                                                }
                                                cantidadPlato_nivel4.setVisibility(View.VISIBLE);
                                                cantidadPlato_nivel4.setText("" + cantidadNivel4);

                                                guardaPlato(niveles3poner[este].cod_subnivel[estePlato], cantidadNivel4, niveles3poner[este].nombre_subnivel[estePlato], niveles3poner[este].detalle_subnivel[estePlato], String.valueOf(niveles3poner[este].precio_subnivel[estePlato]), kartaPoner.cod_restaurante, "4", niveles3poner[este].cod_nivel_sup);

                                            } else {

                                                ponAyuda("Quita platos para añadir uno nuevo");

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
                        criterio.setText(niveles3poner[i].nombre_nivel);

                        if ((totalAelegir[i] * cantidad) - elegidoTotalEste[i] > 0) {

                            cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * niveles3poner[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

                        } else {

                            cantidadCriterio.setText("");

                        }

                    }

                    // -------------------------------------------

                    aceptaMenu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                            activaBoton(true,boton);

                            int elegido = 0;
                            int total = 0;

                            float alturaBarra = cuantosMenus.getHeight();

                            for (int i = 0; i < elegidoTotalEste.length; i++) {

                                elegido = elegido + (elegidoTotalEste[i]);
                                total = total + (totalAelegir[i] * cantidad);
                            }

                            if (elegido < total) {

                                ponAyuda(contexto.getResources().getString(R.string.faltan_platos));

                                scroll[0]=0;

                                for(int i=0;i<rutacontenedorMenu.getChildCount();i++){

                                    View child = rutacontenedorMenu.getChildAt(i);

                                    TextView cuantos=child.findViewById(R.id.cantidad_criterio);

                                    if(i>0) {
                                        scroll[i] = child.getHeight() + scroll[i - 1];
                                    }

                                    if(!cuantos.getText().equals("")){

                                        menusScroll.scrollTo(0,scroll[i]);
                                        break;
                                    }

                                }

                            } else {

                                if (cantidad > 0) {

                                    cuantosMenus.setAlpha(1f);

                                    int contenidoContador;

                                    if (cuantosMenus.getText().toString().equals("")) {

                                        contenidoContador = 0;
                                    } else {

                                        contenidoContador = Integer.parseInt(cuantosMenus.getText().toString());
                                    }

                                    cuantosMenus.setText(cantidad + "");
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


                                    guardaPlato(kartaPoner.cod_subnivel[cualNivel2], cantidad, kartaPoner.nombre_subnivel[cualNivel2], kartaPoner.detalle_subnivel[cualNivel2], String.valueOf(kartaPoner.precio_subnivel[cualNivel2]), kartaPoner.cod_restaurante, "2", "tiene");
                                    ponGastoTotal(kartaPoner.cod_restaurante);
                                    Contenedor_Lakarta.hayCambios = true;


                                } else {
                                    quitaPlato(kartaPoner.cod_subnivel[cualNivel2]);
                                    ponGastoTotal(kartaPoner.cod_restaurante);

                                    cuantosMenus.setText(cantidad + "");

                                    if (cuantosMenus.getY() != -alturaBarra) {

                                        cuantosMenus.setY(0);
                                        cuantosMenus.animate().yBy(-alturaBarra);

                                    }

                                }

                                dialog.cancel();

                            }

                        }
                    });

                }else{

                    ponAyuda(contexto.getString(R.string.elige_cuantos_menus));
                }
            }
        });


        for(int i=0;i<rutacontenedorMenu.getChildCount();i++) {
            TextView criterio =rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
            TextView cantidadCriterio =rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
            criterio.setText(niveles3poner[i].nombre_nivel);
            if(((cantidad*niveles3poner[i].cantidad_nivel)-elegidoTotalEste[i])>0) {
                cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * niveles3poner[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

            }else{

                cantidadCriterio.setText("");

            }

        }


        masMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                precioFinal=precioFinal+precio;
                cantidad=cantidad+1;
                cantidadMenu.setText(""+cantidad);
                cantidadMenu.setAlpha(0.2f);
                cantidadMenu.setScaleX(0.8f);
                cantidadMenu.setScaleY(0.8f);
                cantidadMenu.animate().scaleX(1f);
                cantidadMenu.animate().scaleY(1f);
                cantidadMenu.animate().alpha(1f);
                totalMenu.setText(formato.format(precioFinal)+"€");
                ayuda.setVisibility(View.GONE);
                totalMenu.setVisibility(View.VISIBLE);

                if(menuCargado) {

                    for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
                        TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
                        TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
                        criterio.setText(niveles3poner[i].nombre_nivel);

                        if ((totalAelegir[i] * cantidad) - elegidoTotalEste[i] > 0) {

                            cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * niveles3poner[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

                        } else {

                            cantidadCriterio.setText("");

                        }

                        //cantidadCriterio.setText("("+getResources().getString(R.string.elige)+" "+cantidad*niveles3poner[i].cantidad_nivel+")");

                    }
                }

            }
        });

        menosMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (cantidad == 1) {

                    totalMenu.setVisibility(View.GONE);
                }

                boolean quitaPlato = false;

                if (menuCargado) {

                    for (int i = 0; i < elegidoTotalEste.length; i++) {

                        if (elegidoTotalEste[i] == cantidad * totalAelegir[i] && cantidad > 0) {

                            quitaPlato = true;

                            break;
                        }

                    }
                }

                if (quitaPlato) {

                    ponAyuda(contexto.getResources().getString(R.string.elimine_platos));


                } else {


                    if (cantidad > 0) {
                        cantidad = cantidad - 1;

                        cantidadMenu.setText(String.valueOf(cantidad));
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

                            totalMenu.setText(formato.format(precioFinal) + "€");

                            if (menuCargado) {

                                for (int i = 0; i < rutacontenedorMenu.getChildCount(); i++) {
                                    TextView criterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.nombre_criterio);
                                    TextView cantidadCriterio = rutacontenedorMenu.getChildAt(i).findViewById(R.id.cantidad_criterio);
                                    criterio.setText(niveles3poner[i].nombre_nivel);

                                    if ((totalAelegir[i] * cantidad) - elegidoTotalEste[i] > 0) {

                                        cantidadCriterio.setText("(" + contexto.getResources().getString(R.string.elige) + " " + ((cantidad * niveles3poner[i].cantidad_nivel) - elegidoTotalEste[i]) + " más)");

                                    } else {

                                        cantidadCriterio.setText("");
                                        //cantidadCriterio.setTextColor(getResources().getColor(R.color.colorVerde,getActivity().getTheme()));

                                    }

                                    //cantidadCriterio.setText("("+getResources().getString(R.string.elige)+" "+cantidad*niveles3poner[i].cantidad_nivel+")");

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

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true,boton);

                float alturaBarra=cuantosMenus.getHeight();

                Animation move = new TranslateAnimation(0f,0f,0f,-alturaBarra);
                move.setDuration(300);

                dialog.cancel();

                move.setAnimationListener(new Animation.AnimationListener(){

                    @Override
                    public void onAnimationStart(Animation animation){}

                    @Override
                    public void onAnimationRepeat(Animation animation){}

                    @Override
                    public void onAnimationEnd(Animation animation){

                        cuantosMenus.setText("");
                        cuantosMenus.setAlpha(0f);
                        quitaPlato(kartaPoner.cod_subnivel[cualNivel2]);
                        for(int i=0;i<niveles3poner.length;i++){

                            for(int e=0;e<niveles3poner[i].cod_subnivel.length;e++) {

                                quitaPlato(niveles3poner[i].cod_subnivel[e]);
                            }
                        }

                        ponGastoTotal(kartaPoner.cod_restaurante);

                    }
                });
                cuantosMenus.startAnimation(move);

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        if(cantidad>0){

            ayuda.setVisibility(View.GONE);

            aceptaMenu.callOnClick();
        }else{

            ponAyuda(contexto.getString(R.string.elige_cuantos_menus));


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

                    total=total+(Double.parseDouble(cursor.getString(5))*Integer.parseInt(cursor.getString(2)));

                }

                Contenedor_Lakarta.botonPedido.setAlpha(1f);
                mover(Contenedor_Lakarta.botonPedido,false, true);
                System.out.println("PONE BOTON PEDIDO");

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
/*
        if(total>0) {

            System.out.println("PONE CONTADOR");


            mover(Contenedor_Lakarta.contador_total,false, true);
            Contenedor_Lakarta.contador_total.setText("Total: " + formato.format(total) + "€");

        }else{


            System.out.println("QUITA CONTADOR");
            mover(Contenedor_Lakarta.contador_total,false, false);

            //Contenedor_Lakarta.contador_total.setAlpha(0f);

        }

 */

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

            System.out.println("DESDE menos a 0");


        }else{

            move = ObjectAnimator.ofFloat(vista, "translationX", 0f,-alturaBarra);
        }

        move.setDuration(300);
        move.start();

/*
        Animation move;

        if(donde){

            //vista.setX(-alturaBarra);

            move = new TranslateAnimation(-alturaBarra,0f,0f,0f);

            System.out.println("DESDE menos a 0");


        }else{

            move = new TranslateAnimation(0f,-alturaBarra,0f,0f);
        }


        move.setDuration(300);

        move.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation){


                if(donde){

                    vista.setAlpha(1f);
                    vista.setVisibility(View.VISIBLE);

                }

                System.out.println("INICIA EN: "+vista.getX());
            }



            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation){



                if(!donde) {
                    vista.setAlpha(0f);
                    vista.setVisibility(View.GONE);

                }

                System.out.println("ESTA EN: "+vista.getX());

            }
        });

        vista.startAnimation(move);

 */

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

    private void ponAyuda(String mensaje){

        ayuda.setVisibility(View.VISIBLE);
        ayuda.setText(mensaje);
        ayuda.setScaleX(1.2f);
        //ayuda.setScaleY(1.2f);
        ayuda.animate().scaleX(1f);
        //ayuda.animate().scaleY(1f);


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

    private void verFoto(final Kartas criterio,final int cual, final View boton){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);


        inflador = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View alertLayout = inflador.inflate(R.layout.ver_foto, null);

        final ImageView queFoto = alertLayout.findViewById(R.id.ver_foto);
        TextView nombreCriterio=alertLayout.findViewById(R.id.nombre_categoria);
        TextView detalleCriterio=alertLayout.findViewById(R.id.detalle_categoria);


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
                activaBoton(true, boton);

            }
        });

        queFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Contenedor_Lakarta.blurview.setVisibility(View.GONE);
                activaBoton(true, boton);
                dialog.cancel();

            }
        });


        nombreCriterio.setText(criterio.nombre_subnivel[cual]);

        if(criterio.detalle_nivel.equals("")){

            detalleCriterio.setVisibility(View.GONE);
        }else {

            detalleCriterio.setText(criterio.detalle_subnivel[cual]);
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                if (!criterio.imagen_subnivel[cual].equals("") && criterio.mostrar_imagen_subnivel[cual]!=0) {

                    Glide.with(contexto)
                            .load(criterio.imagen_subnivel[cual])
                            .error(R.drawable.no_photo)
                            .into(queFoto);

                } else {

                    queFoto.setImageResource(R.drawable.no_photo);
                }



            }
        });

        dialog.show();


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

    private void activaBoton(boolean activar, View boton){

        if(activar){

            boton.setEnabled(true);
            boton.setAlpha(1f);
        }else{

            boton.setEnabled(false);
            boton.setAlpha(0.4f);

        }

    }

    private void mueveVista(AlertDialog vista){

        System.out.println("MOVIENDO");



        //vista.animate().scaleX(1f).setDuration(300);
        //vista.animate().scaleY(1f).setDuration(300);


    }



}
