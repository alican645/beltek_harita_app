package com.example.beltekprojevol3;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.beltekprojevol3.databinding.ActivityAddNoteBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddNoteActivity extends AppCompatActivity {

    ActivityAddNoteBinding binding;
    SQLiteDatabase database;
    Intent getIntent;
    String locationName;
    private static final int PICK_IMAGE = 1;
    Bitmap selectedImage ;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    private  void LauncherKaydet(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentFromResult=result.getData();
                    if(intentFromResult!=null){
                        Uri imageLocation=intentFromResult.getData();

                        try {
                            if(Build.VERSION.SDK_INT>=28){
                                ImageDecoder.Source source =ImageDecoder.createSource(getContentResolver(),imageLocation);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                binding.image.setImageBitmap(selectedImage);
                            }else{
                                selectedImage=MediaStore.Images.Media.getBitmap(AddNoteActivity.this.getContentResolver(),imageLocation);
                                binding.image.setImageBitmap(selectedImage);

                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    public  void  selectImage(View v){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(v,"Galeriye Erişim İçin İzin İsteniyor",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }
        }else{
            Intent intentToGaleri=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGaleri);
        }
    }
    public byte[] save(Bitmap image){
        Bitmap kucukGorsel=smallerImage(image,300);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        kucukGorsel.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] gorselArray=outputStream.toByteArray();
        return gorselArray;
    }

    public Bitmap smallerImage(Bitmap image,int maximumSize){
        int width=image.getWidth();
        int height=image.getHeight();
        float scale =(float) width/(float) height;
        if (scale>1){
            width=maximumSize;
            height=(int)(width/scale);
        }else{
            height=maximumSize;
            width=(int) (height*scale);
        }
        return image.createScaledBitmap(image, width, height, true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);



        getIntent=getIntent();
        locationName=getIntent.getStringExtra("locationName");



        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists contents(id Integer Primary Key,username VARCHAR,locationName VARCHAR ,notes VARCHAR ,image BLOB,time VARCHAR)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        LauncherKaydet();
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                Intent inentToGaleri=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(inentToGaleri);

                Toast.makeText(AddNoteActivity.this, "Galeriye Gitmek İçin Gerekli", Toast.LENGTH_SHORT).show();
            }
        });


        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

        binding.kaydetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


              String note=binding.addNotePageEdt.getText().toString();
              byte[] image=save(selectedImage);
              ContentValues values = new ContentValues();
              values.put("locationName", locationName);
              values.put("notes",note);
              values.put("image",image);

              database.insert("contents",null,values);

              database.close();

                // In AddNoteActivity after saving note
                Intent resultIntent = new Intent();
                resultIntent.putExtra("noteSaved", true);
                setResult(RESULT_OK, resultIntent);



                finish();
            }
        });
    }


}