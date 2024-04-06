package com.example.beltekprojevol3;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beltekprojevol3.databinding.ActivityAddLocationBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

public class AddLocationActivity extends AppCompatActivity {

    ActivityAddLocationBinding binding;
    String locationName;
    int noteCounter=0;
    SharedPreferences sharedPreferences;
    ArrayList<String> arrayList;
    Intent getIntent;
    private static final int PICK_IMAGE = 1;
    Bitmap selectedImage;

    SQLiteDatabase database;
    //bitmap şeklindeki seçilen fotoğrafı akitiviteler arası göndermek için byte[] 'a
    //dönüştüren fonksiyonlar
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

        //binding başlatıldı
        binding=ActivityAddLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //shared preferences başladı
        sharedPreferences=this.getSharedPreferences("com.example.beltekprojevol3",MODE_PRIVATE);

        //intet  getIntent() nesnesi olarak başladırdı
        getIntent =getIntent();

        String username=getIntent.getStringExtra("username");

        binding.kaydetBtn.setEnabled(false);

        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists locations (id Integer Primary Key,username VARCHAR,locationName VARCHAR,latitude DOUBLE,longitude DOUBLE,mainImage BLOB)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }



        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE);


            }
        });
        binding.kaydetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // editText içerisindeki ifadeyi bir değişkene çektik..
                locationName=binding.addNotePageEdt.getText().toString();

                double latitude=getIntent.getDoubleExtra("latitude",0);
                double longitude=getIntent.getDoubleExtra("longitude",0);


                arrayList=getIntent.getStringArrayListExtra("arrayList");
                if(arrayList.contains(locationName)){
                    Toast.makeText(AddLocationActivity.this, "Bu İsimde Bir Lokasyon Var", Toast.LENGTH_SHORT).show();
                }else{
                    ContentValues values = new ContentValues();
                    values.put("username",username);
                    values.put("locationName", locationName);
                    values.put("latitude", latitude);
                    values.put("longitude", longitude);
                    values.put("mainImage",save(selectedImage));

                    database.insert("locations", null, values);

                    // listeyi güncellediğimizde bu liste sadece bu sınıf bazında güncellenir güncellenen listeyi
                    // diğer sayfada da kullanmak istiyorsak güncellenen yeni listeyi diğer sayfaya aktarmamız gerekir.
                    Intent resultIntent = new Intent();

                    setResult(RESULT_OK, resultIntent);
                    //setResult(RESULT_OK, resultIntent);:
                    // Bu satır, AddNoteActivity'nin sonlandığında geri dönüş kodunu
                    // ve içerdiği veriyi belirler. RESULT_OK, bir aktivitenin başarıyla tamamlandığını
                    // ifade eden bir sabittir. resultIntent ise içinde taşıdığı veriyle
                    // birlikte dönüş olarak ayarlanır.
                    // MainActivity, onActivityResult metodunu kullanarak bu dönüşü ele alabilir.

                    finish();
                }







            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                //Seçilen Fotoğrafın Adresini Alan Bölüm
                Uri selectedImageURI = data.getData();

                //Adresteki fotoğrafı bir bitmape dönüştüren bölüm
                ImageDecoder.Source source=ImageDecoder.createSource(getContentResolver(),selectedImageURI);
                try {
                    selectedImage=ImageDecoder.decodeBitmap(source);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //seçilen fotoğraf  bitmape dömüştükten sonra bu şekilde imageview üzerinde görüntüleyen bölüm
                binding.image.setImageBitmap(selectedImage);
                binding.kaydetBtn.setEnabled(true);// ImageView'da görüntüyü ayarlayın

            }
        }
    }
}