package com.p14osheagmail.assistant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldManager;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        OneSheeldSdk.init(this);
        OneSheeldSdk.setDebugging(true);
        OneSheeldManager manager = OneSheeldSdk.getManager();
        manager.setConnectionRetryCount(1);
        manager.setAutomaticConnectingRetriesForClassicConnections(true);

//        OneSheeldScanningCallback scanningCallback = new OneSheeldScanningCallback() {
//            @Override
//            public void onScanStart() {
//
//            }
//
//            @Override
//            public void onDeviceFind(OneSheeldDevice device) {
//                OneSheeldSdk.getManager().cancelScanning();
//                device.connect();
//            }
//
//            @Override
//            public void onScanFinish(List<OneSheeldDevice> foundDevices) {
//
//            }
//        };
//
//        OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
//            @Override
//            public void onConnect(OneSheeldDevice device) {
//                // Output high on pin 13
//                device.digitalWrite(13,true);
//
//                // Read the value of pin 12
//                boolean isHigh=device.digitalRead(12);
//            }
//
//            @Override
//            public void onDisconnect(OneSheeldDevice device) {
//
//            }
//
//            @Override
//            public void onConnectionRetry(OneSheeldDevice device, int retryCount) {
//
//            }
//        };

//        manager.addConnectionCallback(connectionCallback);
//        manager.addScanningCallback(scanningCallback);
//        manager.scan();

        findViewById(R.id.microphoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak("Hello");

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
    }

    private String whatICanDo(){
        return getString(R.string.can_do);
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }

    private void recognition(String text){
        Log.e("Speech",""+text);
        String[] speech = text.split(" ");


        if(text.isEmpty()){
            speak("didnt not hear that can you repeat ");
        }

        if(text.contains("what can you do")){
            speak(whatICanDo());
        }


        if(text.contains("what") && text.contains("time")){
            SimpleDateFormat digitalTime = new SimpleDateFormat("HH:mm");
            SimpleDateFormat analogTime = new SimpleDateFormat("HH:mm a");
            Date now = new Date();
            String[] strDate = digitalTime.format(now).split(":");
            speak("The time is " + digitalTime.format(now) + " .or " + analogTime.format(now));
        }

        if(text.contains("what") && text.contains("date")){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = df.format(c.getTime());
            String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            speak(formattedDate);

        }

        if(text.contains("weather")){
            speak("it is probably raining");
        }

        if(text.contains("play") && text.contains("game")){
            //for the memory game
        }

        if(text.contains("play") && text.contains("music")){
            //for music
        }

        if(text.contains("lights")) {
            if (text.contains("turn on")) {
                speak("lights turned on");
            } else if (text.contains("turn off")) {
                speak("lights turned off");
            }
        }

        if(text.contains("wake me up at") || text.contains("alarm")){
            speak(speech[speech.length-1]);
            String[] time = speech[speech.length-1].split(":");
            String hour = time[0];
            String minutes = time[1];
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, Integer.valueOf(hour));
            i.putExtra(AlarmClock.EXTRA_MINUTES, Integer.valueOf(minutes));
            startActivity(i);
            speak("Setting alarm to ring at " + hour + ":" + minutes);
        }

        if(text.contains("thank you")){
            speak("Thank you too ");
        }
    }


}
