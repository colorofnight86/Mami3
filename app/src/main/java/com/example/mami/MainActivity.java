package com.example.mami;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity{
    private View mContentView;
    private VideoView videoView;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaPlayer mediaPlayer2 = new MediaPlayer();
    private int NUM = 16;//soundPool最大音频数不超过256
    private int phase = 0;//游戏阶段
    private int phase2 = 0;
    private String type = "ED";
    private int H = 0;//场景id
    private int ud = 0;//服装
    private int ud_n;//选择的服装
    private String path = "Mami/";
    private SoundPool soundPool;
    private Map<String, Integer> poolMap;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mContentView = findViewById(R.id.fullscreen_content);
        videoView = (VideoView)findViewById(R.id.video_view);
        final Button backward = (Button)findViewById(R.id.backward);
        Button forward = (Button)findViewById(R.id.forward);
        final ImageButton h1 = (ImageButton)findViewById(R.id.h1);
        final ImageButton h2 = (ImageButton)findViewById(R.id.h2);
        final ImageButton h3 = (ImageButton)findViewById(R.id.h3);
        final ImageButton h4 = (ImageButton)findViewById(R.id.h4);
        final ImageButton ud1 = (ImageButton)findViewById(R.id.ud1);
        final ImageButton ud2 = (ImageButton)findViewById(R.id.ud2);
        final ImageButton ud3 = (ImageButton)findViewById(R.id.ud3);
        final ImageButton ud4 = (ImageButton)findViewById(R.id.ud4);
        poolMap = new HashMap<>();
        createSoundPoolIfNeeded();//创建soundPool
        poolMap.put("se_ok",soundPool.load(getPath(path+"se/se_ok.mp3"),1));
        poolMap.put("ud_click",soundPool.load(getPath(path+"se/se_pico.mp3"),1));
        poolMap.put("env_ocean",soundPool.load(getPath(path+"se/amm3_env_ocean_LP.mp3"),1));
        poolMap.put("env_shower",soundPool.load(getPath(path+"se/amm3_env_shower_LP.mp3"),1));
//        Toast.makeText(MainActivity.this, "声音资源加载中...",Toast.LENGTH_SHORT).show();

        //设置横向全屏
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //soundPool加载完成后设置选项监听
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (sampleId == poolMap.size()) {
                    Toast.makeText(MainActivity.this, "资源加载完成!",Toast.LENGTH_SHORT).show();
                    ud1.setOnClickListener(udClick);
                    ud2.setOnClickListener(udClick);
                    ud3.setOnClickListener(udClick);
                    ud4.setOnClickListener(udClick);
                    h1.setOnClickListener(hClick);
                    h2.setOnClickListener(hClick);
                    h3.setOnClickListener(hClick);
                    h4.setOnClickListener(hClick);
                }
            }
        });

        //视频播放完成监听
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                switch (phase){
                    case 0://标题画面
                        initVideoPath(path+"op/amm3_title_dr3_idle.mp4");
                        mediaPlayer.setLooping(true);
                        phase = 1;
                        break;
                    case 1://循环
                        mPlayer.start();
                        mPlayer.setLooping(true);
                        phase2 = 2;
                        break;
                    case 2://过渡动画
                        initVideoPath(path+"ud/amm3_ud0_ST.mp4");
                        phase = 3;
                        break;
                    case 3:////ud0的循环等待界面，只显示换装选项
                        initVideoPath(path+"ud/amm3_ud0_LP.mp4");
                        ud1.setVisibility(View.VISIBLE);
                        ud2.setVisibility(View.VISIBLE);
                        ud3.setVisibility(View.VISIBLE);
                        ud4.setVisibility(View.VISIBLE);
                        phase = 4;
                        phase2 = 4;
                        break;
                    case 4://循环
                        mPlayer.start();
                        mPlayer.setLooping(true);
                        break;
                    case 5://隐藏选项，播放当前服装结束动画，并播放新服装开场动画
                        h1.setVisibility(View.INVISIBLE);
                        h2.setVisibility(View.INVISIBLE);
                        h3.setVisibility(View.INVISIBLE);
                        h4.setVisibility(View.INVISIBLE);
                        ud1.setVisibility(View.INVISIBLE);
                        ud2.setVisibility(View.INVISIBLE);
                        ud3.setVisibility(View.INVISIBLE);
                        ud4.setVisibility(View.INVISIBLE);
                        initVideoPath(path+"ud/amm3_ud"+ud+"_"+type+".mp4");
                        initMediaPlayer(mediaPlayer2,path+"se/amm3_se_ud_change_"+type+".mp3");
                        ud = ud_n;
                        type=type.equals("ED")?"ST":"ED";//ST和ED循环交换
                        phase = ++phase2;//此时phase2=4，执行两边后方可跳出该case
                        break;
                    case 6://ud循环等待界面，显示换装和场景选项，隐藏当前服装选项
                        ud1.setVisibility(View.VISIBLE);
                        ud2.setVisibility(View.VISIBLE);
                        ud3.setVisibility(View.VISIBLE);
                        ud4.setVisibility(View.VISIBLE);
                        h1.setVisibility(View.VISIBLE);
                        h2.setVisibility(View.VISIBLE);
                        h3.setVisibility(View.VISIBLE);
                        h4.setVisibility(View.VISIBLE);
                        switch (ud){//隐藏当前服装选项
                            case 1:ud1.setVisibility(View.INVISIBLE);break;
                            case 2:ud2.setVisibility(View.INVISIBLE);break;
                            case 3:ud3.setVisibility(View.INVISIBLE);break;
                            case 4:ud4.setVisibility(View.INVISIBLE);break;
                            default:
                        }
                        initVideoPath(path+"ud/amm3_ud"+ud+"_LP.mp4");
                        phase = 7;
                        break;
                    case 7://循环
                        mPlayer.start();
                        mPlayer.setLooping(true);
                        break;
                    case 8://场景播放，隐藏选项
                        h1.setVisibility(View.INVISIBLE);
                        h2.setVisibility(View.INVISIBLE);
                        h3.setVisibility(View.INVISIBLE);
                        h4.setVisibility(View.INVISIBLE);
                        ud1.setVisibility(View.INVISIBLE);
                        ud2.setVisibility(View.INVISIBLE);
                        ud3.setVisibility(View.INVISIBLE);
                        ud4.setVisibility(View.INVISIBLE);
                        initVideoPath(path+"h/amm3_H"+H+"_def_ud"+ud+".mp4");
                        mediaPlayer.stop();
                        initMediaPlayer(mediaPlayer,path+"bg/bgm_H"+H+".mp3");
                        mediaPlayer.setLooping(true);
                        phase = 9;
                        phase2 = 0;
                        break;
                    case 9:
                        h1.setVisibility(View.INVISIBLE);
                        h2.setVisibility(View.INVISIBLE);
                        h3.setVisibility(View.INVISIBLE);
                        h4.setVisibility(View.INVISIBLE);
                        ud1.setVisibility(View.INVISIBLE);
                        ud2.setVisibility(View.INVISIBLE);
                        ud3.setVisibility(View.INVISIBLE);
                        ud4.setVisibility(View.INVISIBLE);
                        mediaPlayer.stop();
                        mediaPlayer2.stop();
                        initVideoPath(path+"op/amm3_title_dr3_start.mp4");
                        initMediaPlayer(mediaPlayer,path+"bg/bgm_main.mp3");
                        phase = 0;
                        ud = 0;
                        soundPool.pause(poolMap.get("env_ocean"));
                        soundPool.pause(poolMap.get("env_shower"));
                        break;
                    default:
                }
            }
        });

        //前进按钮设置监听
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (phase2){
                    case 0://播放开头动画
                        initVideoPath(path+"op/amm3_title_dr3_idle.mp4");
                        phase = 1;
                        phase2 = 2;
                        break;
                    case 2://循环播放标题画面
                        initMediaPlayer(mediaPlayer2,path+"se/se_ok.mp3");
                        initVideoPath(path+"ud/amm3_ud0_ST.mp4");
                        phase = 3;
                        phase2 = 3;
                        break;
                    case 3://播放过渡动画
                        initVideoPath(path+"ud/amm3_ud0_LP.mp4");
                        phase = 3;
                        break;
                    case 5:
                        initVideoPath(path+"ud/amm3_ud"+ud+"_LP.mp4");
                        phase = 6;
                        break;
                    default:
                }

            }
        });

        //后退按钮设置监听
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    switch (phase2){
                        case 4://从换装界面返回标题界面
                            phase = 9;
                            phase2 = 0;
                            break;
                        case 8:
                            phase = 5;
                            phase2 = 4;
                            break;
                        default:
                    }

            }
        });

        //获取读取存储权限并初始化开头视频音频
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            sharedPreferences = getPreferences(Activity.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            boolean copy = sharedPreferences.getBoolean("copy",false);
            //从assets复制文件到sdcard/Mami
            if(!copy) {//如果文件没拷贝过
                FileUtils.getInstance(getApplicationContext()).copyAssetsToSD("", "Mami").setFileOperateCallback(new FileUtils.FileOperateCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "释放文件", Toast.LENGTH_SHORT).show();
                        editor.putBoolean("copy", true);//表示已拷贝资源文件
                        if(editor.commit()){
                            Toast.makeText(MainActivity.this,"释放完成，请重新启动应用",Toast.LENGTH_SHORT)
                                    .show();
                        }
                        System.exit(0);//重启
                    }

                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(MainActivity.this, "释放文件失败", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }else {
                initVideoPath(path + "op/amm3_title_dr3_start.mp4");
                initMediaPlayer(mediaPlayer, path + "bg/bgm_main.mp3");
            }
        }
    }

    //换装选项点击事件
    private View.OnClickListener udClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ud_n = 0;
            phase = 5;
            phase2 = 4;
            soundPool.play(poolMap.get("ud_click"), 1.0f, 1.0f, 0, 0, 1.0f);
            switch (v.getId()) {
                case R.id.ud4:
                    ud_n++;
                case R.id.ud3:
                    ud_n++;
                case R.id.ud2:
                    ud_n++;
                case R.id.ud1:
                    ud_n++;
                    break;
                default:
            }
        }
    };

    //场景选项点击事件
    private View.OnClickListener hClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            phase = 8;
            phase2 = 8;
            soundPool.play(poolMap.get("se_ok"), 1.0f, 1.0f, 0, 0, 1.0f);
            switch (v.getId()) {
                case R.id.h1:
                    H = 1;
                    break;
                case R.id.h2:
                    poolMap.put("env_ocean",soundPool.play(poolMap.get("env_ocean")
                            ,1.0f,1.0f,1,-1,1.0f));
                    H = 2;
                    break;
                case R.id.h3:
                    poolMap.put("env_shower",soundPool.play(poolMap.get("env_shower")
                            ,1.0f,1.0f,1,-1,0.5f));
                    H = 3;
                    break;
                case R.id.h4:
                    H = 4;
                    break;
                default:
            }
        }
    };

    //获取文件存储路径
    private String getPath(String path){
        File file = new File(Environment.getExternalStorageDirectory(),path);
        return  file.getPath();
    }

    //初始化视频并播放
    private void initVideoPath(String path){
        File file = new File(Environment.getExternalStorageDirectory(),path);
        Toast.makeText(MainActivity.this,file.getPath(),Toast.LENGTH_SHORT);
        videoView.setVideoPath(file.getPath());
        videoView.start();
    }

    //初始化音频并播放
    private  void initMediaPlayer(MediaPlayer mediaPlayer,String path){
        if(mediaPlayer != null){
            mediaPlayer.reset();
        }
        try {
            File file = new File(Environment.getExternalStorageDirectory(),path);
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createSoundPoolIfNeeded() {
        if (soundPool == null) {
            // 5.0 及 之后
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                soundPool = new SoundPool.Builder()
                        .setMaxStreams(NUM)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else { // 5.0 以前
                soundPool = new SoundPool(NUM, AudioManager.STREAM_MUSIC, 0);  // 创建SoundPool
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoView != null){
            videoView.suspend();
        }
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if(mediaPlayer2 != null){
            mediaPlayer2.stop();
            mediaPlayer2.release();
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            }
    }


}
