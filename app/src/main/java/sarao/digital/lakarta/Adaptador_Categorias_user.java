package sarao.digital.lakarta;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class Adaptador_Categorias_user extends RecyclerView.Adapter<Adaptador_Categorias_user.ViewHolder> {

    private Kartas kartaPoner;
    private Context contexto;
    private View v;
    private DecimalFormat formato=new DecimalFormat("0.00");

    int cantidad;
    double precioFinal;

    public Adaptador_Categorias_user(Kartas kartaPoner, Context contexto, View v) {

        this.contexto = contexto;
        this.kartaPoner=kartaPoner;


        this.v=v;

    }

    @NonNull
    @Override
    public Adaptador_Categorias_user.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.barra_platos,parent,false);

        return new Adaptador_Categorias_user.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final Adaptador_Categorias_user.ViewHolder holder, final int position) {

        if (!kartaPoner.imagen_subnivel[position].equals("null")) {
            Glide.with(v.getContext())
                    .load(kartaPoner.imagen_subnivel[position])
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.noimage)
                    .into(holder.imagenPlato);
        }else{

            holder.cardPlato.setVisibility(View.GONE);
        }


        holder.plato.setText(kartaPoner.nombre_subnivel[position]);
        holder.plato.setText(kartaPoner.nombre_subnivel[position]);
        holder.precio.setText(formato.format(kartaPoner.precio_subnivel[position]) + " €");
        holder.detalle.setText(kartaPoner.detalle_subnivel[position]);

        //final int cuantos = fragment_karta.compruebaPlatoGuardado(kartaPoner.cod_subnivel[position]);

        int cuantos=0;

        if (cuantos > 0) {

            holder.cantidadPlatos.setText("" + cuantos);

        } else {

            holder.cantidadPlatos.setAlpha(0f);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(kartaPoner.conOpciones[position]==0){

                    esPlato(kartaPoner, position, holder.cantidadPlatos);
                }else{


                }
                System.out.println(" CLICK");

            }
        });


    }


    @Override
    public int getItemCount() {
        return kartaPoner.cod_subnivel.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imagenPlato;
        TextView plato, precio,detalle,cantidadPlatos;
        CardView cardPlato;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            plato = itemView.findViewById(R.id.nombre_plato);
            precio = itemView.findViewById(R.id.precio_plato);
            detalle = itemView.findViewById(R.id.detalle_plato);
            cantidadPlatos = itemView.findViewById(R.id.cantidad_platos);
            imagenPlato = itemView.findViewById(R.id.imagen_plato);
            cardPlato=itemView.findViewById(R.id.contenedor_cardview);

        }
    }

    private void esPlato(final Kartas queplato, final int cual, final TextView cuantosPlatos){

        //InputMethodManager introduce = (InputMethodManager) v.getSystemService(v.INPUT_METHOD_SERVICE);
        //introduce.hideSoftInputFromWindow(v.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_FORCED);



        LayoutInflater inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater.inflate(R.layout.emerg_cuantos_platos, null);

        TextView nombrePlato=alertLayout.findViewById(R.id.emerg_nombre_plato);
        TextView detallePlato=alertLayout.findViewById(R.id.emerg_detalle_plato);
        TextView precioPlato=alertLayout.findViewById(R.id.emerg_precio_plato);
        final TextView cantidadPlatos=alertLayout.findViewById(R.id.emerg_cantidad);
        final TextView precioTotal=alertLayout.findViewById(R.id.emerg_precio_total);
        ImageView imagenPlato=alertLayout.findViewById(R.id.emerg_imagen_plato);
        ImageView eliminaPlato=alertLayout.findViewById(R.id.elimina_plato);
        ImageView masPlato=alertLayout.findViewById(R.id.plato_mas);
        ImageView menosPlato=alertLayout.findViewById(R.id.plato_menos);
        TextView aceptaPlato=alertLayout.findViewById(R.id.acepta_plato);
        TextView cancelaPlato=alertLayout.findViewById(R.id.cancela_plato);
        final ConstraintLayout foto=alertLayout.findViewById(R.id.contenedor_foto_plato);

        //final double precio = Double.parseDouble(queplato.precio_subnivel[cual]);
        final double precio = queplato.precio_subnivel[cual];

        if(!cuantosPlatos.getText().toString().equals("")){
            cantidad=Integer.parseInt(cuantosPlatos.getText().toString());
        }else{

            cantidad=0;
        }

        cantidadPlatos.setText(""+cantidad);

        if(cantidad>0){

            precioFinal=precio*cantidad;
            precioTotal.setText(contexto.getResources().getString(R.string.total)+" "+formato.format(precioFinal)+"€");

        }else{

            precioTotal.setAlpha(0f);

            precioFinal=0;
        }

        masPlato.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                precioFinal=((double)Math.round(precioFinal * 100d) / 100d)+precio;

                precioTotal.setText(contexto.getResources().getString(R.string.total)+" "+formato.format(precioFinal)+"€");

                if(cantidad==0) {

                    float alto=foto.getHeight();
                    float alturaPrecio=precioTotal.getHeight();
                    precioTotal.setAlpha(1f);
                    precioTotal.setY(alto);
                    precioTotal.animate().yBy(-alturaPrecio).setDuration(100);

                }

                cantidad=cantidad+1;
                cantidadPlatos.setText(""+cantidad);
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

                if(cantidad==1){

                    float alto=foto.getHeight();
                    float alturaPrecio=precioTotal.getHeight();
                    precioTotal.setY(alto-alturaPrecio);
                    precioTotal.animate().yBy(alturaPrecio).setDuration(100);

                }

                if(precioFinal>0){

                    precioFinal=((double)Math.round(precioFinal * 100d) / 100d)-precio;
                    if(cantidad>0){
                        cantidad=cantidad-1;
                    }
                    cantidadPlatos.setText(""+cantidad);
                    cantidadPlatos.setAlpha(0.2f);
                    cantidadPlatos.animate().alpha(1f);
                    cantidadPlatos.setScaleX(1.2f);
                    cantidadPlatos.setScaleY(1.2f);
                    cantidadPlatos.animate().scaleX(1f);
                    cantidadPlatos.animate().scaleY(1f);
                    precioTotal.setText(contexto.getResources().getString(R.string.total)+" "+formato.format(precioFinal)+"€");

                }

            }
        });

        nombrePlato.setText(queplato.nombre_subnivel[cual]);
        detallePlato.setText(queplato.detalle_subnivel[cual]);
        precioPlato.setText(formato.format(precio)+"€");

        if(!queplato.imagen_subnivel[cual].equals("null")) {

            Picasso.get()
                    .load(queplato.imagen_subnivel[cual])
                    .error(R.drawable.no_photo)
                    .into(imagenPlato);

        }else{

            imagenPlato.setImageResource(R.drawable.no_photo);
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);

        final AlertDialog dialog = alert.create();

        aceptaPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float alturaBarra=cuantosPlatos.getHeight();

                if(cantidad>0) {

                    cuantosPlatos.setAlpha(1f);

                    int contenidoContador;
                    if(cuantosPlatos.getText().toString().equals("")){

                        contenidoContador=0;
                    }else{

                        contenidoContador=Integer.parseInt(cuantosPlatos.getText().toString());
                    }

                    cuantosPlatos.setText(cantidad + "");

                    if(cuantosPlatos.getY()!=Math.round(fragment_karta.px) || contenidoContador==0) {

                        cuantosPlatos.setY(-alturaBarra+Math.round(fragment_karta.px));
                        cuantosPlatos.animate().yBy(alturaBarra);
                    }

                    //guardaPlato(queplato.cod_subnivel[cual],cantidad,queplato.nombre_subnivel[cual],queplato.detalle_subnivel[cual],String.valueOf(queplato.precio_subnivel[cual]), kartaPoner.cod_restaurante,"2",null);
                    //ponPlatos(kartaPoner.cod_restaurante);
                    Contenedor_Lakarta.hayCambios=true;

                }else{
                    //quitaPlato(queplato.cod_subnivel[cual]);
                    //ponPlatos(kartaPoner.cod_restaurante);

                    //float alturaContenedor=contenedorPlatos.getHeight();

                    //cuantosPlatos.setAlpha(0f);

                    cuantosPlatos.setText(cantidad + "");

                    if(cuantosPlatos.getY()!=-alturaBarra) {

                        cuantosPlatos.setY(0);
                        cuantosPlatos.animate().yBy(-alturaBarra);

                    }

                }
                dialog.cancel();

            }
        });

        cancelaPlato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();

            }
        });

        eliminaPlato.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                float alturaBarra=cuantosPlatos.getHeight();

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

                        cuantosPlatos.setText("");
                        cuantosPlatos.setAlpha(0f);

                        //quitaPlato(queplato.cod_subnivel[cual]);
                        //ponPlatos(kartaPoner.cod_restaurante);

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
        dialog.show();

    }


}
