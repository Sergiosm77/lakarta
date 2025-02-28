package sarao.digital.lakarta;

import android.os.Parcel;
import android.os.Parcelable;

public class Alergenos implements Parcelable {

    String nombre_alergeno;
    String detalle_alergeno;
    String codigo_alergeno;
    String imagen_alergeno;


    public Alergenos(){}

    public Alergenos(Parcel parcel){

        nombre_alergeno=parcel.readString();
        detalle_alergeno=parcel.readString();
        codigo_alergeno=parcel.readString();
        imagen_alergeno=parcel.readString();


    }

    public static final Creator<Alergenos> CREATOR = new Creator<Alergenos>() {
        @Override
        public Alergenos createFromParcel(Parcel in) {
            return new Alergenos(in);
        }

        @Override
        public Alergenos[] newArray(int size) {
            return new Alergenos[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(nombre_alergeno);
        parcel.writeString(detalle_alergeno);
        parcel.writeString(codigo_alergeno);
        parcel.writeString(imagen_alergeno);


    }
}
