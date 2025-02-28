package sarao.digital.lakarta;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Ciudades implements Parcelable {

    String nombre_ciudad;
    Location donde_ciudad;


    public Ciudades(){}

    public Ciudades(Parcel parcel){

        nombre_ciudad=parcel.readString();
        donde_ciudad=Location.CREATOR.createFromParcel(parcel);

    }

    public static final Creator<Ciudades> CREATOR = new Creator<Ciudades>() {
        @Override
        public Ciudades createFromParcel(Parcel in) {
            return new Ciudades(in);
        }

        @Override
        public Ciudades[] newArray(int size) {
            return new Ciudades[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(nombre_ciudad);
        donde_ciudad.writeToParcel(parcel, flags);

    }
}
