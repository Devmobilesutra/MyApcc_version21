package com.change22.myapcc.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.change22.myapcc.R;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.database.TABLE_LOCATIONS;
import com.change22.myapcc.dtoModel.DTOLocation;

import java.util.ArrayList;
import java.util.List;

public class ActivityAreasWeCover extends AppCompatActivity {

    Context context = null;
    List<DTOLocation> arrayDTO = null;
    RecyclerAdapter recyclerAdapter = null;
    TextView txt_toolbar_title,txt_fergusson=null,txt_jangali=null,txt_iti_aundh=null,txt_baner=null,txt_baner_gaon=null;
    ImageButton img_back;
    String fergusson[]= {"18.523508","73.841106"},jangali[]={"18.522801","73.848126"},iti[]={"18.553494","73.809488"},baner[]={"18.564129","73.776968"},baner_gaon[]={"18.5642982","73.7769174"};
    RecyclerView recycler_view_areas = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_areas_we_cover);
        context = this;
        initComponents();
        initComponentListener();
        bindcomponentdata();

        setAreaList();
    }

    private void setAreaList() {
        arrayDTO = new ArrayList<DTOLocation>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recycler_view_areas.setLayoutManager(linearLayoutManager);
        recycler_view_areas.setHasFixedSize(true);

        arrayDTO = TABLE_LOCATIONS.get_covered_locations();
        Log.i("Search", "Searched arrayDTO is " + arrayDTO);

        if(arrayDTO!=null) {
            MyApp.log("In not null");
            recyclerAdapter = new RecyclerAdapter(arrayDTO);
            recycler_view_areas.setAdapter(recyclerAdapter);
        }

    }

    private void initComponents() {
        txt_toolbar_title = (TextView) findViewById(R.id.txt_toolbar_title);
        img_back = (ImageButton) findViewById(R.id.img_back);
        txt_fergusson  =  (TextView)findViewById(R.id.ferguson);
        txt_jangali = (TextView)findViewById(R.id.jangali);
        txt_baner = (TextView)findViewById(R.id.baner);
        txt_baner_gaon = (TextView)findViewById(R.id.baner_gaon);
        txt_iti_aundh = (TextView)findViewById(R.id.iti_aundh);

        recycler_view_areas = (RecyclerView) findViewById(R.id.recycler_view_areas);

    }

    private void initComponentListener() {

        txt_fergusson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             search_on_map("Fergusson College Road",fergusson);
            }
        });

        txt_baner_gaon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_on_map("Baner Gaon Road",baner_gaon);
            }
        });

        txt_baner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_on_map("Baner Road",baner);
            }
        });

        txt_iti_aundh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_on_map("ITI Road, Aundh",iti);
            }
        });

        txt_jangali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_on_map("Jangali Maharaj Road", jangali);
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void bindcomponentdata() {
        txt_toolbar_title.setText("Areas We Cover");
    }

    public void search_on_map(String address,String[]latlong)
    {
        MyApp.set_session(MyApp.SESSION_SEARCH_LATITUDE, latlong[0]);
        MyApp.set_session(MyApp.SESSION_SEARCH_LONGITUDE, latlong[1]);
        MyApp.set_session(MyApp.SESSION_SEARCH_AREA, address);
        MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "Y");
        finish();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        List<DTOLocation> rowItems;
        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        public RecyclerAdapter(List<DTOLocation> rowItems) {
            MyApp.log("Fellows_list", "In RecyclerAdapter Constructor");
            this.rowItems = rowItems;
            MyApp.log("Fellows_list", "In RecyclerAdapter rowItems = " + rowItems);
        }

        @Override
        public RecyclerView.ViewHolder  onCreateViewHolder(final ViewGroup viewGroup, int i) {
            final RecyclerView.ViewHolder vh;
            MyApp.log("Fellows_list", "In RecyclerAdapter onCreateViewHolder");

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row_area_covers, viewGroup, false);
            vh = new MyViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView txt_latitude = (TextView) view.findViewById(R.id.txt_latitude);
                    TextView txt_longitude = (TextView) view.findViewById(R.id.txt_longitude);
                    TextView txt_address = (TextView) view.findViewById(R.id.txt_address);
                    String str_latitude = txt_latitude.getText().toString();
                    String str_longitude = txt_longitude.getText().toString();
                    String str_address = txt_address.getText().toString();
                    MyApp.set_session(MyApp.SESSION_SEARCH_LATITUDE, str_latitude);
                    MyApp.set_session(MyApp.SESSION_SEARCH_LONGITUDE, str_longitude);
                    MyApp.set_session(MyApp.SESSION_SEARCH_AREA, str_address);
                    MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "Y");
                       /* Intent intent = new Intent(context, Activity_dashboard.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);*/
                    finish();
                }
            });

            return vh;
        }

        @Override
        public int getItemViewType(int position) {
            MyApp.log("Fellows_list", "In RecyclerAdapter getItemViewType");
            return rowItems.get(position)!=null? VIEW_ITEM: VIEW_PROG;
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            MyApp.log("Fellows_list", "In RecyclerAdapter onBindViewHolder");
            if(holder instanceof MyViewHolder) {
                MyApp.log("Fellows_list", "In RecyclerAdapter instanceof MyViewHolder");
                DTOLocation item = rowItems.get(position);
                ((MyViewHolder)holder).txt_row_id.setText(item.getRow_id()+".");
                ((MyViewHolder)holder).txt_address.setText(item.getAddress());
                ((MyViewHolder)holder).txt_latitude.setText(item.getLatitude());
                ((MyViewHolder)holder).txt_longitude.setText(item.getLongitude());

            }


        }

        @Override
        public int getItemCount() {
            MyApp.log("Fellows_list", "In RecyclerAdapter getItemCount");
            return rowItems == null ? 0 : rowItems.size();
        }

        public void removeLastItem() {
            if(rowItems.get(rowItems.size()-1) == null) {
                rowItems.remove(rowItems.size() - 1);
                notifyItemRemoved(rowItems.size());
            }
        }

        public void removeAllItems() {
            rowItems.clear();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView txt_address = null, txt_latitude, txt_longitude, txt_row_id;
            public MyViewHolder(View v) {
                super(v);
                MyApp.log("Fellows_list", "In RecyclerAdapter MyViewHolder");
                txt_row_id = (TextView) v.findViewById(R.id.txt_row_id);
                txt_address = (TextView) v.findViewById(R.id.txt_address);
                txt_latitude = (TextView) v.findViewById(R.id.txt_latitude);
                txt_longitude = (TextView) v.findViewById(R.id.txt_longitude);
            }


        }

    }
}
