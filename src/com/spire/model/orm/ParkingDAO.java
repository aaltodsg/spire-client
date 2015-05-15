package com.spire.model.orm;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.spire.debug.Debug;
import com.spire.model.struct.Parking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by volodymyr on 02.08.13.
 */
public class ParkingDAO extends BaseDaoImpl<Parking, Integer>{
    protected  ParkingDAO ( ConnectionSource connectionSource, Class<Parking> dataClass ) throws SQLException{
        super (connectionSource, dataClass );
    }

    public List<Parking> getAllParking(String email) throws SQLException{
        return this.queryBuilder().orderBy("parkid", true).where().eq("email", email).query();
    }

    @Override
    public Parking createIfNotExists(Parking data) throws SQLException {
        return super.createIfNotExists(data);
    }

    public boolean isFavoritesParking(Parking parking){
        List<Parking> from_orm = null;
        try {
            from_orm = queryBuilder().where().eq("latitude",parking.getLatitude()+"").and().eq("longitude", parking.getLongitude() + "").and().eq("email",parking.getEmail()).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return from_orm.size() > 0;
    }

    public Parking getParkingForCoordinates(String email, double lat, double lng){
        List<Parking> from_orm = null;
        try {
            from_orm = queryBuilder().where().eq("latitude",lat).and().eq("longitude", lng).and().eq("email",email).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Debug.log("ParkingsDAO",from_orm.size()+"");

        if (from_orm.size() > 0){ return  from_orm.get(0);}
        else return null;
    }

    public void updateParking(ArrayList<Parking> parkings, String email){
        if (parkings.size() > 0){

            for (Parking parking : parkings){
                try {



                    Debug.log("Favorites",parking.getArea());

                    UpdateBuilder<Parking, Integer> builder = this.updateBuilder();
                    builder.where().eq("area",parking.getArea()).and().eq("email",email);
                    builder.updateColumnValue("status",parking.getStatus());
                    builder.updateColumnValue("parkid",parking.getParkid());
                    builder.updateColumnValue("latitude",parking.getLatitude());
                    builder.updateColumnValue("longitude",parking.getLongitude());



                    builder.update();



                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
