package com.k.xmlrpc6;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.k.xmlrpc6.Record;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordManager {

    public static final String TABLE_NAME = "records";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BARCODE = "barcode";
    public static final String COLUMN_EMPLOYEE_ID = "employee_id";
    public static final String COLUMN_EMPLOYEE_UID = "employee_uid";
    public static final String COLUMN_TAG= "tag_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DATE_DISPLAYED = "date_displayed";


    public static final  String CREATE_TABLE_RECORD =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_BARCODE + " TEXT," +
                    COLUMN_EMPLOYEE_ID + " INT," +
                    COLUMN_EMPLOYEE_UID + " INT," +
                    COLUMN_TAG + " INT," +
                    COLUMN_DATE + " TEXT," +
                    COLUMN_DATE_DISPLAYED + " TEXT);";

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static RecordManager instance;
    private static MySQLite maBaseSQLite; // notre gestionnaire du fichier SQLite (SQLiteOpenHelper)
    private SQLiteDatabase db;


    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new RecordManager();
            maBaseSQLite = MySQLite.getInstance(context);
        }
    }
    public static synchronized RecordManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(RecordManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }
    // Constructeur
    /*public RecordManager(Context context)
    {
        maBaseSQLite = MySQLite.getInstance(context);
    }*/

     /*public void open()
    {
        //on ouvre la table en lecture/écriture
        db = maBaseSQLite.getWritableDatabase();
    }*/
     public synchronized SQLiteDatabase openDatabase() {
         if(mOpenCounter.incrementAndGet() == 1) {
             // Opening new database
             db = maBaseSQLite.getReadableDatabase();
         }
         return db;
     }
    /*public void close()
    {
        //on ferme l'accès à la BDD
        db.close();
    }*/
    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            db.close();

        }
    }
   /* public long addAnimal(Record animal) {
        // Ajout d'un enregistrement dans la table

        ContentValues values = new ContentValues();
        values.put(KEY_NOM_ANIMAL, animal.getNom_animal());

        // insert() retourne l'id du nouvel enregistrement inséré, ou -1 en cas d'erreur
        return db.insert(TABLE_NAME,null,values);
    }

    public int modAnimal(Record animal) {
        // modification d'un enregistrement
        // valeur de retour : (int) nombre de lignes affectées par la requête

        ContentValues values = new ContentValues();
        values.put(KEY_NOM_ANIMAL, animal.getNom_animal());

        String where = KEY_ID_ANIMAL+" = ?";
        String[] whereArgs = {animal.getId_animal()+""};

        return db.update(TABLE_NAME, values, where, whereArgs);
    }

    public int supAnimal(Record animal) {
        // suppression d'un enregistrement
        // valeur de retour : (int) nombre de lignes affectées par la clause WHERE, 0 sinon

        String where = KEY_ID_ANIMAL+" = ?";
        String[] whereArgs = {animal.getId_animal()+""};

        return db.delete(TABLE_NAME, where, whereArgs);
    }

    public Record getAnimal(int id) {
        // Retourne l'animal dont l'id est passé en paramètre

        Record a=new Record();

        Cursor c = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+KEY_ID_ANIMAL+"="+id, null);
        if (c.moveToFirst()) {
            a.setId_animal(c.getInt(c.getColumnIndex(KEY_ID_ANIMAL)));
            a.setNom_animal(c.getString(c.getColumnIndex(KEY_NOM_ANIMAL)));
            c.close();
        }

        return a;
    }

    public Cursor getAnimaux() {
        // sélection de tous les enregistrements de la table
        return db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
    }*/
   public Record GetLastRecord(){
       String query1 = "SELECT * FROM "+ TABLE_NAME + " WHERE " +COLUMN_ID +" = (SELECT MIN("+ COLUMN_ID +") FROM "+TABLE_NAME+")";
       SQLiteDatabase db = this.openDatabase();
       Cursor cursor = db.rawQuery(query1, null);
       Record newUser = new Record();
       try {
           if (cursor.moveToFirst()) {
               cursor.moveToFirst();
               newUser.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
               newUser.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
               newUser.setBarcode(cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE)));
               newUser.setEmployee_id(cursor.getInt(cursor.getColumnIndex(COLUMN_EMPLOYEE_ID)));
               newUser.setUid(cursor.getInt(cursor.getColumnIndex(COLUMN_EMPLOYEE_UID)));
               newUser.setTag(cursor.getInt(cursor.getColumnIndex(COLUMN_TAG)));
               newUser.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
               newUser.setDate_displayed(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_DISPLAYED)));
           }
       } catch (Exception e) {
           //Log.d("KK", "Error while trying to get posts from database");
       } finally {
           if (cursor != null && !cursor.isClosed()) {
               cursor.close();
           }
       }
       this.closeDatabase();
       return newUser;
   }

    public int getXCount() {
        SQLiteDatabase db = this.openDatabase();
        Cursor c = db.rawQuery("Select * FROM " + TABLE_NAME, null);
        int i = c.getCount();
        c.close();
        this.closeDatabase();
        //Log.i("Number of Records"," :: "+i);
        return i;
    }

    public ArrayList<Record> GetAllRecords(){
        SQLiteDatabase db = this.openDatabase();
        ArrayList<Record> userList = new ArrayList<>();
        String query = "Select * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Record newUser = new Record();
                    newUser.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    newUser.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    newUser.setBarcode(cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE)));
                    newUser.setEmployee_id(cursor.getInt(cursor.getColumnIndex(COLUMN_EMPLOYEE_ID)));
                    newUser.setUid(cursor.getInt(cursor.getColumnIndex(COLUMN_EMPLOYEE_UID)));
                    newUser.setTag(cursor.getInt(cursor.getColumnIndex(COLUMN_TAG)));
                    newUser.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                    newUser.setDate_displayed(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_DISPLAYED)));
                    userList.add(newUser);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            //Log.d("KK", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
                this.closeDatabase();
            }
        }
        return userList;
    }

    public long addRecord(Record student) {
        SQLiteDatabase db = this.openDatabase();
        long result = -1;
        try {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, student.getId());
        values.put(COLUMN_NAME, student.getName());
        values.put(COLUMN_BARCODE, student.getBarcode());
        values.put(COLUMN_EMPLOYEE_ID, student.getEmployee_id());
        values.put(COLUMN_EMPLOYEE_UID, student.getUid());
        values.put(COLUMN_TAG, student.getTag());
        values.put(COLUMN_DATE, student.getDate());
        values.put(COLUMN_DATE_DISPLAYED, student.getDate_displayed());

            result = db.insert(TABLE_NAME,null,values);
        } catch (Exception e) {
            //Log.d("KK", "Error while trying to get posts from database");
        } finally {
            this.closeDatabase();
        }

        return result;
    }

    public Record findRecordWithId(int id) {
        String query = "Select * FROM " + TABLE_NAME + " WHERE" + COLUMN_ID + " = " + id;
        SQLiteDatabase db = this.openDatabase();
        Cursor cursor = db.rawQuery(query, null);
        db.beginTransaction();
        Record record = new Record();
        try {
            if (cursor.moveToFirst()) {
                cursor.moveToFirst();
                record.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                record.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                record.setBarcode(cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE)));
                record.setEmployee_id(cursor.getInt(cursor.getColumnIndex(COLUMN_EMPLOYEE_ID)));
                record.setUid(cursor.getInt(cursor.getColumnIndex(COLUMN_EMPLOYEE_UID)));
                record.setTag(cursor.getInt(cursor.getColumnIndex(COLUMN_TAG)));
                record.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                record.setDate_displayed(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_DISPLAYED)));
            }
        } catch (Exception e) {
            //Log.d("KK", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
                this.closeDatabase();
            }
        }
        return record;
    }

    public int deleteAll() {
        SQLiteDatabase db = this.openDatabase();
        int res = -1;
        try {
            // Order of deletions is important when foreign key relationships exist.
            res = db.delete(TABLE_NAME, null, null);
        } catch (Exception e) {
            //Log.d("KK", "Error while trying to delete all posts and users");
        } finally {
            this.closeDatabase();
        }
        return res;
    }

    public int deleteLastRecord() {
        String sql_delete = "DELETE FROM "+ TABLE_NAME + " WHERE " +COLUMN_ID +" = (SELECT MIN("+ COLUMN_ID +") FROM "+TABLE_NAME+")";
        SQLiteDatabase db = this.openDatabase();
        int res = -1;
        try {
            db.execSQL(sql_delete);
            res = 1;
        } catch (Exception e) {
            //Log.d("KK", "Error while trying to delete all posts and users");
        } finally {
            this.closeDatabase();
        }
        return res;
    }

    public boolean updateTagRecordTagWithId(int ID, int newtag) {
        //Log.d("KKKxx", "updateTagRecordTagWithId: ID " + ID+ " newtag:"+newtag);
        SQLiteDatabase db = this.openDatabase();
        boolean userId = false;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TAG, newtag);
            String  whereArgs[] = {String.valueOf(ID)};
            userId = db.update(TABLE_NAME, values, COLUMN_ID + "=?" , whereArgs) > 0;
            //Log.d("KKKxx", "updateTagRecordTagWithId: " + userId);
        }
        catch (Exception e) {
            //Log.d("KK", "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }
}
