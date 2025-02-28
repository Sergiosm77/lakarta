package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;

public class MenuOp implements Parcelable {

    String cod_restaurante;
    String cod_criterio;
    String cod_plato;
    String cod_plato_op;
    String criterio;
    String imagen_criterio;
    String detalle_criterio;
    int cantidad;
    double precio_criterio;
    String[] plato_codigo;
    String[] plato_nombre;
    String[] plato_detalle;
    String[] plato_precio;
    String[] plato_imagen;


    public MenuOp(){}

    public MenuOp(Parcel parcel){

        cod_restaurante=parcel.readString();
        cod_criterio=parcel.readString();
        cod_plato=parcel.readString();
        cod_plato_op=parcel.readString();
        criterio=parcel.readString();
        imagen_criterio=parcel.readString();
        detalle_criterio=parcel.readString();
        cantidad=parcel.readInt();
        precio_criterio=parcel.readDouble();
        plato_codigo=parcel.createStringArray();
        plato_nombre=parcel.createStringArray();
        plato_detalle=parcel.createStringArray();
        plato_precio=parcel.createStringArray();
        plato_imagen=parcel.createStringArray();


}


    public static final Creator<MenuOp> CREATOR = new Creator<MenuOp>() {
        @Override
        public MenuOp createFromParcel(Parcel in) {
            return new MenuOp(in);
        }

        @Override
        public MenuOp[] newArray(int size) {
            return new MenuOp[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(cod_restaurante);
        parcel.writeString(cod_criterio);
        parcel.writeString(cod_plato);
        parcel.writeString(cod_plato_op);
        parcel.writeString(criterio);
        parcel.writeString(imagen_criterio);
        parcel.writeString(detalle_criterio);
        parcel.writeInt(cantidad);
        parcel.writeDouble(precio_criterio);
        parcel.writeStringArray(plato_codigo);
        parcel.writeStringArray(plato_nombre);
        parcel.writeStringArray(plato_detalle);
        parcel.writeStringArray(plato_precio);
        parcel.writeStringArray(plato_imagen);


    }
}
