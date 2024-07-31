package com.example.rootmap;

import android.app.Application;
import com.kakao.vectormap.KakaoMapSdk;
//카카오맵 sdk초기화
public class MapApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoMapSdk.init(this, "adbabacb6eeba95fa1b0adf991f6505c");
    }
}