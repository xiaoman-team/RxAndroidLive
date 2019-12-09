package cn.xiaoman.library.android.rxandroidlive.sample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author jiechic
 */
public class BaseViewActivity extends AppCompatActivity {

    TextView content;

    StringBuilder sb = new StringBuilder();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context);
        content = findViewById(R.id.content);

        addContent("Activity onCreate");
    }


    @Override
    protected void onStart() {
        super.onStart();
        addContent("Activity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        addContent("Activity onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        addContent("Activity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        addContent("Activity onStop");
    }

    @Override
    protected void onDestroy() {
        addContent("Activity onDestroy");
        super.onDestroy();
    }

    protected void addContent(String content) {
        synchronized (BaseViewActivity.this) {
            sb.append(content);
            sb.append("\n");
            this.content.setText(sb.toString());
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}