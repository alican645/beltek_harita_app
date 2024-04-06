package com.example.beltekprojevol3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.beltekprojevol3.databinding.ActivityLocationContentBinding;

import java.util.Base64;


public class LocationContentActivity extends AppCompatActivity {

    ActivityLocationContentBinding binding;
    Intent intent;
    SQLiteDatabase database;
    String locationName;
    String username;
    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding =ActivityLocationContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent=getIntent();
        locationName=intent.getStringExtra("locationName");
        username=intent.getStringExtra("username");
        binding.locationNameEdt.setText(locationName);
        binding.locationNameEdt.setEnabled(false);
        binding.locationNameEdt.setTextColor(Color.BLACK);

        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists locations (id Integer Primary Key,username VARCHAR,locationName VARCHAR,latitude DOUBLE,longitude DOUBLE,mainImage BLOB)");
            database.execSQL("Create Table If Not Exists contents(id Integer Primary Key,locationName VARCHAR,notes VARCHAR,image BLOB,time VARCHAR)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        String selectQuery = "SELECT mainImage FROM locations WHERE username = '" + username + "' AND locationName = '" + locationName + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst()) {
            int mainImageIndex=cursor.getColumnIndex("mainImage");
            byte[] mainImageBytes = cursor.getBlob(mainImageIndex);
            binding.image.setImageBitmap(byteArrayToBitmap(mainImageBytes));
        }
        cursor.close();


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.notlarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LocationContentActivity.this,MyNotesActivity.class);
                intent.putExtra("locationName", locationName);
                startActivity(intent);
            }
        });


        binding.sesliNotlarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LocationContentActivity.this,VoiceNotesActivity.class);
                intent.putExtra("locationName", locationName);
                startActivity(intent);
            }
        });
        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                String locationName=intent.getStringExtra("locationName");
                String deleteQuery = "DELETE FROM locations WHERE locationName = '" + locationName+ "'";
                database.execSQL(deleteQuery);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });



    }
}