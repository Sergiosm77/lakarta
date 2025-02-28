package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;


public class Limitaciones implements Parcelable {

    int cant_categorias;
    int cant_elementos;
    int cant_categorias_menu;
    int cant_elementos_menu;
    String fechaAlta;
    String fechaFin;
    int activo;


    public Limitaciones(){}

    public Limitaciones(Parcel parcel){

        cant_categorias=parcel.readInt();
        cant_elementos=parcel.readInt();
        cant_categorias_menu=parcel.readInt();
        cant_elementos_menu=parcel.readInt();
        fechaAlta=parcel.readString();
        fechaFin=parcel.readString();
        activo=parcel.readInt();

    }


    public static final Parcelable.Creator<Limitaciones> CREATOR = new Parcelable.Creator<Limitaciones>() {
        @Override
        public Limitaciones createFromParcel(Parcel in) {
            return new Limitaciones(in);
        }

        @Override
        public Limitaciones[] newArray(int size) {
            return new Limitaciones[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeInt(cant_categorias);
        parcel.writeInt(cant_elementos);
        parcel.writeInt(cant_categorias_menu);
        parcel.writeInt(cant_elementos_menu);
        parcel.writeString(fechaAlta);
        parcel.writeString(fechaFin);
        parcel.writeInt(activo);



    }
}
