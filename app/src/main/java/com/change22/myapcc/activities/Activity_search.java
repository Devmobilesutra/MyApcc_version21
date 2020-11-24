package com.change22.myapcc.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.change22.myapcc.R;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.database.TABLE_LOCATIONS;
import com.change22.myapcc.dtoModel.DTOLocation;

import java.util.ArrayList;
import java.util.List;

public class Activity_search extends AppCompatActivity {

    ImageButton btn_back = null;
    EditText edt_search = null;
    Context context = null;
    RecyclerView recycler_search = null;
    TextView txt_empty_search = null;

    List<DTOLocation> arrayDTO = null;
    Activity_search.RecyclerAdapter recyclerAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_search);

        context = this;
        initComponents();
        initComponentListener();
        bindComponentData("");

    }

    private void initComponents() {
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        edt_search = (EditText) findViewById(R.id.edt_search);

        recycler_search = (RecyclerView) findViewById(R.id.recycler_search);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_search.setLayoutManager(linearLayoutManager);
        recycler_search.setHasFixedSize(true);

        txt_empty_search = (TextView) findViewById(R.id.txt_empty_search);
    }

    private void initComponentListener() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_search.this, Activity_dashboard.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        edt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edt_search.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edt_search, 0);

            }
        });

        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bindComponentData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String search_text = edt_search.getText().toString();
                    search_text = search_text.trim();
                    if (search_text.length() > 0)
                        bindComponentData(search_text);
                    else
                        Snackbar.make(edt_search, "Please enter search text", Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    private void bindComponentData(String search_text) {
        arrayDTO = new ArrayList<DTOLocation>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recycler_search.setLayoutManager(linearLayoutManager);
        recycler_search.setHasFixedSize(true);

        arrayDTO = TABLE_LOCATIONS.get_searched_locations(search_text);
        Log.i("Search", "Searched arrayDTO is " + arrayDTO);

        if(arrayDTO!=null) {
            MyApp.log("In not null");
            recycler_search.setVisibility(View.VISIBLE);
            txt_empty_search.setVisibility(View.GONE);
            recyclerAdapter = new RecyclerAdapter(arrayDTO);
            recycler_search.setAdapter(recyclerAdapter);
        }
        else
        {
            recycler_search.setVisibility(View.GONE);
            txt_empty_search.setVisibility(View.VISIBLE);
        }
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

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row_search, viewGroup, false);
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
            public TextView txt_address = null, txt_latitude, txt_longitude;
            public MyViewHolder(View v) {
                super(v);
                MyApp.log("Fellows_list", "In RecyclerAdapter MyViewHolder");
                txt_address = (TextView) v.findViewById(R.id.txt_address);
                txt_latitude = (TextView) v.findViewById(R.id.txt_latitude);
                txt_longitude = (TextView) v.findViewById(R.id.txt_longitude);
            }


        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Activity_search.this, Activity_dashboard.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
