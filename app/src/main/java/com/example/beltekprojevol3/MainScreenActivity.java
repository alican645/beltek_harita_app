package com.example.beltekprojevol3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.beltekprojevol3.databinding.ActivityMainScreenBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainScreenActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    LatLng myLatLng;
    ActivityMainScreenBinding binding;

    ArrayAdapter arrayAdapter;
    ArrayList<String> locationNames;
    ArrayList<LatLng>latLngArrayList;
    Intent intent;
    Intent intent1;
    Intent getIntent;

    String username;

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityMainScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //lokasyon isimlerini tutan arraylist nesnesi başlatıldı.
        locationNames=new ArrayList<>();
        latLngArrayList=new ArrayList<>();

        //ArrayList ve Array Adaptörü birbirine bağladık
        arrayAdapter=new ArrayAdapter<>(MainScreenActivity.this, android.R.layout.simple_list_item_1,android.R.id.text1, locationNames);
        binding.notListview.setAdapter(arrayAdapter);

        //önceki sayfan yönlendirilen username değişkeni alındı.
        getIntent=getIntent();
        username=getIntent.getStringExtra("username");


        try{
            database=this.openOrCreateDatabase("location", MODE_PRIVATE,null);
            database.execSQL("Create Table If Not Exists locations (id Integer Primary Key,username VARCHAR,locationName VARCHAR,latitude DOUBLE,longitude DOUBLE,mainImage BLOB)");
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }

        String selectedUsername = username; // Seçilen kullanıcı adı

        Cursor cursor = database.rawQuery("SELECT locationName FROM locations WHERE username = ?", new String[] { selectedUsername });
        //veritababnında önceki sayfadan gelen kullanıcı adına sahip satırları döndüren ve bu starılar içerisinden location name paratmeresini çekip
        //bunu arraylisteye ekleyen ve her seferinde güncelleyen kod bloğu
        if (cursor.moveToFirst()) {
            do {
                int locationNameIndex=cursor.getColumnIndex("locationName");
                String locationName = cursor.getString(locationNameIndex);
                locationNames.add(locationName);
                arrayAdapter.notifyDataSetChanged();
            } while (cursor.moveToNext());
        }

        cursor.close();

        setUpMap();

        //ListView içerisindeki her bir öğe için aktivite sayfası oluşturan blok
        binding.notListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String locationName=locationNames.get(position);

                intent1=new Intent(MainScreenActivity.this, LocationContentActivity.class);
                intent1.putExtra("username",username);
                intent1.putExtra("locationName",locationName);

                startActivityForResult(intent1,2);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(myLatLng!=null){
                    //lokasyon eklme sayfasına yönlendiren satır
                    intent=new Intent(MainScreenActivity.this,AddLocationActivity.class);

                    //ilgili lokasyonun latitude ve longitude değerlerini veri tabanına eklemek için diğer sayfa yönlendirdik.
                    intent.putExtra("username",username);
                    intent.putExtra("latitude",myLatLng.latitude);
                    intent.putExtra("longitude",myLatLng.latitude);

                    intent.putStringArrayListExtra("arrayList",locationNames);

                    //Lokasyon ekleme işlemi ilgili sayfada olacaği için listeye location ekleme işlemini
                    //diğer sayfada yapmamız gerekir bunun için elimizdeki lokasyon listesini yeni
                    //bir lokasyon eklenmek üzere diğer sayfaya aktarıyoruz.
                    //intent.putStringArrayListExtra("arrayList",arrayList);
                    startActivityForResult(intent,1);
                }else{
                    Toast.makeText(MainScreenActivity.this, "Lütfen bir Lokasyon Seçiniz", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }



    // Bu metod, bir aktiviteden başka bir aktiviteye geçiş sonrasında elde edilen sonuçları işlemek için kullanılır.
    // requestCode, başlatılan aktivitenin isteğinin bir benzersiz tanımlayıcısıdır.
    // resultCode, başlatılan aktivitenin sonuç durumunu belirtir (örneğin, başarıyla tamamlandı ya
    // da kullanıcı tarafından iptal edildi). data, başlatılan aktivitenin dönüş verisini içerir.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Bu if koşulu, başlatılan aktivitenin isteğinin 1
        // (bu durumda AddNoteActivity tarafından başlatıldığını belirtir)
        // ve sonuç durumunun RESULT_OK (aktivite başarıyla tamamlandı) olup olmadığını kontrol eder.
        if (requestCode == 1 && resultCode == RESULT_OK) {

            // MainActivity içindeki listeyi güncelleme
            locationNames.clear();
            arrayAdapter.notifyDataSetChanged();
            //mayLatLng ifadesi aynı zamanda iki seçim yapılırsa ilk seferinde null ikinci seferinde !null olarak döner ve ikinci defa
            //lokason ekle butonna basıldığında lokasyon seçiniz uyarısı verilmez bunun önüne geçmek için myLatlng ifadesi bu sayfaya her dönüüldüğünde
            //null olarak tekrardan güncellenir
            myLatLng=null;

            Cursor cursor = database.rawQuery("SELECT locationName FROM locations WHERE username = ?", new String[] { username });
            if (cursor.moveToFirst()) {
                do {

                    int locationNameIndex=cursor.getColumnIndex("locationName");
                    String locationName = cursor.getString(locationNameIndex);
                    locationNames.add(locationName);
                    arrayAdapter.notifyDataSetChanged();
                } while (cursor.moveToNext());
            }
            cursor.close();

            setUpMap();

        }

        if (requestCode == 2 && resultCode == RESULT_OK) {

            // MainActivity içindeki listeyi güncelleme
            locationNames.clear();
            arrayAdapter.notifyDataSetChanged();
            //mayLatLng ifadesi aynı zamanda iki seçim yapılırsa ilk seferinde null ikinci seferinde !null olarak döner ve ikinci defa
            //lokason ekle butonna basıldığında lokasyon seçiniz uyarısı verilmez bunun önüne geçmek için myLatlng ifadesi bu sayfaya her dönüüldüğünde
            //null olarak tekrardan güncellenir
            myLatLng=null;

            Cursor cursor = database.rawQuery("SELECT locationName FROM locations WHERE username = ?", new String[] { username });

            if (cursor.moveToFirst()) {
                do {

                    int locationNameIndex=cursor.getColumnIndex("locationName");
                    String locationName = cursor.getString(locationNameIndex);
                    locationNames.add(locationName);
                    arrayAdapter.notifyDataSetChanged();
                } while (cursor.moveToNext());
            }
            cursor.close();

            setUpMap();
        }

    }

    private void setUpMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.clear();



        //girilen kullanıcı adına göre markerlerı ekleyen kod bloğu
        Cursor cursor = database.rawQuery("SELECT latitude, longitude FROM locations WHERE username = ?", new String[] { username });
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int latitudeIndex=cursor.getColumnIndex("latitude");
                int longitudeIndex=cursor.getColumnIndex("longitude");
                double latitude = cursor.getDouble(latitudeIndex);
                double longitude = cursor.getDouble(longitudeIndex);
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Haritayı kullanıcı etkileşimine izin verecek şekilde yapılandırın
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Kullanıcı tarafından yer işareti eklemesine izin ver
        mMap.setOnMapClickListener(latLng -> {
            // Mevcut tüm işaretçileri temizleyin
            mMap.clear();
            // Tıklanan konumda yeni bir işaretçi ekleyin
            mMap.addMarker(new MarkerOptions().position(latLng).title("Custom Location"));
            // Kamerayı yeni işaretçiye taşıyın
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            //lokasyon ekleme sayfasına yönlendiren buton
            myLatLng=latLng;

        });


    }







}