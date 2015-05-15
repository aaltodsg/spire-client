package com.spire.model.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.spire.model.struct.Parking;

import java.sql.SQLException;

/**
 * Created by volodymyr on 02.08.13.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName ( );
    private static final String DATABASE_NAME = "spire.db";
    private static final int DATABASE_VERSION = 1;

    private ParkingDAO mParkingDAO = null;

    public DatabaseHelper ( Context context ){
        super ( context, DATABASE_NAME, null, DATABASE_VERSION );
    }


    @Override
    public void onCreate ( SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource ) {

        try {
            TableUtils.createTable ( connectionSource, Parking.class );

        }catch ( SQLException e ){
            Log.e ( TAG, "error creating DB " + DATABASE_NAME );
            throw new RuntimeException ( e );
        }
    }

    @Override
    public void onUpgrade ( SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVer, int newVer ) {
        try {
            TableUtils.dropTable(connectionSource, Parking.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        }
        catch (SQLException e){
            Log.e(TAG,"error upgrading db "+DATABASE_NAME+"from ver "+ oldVer);
            throw new RuntimeException(e);
        }
    }

    public ParkingDAO getmParkingDAO() throws SQLException{
        if(mParkingDAO == null){
            mParkingDAO = new ParkingDAO(getConnectionSource(), Parking.class);
        }
        return mParkingDAO;
    }


    @Override
    public void close() {
        super.close();

        mParkingDAO = null;
    }
}
