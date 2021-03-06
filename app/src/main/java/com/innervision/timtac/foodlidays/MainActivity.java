package com.innervision.timtac.foodlidays;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {

    private String url = UtilitiesConfig.url_base + UtilitiesConfig.URL_LOGIN;

    //UI declaration
    private EditText number;
    private EditText email;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //récup des widgets
        number = (EditText)findViewById(R.id.number);
        email = (EditText)findViewById(R.id.email);
        Button connexion = (Button)findViewById(R.id.connexion);
        ImageView imageqr = (ImageView)findViewById(R.id.imageqr);

        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String semail = email.getText().toString();
                String snumber = number.getText().toString();

                if(ValidForm(snumber,semail))
                {
                    new RequestToConnect().execute(url, snumber, semail);
                }

            }
        });


        imageqr.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setResultDisplayDuration(0);
                integrator.setCameraId(0);
                integrator.setPrompt("");
                integrator.initiateScan();
            }
        });
    }


    public boolean ValidForm(String room, String email)
    {
        if(!UtilitiesFunctions.isNetworkConnected(getApplicationContext()))
        {
            UtilitiesFunctions.DisplayError(getString(R.string.connect_to_internet), MainActivity.this);
            return false;
        }

        if(!AnyEmpty(room,email))
        {
            UtilitiesFunctions.DisplayError(getString(R.string.need_two_field), MainActivity.this);
            return false;
        }

        if(!UtilitiesFunctions.isEmailValid(email))
        {
            UtilitiesFunctions.DisplayError(getString(R.string.email_invalid), MainActivity.this);
            return false;
        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) number.setText(result.getContents());
        else Toast.makeText(getApplicationContext(),R.string.qr_code_failed,Toast.LENGTH_SHORT).show();
    }


    public class RequestToConnect extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            String res = null;
            try{

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost request = new HttpPost(params[0]);
                List<NameValuePair> nameValuePairs = new ArrayList<>(3);
                nameValuePairs.add(new BasicNameValuePair("room_number", params[1]));
                nameValuePairs.add(new BasicNameValuePair("email", params[2]));
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                res = in.readLine();
                in.close();

            }catch(Exception e){
                Log.e("log_tag", "Error in http connection " + e.toString());
            }
            return res;
        }

        @Override
        protected void onPostExecute(String ligne)
        {
            super.onPostExecute(ligne);
            ConnectUser(ligne);
        }
    }


    public void ConnectUser(String result)
    {
        //si pas de réponse
        if(result == null)
        {
            UtilitiesFunctions.DisplayError(getString(R.string.no_connection), MainActivity.this);
            return;
        }

        //si mauvais résultat
        if(result.length() < 30)
        {
            UtilitiesFunctions.DisplayError(getString(R.string.bad_room_nuber), MainActivity.this);
            return;
        }


        try {

            JSONObject object = new JSONObject(result);
            JSONObject object2 = object.getJSONObject("room");

            //si l'objet ne contient pas ce que l'on attend
            if (!object.has("email"))
            {
                UtilitiesFunctions.DisplayError(getString(R.string.bad_room_nuber), MainActivity.this);
                return;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            prefs.edit().putBoolean("logged",true).apply();
            prefs.edit().putString("session_email", object.getString("email")).apply();
            prefs.edit().putString("session_type", object.getString("place_type")).apply();
            prefs.edit().putString("session_id", object2.getString("id")).apply();
            prefs.edit().putString("session_user_id", object2.getString("user_id")).apply();
            prefs.edit().putString("session_street_address", object2.getString("street_address")).apply();
            prefs.edit().putString("session_city", object2.getString("city")).apply();
            prefs.edit().putString("session_country", object2.getString("country")).apply();
            prefs.edit().putString("session_zip", object2.getString("zip")).apply();
            prefs.edit().putString("session_room_number", object2.getString("room_number")).apply();

            if(object.getString("place_type").equals("place"))
            {
                prefs.edit().putString("session_name_place", object2.getString("name")).apply();
            }
            else if(object.getString("place_type").equals("room"))
            {
                prefs.edit().putString("session_floor", object2.getString("floor")).apply();
                prefs.edit().putString("session_room", object2.getString("room")).apply();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("log_tag", "error in parsing " + e.toString());
        }

        SuperToast.create(getApplicationContext(),getString(R.string.signed_in),SuperToast.Duration.MEDIUM,Style.getStyle(Style.GREEN, SuperToast.Animations.POPUP)).show();

        finish();
        Intent intent = new Intent(MainActivity.this, Disposer.class);
        startActivity(intent);

    }


    public static Boolean AnyEmpty(String s1, String s2)
    {
        return !s1.isEmpty() && !s2.isEmpty();
    }

}