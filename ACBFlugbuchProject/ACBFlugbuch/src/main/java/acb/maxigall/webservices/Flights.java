package acb.maxigall.webservices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.List;


public class Flights extends Activity {
    TextView textView;
    WebView webView;
    static String sec_id;
    SharedPreferences settings;

    static String startDate, endDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flights);

        textView = (TextView) findViewById(R.id.section_label);
        webView = (WebView) findViewById(R.id.webView);
        settings = getSharedPreferences("login_data", 0);
        sec_id=getSharedPreferences("login_data",0).getString("secId","");

        startDate=settings.getString("sDate","2001-06-01");
        endDate=settings.getString("eDate","2020-06-01");
        setup();
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
        switch(item.getItemId()){
            case R.id.about:
                about();
                return true;
            case R.id.reload:
                reload();
                return true;
            case R.id.getStartDate:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        settings.edit().putString("sDate",year+"-"+(month+1)+"-"+day).commit();
                    }
                }, 2014,1,1).show();
                reload();
                return true;
            case R.id.getEndDate:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        settings.edit().putString("eDate",year+"-"+(month+1)+"-"+day).commit();
                    }
                }, 2014,1,1).show();
                reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void about(){
        WebView v = new WebView(this);
        v.loadData("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de-DE\" lang=\"de-DE\">" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /> <head></head><body> <h1> ACBFlugbuch </h1> <br> (c) 2013-2014 Maximilian Gall. </body></html>","text/html; charset=UTF-8" ,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v)
                .setTitle("FlugZusammenfassung");
        AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    public void setup(){
        textView.setText("Flüge für " + settings.getString("username",""));
        if(settings.getString("content","").equals("")){
            try{new LoginToACB(settings.getString("username", ""),settings.getString("password", ""), "http://www.aeroclub-bamberg.de/index.php/component/comprofiler/login", new LoginToACB.LoginToACBListener() {
                @Override
                public void completionCallBack(String html) {
                    webView.loadData(formatWebViewContent(html), "text/html; charset=UTF-8", null);
                    settings.edit().putString("content", html).commit();
                }
            }).execute();}
            catch(Exception e){textView.setText(e.getMessage());}}
        else{
            webView.loadData(formatWebViewContent(settings.getString("content", "")), "text/html; charset=UTF-8", null);
        }
    }

    public void reload(){
        textView.setText("Flüge für " + settings.getString("username",""));
        new LoginToACB(settings.getString("username", ""),settings.getString("password", ""), "http://www.aeroclub-bamberg.de/index.php/component/comprofiler/login", new LoginToACB.LoginToACBListener() {
            @Override
            public void completionCallBack(String html) {
                webView.loadData(formatWebViewContent(html), "text/html; charset=UTF-8", null);
                settings.edit().putString("content", html).commit();
            }
        }).execute();
    }

    public String formatWebViewContent(String html){
        StringBuilder sb = new StringBuilder();
        sb.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de-DE\" lang=\"de-DE\">" +
                        "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"
        );
        sb.append("<head></head><body>");
        String s=html.substring(html.indexOf("</form>")+5);
        sb.append(s.substring(s.indexOf("<table>"),s.indexOf("</table>")));
        sb.append(s);
        sb.append("</table>");

        //Zusammenfassung

        s=s.substring(s.indexOf("</table>")+8);
        s=s.substring(s.indexOf("<table>"),s.indexOf("</table>"));
        sb.append(s);
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
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

                HttpResponse responseFlights = client.execute(new HttpGet(
                        "http://www.aeroclub-bamberg.de/index.php?option=com_flugbuch&boxchecked=0&controller=flugbuch&showstart="+startDate+"&showend="+endDate), localContext);

                InputStream in;
                in = responseFlights.getEntity().getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line+"\n");
                }
                in.close();

                result = str.toString();

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
