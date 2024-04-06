package com.example.beltekprojevol3;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class MyNotesActivity extends AppCompatActivity {
    com.example.beltekprojevol3.databinding.ActivityMyNotesBinding binding;

    Intent getIntent;

    SQLiteDatabase database;

    CustomAdapter customAdapter;
    ArrayList<String> noteArrayList;
    ArrayList<byte[]>imageArrayList;
    String locationName;

    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= com.example.beltekprojevol3.databinding.ActivityMyNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteArrayList =new ArrayList<>();
        imageArrayList=new ArrayList<>();

        //ArrayList ve Array Adaptörü birbirine bağladık
        customAdapter =new CustomAdapter(MyNotesActivity.this, noteArrayList,imageArrayList);
        binding.listView.setAdapter(customAdapter);

        getIntent =getIntent();
        locationName=getIntent.getStringExtra("locationName");

        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists contents(id Integer Primary Key,locationName VARCHAR,notes VARCHAR,image BLOB,time VARCHAR)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        guncelle();




        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(MyNotesActivity.this,AddNoteActivity.class);
                intent.putExtra("locationName",locationName);
                startActivityForResult(intent,1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            guncelle();

        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            // AddNoteActivity'den dönen veriyi alma
            guncelle();
        }
    }

    void guncelle(){
        noteArrayList.clear();
        imageArrayList.clear();
        customAdapter.notifyDataSetChanged();

        String[] projection = {"notes", "image"}; // "image" sütununu da projeksiyona ekleyin
        String selection = "locationName = ?";
        String[] selectionArgs = {locationName}; // Hedef locationName değeri

        Cursor cursor = database.query("contents", projection, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            byte[] imageData = cursor.getBlob(cursor.getColumnIndexOrThrow("image")); // Resim verisini al

            if(notes!=null){
                noteArrayList.add(notes);
                imageArrayList.add(imageData); // Resim verisini imageArrayList'e ekle
                customAdapter.notifyDataSetChanged();
            }

        }

        cursor.close();
    }

    public class CustomAdapter extends ArrayAdapter<String> {

        private final Context context;
        private final ArrayList<String> values;

        private final ArrayList<byte[]> images;

        public CustomAdapter(Context context, ArrayList<String> notes,ArrayList<byte[]>images) {
            super(context, R.layout.custom_list_item, notes);
            this.context = context;
            this.values = notes;
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custom_list_item, parent, false);
            TextView textView = rowView.findViewById(R.id.textView);
            ImageView imageView = rowView.findViewById(R.id.imageView);
            textView.setText(values.get(position));
            Bitmap bitmap=byteArrayToBitmap(images.get(position));
            imageView.setImageBitmap(bitmap);
            // Görüntüyü ayarlamak için gerekli kodu buraya ekleyin (örneğin, Glide veya Picasso kütüphaneleri kullanılabilir)
            return rowView;
        }
    }
}