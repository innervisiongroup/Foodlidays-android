package com.innervision.timtac.foodlidays;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.innervision.timtac.foodlidays.UtilitiesClass.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FragmentMenu extends Fragment implements AdapterView.OnItemSelectedListener {

    public static String result = "";
    public static String result_cat = "";

    public static Spinner spinner;
    public static ListView myList;
    public static TextView any_restaurants;

    public static JSONArray jArray_articles = new JSONArray();
    public static JSONArray jArray_cat = new JSONArray();

    public static ArrayList<String> all_cat = new ArrayList<>();
    public static ArrayList<String> cat = new ArrayList<>();
    public static ArrayList<Article> liste_articles = new ArrayList<>();


    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
        View v = inflater.inflate(R.layout.activity_food_card, group, false);

        myList = (ListView) v.findViewById(R.id.list);
        any_restaurants = (TextView) v.findViewById(R.id.any_restaurant);
        spinner = (Spinner) v.findViewById(R.id.spinner);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String is_co = prefs.getString("session_room_number","");

        if(!is_co.equals(""))
        {
            MainActivity.session_room_number = is_co;
            MainActivity.session_email = prefs.getString("session_email","email");
            MainActivity.session_room = prefs.getString("session_room","number");
            MainActivity.session_city = prefs.getString("session_city","city");
            MainActivity.session_country = prefs.getString("session_country","country");
            MainActivity.session_floor = prefs.getString("session_floor","floor");
            MainActivity.session_id = prefs.getString("session_id","id");
            MainActivity.session_street_address = prefs.getString("session_street_address","address");
            MainActivity.session_type = prefs.getString("session_type","type");
            MainActivity.session_zip = prefs.getString("session_zip","zip");
            MainActivity.session_user_id = prefs.getString("session_user_id","user_id");
        }
        else
        {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }




        spinner.setOnItemSelectedListener(this);



        if(isNetworkAvailable())
        {

            //*************************** Récupération des catégories ******************************

            String url_cat = UtilitiesConfig.url_base + "/api/v1/category";
            try {

                result_cat = new GetRequest().execute(url_cat).get();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }


            if (result_cat != null)
            {
                try {

                    jArray_cat = new JSONArray(result_cat);

                    for (int i = 0; i < jArray_cat.length(); i++) {

                        JSONObject jsonObject = jArray_cat.getJSONObject(i);
                        all_cat.add(jsonObject.getString("name"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else Toast.makeText(getActivity(), "Erreur lors de l'accès au réseau, veuillez réessayer plus tard", Toast.LENGTH_LONG).show();



            //********************* Création de la liste des catégories disponibles ****************

            String zip_code_temp = "1435";
            String url = UtilitiesConfig.url_base + "/api/v1/food/cat/all/" + zip_code_temp;

            try {
                result = new GetRequest().execute(url).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (result != null)
            {
                if(result.length() > 70)
                {

                    try {

                        jArray_articles = new JSONArray(result);

                        for (int i = 0; i < jArray_articles.length(); i++) {
                            JSONObject jsonObject = jArray_articles.getJSONObject(i);

                            int n_cat = jsonObject.getInt("category_id");

                            for(int j = 0;j<jArray_cat.length();j++)
                            {
                                JSONObject jObj = jArray_cat.getJSONObject(j);

                                if(n_cat == jObj.getInt("id"))
                                {
                                    if(!cat.contains(jObj.getString("name")))
                                        cat.add(jObj.getString("name"));
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, cat);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                } else any_restaurants.setVisibility(View.VISIBLE);

            } else Toast.makeText(getActivity(), "Erreur lors de l'accès au réseau, veuillez réessayer plus tard", Toast.LENGTH_LONG).show();

        } else Toast.makeText(getActivity(), "Pas de connexion internet valide", Toast.LENGTH_LONG).show();


        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }


    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivity(intent);
                return true;
            case R.id.panier:
                Intent intent2 = new Intent(getActivity(), Card.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

       String item = parent.getItemAtPosition(pos).toString();
       int index = 0;

        /*** on récupère l'id de la catégorie ***/
       for(int i=0;i<jArray_cat.length();i++)
       {
           try {
               JSONObject jObj = jArray_cat.getJSONObject(i);

               if(jObj.getString("name").equals(item))
               {
                   index = jObj.getInt("id");
               }

           } catch (JSONException e) {
               e.printStackTrace();
           }

       }

        liste_articles.clear();


        /*** on liste les articles de la catégorie ***/
        for(int i=0;i<jArray_articles.length();i++)
        {
            try {

                JSONObject js = jArray_articles.getJSONObject(i);

                if(index == js.getInt("category_id"))
                {
                    Article myArt = new Article();
                    myArt.name = js.getString("name");
                    myArt.detail = js.getString("note");
                    myArt.prix = js.getString("price");
                    myArt.image = js.getString("image");

                    liste_articles.add(myArt);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        CustomArrayAdapter ad = new CustomArrayAdapter();
        myList.setAdapter(ad);


        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Article article = (Article)myList.getItemAtPosition(position);


                /********************* on lance un dialog pour commander ***************************/
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View dialog_view = inflater.inflate(R.layout.dialog_article, null);


                ImageView im = (ImageView) dialog_view.findViewById(R.id.big_pic);
                Picasso.with(getActivity()).load(UtilitiesConfig.url_base + "/uploads/" + article.image).into(im);
                final NumberPicker pick = (NumberPicker)dialog_view.findViewById(R.id.numberPicker);
                pick.setMaxValue(25);
                pick.setMinValue(1);
                setNumberPickerTextColor(pick,0xff000000);

                builder.setTitle(article.name);
                builder.setView(dialog_view);

                builder.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Boolean isAlreadyIn = false;
                        for(Order_Article a : FragmentCard.myOrderArticles)
                        {
                            // Si un item dans la liste possède le même nom, on augmente juste la quantité
                            if(a.name.equals(article.name))
                            {
                                isAlreadyIn = true;
                                a.quantity += pick.getValue();
                            }
                        }


                        if(!isAlreadyIn) // Si pas déjà dedans, on rajoute l'item
                        {
                            Order_Article order = new Order_Article();
                            order.name = article.name;
                            order.quantity = pick.getValue();
                            order.image = article.image;
                            order.prix = article.prix;
                            FragmentCard.myOrderArticles.add(order);
                        }

                        Disposer.mSectionsPagerAdapter.notifyDataSetChanged();

                        Toast.makeText(getActivity(),"article ajouté au panier !",Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }


    public class CustomArrayAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return liste_articles.size();
        }

        @Override
        public Object getItem(int position) {
            return liste_articles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.detail_list,parent,false);

            TextView name = (TextView)convertView.findViewById(R.id.titre);
            TextView descr = (TextView)convertView.findViewById(R.id.description);
            TextView prix = (TextView)convertView.findViewById(R.id.prix);
            ImageView pic = (ImageView)convertView.findViewById(R.id.img);

            Article art = liste_articles.get(position);

            Picasso.with(getActivity()).load("http://foodlidays.dev.innervisiongroup.com/uploads/" + art.image).into(pic);
            name.setText(art.name);
            descr.setText(art.detail);
            prix.setText(art.prix + " €");

            return convertView;

        }
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    private boolean isNetworkAvailable() {
        /*getActivity();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();*/
        return true;
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e){
                    Log.w("setNumberPickerError", e);
                }
            }
        }
        return false;
    }

}