package sarao.digital.lakarta;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BBDDmiskartas {


    public BBDDmiskartas() {}

    /* Inner class that defines the table contents */
    // public static class Estructura_BBDD implements BaseColumns {
    static final String TABLE_NAME = "kartasGuardadas";
    static final String NOMBRE_COLUMNA1 = "Id";
    static final String NOMBRE_COLUMNA2 = "cod_plato";
    static final String NOMBRE_COLUMNA3 = "cantidad";
    static final String NOMBRE_COLUMNA4 = "nombre";
    static final String NOMBRE_COLUMNA5 = "detalle";
    static final String NOMBRE_COLUMNA6 = "precio";
    static final String NOMBRE_COLUMNA7 = "cod_restaurante";
    static final String NOMBRE_COLUMNA8 = "nivel";
    static final String NOMBRE_COLUMNA9 = "cod_sup";

//  -------------- Metodo de operaciones

    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BBDDmiskartas.TABLE_NAME + " (" +
                    BBDDmiskartas.NOMBRE_COLUMNA1 + " INTEGER PRIMARY KEY," +
                    BBDDmiskartas.NOMBRE_COLUMNA2 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA3 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA4 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA5 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA6 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA7 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA8 + " TEXT," +
                    BBDDmiskartas.NOMBRE_COLUMNA9 + " TEXT)";

    static final String SQL_DELETE_ENTRIES =  // Elimina la tabla si ya existiese una con el mismo nombre
            "DROP TABLE IF EXISTS " + BBDDmiskartas.TABLE_NAME;


}

class BBDD_Helper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1; // Informa de la version de esta base de datos, por si queremos informar
    private static final String DATABASE_NAME = "misKartas.db";

    public BBDD_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BBDDmiskartas.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(BBDDmiskartas.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
