package com.example.enter_slice_assignment.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.enter_slice_assignment.database.enitities.MyLocation

class LocationDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "location_database"
        private const val LOCATION_TABLE = "location_table"

        private val ID: String = "id"
        private val LAT: String = "lat"
        private val LONG: String = "long"
        private val ADDRESS: String = "address"

        private val CREATE =
            "CREATE TABLE $LOCATION_TABLE ($ID INTEGER PRIMARY KEY AUTOINCREMENT, $LAT TEXT , $LONG TEXT , $ADDRESS TEXT )"

    }

    private val _liveLocationLiveData = MutableLiveData<List<MyLocation>>()

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $LOCATION_TABLE")
        db?.execSQL(CREATE)

    }

    fun insertData(myLocation: MyLocation){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put(LAT, myLocation.lat)
        values.put(LONG, myLocation.long)
        values.put(ADDRESS, myLocation.address)

        db.insert(LOCATION_TABLE, null, values)

        db.close()

    }

    fun getLiveLocation(): LiveData<List<MyLocation>>{

        val db = this.readableDatabase

        val locationList = ArrayList<MyLocation>()

        val cursor = db.rawQuery("SELECT * FROM $LOCATION_TABLE", null)

        if(cursor.columnCount>0){

            while (cursor.moveToNext()){

                val lat = cursor.getString(cursor.getColumnIndexOrThrow(LAT))
                val long = cursor.getString(cursor.getColumnIndexOrThrow(LONG))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS))

                val myLocation = MyLocation(lat, long, address)

                locationList.add(myLocation)
            }
        }

        _liveLocationLiveData.postValue(locationList)

        cursor.close()
        return _liveLocationLiveData
    }
}