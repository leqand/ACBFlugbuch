package acb.maxigall.webservices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    TextView username;
    TextView password;
    ProgressBar progressBar;
    Boolean clickable;
    public static String sec_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.password);
        clickable=true;
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        SharedPreferences settings = getSharedPreferences("login_data", 0);

        if(settings.getString("username", null)!=null){
            this.username.setText(settings.getString("username", ""));
        }
        if(settings.getString("password", null)!=null){
            this.password.setText(settings.getString("password", ""));
        }

    }

    public void doLogIn(View v){
        if(clickable){
            clickable=false;
        if(username.getText()=="" || password.getText()==""){
            showPopUp("Error","Bitte Eingaben vervollständigen");
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            new GetSecIdACB("http://www.aeroclub-bamberg.de/index.php/component/comprofiler/login", new GetSecIdACB.GetSecIdACBListener() {
                @Override
                public void completionCallBack(String html) {
                    String s = html.substring(html.indexOf("cbsecuritym3"));
                    //showPopUp("1.",s);
                    s=s.substring(s.indexOf("value"));
                    //showPopUp("2",s);
                    s=s.substring(s.indexOf("\"")+1);
                    //showPopUp("3.",s);
                    s=s.substring(0,s.indexOf("\""));
                    //showPopUp("id",s);
                    SharedPreferences settings = getSharedPreferences("login_data", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("secId",s);
                    editor.commit();
                    sec_id=s;

                    new LoginToACB(username.getText().toString() , password.getText().toString(), "http://www.aeroclub-bamberg.de/index.php/component/comprofiler/login", new LoginToACB.LoginToACBListener() {
                        @Override
                        public void completionCallBack(String html) {
                            if(html.contains("Die Anmeldung war erfolgreich")){
                                SharedPreferences settings = getSharedPreferences("login_data", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("username", username.getText().toString());
                                editor.putString("password", password.getText().toString());
                                editor.commit();
                                clickable=true;
                                progressBar.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(getBaseContext(),Flights.class);
                                startActivity(intent);
                            }
                            else{
                                showPopUp("Error", "Die Anmeldung ist fehlgeschlagen. \n Reason: "+html);
                                progressBar.setVisibility(View.INVISIBLE);
                                clickable=true;
                            }
                        }
                    }).execute();
                }
            }).execute();
        }}
        else{}
    }

    public void showPopUp(String head, String content){
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(content);
            builder.setTitle(head);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }
            );
            AlertDialog dialog = builder.create();
            dialog.show();
        }catch(Exception e){
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    public static class LoginToACB extends AsyncTask<Void, Void, String> {

        public static interface LoginToACBListener {
            public abstract void completionCallBack(String html);
        }

        public LoginToACBListener listener;
        public String link;
        private String username;
        private String password;

        public LoginToACB (String username, String password, String aLink, LoginToACBListener aListener) {
            listener = aListener;
            link = aLink;
            this.username=username;
            this.password=password;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext localContext = new BasicHttpContext();
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE,cookieStore);
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(link);
            String result = "";


            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", username));
                nameValuePairs.add(new BasicNameValuePair("passwd", password));

                nameValuePairs.add(new BasicNameValuePair("op2", "login"));
                nameValuePairs.add(new BasicNameValuePair("lang", "german"));
                nameValuePairs.add(new BasicNameValuePair("force_session", "1"));
                nameValuePairs.add(new BasicNameValuePair("return", "B:aHR0cDovL3d3dy5hZXJvY2x1Yi1iYW1iZXJnLmRlLw=="));
                nameValuePairs.add(new BasicNameValuePair("message", "1"));
                nameValuePairs.add(new BasicNameValuePair("loginform", "loginform"));
                nameValuePairs.add(new BasicNameValuePair("cbsecuritym3", sec_id));

                nameValuePairs.add(new BasicNameValuePair("remember", "yes"));
                nameValuePairs.add(new BasicNameValuePair("submit", "Login"));

                // Execute HTTP Post Request
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(request, localContext);

                /*for(Header h : response.getAllHeaders()){
                    sb.append(h.getValue()+"\n");
                }*/

                /*HttpResponse responseFlights = client.execute(new HttpGet(
                        "http://www.aeroclub-bamberg.de/index.php?option=com_flugbuch&boxchecked=0&controller=flugbuch&showstart=2013-06-01&showend=2014-02-09"), localContext);*/

                InputStream in;
                in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line+"\n");
                }
                in.close();

                result = str.toString();

                /*in = response.getEntity().getContent();
                reader = new BufferedReader(
                        new InputStreamReader(in));
                str = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line+"\n");
                }
                in.close();

                result+= "+\n\n\n\n"+str.toString();*/
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                listener.completionCallBack(result);
            }
        }
    }

    public static class GetSecIdACB extends AsyncTask<Void, Void, String> {

        public static interface GetSecIdACBListener {
            public abstract void completionCallBack(String html);
        }

        public GetSecIdACBListener listener;
        public String link;
        private String username;
        private String password;

        public GetSecIdACB (String aLink, GetSecIdACBListener aListener) {
            listener = aListener;
            link = aLink;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext localContext = new BasicHttpContext();
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE,cookieStore);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(link);
            String result = "";

            try {

               /* List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", username));
                nameValuePairs.add(new BasicNameValuePair("passwd", password));

                nameValuePairs.add(new BasicNameValuePair("op2", "login"));
                nameValuePairs.add(new BasicNameValuePair("lang", "german"));
                nameValuePairs.add(new BasicNameValuePair("force_session", "1"));
                nameValuePairs.add(new BasicNameValuePair("return", "B:aHR0cDovL3d3dy5hZXJvY2x1Yi1iYW1iZXJnLmRlLw=="));
                nameValuePairs.add(new BasicNameValuePair("message", "1"));
                nameValuePairs.add(new BasicNameValuePair("loginform", "loginform"));
                nameValuePairs.add(new BasicNameValuePair("cbsecuritym3", "cbm_6fa8bda5_07452fb2_42b59ac59020ac554318307787bec33f"));

                nameValuePairs.add(new BasicNameValuePair("remember", "yes"));
                nameValuePairs.add(new BasicNameValuePair("submit", "Login"));

                // Execute HTTP Post Request
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));*/
                HttpResponse response = client.execute(request, localContext);

                /*for(Header h : response.getAllHeaders()){
                    sb.append(h.getValue()+"\n");
                }*/

                /*HttpResponse responseFlights = client.execute(new HttpGet(
                        "http://www.aeroclub-bamberg.de/index.php?option=com_flugbuch&boxchecked=0&controller=flugbuch&showstart=2013-06-01&showend=2014-02-09"), localContext);*/

                InputStream in;
                in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line+"\n");
                }
                in.close();

                result = str.toString();

                /*in = response.getEntity().getContent();
                reader = new BufferedReader(
                        new InputStreamReader(in));
                str = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line+"\n");
                }
                in.close();

                result+= "+\n\n\n\n"+str.toString();*/
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                listener.completionCallBack(result);
            }
        }
    }
}
