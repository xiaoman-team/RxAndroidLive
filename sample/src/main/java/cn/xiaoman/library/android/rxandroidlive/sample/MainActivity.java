package cn.xiaoman.library.android.rxandroidlive.sample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author jiechic
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ObservableNotRestart).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ObservableNotRestartActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ObservableAutoRestart).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ObservableAutoRestartActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.FlowableNotRestart).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FlowableNotRestartActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.FlowableAutoRestart).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FlowableAutoRestartActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.Single).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SingleActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.Maybe).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MaybeActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.Completable).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CompleteActivity.class);
                startActivity(intent);
            }
        });
    }
}
