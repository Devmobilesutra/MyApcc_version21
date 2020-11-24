package com.change22.myapcc.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.change22.myapcc.R;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.database.TABLE_GARBAGE;
import com.change22.myapcc.dtoModel.DTOIssue;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Activity_issues extends AppCompatActivity {

    RecyclerView recycler_view = null;
    Context context = null;

    RecyclerAdapter recyclerAdapter = null;
    ImageView img_upload_new = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_issues);
        context = this;
        initComponents();
        initComponentListener();
        // bindComponentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_issues"));

        bindComponentData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_issues"));
        context.unregisterReceiver(mMessageReceiver);
    }

    private void bindComponentData() {

        ArrayList<DTOIssue> issueArrayList = TABLE_GARBAGE.load_issue_items();
        MyApp.log("in initComponentListener() issueArrayList:" + issueArrayList);
        if (recyclerAdapter == null) {
            recyclerAdapter = new RecyclerAdapter(issueArrayList);
            recycler_view.setAdapter(recyclerAdapter);
        } else {
            recyclerAdapter.rowItems.clear();
            recyclerAdapter.rowItems.addAll(issueArrayList);
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    private void initComponents() {

        img_upload_new = (ImageView) findViewById(R.id.imageView);

        recycler_view = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recycler_view.setLayoutManager(linearLayoutManager);
        recycler_view.setHasFixedSize(true);



        /*recyclerAdapter = new RecyclerAdapter(issueArrayList);
        recycler_view.setAdapter(recyclerAdapter);*/
    }

    private void initComponentListener() {
       /* issueArrayList.addAll(TABLE_GARBAGE.load_issue_items());
        MyApp.log("in initComponentListener() issueArrayList:" + issueArrayList);*/

        /*img_upload_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MyApp.get_session(MyApp.SESSION_IS_REGISTERED).equalsIgnoreCase("Y")) {
                    Intent i = new Intent(Activity_issues.this, Activity_capture_photo.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(Activity_issues.this, Activity_Login.class);
                    startActivity(i);
                }

            }
        });*/
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<DTOIssue> rowItems;
        private final int VIEW_ISSUE = 1;
        private final int VIEW_AREA = 0;

        String fontPathBold = "roboto_regular_bold.ttf";
        // Loading Font Face
        Typeface tf_bold = Typeface.createFromAsset(context.getAssets(), fontPathBold);
        // Applying font
        // Font path
        String fontPathRegular = "roboto_regular.ttf";
        // Loading Font Face
        Typeface tf_regular = Typeface.createFromAsset(context.getAssets(), fontPathRegular);

        public RecyclerAdapter(ArrayList<DTOIssue> rowItems) {
            this.rowItems = rowItems;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ISSUE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_issues, parent, false);
                vh = new MyViewHolder(view);
            } else if (viewType == VIEW_AREA) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_area, parent, false);
                vh = new AreaViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_more, parent, false);
                vh = new MoreViewHolder(view);
            }

            return vh;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof MyViewHolder) {
                DTOIssue dtoIssue = rowItems.get(position);
                ((MyViewHolder) holder).txt_issue.setText(dtoIssue.getTitle());
                ((MyViewHolder) holder).txt_issue_address.setText(dtoIssue.getAddress());
                String str_date = dtoIssue.getStr_date();

                SimpleDateFormat input_sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat output_sdf = new SimpleDateFormat("dd-MMM-yyyy");

                Date input_date;

                try {
                    input_date = input_sdf.parse(str_date);
                    //MyApp.log("Activity_issues", "Date after input Date format " + input_date);
                    str_date = output_sdf.format(input_date);
                    //MyApp.log("Activity_issues", "str_date after output Date format " + str_date);
                } catch (ParseException e) {
                    MyApp.log("Activity_issues", "Date format exception is " + e.getMessage());
                }

                ((MyViewHolder) holder).txt_issue_date.setText(str_date);
                ((MyViewHolder) holder).txt_name.setText(dtoIssue.getName());
                String status = dtoIssue.getIssue_status();
                ((MyViewHolder) holder).txt_status.setText(status);
                if (status.equalsIgnoreCase("REPORTED")) {
                    ((MyViewHolder) holder).txt_status.setBackgroundColor(getResources().getColor(R.color.red));
                } else if (status.equalsIgnoreCase("RESOLVED")) {
                    ((MyViewHolder) holder).txt_status.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else if (status.equalsIgnoreCase("IN PROCESS")) {
                    ((MyViewHolder) holder).txt_status.setBackgroundColor(getResources().getColor(R.color.colororange));
                }

                MyApp.log("Activity_issue", "Image url is " + dtoIssue.getImage_url().toString());

                if (dtoIssue.getImage_url().toString().length() > 0) {
                    Picasso.with(context)
                            .load(dtoIssue.getImage_url().toString())
                            .placeholder(R.drawable.camera)
                            .error(R.drawable.camera)
                            .resize((int) getResources().getDimension(R.dimen.img_deal_medium_hw), (int) getResources().getDimension(R.dimen.img_deal_medium_hw))
                            .into(((MyViewHolder) holder).img_issue);
                } else {
                    Picasso.with(context)
                            .load(R.drawable.camera)
                            .resize((int) getResources().getDimension(R.dimen.img_deal_medium_hw), (int) getResources().getDimension(R.dimen.img_deal_medium_hw))
                            .into(((MyViewHolder) holder).img_issue);
                }
            } else if (holder instanceof AreaViewHolder) {
                DTOIssue dtoIssue = rowItems.get(position);
                ((AreaViewHolder) holder).btn_area.setText(dtoIssue.getArea());
            } else {
                //set area and page no to txtview invisible textview
                DTOIssue dtoIssue = rowItems.get(position);
                ((MoreViewHolder) holder).txt_more_area.setText(dtoIssue.getArea());
                MyApp.log("dtoIssue.getRow_count():" + dtoIssue.getRow_count());
                ((MoreViewHolder) holder).txt_more_page_no.setText(String.valueOf(dtoIssue.getRow_count()));


                ((MoreViewHolder) holder).img_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MyApp.log("txt_more_area:" + ((MoreViewHolder) holder).txt_more_area.getText() +
                                " txt_more_page_no:" + ((MoreViewHolder) holder).txt_more_page_no.getText());
                        String area = (String) ((MoreViewHolder) holder).txt_more_area.getText();
                        String page_no = (String) ((MoreViewHolder) holder).txt_more_page_no.getText();
                        MyApp.log("position before remove :" + position);
                        rowItems.remove(position);
                        MyApp.log("position after remove :" + position);
                        rowItems.addAll(position, TABLE_GARBAGE.load_more_items(area, page_no));

                        notifyDataSetChanged();

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return rowItems == null ? 0 : rowItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            MyApp.log("Activity_issuw", "In RecyclerAdapter getItemViewType");
            return rowItems.get(position).getItem_type();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {

            public ImageView img_issue = null;
            public TextView txt_issue = null, txt_issue_date = null, txt_issue_address = null, txt_name = null, txt_status = null;

            public MyViewHolder(View view) {
                super(view);

                img_issue = (ImageView) view.findViewById(R.id.img_issue);
                txt_issue = (TextView) view.findViewById(R.id.txt_issue);
                txt_issue_date = (TextView) view.findViewById(R.id.txt_issue_date);
                txt_issue_date.setTypeface(tf_regular);
                txt_issue_address = (TextView) view.findViewById(R.id.txt_issue_address);
                txt_issue_address.setTypeface(tf_regular);
                txt_name = (TextView) view.findViewById(R.id.txt_name);
                txt_name.setTypeface(tf_bold);
                txt_status = (TextView) view.findViewById(R.id.txt_status);
                txt_status.setTypeface(tf_bold);

            }
        }

        private class AreaViewHolder extends RecyclerView.ViewHolder {


            public Button btn_area = null;

            public AreaViewHolder(View view) {
                super(view);
                btn_area = (Button) view.findViewById(R.id.btn_area);
            }
        }

        private class MoreViewHolder extends RecyclerView.ViewHolder {

            public ImageView img_more = null;
            public TextView txt_more = null, txt_more_area = null, txt_more_page_no = null;

            public MoreViewHolder(View view) {
                super(view);
                img_more = (ImageView) view.findViewById(R.id.img_more);
                txt_more = (TextView) view.findViewById(R.id.txt_more);
                txt_more_area = (TextView) view.findViewById(R.id.txt_more_area);
                txt_more_page_no = (TextView) view.findViewById(R.id.txt_more_page_no);

            }
        }
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyApp.log("messege recieved");
            // Extract data included in the Intent
            //String service = intent.getStringExtra("service");
            if (intent != null) {
                if (intent.hasExtra("Flag")) {
                    // Toast.makeText(context, "Receive", Toast.LENGTH_SHORT).show();
                    if (intent.getStringExtra("Flag").equalsIgnoreCase("getTruckData")) {
                      /*if (arrMarker != null) {
                          int Size = arrMarker.size();
                          for (int i = 0; i < Size; i++) {
                              arrMarker.get(i).remove();
                          }
                      }
                      loadMarker();*/
                    } else if (intent.getStringExtra("Flag").equalsIgnoreCase("complaint_resolved")) {
                        MyApp.log("Flag->complaint_resolved");

                        bindComponentData();
                    }
                }
            }
        }

    };
}
