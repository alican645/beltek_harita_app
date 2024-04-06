package com.example.beltekprojevol3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beltekprojevol3.databinding.ActivityVoiceNotesBinding;

import java.io.IOException;
import java.util.ArrayList;

public class VoiceNotesActivity extends AppCompatActivity {



    int minutes = 0;
    int seconds = 0;
    boolean isRecording = false;
    ArrayList<String> timeArrayList;
    Handler handler;
    Runnable runnable;
    //ortamdaki sesleri ses dosyası olarak kaydetmemizi sağlayan sınıf
    SQLiteDatabase database;
    String kayitYolu;
    ActivityVoiceNotesBinding binding;
    Intent getIntent;
    String locationName;
    CustomAdapter customAdapter;
    String timeUpdate;
    int progressUpdate;
    MediaRecorder recorder;
    private final String  filepath = Environment.getExternalStorageDirectory().getPath() + "/record.3gp";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoiceNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists contents(id Integer Primary Key,locationName VARCHAR,notes VARCHAR,image BLOB,time VARCHAR)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        getIntent=getIntent();
        locationName=getIntent.getStringExtra("locationName");

        timeArrayList=new ArrayList<>();
        customAdapter=new CustomAdapter(VoiceNotesActivity.this,timeArrayList);
        binding.listView.setAdapter(customAdapter);

        guncelle();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    startRecording();
//                    startRecordingVoice();

                    binding.recordBtn.setText("Kaydet");
                }
                else {

                    ContentValues values = new ContentValues();
                    values.put("locationName", locationName);
                    String time;
                    time = getTotalSeconds(minutes,seconds);
                    values.put("time",time);
                    Log.println(seconds,"asdasd","asd");

                    database.insert("contents",null,values);
                    stopRecording();
//                    stopRecordingVoice();
                    guncelle();


                    binding.recordBtn.setText("Başlat");
                }
            }
        });
    }

    void guncelle(){
        timeArrayList.clear();
        customAdapter.notifyDataSetChanged();

        String[] projection = {"time"}; // "image" sütununu da projeksiyona ekleyin
        String selection = "locationName = ?";
        String[] selectionArgs = {locationName}; // Hedef locationName değeri

        Cursor cursor = database.query("contents", projection, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int timeIndex=cursor.getColumnIndex("time");
            String time = cursor.getString(timeIndex);

            if(time!=null){
                timeArrayList.add(time); // Resim verisini imageArrayList'e ekle
                customAdapter.notifyDataSetChanged();
            }
        }

        cursor.close();
    }

    public class CustomAdapter extends ArrayAdapter<String> {

        private Context context; // Bağlam değişkeni, etkinlikler ve kaynaklara erişim sağlar
        private ArrayList<String> timeArrayList; // Görüntülenen süre verilerinin listesi
        private boolean isPlaying = false; // Sesin oynatılıp oynatılmadığını belirten bayrak
        private Handler handler; // Asenkron işlemler için zamanlayıcı yönetimi sağlar
        private int currentPosition = -1; // Mevcut oynatma pozisyonunu takip eden değişken

        public CustomAdapter(Context context, ArrayList<String> timeArrayList) {
            super(context, R.layout.custom_list_item2, timeArrayList); // Üst sınıfın kurucusunu çağırır
            this.context = context; // Bağlamı atanır
            this.timeArrayList = timeArrayList; // Süre veri listesini atar
            handler = new Handler(); // Zamanlayıcıyı başlatır
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); // Layout inflater'ı başlatır
            View rowView = inflater.inflate(R.layout.custom_list_item2, parent, false); // Özel liste öğesini şişirir

            final ProgressBar progressBar = rowView.findViewById(R.id.progressBar); // İlerleme çubuğunu alır
            final ImageView imageView = rowView.findViewById(R.id.imageView); // ImageView'ı alır
            final TextView timeTextView = rowView.findViewById(R.id.timeTEXTVIEW); // Zaman TextView'ını alır

            timeTextView.setText(convertToMinuteSecond(Integer.parseInt(timeArrayList.get(position)))); // Süreyi zaman TextView'ına yazar


            imageView.setOnClickListener(new View.OnClickListener() { // ImageView'a tıklama dinleyicisi ekler
                @Override
                public void onClick(View view) {
                    if (!isPlaying) { // Eğer ses oynatılmıyorsa
                        int totalSeconds = Integer.parseInt(timeArrayList.get(position)); // Toplam süreyi alır
                        final int progressBarMaxValue = 100; // Progress barın maksimum değerini ayarlar

                        final long stepDuration = (totalSeconds * 1000) / progressBarMaxValue; // Adım süresini belirler

                        currentPosition = position; // Mevcut pozisyonu belirler

                        imageView.setImageResource(R.drawable.baseline_pause_24); // Duraklatma simgesini ayarlar

                        final int finalTotalSeconds = totalSeconds; // Toplam saniyeyi kaydeder

                        handler.postDelayed(new Runnable() { // Zamanlanmış bir işlemi gerçekleştirir
                            int progress = 0; // İlerleme başlangıç değeri


                            @Override
                            public void run() { // İlerleme işlemini gerçekleştirir
                                progressUpdate=0;//progressUpdate Sıfırlandı
                                if (position != currentPosition) { // Eğer pozisyon değişmişse
                                    return; // İşlemi sonlandırır
                                }

                                progressBar.setProgress(progress); // İlerlemeyi günceller
                                timeTextView.setText(convertToMinuteSecond(progress * finalTotalSeconds / 100)); // Zamanı günceller

                                progressUpdate=progress;
                                timeUpdate =convertToMinuteSecond(progress*finalTotalSeconds/100);

                                if (progress >= progressBarMaxValue) { // Eğer ilerleme maksimum değere ulaştıysa
                                    handler.removeCallbacks(this); // Zamanlayıcıyı durdurur
                                } else { // Değilse
                                    progress++; // İlerlemeyi artırır
                                    handler.postDelayed(this, stepDuration); // İlerlemeyi devam ettirir
                                }
                            }
                        }, stepDuration); // Belirtilen zaman aralığında işlemi gerçekleştirir

                        isPlaying = true; // Ses oynatıldığını işaretler
                    } else { // Eğer ses oynatılıyorsa
                        if (position == currentPosition) { // Eğer tıklanan pozisyon mevcut pozisyon ise
                            handler.removeCallbacksAndMessages(null); // Tüm işlemleri ve mesajları temizler
                            progressBar.setProgress(progressUpdate); // İlerlemeyi durdurulan süreye göre ilerler
                            timeTextView.setText(timeUpdate); // Zamanı durdurulan süreye göre ayarlar
                            imageView.setImageResource(R.drawable.baseline_play_arrow_24); // Oynatma simgesini ayarlar
                            isPlaying = false; // Sesin oynatılmadığını işaretler
                        }
                    }
                }
            });

            return rowView; // Görünümü döndürür
        }

    }

    private void startRecording1(boolean isRecording) {
        if (!isRecording) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filepath);
            try {
                recorder.prepare();
                recorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    seconds++;
                    if (seconds == 60) {
                        minutes++;
                        seconds = 0;
                    }
                    updateTimeTextView();
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnable);
            this.isRecording = true;
        } else {
            if (recorder != null) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            }
            handler.removeCallbacks(runnable);
            this.isRecording = false;
            resetTimer();
        }
    }


    private void startRecordingVoice() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(filepath);
        try {
            recorder.prepare();
            recorder.start();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingVoice() {
        if (recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    private String getTotalSeconds(int minutes, int seconds) {
        return String.valueOf(minutes * 60 + seconds);
    }
    public String convertToMinuteSecond(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    private void startRecording() {

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                if (seconds == 60) {
                    minutes++;
                    seconds = 0;
                }
                updateTimeTextView();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
        isRecording = true;
    }
    private void stopRecording() {
        handler.removeCallbacks(runnable);
        isRecording = false;
        resetTimer();
    }
    private void resetTimer() {
        minutes = 0;
        seconds = 0;
        updateTimeTextView();
    }
    private void updateTimeTextView() {
        String timeText = String.format("%02d:%02d", minutes, seconds);
        binding.time.setText(timeText);
    }

}
