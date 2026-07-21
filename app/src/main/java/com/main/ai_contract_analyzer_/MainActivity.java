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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

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

        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                API_KEY
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        TextView tv = findViewById(R.id.main_text);

        Executor executor = Executors.newSingleThreadExecutor();
        // 질문 문자열을 객체로 보내야함

        Content prompt = new Content.Builder().addText("안드로이드 개발에 대해 설명해줘.").build();


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
}