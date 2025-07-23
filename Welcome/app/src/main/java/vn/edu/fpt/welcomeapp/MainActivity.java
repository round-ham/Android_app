package vn.edu.fpt.welcomeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private int danhDauChayThu = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("Danh dau onCreate chay thu :" +danhDauChayThu++);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Danh dau onStart chay thu :" +danhDauChayThu++);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Danh dau onResume chay thu :" +danhDauChayThu++);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Danh dau onPause chay thu :" +danhDauChayThu++);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Danh dau onStop chay thu :" +danhDauChayThu++);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Danh dau onDestroy chay thu :" +danhDauChayThu++);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("Danh dau onRestart chay thu :" +danhDauChayThu++);
    }
}