package com.example.beltekprojevol3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beltekprojevol3.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists users(id Integer Primary Key,username VARCHAR,password VARCHAR,email VARCHAR)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //kauıt ol yazısınına basıldığında kayıt ekranına gidilmesini sağlayan bölüm
        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MainActivity.this, SignupActivity.class);

                startActivity(intent);
            }
        });

        binding.loginBtn.setOnClickListener(view -> {
            String username = binding.usernameEdt.getText().toString().trim();
            String password = binding.passwordEdt.getText().toString().trim();

            if (isValidCredentials(username,password)) {
                // isValidCredentials fonksiyonunun true döndürmesi halinde çalışacak ve harita ekranına gidilecek kısım
                Intent intent=new Intent(MainActivity.this, MainScreenActivity.class);
                intent.putExtra("username",username);
                startActivity(intent);

            } else {
                showToast("Geçersiz kimlik bilgileri. Lütfen kullanıcı adınızı ve şifrenizi kontrol edin.");
            }
        });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidCredentials(String username, String password) {
        // Veritabanında kullanıcı adı ve şifre kontrolü
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        Cursor cursor = database.rawQuery(query, new String[]{username, password});

        // Eğer sorgudan sonuç döndüyse, kullanıcı adı ve şifre doğrudur
        if (cursor.getCount() > 0) {
            cursor.close();
            showToast("Giriş Başarılı");
            return true;
        } else {
            cursor.close();
           showToast("Giriş Başarısız");
            return false;
        }
    }
}
