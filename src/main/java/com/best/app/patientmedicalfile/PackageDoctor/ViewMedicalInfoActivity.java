package com.best.app.patientmedicalfile.PackageDoctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.best.app.patientmedicalfile.PackageClass.ClassMedicalinfo;
import com.best.app.patientmedicalfile.PackageClass.DB_Connection;
import com.best.app.patientmedicalfile.R;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class ViewMedicalInfoActivity extends AppCompatActivity {

    public ListView listView;
    private ArrayList<ClassMedicalinfo> DataList = new ArrayList<ClassMedicalinfo>();
    CenterAdapter adapter;
    SearchView searchView;
    SharedPreferences loginPrefs;
    int mfid=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_medical_info);

        // ini
        loginPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        mfid=loginPrefs.getInt("mfid",0);
        setTitle("List of Disease");
        listView = (ListView) findViewById(R.id.list1);
        searchView=(SearchView) findViewById(R.id.searchView);
        ////////////////
        get_date();
        adapter = new CenterAdapter(getApplicationContext(), R.layout.rowinfofordoctor, DataList);
        listView.setAdapter(adapter);
        searchView.setQueryHint("Search title");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.toString().equals("")) {
                    get_date();
                    adapter = new CenterAdapter(getApplicationContext(), R.layout.rowinfofordoctor, DataList);
                    listView.setAdapter(adapter);
                } else {
                    searchItem(newText.toString());
                }
                return false;
            }
        });


    }

    public void searchItem(String textToSearch){
        Iterator<ClassMedicalinfo> iter = DataList.iterator();
        while(iter.hasNext()){

            if(!iter.next().getFullname().contains(textToSearch)){
                iter.remove();
            }
        }
        adapter.notifyDataSetChanged();
    }
    private void get_date() {
        DataList.clear();
        Thread t1;
        t1=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    DB_Connection dbconn = new DB_Connection();
                    dbconn.DB_Connection_open();
                    PreparedStatement sql_statment;
                    PreparedStatement st=dbconn.connection.prepareStatement("SELECT `infoid`,medicalfile.medicalfileid ,user.fullname,`title`,`description`,`infovalue`FROM `medicalinfo`, medicalfile,patient,user WHERE medicalinfo.medicalfileid=medicalfile.medicalfileid AND medicalfile.patientid=patient.patientid and patient.patientid=user.userid     and   medicalfile.medicalfileid =?");
                    st.setInt(1,mfid);
                    ResultSet rs = st.executeQuery();
                    while (rs.next()) {
                        DataList.add(new ClassMedicalinfo(
                                rs.getInt(1),
                                rs.getInt(2),
                                rs.getString(3),
                                rs.getString(4),
                                rs.getString(5),
                                rs.getString(6)

                        )  );
                    }
                } catch (Exception s) {
                    s.printStackTrace();
                }
            }
        });
        try {
            t1.start();
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class CenterAdapter extends BaseAdapter {
        private Context context;
        private int layout;

        public CenterAdapter(Context context, int layout, ArrayList<ClassMedicalinfo> allSubSerList) {
            this.context = context;
            this.layout = layout;
            AllSubSerList = allSubSerList;
        }
        ArrayList<ClassMedicalinfo> AllSubSerList = new ArrayList<ClassMedicalinfo>();
        @Override
        public int getCount() {
            return AllSubSerList.size();
        }
        @Override
        public Object getItem(int position) {
            return AllSubSerList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.rowinfofordoctor, null);
            }

            final TextView medicalfileid = (TextView) row.findViewById(R.id.medicalfileid);
            final TextView fullname = (TextView) row.findViewById(R.id.fullname);
            final TextView title = (TextView) row.findViewById(R.id.title);
            final TextView description = (TextView) row.findViewById(R.id.description);
            final TextView infovalue = (TextView) row.findViewById(R.id.infovalue);


            Button btnDelete = (Button) row.findViewById(R.id.btnDelete);




            final ClassMedicalinfo item = AllSubSerList.get(position);
            medicalfileid.setText(String.valueOf( item.getMedicalfileid()));
            fullname.setText(item.getFullname());
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            infovalue.setText(item.getInfovalue());




            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UpdateFunction update = new UpdateFunction(item.getInfoid());
                    update.execute();
                }
            });
            return row;
        }
    }
    private class UpdateFunction extends AsyncTask<Void,Void,Void> {
        int itemid;
        public UpdateFunction(int itemid  ) {
            this.itemid = itemid;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                DB_Connection   dbconn = new DB_Connection();
                dbconn.DB_Connection_open();
                PreparedStatement st = null;
                PreparedStatement st2= null;
                st=dbconn.connection.prepareStatement(" delete from  medicalinfo  where infoid =?");
                st.setInt(1,itemid);

                int rs = st.executeUpdate();
                //
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"successfully update",Toast.LENGTH_LONG).show();
                    get_date();
                    adapter = new CenterAdapter(getApplicationContext(), R.layout.rowinfofordoctor, DataList);
                    listView.setAdapter(adapter);
                }
            });
            return null;
        }
    }
}