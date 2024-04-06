package com.example.beltekprojevol3;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beltekprojevol3.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists users(id Integer Primary Key,username VARCHAR,password VARCHAR,email VARCHAR)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Button registerButton = binding.buttonRegister;
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kullanıcı adı ve şifreyi al
                String username = binding.editTextUsername.getText().toString().trim();
                String email = binding.editTextEmail.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString().trim();

                // geçerli e posta sorgusu
                if (!isValidEmail(email)) {
                    showToast("Invalid email address");
                    return;
                }

                // şifre uzunluğu sorgusu
                if (password.length() < 8 || password.length() > 16) {
                    showToast("Password must be between 8 and 16 characters");
                    return;
                }

                // geçerli şifre sorgusu
                if (!isPasswordValid(password)) {
                    showToast("Password must contain at least one uppercase letter, one digit, and one special character");
                    return;
                }

                // geçerli kullanıcı adı sorgusu
                if (containsTurkishCharacters(username)) {
                    showToast("Username cannot contain Turkish characters");
                    return;
                }

                if (isValidEmail(email) && isPasswordValid(password) && !containsTurkishCharacters(username)) {
                    //doğru kayıt esnasında yeni kullanıcı nesnesini oluşturan satır
                    UserRegistrationInfo registrationInfo = new UserRegistrationInfo(username, email, password);

                    //oluşturulan kullanıcıyı veri tabanına ekleyen kod bloğu
                    ContentValues values = new ContentValues();
                    values.put("username",registrationInfo.getUsername());
                    values.put("password",registrationInfo.getPassword());
                    values.put("email",registrationInfo.getEmail());

                    database.insert("users",null,values);

                    database.close();
                    //finish();
                    Toast.makeText(SignupActivity.this, "Kayıt Başarılı Bir Şekilde Gerçekleştirildi", Toast.LENGTH_SHORT).show();
                } else {
                    // kayıt işlemleri düzgün gerçekleştirilmediyse gösterilecek mesaj
                    showToast("Invalid user information. Please check your input.");
                }

            }
        });
    }

    //geçerli ema
    private boolean isValidEmail(CharSequence target) {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //girilen şifre kriterkerini sorgulayan fonksiyon
    private boolean isPasswordValid(String password) {

        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
        return password.matches(passwordRegex);
    }
    //girilen kullanıcı adını sorgulayan fonksiyon
    private boolean containsTurkishCharacters(String username) {
        return username.matches(".*[çÇğĞıİöÖşŞüÜ].*");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    //Kullanıcı oluşturan sınıf
    private class UserRegistrationInfo {
        private String _username;
        private String _email;
        private String _password;

        public UserRegistrationInfo(String username, String email, String password) {
            this._username = username;
            this._email = email;
            this._password = password;
        }

        String getUsername(){
            return _username;
        }

        String getEmail(){
            return _email;
        }

        String getPassword(){
            return _password;
        }
    }
}
