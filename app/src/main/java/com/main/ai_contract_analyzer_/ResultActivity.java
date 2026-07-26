package com.main.ai_contract_analyzer_;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String result_json = getIntent().getStringExtra("JSON");

        try {
            JSONObject rs = new JSONObject(result_json);
            // warning_persentage
            // warning_wrapper
            // ㄴ warning_item_title
            // ㄴ warning_item_dec

            // recommend_message

            TextView warn_persentage = ((TextView) findViewById(R.id.warning_persentage));
            ProgressBar warn_progress = findViewById(R.id.warning_progressbar);
            LinearLayout warn_list = findViewById(R.id.warn_list);
            TextView recommend_msg = findViewById(R.id.recommend);

            warn_persentage.setText(rs.optString("warning_persentage"));
            warn_progress.setProgress(Integer.parseInt((rs.optString("warning_persentage").replace("%", "").trim())));


            JSONArray ja = rs.optJSONArray("warning_wrapper");
            if(warn_list != null){

                for(int i = 0; i < ja.length(); i++){

                    JSONObject jo = ja.optJSONObject(i);

                    if(jo != null){
                        String item_title = jo.optString("warning_item_title");
                        String item_dec = jo.optString("warning_item_dec")  ;

                        View view = getLayoutInflater().inflate(R.layout.item_layout, warn_list, false);

                        ((TextView) view.findViewById(R.id.warning_title)).setText(item_title);
                        ((TextView) view.findViewById(R.id.warning_dec)).setText(item_dec);

                        warn_list.addView(view);

                    }

                }

            }

            recommend_msg.setText(rs.optString("recommend_message"));




        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
