package com.k.xmlrpc6;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.prefs.Preferences;

import com.k.xmlrpc6.Preferences;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Record> recordlist;
    public  CustomListAdapter adapter;
    ListView lv;
    private EditText barcode_edittext;
    public ProgressBar loading_spinner;
    public TextView infoTv;
    public Button scanBtn;
    public TextView timerinfos,textView2;
    public FloatingActionButton fabsend;

    public CountDownTimer timer;
    public MyCountDownTimer mycounter;
    public int frequence_check = 10;
    //private boolean status;
    //private Timer timer;
    //private TimerTask timerTask;
    final Handler handler = new Handler();
    public boolean is_running = false;
    public boolean user_is_away = false;

    public boolean timer_should_run = false;

    public boolean database_access = false;

     /*final String url = "https://facingtoto.easypme.com",
            db_name = "facingtoto";*/
   final String url = "https://www.facingtahiti.com",
            db_name = "facingtahiti";

    String username_pref, password_pref;
    int uid_pref, employeeid;


    public int counter;

    final public String TAG = "KK";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Preferences yourPrefrence = Preferences.getInstance(getApplicationContext());

     /* yourPrefrence.saveStringData("userName", "");
        yourPrefrence.saveStringData("passwd", "");
        yourPrefrence.saveIntData("uid", -1);
        yourPrefrence.saveIntData("employee_id", -1); */

        username_pref = yourPrefrence.getStringData("userName");
        password_pref = yourPrefrence.getStringData("passwd");
        uid_pref = yourPrefrence.getIntData("uid");
        employeeid = yourPrefrence.getIntData("employee_id");



        final String TAG = "KK";
        this.setTitle("Facing Tahiti: Scan & Send");
        lv = (ListView) findViewById(R.id.user_list);
        barcode_edittext = findViewById(R.id.barcode_edittext);
        barcode_edittext.setEnabled(false);
        fabsend = findViewById(R.id.fab);
        fabsend.setVisibility(View.INVISIBLE);
        loading_spinner = findViewById(R.id.loading_spinner);
        loading_spinner.setVisibility(View.INVISIBLE);
        infoTv = findViewById(R.id.infoTv);
        textView2 = findViewById(R.id.textView2);
        textView2.setText("Timer");
        infoTv.setText("");
        timerinfos = findViewById(R.id.timerinfos);
        scanBtn = findViewById(R.id.scanBtn);
        counter = 0;
        recordlist = new ArrayList<Record>();
        //mycounter = new MyCountDownTimer(frequence_check*1000, 1000);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new UpdateRecordList().execute();
                //Log.d(TAG, "onClick received in scanBtn OnClickListener");
                handleScanBtnClick();
                //recordlist = RecordManager.getInstance().GetAllRecords();
                //Log.d(TAG,String.valueOf(recordlist.size()));
            }
        });
        RecordManager.initializeInstance(getApplicationContext());
        //RecordManager.getInstance().deleteAll();
        maj_adapter();

        timer = new CountDownTimer(600000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                handleTimerTick(millisUntilFinished);
            }
            public void onFinish() {
               // mTextField.setText("done!");
                handleFinishTimer();
            }
        };

        if(username_pref == "" ||
                password_pref == ""||
                uid_pref < 0  ||
                employeeid < 0) {
            database_access = true;
            handleGoSettings();
            return;
        }
        check();
    } // fin onCreate


    ///////////////////// Overrides  ///////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            handleGoSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {

    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            barcode_edittext.setError(null);
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onResume() {
        super.onResume();
        user_is_away = false;
        //Log.d(TAG, "onResume invoked");
    }
    @Override
    protected void onPause() {
        super.onPause();
        user_is_away = true;
        //Log.d(TAG, "onPause invoked");
    }
    @Override
    protected void onStop() {
        super.onStop();
        //Log.d(TAG, "onStop invoked");
    }
    @Override
    protected void onDestroy() {
        //Log.d(TAG, "onDestroy invoked");
        super.onDestroy();
        RecordManager.getInstance().closeDatabase();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        user_is_away = false;
        //Log.d(TAG, "onActivityResult invoked with requestCode " + requestCode + ", resultCode " + resultCode + ", data " + data);
        if (requestCode == 1) { handleBackFromSettings(resultCode, data);}
        if (requestCode == 2) { handleBackFromScan(resultCode, data);}
        /* retour autentification */

        /* retour codebar */
        if (data != null && requestCode == 2) {
            //Intent iin= getIntent();
            //Bundle b = data.getExtras();
            if (data.hasExtra("CODE")) {
                String result_code = data.getStringExtra("CODE");
                if (!result_code.equals("")) {
                    ////Log.d(TAG,"onActivityResult received code  "+result_code);
                    /* loading_spinner.setVisibility(View.VISIBLE);
                    barcode_edittext.setText(result_code);
                    addStudent();
                    searchIdforBarCode(result_code);*/
                }
            }
        }
    }
    ////////////////////  fin Override  //////////////////////


    private class UpdateRecordList extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            Record record = RecordManager.getInstance().GetLastRecord();
            String cc = String.valueOf(record.getId());

            return cc;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPreExecute();
            //Log.d(TAG, "onPostExecute UpdateRecordList");
            //scanBtn.setText(result);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.d(TAG, "onPreExecute UpdateRecordList");
            //scanBtn.setEnabled(false);
        }
        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    ////////////* gestion actions Settings *//////////////
    public void handleGoSettings() {
        //Log.d(TAG, "handleGoSettings");
        //stopTimer();
        //stopTimer();
        if(database_access) {
            user_is_away = true;
            Intent setting_intend = new Intent(MainActivity.this, Settings.class);
            startActivityForResult(setting_intend, 1);
        } else {
            Toast.makeText(this,"Cette action n'est pas permise pour l'instant", Toast.LENGTH_SHORT).show();
        }
    }
    public void handleBackFromSettings(int resultCode, Intent data) {
        //Log.d(TAG, "handleBackFromSettings with resultCode " + resultCode + " data " + data);
        if(!database_access) return;
        if(resultCode == 0){
            /* retour sans changement */
            //Log.d(TAG, "BackFromSettings sans changement" + resultCode + " data " + data);
        }
        if(resultCode == 1){
            /* retour avec changements */
            //Log.d(TAG, "BackFromSettings retour avec changements");
             if (data != null) {
               if (data.hasExtra("user") &&
                       data.hasExtra("passwd")&&
                       data.hasExtra("employee_id")&&
                       data.hasExtra("uid")) {
                    String result_code = data.getStringExtra("user");

                   //Log.d(TAG, "BackFromSettings user " + data.getStringExtra("user"));
                   //Log.d(TAG, "BackFromSettings passwd " + data.getStringExtra("passwd"));
                   //Log.d(TAG, "BackFromSettings employee_id " + String.valueOf(data.getIntExtra("employee_id",-1)));
                   //Log.d(TAG, "BackFromSettings uid " + String.valueOf(data.getIntExtra("uid",-1)));
                   Toast.makeText(this, "Bienvenue "+result_code, Toast.LENGTH_LONG).show();

                   username_pref = data.getStringExtra("user");
                   password_pref = data.getStringExtra("passwd");
                   employeeid = data.getIntExtra("employee_id",-1);
                   uid_pref = data.getIntExtra("uid",-1);

                   check();
                  /*  Preferences yourPrefrence = Preferences.getInstance(getApplicationContext());
                   username_pref = yourPrefrence.getStringData("userName");
                   password_pref = yourPrefrence.getStringData("passwd");
                   uid_pref = yourPrefrence.getIntData("uid");
                   employeeid = yourPrefrence.getIntData("employee_id");
                    if (result_code.equals("ok")) {
                        //Log.d(TAG,"onActivityResult received code  "+result_code);
                   Preferences yourPrefrence = Preferences.getInstance(getApplicationContext());
                    username_pref = yourPrefrence.getStringData("userName");
                    password_pref = yourPrefrence.getStringData("passwd");
                    uid_pref = yourPrefrence.getIntData("uid");
                    employeeid = yourPrefrence.getIntData("employee_id");
                    Toast.makeText(this, "Bienvenue "+username_pref, Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        }
    }

    ////////////* gestion actions SCAN *//////////////
    public void handleScanBtnClick() {
        //Log.d(TAG, "handleScanBtnClick");

        //reStartTimer();
        //if(timer_should_run) {reStartTimer(); timer_should_run = false;}
        //else {stopTimer(); timer_should_run = true;}
        if(database_access) {
            user_is_away = true;
            empeche();
            Intent intent = new Intent(MainActivity.this, ScanCode.class);
            startActivityForResult(intent, 2);// Activity is started with requestCode 2*/
        } else {
            Toast.makeText(this,"Cette action n'est pas permise pour l'instant", Toast.LENGTH_SHORT).show();
        }
    }
    public void handleBackFromScan(int resultCode, Intent data) {

        //Log.d(TAG, "handleBackFromScan with resultCode " + resultCode + " data " + data);
        if(resultCode == 0){
            /* retour sans changement */
            //Log.d(TAG, "BackFromScan sans changement" + resultCode + " data " + data);
            libere();
        }
        if(resultCode == 1){
            /* retour avec changements */
            if(!database_access) {
                Toast.makeText(this,"Ce SCAN ne sera pas pris en compte", Toast.LENGTH_SHORT).show();
                return;
            }

            //Log.d(TAG, "BackFromScan retour avec changements");
            if (data.hasExtra("CODE")) {
                String result_code = data.getStringExtra("CODE");
                if (!result_code.equals("")) {
                    addRecord(result_code);
                    //Log.d(TAG,"onActivityResult received code  "+result_code);
                    /* loading_spinner.setVisibility(View.VISIBLE);
                    barcode_edittext.setText(result_code);
                    addStudent();
                    searchIdforBarCode(result_code);*/
                }
            }
        }
    }

    //////////// /* gestion timer d'envoi */////////////
    public void handleTimerTick(Long d) {
        //counter++;
        //Log.d(TAG, "handleTimer Tick "+Long.toString(d));
        String infox = getFormaterTimerRemaining(d);
        timerinfos.setText(infox);
    }
    public void handleFinishTimer() {
        //Log.d(TAG, "handleTimer FINISH");
        check();
        //mycounter.Start();
        String infox = getFormaterTimerRemaining(0);
        timerinfos.setText(infox);
        //if(timer_should_run) reStartTimer();
        //reStartTimer();
    }





    /************  timer *************/
    public void stopTimer() {
        timer.cancel();
    }
    public void reStartTimer() {
        if(timer_should_run) {
            timer.cancel();
            startTimer();
        }
    }
    public void startTimer() {
        timer = new CountDownTimer(600000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                handleTimerTick(millisUntilFinished);
            }

            public void onFinish() {
                // mTextField.setText("done!");
                handleFinishTimer();
            }
        };
        timer.start();
    }
   /* public void stopTimer() {
        mycounter.Stop();
    }
    public void reStartTimer() {
        if(timer_should_run) {
            mycounter = new MyCountDownTimer(frequence_check * 1000, 1000);
            mycounter.Start();
        }
    }
    public void startTimer() {
        mycounter.Start();
    }*/

   //https://stackoverflow.com/questions/8857590/android-countdowntimer-skips-last-ontick
    public class MyCountDownTimer {
        private long millisInFuture;
        private long countDownInterval;
        private boolean status;
        public MyCountDownTimer(long pMillisInFuture, long pCountDownInterval) {
            this.millisInFuture = pMillisInFuture;
            this.countDownInterval = pCountDownInterval;
            status = false;
            Initialize();
        }

        public void Stop() {status = false;}
        public long getCurrentTime() {return millisInFuture;}
        public void Start() {status = true;}
        public void Initialize() {
            final Handler handler = new Handler();
            //Log.v("status", "starting");
            final Runnable counter = new Runnable(){
                public void run(){
                    long sec = millisInFuture/1000;
                    if(status) {
                        if(millisInFuture <= 0) {
                            //Log.v("status", "done");
                            handleFinishTimer();
                        } else {
                            //Log.v("status", Long.toString(sec) + " seconds remain");
                            //handleTimerTick();
                            handleTimerTick(sec);
                            millisInFuture -= countDownInterval;
                            handler.postDelayed(this, countDownInterval);
                        }
                    } else {
                        //Log.v("status", Long.toString(sec) + " seconds remain and timer has stopped!");
                        //handler.postDelayed(this, countDownInterval);
                    }
                }
            };
            handler.postDelayed(counter, countDownInterval);
        }
    }
    /*public void RefreshTimer()
    {
        final Handler handler = new Handler();
        final Runnable counterx = new Runnable(){

            public void run(){
                //timeText.setText(Long.toString(mycounter.getCurrentTime()));

                handler.postDelayed(this, 2000);
            }
        };

        handler.postDelayed(counterx, 2000);
    }*/



    /****************************************************/
    /*** envoi au serveur des enregistrement en base ****/
    /****************************************************/
    public void check() {
        //Log.d(TAG,"fire_Send");
        if(user_is_away) {
            reStartTimer();
            //Log.d(TAG,"fire_Send wont fire coz user_is_away");
            return;}
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoTv.setText("Vérification");
                database_access = false;
                stopTimer();
                timer_should_run = false;
                scanBtn.setText("...");
                empeche();
            }
        });
        Record x = RecordManager.getInstance().GetLastRecord();
        //Log.d(TAG,"last record has "+ String.valueOf(x.getId()));
        //final boolean copy_timer_should_run = timer_should_run;
        if(x.getId() <= 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    database_access = true;
                    timer_should_run = true;
                    barcode_edittext.setText("");
                    infoTv.setText("Prêt à scanner");
                    libere();
                    reStartTimer();
                }
            });
            return;
        }
        final Record rec = x;

        new Thread( new Runnable() { @Override public void run() {
            // Run whatever background code you want here.
            searchIdforRecord();
        } } ).start();


    }

    public void hasSentLastRecord() {
        check();
    }

    public boolean sendLastRecordWithTag(final int recordid,final int tagid) {
        //Log.d("KKKxx", "sending LastRecordWithTag id " +String.valueOf(recordid)+ " with tag_id = "+String.valueOf(tagid));
        //if(tig <=0) return false;
        final Record record = RecordManager.getInstance().GetLastRecord();
        if(record == null) {
            //Log.d("KKKxx","sendLastRecord: no record to send");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoTv.setText("Mise à jour locale");
                    maj_adapter();
                    hasSentLastRecord();
                }
            });
            return false;
        }

        XMLRPCCallback listener2 = new XMLRPCCallback() {
            public void onResponse(long id, Object result) {
                //Log.d("KKKxx","listener d'insertion reçoit " +result.toString() );
                // final String str = "sendLastRecord: " +result.toString();
                if(Integer.valueOf(result.toString()) > 0) {
                    //Log.d("KKKxx","listener d'insertion result.toString()) > 0 " +result.toString() );
                    //Log.d("KKKxx","listener sendLastRecord supprime le dernier record");
                    //RecordDbHandler dbx = RecordDbHandler.getInstance(getApplicationContext());
                    int result_db = RecordManager.getInstance().deleteLastRecord();
                    //Log.d("KKKxx","DELETE said = "+result_db);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            maj_adapter();
                            hasSentLastRecord();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            abandonneEnvoi();
                        }
                    });
                }

            }
            public void onError(long id, XMLRPCException error) {
                //Log.d("KKKxx","SendDatas error : "+error.getMessage() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        abandonneEnvoi();
                    }
                });

            }
            public void onServerError(long id, XMLRPCServerException error) {
                // Handling an error response from the server
                //Log.d("KKKxx","SendDatas onServerError : "+error.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        abandonneEnvoi();
                    }
                });

            }
        };
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanBtn.setText("Sending");
                infoTv.setText("Envoi du Scan");
            }
        });

        XMLRPCClient models;
        try {
            models = new XMLRPCClient(new URL(String.format("%s/xmlrpc/2/object", url)));
            long id = models.callAsync(listener2, "execute_kw",db_name, uid_pref, password_pref,"facing_profile.pointage", "create",  asList(new HashMap() {{ put("name", record.getName());put("employee_id", employeeid);put("tag_id", tagid);put("date", record.getDate());}}));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void abandonneEnvoi() {
        //Log.d("KKKxx","abandonneEnvoi : ");
        database_access = true;
        timer_should_run = true;
        barcode_edittext.setText("");
        infoTv.setText("Prêt à scanner");
        libere();
        reStartTimer();
    }



    // callback retour de searchForRecordId
    public void retourCallbackSearch(final int recordid,final int tagid){
        //Log.d("KKKxx", "retourCallbackSearch of record id " +String.valueOf(recordid)+ " with tag_id = "+String.valueOf(tagid));
        if(tagid > 0) {
            infoTv.setText("Tag "+String.valueOf(tagid));
            View v = lv.getChildAt(lv.getFirstVisiblePosition());
            if(v != null) {
                //scanBtn.setText("Found Tag");
                TextView someText = (TextView) v.findViewById(R.id.record_tag);
                someText.setText(String.valueOf(tagid));
            }
        }
        else {
            infoTv.setText("Aucun Tag trouvé");
            //scanBtn.setText("...");
            //scanBtn.setText("No Tag Found"+String.valueOf(tagid));
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendLastRecordWithTag(recordid,tagid);
            }
        }, 200);
        return;
    }

    //recherche de du tag du dernier enregistrement
    //public boolean searchIdforRecord(final Record record) {
    public boolean searchIdforRecord() {

        final Record record = RecordManager.getInstance().GetLastRecord();

        XMLRPCCallback listener = new XMLRPCCallback() {
            public void onResponse(long id, Object result) {
                //Log.d("KKKxx", "found Object  " +result.toString());

                Object[] x = (Object[]) result;
                if(x.length > 0) {
                    Log.d("KKKxx", "found code: " + x[0].toString());
                    if (Integer.valueOf(x[0].toString()) > 0) {
                        //Log.d("KKKxx", "updating last record (id="+record.getId()+") with tag_id = " + x[0].toString());
                        final String fff = x[0].toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                retourCallbackSearch(record.getId(),Integer.valueOf(fff));
                                return;
                            }
                        });

                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            retourCallbackSearch(Integer.valueOf(record.getId()),0);
                            return;


                        }
                    });
                }
                //sendLastRecord();
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                //Log.d("KKKxx","SendDatas error : "+error.getMessage() );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        retourCallbackSearch(Integer.valueOf(record.getId()),0);
                        return;
                    }
                });
            }
            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                //Log.d("KKKxx","SendDatas onServerError : "+error.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        retourCallbackSearch(Integer.valueOf(record.getId()),0);
                        return;
                    }
                });
            }
        };
        // fin de la def du premier listener
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanBtn.setText("Cheking");
                infoTv.setText("Vérification Tag");
            }
        });

        XMLRPCClient models;
        try {
            models = new XMLRPCClient(new URL(String.format("%s/xmlrpc/2/object", url)));
            List condition = asList(asList(asList("name", "=",record.getBarcode())));
            List ids = asList( models.callAsync(listener, "execute_kw",db_name, uid_pref, password_pref,"facing_profile.tags", "search",  condition));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return true;
    } // fin de la recherche de du tag du dernier enregistrement



    public void empeche() {
        //Log.d(TAG,"***************** empeche *****************");
        //ui_and_database_access = false;
        //barcode_edittext.setEnabled(false);
        scanBtn.setEnabled(false);
        scanBtn.setText("...");
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                libere();
            }
        }, 20000);*/

    }
    public void libere() {
        //Log.d(TAG,"***************** libere *****************");
        //ui_and_database_access = true;
        //barcode_edittext.setEnabled(true);
        scanBtn.setEnabled(true);
        scanBtn.setText("Scan");
    }



    public void maj_adapter() {
        recordlist = RecordManager.getInstance().GetAllRecords();
        //adapter.notifyDataSetChanged();
        //lv.invalidateViews();
        adapter = new CustomListAdapter(this, recordlist);
        // Assign adapter to ListView
        lv.setAdapter(adapter);
    }


    public boolean addRecord(String result_code){
            /*String texttosend = barcode_edittext.getText().toString();
            if(texttosend == "") { return false; }*/
            barcode_edittext.setText(result_code);

            infoTv.setText("Insertion en base de donnée");
            //RecordDbHandler db = RecordDbHandler.getInstance(this);
            int num = RecordManager.getInstance().getXCount();
            Record record = new Record();

            int ii = num + 1;
            record.setId(ii);

            Date now = new Date();
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            record.setDate_displayed(f.format(now));

            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            final String timezone_date = f.format(now);
            record.setDate(timezone_date);

            /* setName */
            //record.setName(timezone_date);
            //long xxx = System.currentTimeMillis();
            long unixTime = System.currentTimeMillis() / 1000L;
            String eee = Long.toString(unixTime);
            record.setName(eee);
            //Log.d("DateX",eee );
            // private String barcode;
            record.setBarcode(result_code);
            // private int employee_id;

            record.setUid(uid_pref);
            record.setEmployee_id(employeeid);

            long x = RecordManager.getInstance().addRecord(record);
            if(x >= 0) {
                //infoTv.setText("Insertion confirmée");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Enregistrement inséré ", Toast.LENGTH_SHORT).show();
                                database_access = true;
                                libere();
                                infoTv.setText("Prêt à scanner");
                                //barcode_edittext.setText("");
                                maj_adapter();
                            }
                        });

                    }
                }, 500);

                return true;
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        database_access = true;
                        libere();
                        Toast.makeText(getApplicationContext(), "Enregistrement non inséré !!!", Toast.LENGTH_SHORT).show();
                        infoTv.setText("Prêt à scanner");
                        //barcode_edittext.setText("");
                        maj_adapter();
                    }
                });

                Toast.makeText(this, "Erreur d'insertion ", Toast.LENGTH_SHORT).show();
                //barcode_edittext.setText("");
                return false;
            }
        }


    public class CustomListAdapter extends BaseAdapter {
        private Context context; //context
        private ArrayList<Record> items; //data source of the list adapter

        //public constructor
        public CustomListAdapter(Context context, ArrayList<Record> items) {
            this.context = context;
            this.items = items;
        }
        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return items.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return items.get(position); //returns list item at the specified position
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // inflate the layout for each list row
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.record_row, parent, false);
            }

            // get current item to be displayed
            Record currentItem = (Record) getItem(position);

            // get the TextView for item name and item description
            TextView record_id_tv = (TextView)
                    convertView.findViewById(R.id.record_id);
            TextView record_name_tv = (TextView)
                    convertView.findViewById(R.id.record_name);
            TextView record_barcode_tv = (TextView)
                    convertView.findViewById(R.id.record_barcode);

            TextView record_emmployee_id_tv = (TextView)
                    convertView.findViewById(R.id.record_emmployee_id);
            TextView record_tag = (TextView)
                    convertView.findViewById(R.id.record_tag);
            TextView record_date_tv = (TextView)
                    convertView.findViewById(R.id.record_date);
            TextView record_date_displayed_tv = (TextView)
                    convertView.findViewById(R.id.record_date_displayed);
            ProgressBar record_progressBar_pg = (ProgressBar)
                    convertView.findViewById(R.id.record_progressBar);

            //sets the text for item name and item description from the current item object
            record_id_tv.setText(currentItem.getId() + "");
            record_name_tv.setText(currentItem.getName() + "");
            record_barcode_tv.setText(currentItem.getBarcode() + "");
            record_emmployee_id_tv.setText(currentItem.getEmployee_id() + "");
            record_tag.setText(currentItem.getTag() + "");
            record_date_tv.setText(currentItem.getDate() + "");
            record_date_displayed_tv.setText(currentItem.getDate_displayed() + "");
            record_progressBar_pg.setVisibility(View.INVISIBLE);


            // returns the view for the current row
            return convertView;
        }
    }

    public static String getFormaterTimerRemaining(long millis) {
        DateFormat df = new SimpleDateFormat("mm:ss");
        return df.format(new Date(millis));
    }
}
