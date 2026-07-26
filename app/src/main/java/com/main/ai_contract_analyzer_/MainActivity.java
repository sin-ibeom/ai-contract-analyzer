package com.main.ai_contract_analyzer_;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;


import android.database.Cursor;
import android.provider.OpenableColumns;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.coroutines.jvm.internal.GeneratedCodeMarkers;


public class MainActivity extends AppCompatActivity {

    // ActivityResultLauncher : 다른 화면을 실행하고
    // 해당 화면에서 데이터를 가쟈온 후 반환
    private ActivityResultLauncher<String[]> pickLauncher;


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
        String gemini_model = "gemini-2.5-flash";
        String API_KEY = BuildConfig.API_KEY;
        GenerationConfig gc = setupConfig();

        GenerativeModel gm = new GenerativeModel(
                gemini_model,
                API_KEY,
                gc
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        TextView tv = findViewById(R.id.main_text);


        // registerForActivityResult :
        // 결과가 나오면 전달?

        // ActivityResultContracts.OpenDocument() :
        // 파일 탐색리를 키는 코드.
        //
        int MAX_MEGABYTE = 50;
        int MAX_SIZE = MAX_MEGABYTE * 1024 * 1024;
        pickLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                uri  -> {
                    if(uri != null){
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        if(cursor != null && cursor.moveToFirst()){
                            ((TextView) findViewById(R.id.TEXTTEXT)).setText(cursor.toString());
                            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            String fileName = cursor.getString(nameIndex);

                            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                            long fileSize = cursor.getLong(sizeIndex);

                            if(fileSize == 0 && !(cursor.isNull(sizeIndex))){
                                fileSize = 0;
                            }

                            if(fileSize > MAX_SIZE){
                                return;
                            }

                            cursor.close();
                            try {
                                ParcelFileDescriptor pdf = getContentResolver().openFileDescriptor(uri, "r");
                                PdfRenderer pdfRenderer = new PdfRenderer(pdf);
                                ((TextView) findViewById(R.id.TEXTTEXT)).setText(fileName);
                                PdfRenderer.Page page = pdfRenderer.openPage(0);


                                float density = getResources().getDisplayMetrics().density;
                                int pageWidthSize = (int) (page.getWidth() * density);
                                int pageHeightSize = (int) (page.getHeight() * density);

                                Bitmap bitmap = Bitmap.createBitmap(
                                        pageWidthSize,
                                        pageHeightSize,
                                        Bitmap.Config.ARGB_8888
                                );

                                Canvas canvas = new Canvas(bitmap);
                                canvas.drawColor(android.graphics.Color.WHITE);

                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);


                                ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputstream);
                                byte[] bytes = outputstream.toByteArray();


                                Bitmap combitemap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

//                                Gemini_Analyzer(model, combitemap);

                                runOnUiThread(() -> showPreviewDialog(model, combitemap));

                                page.close();
                                pdfRenderer.close();
                                pdf.close();

                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    } else {
                        ((TextView) findViewById(R.id.TEXTTEXT)).setText("pdf 선택 취소");
                    }
                });

        Button OpenPdf = findViewById(R.id.button);
        OpenPdf.setOnClickListener(view -> {
            pickLauncher.launch(new String[]{"application/pdf"});
        });


        ////////////////////////////////////////////////////////

        ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if(bitmap != null){
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                        byte[] bytes = outputStream.toByteArray();
                        Bitmap cobitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        runOnUiThread(() -> showPreviewDialog(model, cobitmap));

                    } else {
                        ((TextView) findViewById(R.id.TEXTTEXT)).setText("카메라 선택 취소");
                    }
                }
        );

        Button OpenCamera = findViewById(R.id.button2);
        OpenCamera.setOnClickListener(v -> {
            cameraLauncher.launch(null);
        });



        ////////////////////////////////////////////////////////











    }

    public void Gemini_Analyzer(GenerativeModelFutures model, Bitmap bitmap){
        Executor executor = Executors.newSingleThreadExecutor();
        TextView tv = findViewById(R.id.main_text);



        Content prompt = new Content.Builder()
                .addImage(bitmap)
                .addText("해당 계약서를 보고 JSON에 맞춰서 보내줘, 계약서가 아니라면 그냥 없음으로 해, 최대한 꼼꼼히 보며 해야해")
                .build();



        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);

        tv.setText("분석하는 동안 화면을 종료하지 마세요.");
        Toast.makeText(this, "계약서 분석 중...", Toast.LENGTH_SHORT).show();
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        // AI 응답 도착 시
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    try {
                        String rs = result.getText();
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("JSON", rs);
                        startActivity(intent);

                        // 옛 코드
//                        ((TextView) findViewById(R.id.warning_persentage)).setMovementMethod(new android.text.method.ScrollingMovementMethod());
//                        ((TextView) findViewById(R.id.recommend)).setMovementMethod(new android.text.method.ScrollingMovementMethod());
//                        ((TextView) findViewById(R.id.warning_item)).setMovementMethod(new android.text.method.ScrollingMovementMethod());
//
//                        ((TextView) findViewById(R.id.warning_persentage)).append(rs.optString("warning_persentage"));
//                        ((TextView) findViewById(R.id.recommend)).append(rs.optString("recommend_message"));
//
//
//
//                        JSONArray wrapper_arr = rs.getJSONArray("warning_wrapper");
//                        for(int i = 0; i < wrapper_arr.length(); i++){
//                            JSONObject item = wrapper_arr.getJSONObject(i);
//                            ((TextView) findViewById(R.id.warning_item))
//                                    .append("-위험 조항-\n" +
//                                            item.optString("warning_item_title") +
//                                            "-위험 조항 이유-" +
//                                            item.optString("warning_item_dec"));
//                        }



                    } finally {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }

                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(()-> {
                    String msg;
                    if (t instanceof java.net.UnknownHostException) {
                        msg = "인터넷 연결을 확인해주세요.";
                    } else if (t instanceof java.net.SocketTimeoutException) {
                        msg = "응답 시간이 초과되었습니다. 다시 시도해주세요.";
                    } else if (t instanceof java.net.SocketException) {
                        msg = "서버가 응답하지 않습니다. 다시시도해 주세요.";
                    } else if (t instanceof java.net.ConnectException) {
                        msg = "네트워크에 연결하지 못했습니다. 다시시도해 주세요.";
                    } else {
                        Snackbar.make(findViewById(R.id.main), "알 수 없는 오류가 발생했습니다.", Snackbar.LENGTH_SHORT)
                                .show();
                        msg = "오류: " + t.getClass().getSimpleName();
                        Log.e("GeminiAnalyzer", "메시지: " + t.getMessage());
                    }
                    tv.setText(msg);
                });
                /*
                OKHttpClient를 사용해서 재시도 정책
                1 -> 2 -> 4

                - 무산 -
                 */

            }
        }, executor);
    }

    public GenerationConfig setupConfig(){
        Schema warningPersentage = Schema.Companion.str(
                "warning_persentage",
                "해당 계약서/약관을 보고 위험도 퍼센트 측정"
        );

        Schema warningWrapper = Schema.Companion.arr(
                "warning_wrapper",
                "위험 조항들을 모아둔 배열",
                Schema.Companion.obj (
                        "warning_item",
                        "위험 조항 항목",
                        Schema.Companion.str("warning_item_title", "위험 조항 항목의 원문"),
                        Schema.Companion.str("warning_item_dec", "위험 조항 항목애 왜 위험한지 쉬운 설명")
                )
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
                warningWrapper,
                recommendMessage
        );


        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = schema1;
        GenerationConfig gc = configBuilder.build();

        return gc;
    }


    ////////////////////////////////////////////////////////
    private void showPreviewDialog(GenerativeModelFutures model, Bitmap previewBitmap) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_preview, null);

        ImageView previewImage = dialogView.findViewById(R.id.dialog_image);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        if (previewBitmap != null) {
            previewImage.setImageBitmap(previewBitmap);
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)
                .setCancelable(false) // 바깥 배경을 눌러서 꺼지는 것 방지
                .create();

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // 팝업창 닫기
            ((TextView) findViewById(R.id.TEXTTEXT)).setText("분석을 취소했습니다.");
        });

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss(); // 팝업창을 먼저 닫고
            // 제미나이 분석을 시작
            Gemini_Analyzer(model, previewBitmap);
        });

        // 6. 완성된 팝업창 보여줌
        dialog.show();
    }
}