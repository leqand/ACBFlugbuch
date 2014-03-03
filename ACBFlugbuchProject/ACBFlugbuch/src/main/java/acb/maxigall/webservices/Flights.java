package acb.maxigall.webservices;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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

public class Flights extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static String sec_id;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flights);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        sec_id=getSharedPreferences("login_data",0).getString("secId","");

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle ="Flüge";
                break;
            case 2:
                mTitle ="Über";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.flights, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.reload:
                reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void reload(){
        final SharedPreferences settings = getSharedPreferences("login_data", 0);
        final TextView textView = (TextView) findViewById(R.id.section_label);
        final WebView v = (WebView) findViewById(R.id.webView);
        textView.setText("Flüge für " + settings.getString("username",""));
        new LoginToACB(settings.getString("username", ""),settings.getString("password", ""), "http://www.aeroclub-bamberg.de/index.php/component/comprofiler/login", new LoginToACB.LoginToACBListener() {
            @Override
            public void completionCallBack(String html) {
                v.loadData(formatWebViewContent(html),"text/html; charset=UTF-8", null);
                settings.edit().putString("content", html).commit();
                recreate();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_flights, container, false);
            final TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            final WebView v = (WebView) rootView.findViewById(R.id.webView);

            final SharedPreferences settings = getActivity().getSharedPreferences("login_data", 0);

            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                textView.setText("Flüge für " + settings.getString("username",""));
                if(settings.getString("content","").equals("")){
                try{new LoginToACB(settings.getString("username", ""),settings.getString("password", ""), "http://www.aeroclub-bamberg.de/index.php/component/comprofiler/login", new LoginToACB.LoginToACBListener() {
                    @Override
                    public void completionCallBack(String html) {
                        v.loadData(formatWebViewContent(html),"text/html; charset=UTF-8", null);
                        settings.edit().putString("content", html).commit();
                        //v.loadData(html,"text/html; charset=UTF-8", null);
                        //showPopUp("File:", html);
                    }
                }).execute();}
                catch(Exception e){textView.setText(e.getMessage());}}
                else{
                    v.loadData(formatWebViewContent(settings.getString("content", "")), "text/html; charset=UTF-8", null);
                }
                break;
                case 2:
                    textView.setText("\t Flugbuch ACB\n (c)2013-2014 Maximilian Gall");
            }

            return rootView;
        }

        public void showPopUp(String head, String content){
            try{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getBaseContext());
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

        public String formatWebViewContent(String html){
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"de-DE\" lang=\"de-DE\">" +
                    "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />");
            sb.append("<head></head><body>");
            String s=html.substring(html.lastIndexOf("</form>"));
            sb.append(s.substring(s.indexOf("<table>"),s.indexOf("</table>")));
            sb.append("</table>");

            //Zusammenfassung

            s=s.substring(s.indexOf("</table>")+8);
            s=s.substring(s.indexOf("<table>"),s.indexOf("</table>"));
            sb.append(s);
            sb.append("</table>");
            sb.append("</body></html>");
            return sb.toString();
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Flights) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
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
                        "http://www.aeroclub-bamberg.de/index.php?option=com_flugbuch&boxchecked=0&controller=flugbuch&showstart=2001-06-01&showend=2020-02-09"), localContext);

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
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
