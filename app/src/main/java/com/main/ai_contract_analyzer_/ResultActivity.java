package com.main.ai_contract_analyzer_;

import android.graphics.Color;
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
            int persentage = Integer.parseInt((rs.optString("warning_persentage").replace("%", "").trim()));
            warn_progress.setProgress(persentage);
            // 빨강
            if (persentage >= 90) {
                warn_persentage.setTextColor(Color.rgb(255, 59, 48));
            } else if (persentage >= 80) {
                warn_persentage.setTextColor(Color.rgb(255, 105, 51));

            // 주황
            } else if (persentage >= 70) {
                warn_persentage.setTextColor(Color.rgb(255, 149, 0));
            } else if (persentage >= 60) {
                warn_persentage.setTextColor(Color.rgb(255, 179, 64));

            // 노랑
            } else if (persentage >= 50) {
                warn_persentage.setTextColor(Color.rgb(255, 204, 0));
            } else if (persentage >= 40) {
                warn_persentage.setTextColor(Color.rgb(235, 219, 0));

            // 초록 계열
            } else if (persentage >= 30) {
                warn_persentage.setTextColor(Color.rgb(175, 219, 20));
            } else if (persentage >= 20) {
                warn_persentage.setTextColor(Color.rgb(102, 212, 69));
            } else if (persentage >= 10) {
                warn_persentage.setTextColor(Color.rgb(52, 199, 89));

            // 파랑
            } else {
                warn_persentage.setTextColor(Color.rgb(0, 199, 190));
            }


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
