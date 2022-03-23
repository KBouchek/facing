package com.k.xmlrpc6;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

import static java.util.Collections.emptyMap;

import com.k.xmlrpc6.Preferences;
import com.k.xmlrpc6.RecordManager;
import com.k.xmlrpc6.Record;


public class Settings extends AppCompatActivity {
    /*    final String url = "https://facingtoto.easypme.com",
            db_name = "facingtoto";*/
    final String url = "https://www.facingtahiti.com",
            db_name = "facingtahiti";

     String username_pref,password_pref;
     int uid_pref,employeeid;
     EditText usernameEdit,passwordEd,uidEdit ,employeeidEdit;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_settings);
         Toolbar toolbar = findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);

         FloatingActionButton fab = findViewById(R.id.fab);
         //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         usernameEdit = findViewById(R.id.usernameEdit);
         passwordEd = findViewById(R.id.passwordEdit);
         uidEdit  = findViewById(R.id.uidEdit);
         employeeidEdit = findViewById(R.id.employeeidEdit);
         this.setTitle("Réglages utilisateur");


         Preferences yourPrefrence = Preferences.getInstance(getApplicationContext());
         username_pref = yourPrefrence.getStringData("userName");
         password_pref = yourPrefrence.getStringData("passwd");
         employeeid = yourPrefrence.getIntData("employee_id");
         uid_pref = yourPrefrence.getIntData("uid");

         boolean c = (username_pref != "" && password_pref != "" && employeeid > 0 && uid_pref > 0);
         getSupportActionBar().setDisplayHomeAsUpEnabled(c);

         usernameEdit.setText(username_pref);
         passwordEd.setText(password_pref);
         //passwordEdit.setText("");
         if(uid_pref > 0) {uidEdit.setText(String.valueOf(uid_pref));}
         if(employeeid > 0) {employeeidEdit.setText(String.valueOf(employeeid));}

         fab.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 int num_records = RecordManager.getInstance().getXCount();
                 if(num_records > 0){
                     messageAlertVideRecord();
                     return;
                 }
                 /* sauvegarde des infos */
                verifie();
                /* retour
                Intent intent=new Intent();
                setResult(1,intent);
                intent.putExtra("user","karim");
                intent.putExtra("passwd","passwd");
                intent.putExtra("employee_id",159);
                //intent.putExtra("uid",15);
                finish();//finishing activity*/
            }
        });
    }
    @Override
    public void onBackPressed(){
        // Add data to your intent
        if(username_pref.contentEquals("") ||
                password_pref.contentEquals("") ||
                uid_pref < 0  ||
                employeeid < 0) {return;}
        Intent intent = new Intent();
        setResult(0,intent);
        finish();//finishing activity
    }


    public boolean verifie(){
      if (isEmpty(usernameEdit) || isEmpty(passwordEd)|| isEmpty(employeeidEdit)) {
          wrongAuth();
          return false;
      }
       boolean isDigits = TextUtils.isDigitsOnly(employeeidEdit.getText().toString());
       if(!isDigits){  wrongAuth();  return false;  }

        username_pref = usernameEdit.getText().toString();
        password_pref = passwordEd.getText().toString();
        employeeid = Integer.valueOf(employeeidEdit.getText().toString());

        if(username_pref == "" || password_pref == "" || employeeid <= 0) {wrongAuth(); return false;}

        XMLRPCCallback listener = new XMLRPCCallback() {
            public void onResponse(long id, Object result) {
                Log.d("KKKxx",String.valueOf(id) + " " +result.toString() );
                //Log.d("KKKxx","onResponse ok : "+result.toString() );
                int i = 0;
                if(result != null && result.toString() != "false"){
                    if(Integer.valueOf(result.toString()) > 0){
                        final int uidx = Integer.valueOf(result.toString());
                        //final int emp_id = Integer.valueOf(employeeidEdit_text);
                        i = uidx;
                        //Log.d("KKKxx","onResponse ok : "+result.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //setTitle("Autentification réussie");
                                //uidEdit.setText(String.valueOf(uidx));
                                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                setPreferencesUser(uidx);
                            }
                        });

                    }
                }
                if(i <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wrongAuth();
                        }
                    });
                }
            }
            public void onError(long id, XMLRPCException error) {
                Log.d("KKKxx",error.getMessage() );
                //Log.d("KKKxx",String.valueOf(id) + " " +result.toString() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wrongAuth();
                    }
                });

            }
            public void onServerError(long id, XMLRPCServerException error) {
                // Handling an error response from the server
                Log.d("KKKxx",error.getMessage() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wrongAuth();
                    }
                });

            }
        };

        XMLRPCClient client = null;
        try {
            client = new XMLRPCClient(new URL(String.format("%s/xmlrpc/2/common", url)));
            //client.callAsync(listener, "authenticate", asList(
            //      db, username, password, emptyMap()));
            long id = client.callAsync(listener, "authenticate",db_name, username_pref, password_pref,emptyMap());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setPreferencesUser(int user_id) {
        //this.setTitle("Autentification réussie");
        //uidEdit.setText(String.valueOf(user_id));
        setTitle("Autentification réussie");
        uidEdit.setText(String.valueOf(user_id));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Preferences yourPrefrence = Preferences.getInstance(getApplicationContext());
        yourPrefrence.saveStringData("userName", username_pref);
        yourPrefrence.saveStringData("passwd", password_pref);
        yourPrefrence.saveIntData("uid", user_id);
        yourPrefrence.saveIntData("employee_id", employeeid);

        final String username_pref_final = username_pref;
        final String password_pref_final = password_pref;
        final int uidfinal = user_id;
        final int employeeid_final = employeeid;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();

                setResult(1,intent);
                intent.putExtra("user",username_pref_final);
                intent.putExtra("passwd",password_pref_final);
                intent.putExtra("employee_id",employeeid_final);
                intent.putExtra("uid",uidfinal);

                finish();//finishing activity
            }
        }, 1000);
    }

    public void messageAlertVideRecord(){
        new AlertDialog.Builder(Settings.this)
                .setTitle("Envoyez les Scans enregistrés")
                .setMessage("Veuillez envoyer les Scans restants avant de changer d'utilisateur")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }


    public void wrongAuth() {
        new AlertDialog.Builder(Settings.this)
                .setTitle("Autentification erronée")
                .setMessage("Veuillez indiquer vos identifiants et votre numéro d'employé")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }


}
