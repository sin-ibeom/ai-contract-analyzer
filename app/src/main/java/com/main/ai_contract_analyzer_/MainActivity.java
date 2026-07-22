package com.main.ai_contract_analyzer_;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.Schema;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.coroutines.jvm.internal.GeneratedCodeMarkers;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String API_KEY = BuildConfig.API_KEY;


        Schema warningPersentage = Schema.Companion.str(
                "warning_persentage",
                "해당 계약서/약관을 보고 위험도 퍼센트 측정"
        );

        Schema warningItem = Schema.Companion.obj (
                "warning_item",
                "위험 조항 항목",
                Schema.Companion.str("warning_item_title", "위험 조항 항목의 원문"),
                Schema.Companion.str("warning_item_dec", "위험 조항 항목애 왜 위험한지 쉬운 설명")
        );

        Schema recommendMessage = Schema.Companion.str (
                "recommend_message",
                "협상 포인트"
        );

        // obj

        Schema schema1 = Schema.Companion.obj(
                "analyze",
                "계약서 분석 결과",
                warningPersentage,
                warningItem,
                recommendMessage
        );

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = schema1;
        GenerationConfig gc = configBuilder.build();


        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                API_KEY,
                gc
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        TextView tv = findViewById(R.id.main_text);

        Executor executor = Executors.newSingleThreadExecutor();
        // 질문 문자열을 객체로 보내야함

        String user_message = "";

        Content prompt = new Content.Builder().addText(user_message).build();



        // 약속 상자 제작 + 요청 시작
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);


        // AI 응답 도착 시
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    tv.setText(result.getText());
                });
            }

            @Override
            public void onFailure(Throwable t) {
                tv.setText("응답에 실패함");
                t.printStackTrace();

            }
        }, executor);



    }

    public void Gemini_Analyzer(GenerativeModelFutures model, Content prompt){
        Executor executor = Executors.newSingleThreadExecutor();
        TextView tv = findViewById(R.id.main_text);

        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);


        // AI 응답 도착 시
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    tv.setText(result.getText());
                });
            }

            @Override
            public void onFailure(Throwable t) {
                if(t instanceof java.net.SocketException){
                    tv.setText("서버가 응답하지 않습니다. 다시시도해 주세요.");
                } else if(t instanceof java.net.ConnectException){
                    tv.setText("네트워크에 연결하지 못했습니다. 다시시도해 주세요.");
                } else {
                    tv.setText("알 수 없는 오류가 발생했습니다.");
                }

                /*
                OKHttpClient를 사용해서 재시도 정책
                1 -> 2 -> 4
                 */

            }
        }, executor);
    }
}