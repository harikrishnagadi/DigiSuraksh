package com.example.digidriver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by harikrishna on 13-11-2017.
 */

public class thanksact extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thankslayout);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
       TextView rustext=findViewById(R.id.rushtext);
       rustext.setVisibility(View.VISIBLE);

    }
}
