package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;

public class Kartas implements Parcelable {

    String cod_restaurante;
    String cod_nivel;
    String cod_nivel_sup;
    String nombre_nivel;
    String imagen_nivel;
    int mostrar_imagen;
    String detalle_nivel;
    String alergenos;
    double precio_nivel;
    int cantidad_nivel;
    int esmenu;
    int destacado;
    int agotado;
    int tipoMenu;
    int orden_nivel;

    String[] cod_subnivel;
    String[] nombre_subnivel;
    String[] imagen_subnivel;
    int[] mostrar_imagen_subnivel;
    String[] detalle_subnivel;
    String[] alergenos_subnivel;
    double[] precio_subnivel;
    int[] orden_subnivel;
    int[] conOpciones;
    int[] destacado_subnivel;
    int[] agotado_subnivel;
    int[] visible;

    public Kartas() {

    }


    public Kartas(Kartas copia){ // ------- HACE UNA COPIA NO VINCULADA

        this.cod_restaurante= copia.cod_restaurante;
        this.cod_nivel= copia.cod_nivel;
        this.cod_nivel_sup= copia.cod_nivel_sup;
        this.nombre_nivel= copia.nombre_nivel;
        this.imagen_nivel= copia.imagen_nivel;
        this.mostrar_imagen= copia.mostrar_imagen;
        this.detalle_nivel= copia.detalle_nivel;
        this.alergenos= copia.alergenos;

        this.precio_nivel= copia.precio_nivel;
        this.cantidad_nivel= copia.cantidad_nivel;
        this.esmenu= copia.esmenu;
        this.destacado= copia.destacado;
        this.agotado= copia.agotado;
        this.tipoMenu= copia.tipoMenu;
        this.orden_nivel= copia.orden_nivel;

        this.cod_subnivel= copia.cod_subnivel;
        this.nombre_subnivel= copia.nombre_subnivel;
        this.imagen_subnivel= copia.imagen_subnivel;
        this.mostrar_imagen_subnivel= copia.mostrar_imagen_subnivel;
        this.detalle_subnivel= copia.detalle_subnivel;
        this.alergenos_subnivel= copia.alergenos_subnivel;
        this.precio_subnivel= copia.precio_subnivel;
        this.conOpciones= copia.conOpciones;
        this.destacado_subnivel= copia.destacado_subnivel;
        this.agotado_subnivel= copia.agotado_subnivel;
        this.visible= copia.visible;
        this.orden_subnivel= copia.orden_subnivel;



    }

    public Kartas(Parcel parcel){

        cod_restaurante=parcel.readString();
        cod_nivel=parcel.readString();
        cod_nivel_sup=parcel.readString();
        nombre_nivel=parcel.readString();
        imagen_nivel=parcel.readString();
        mostrar_imagen=parcel.readInt();
        detalle_nivel=parcel.readString();
        alergenos=parcel.readString();
        precio_nivel=parcel.readDouble();
        cantidad_nivel=parcel.readInt();
        esmenu=parcel.readInt();
        destacado=parcel.readInt();
        agotado=parcel.readInt();
        tipoMenu=parcel.readInt();
        orden_nivel=parcel.readInt();

        cod_subnivel=parcel.createStringArray();
        nombre_subnivel=parcel.createStringArray();
        imagen_subnivel=parcel.createStringArray();
        mostrar_imagen_subnivel=parcel.createIntArray();
        detalle_subnivel=parcel.createStringArray();
        alergenos_subnivel=parcel.createStringArray();
        precio_subnivel=parcel.createDoubleArray();
        conOpciones=parcel.createIntArray();
        destacado_subnivel=parcel.createIntArray();
        agotado_subnivel=parcel.createIntArray();
        orden_subnivel=parcel.createIntArray();

        visible=parcel.createIntArray();



}


    public static final Creator<Kartas> CREATOR = new Creator<Kartas>() {
        @Override
        public Kartas createFromParcel(Parcel in) {
            return new Kartas(in);
        }

        @Override
        public Kartas[] newArray(int size) {
            return new Kartas[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(cod_restaurante);
        parcel.writeString(cod_nivel);
        parcel.writeString(cod_nivel_sup);
        parcel.writeString(nombre_nivel);
        parcel.writeString(imagen_nivel);
        parcel.writeInt(mostrar_imagen);
        parcel.writeString(detalle_nivel);
        parcel.writeString(alergenos);
        parcel.writeDouble(precio_nivel);
        parcel.writeInt(cantidad_nivel);
        parcel.writeInt(esmenu);
        parcel.writeInt(destacado);
        parcel.writeInt(agotado);
        parcel.writeInt(tipoMenu);
        parcel.writeInt(orden_nivel);

        parcel.writeStringArray(cod_subnivel);
        parcel.writeStringArray(nombre_subnivel);
        parcel.writeStringArray(imagen_subnivel);
        parcel.writeIntArray(mostrar_imagen_subnivel);
        parcel.writeStringArray(detalle_subnivel);
        parcel.writeStringArray(alergenos_subnivel);
        parcel.writeDoubleArray(precio_subnivel);
        parcel.writeIntArray(conOpciones);
        parcel.writeIntArray(destacado_subnivel);
        parcel.writeIntArray(agotado_subnivel);
        parcel.writeIntArray(orden_subnivel);
        parcel.writeIntArray(visible);


    }
}
