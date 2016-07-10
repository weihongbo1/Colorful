package leon.colorful;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ColorfulHelper.ColorfulSwitchListener {

    private static final int OTHER_COLORFUL = 1;

    private Button btn;
    private ImageView imageView;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn_switch);
        imageView = (ImageView) findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorfulHelper colorfulHelper = ColorfulHelper.getColorfulHelper();
                if (colorfulHelper.getWhich() == OTHER_COLORFUL) {
                    ColorfulHelper.getColorfulHelper().switchColorful(ColorfulHelper.DEFAULT_COLORFUL, null);
                } else {
                    ColorfulHelper.getColorfulHelper().switchColorful(OTHER_COLORFUL, null);
                }
            }
        });
        ColorfulHelper colorfulHelper = ColorfulHelper.getColorfulHelper();
        colorfulHelper.init(getApplication());
        colorfulHelper.addColorful(new ColorfulHelper.ColorfulParameter(OTHER_COLORFUL, "leon.othercolorful", "othercolorful.apk"));
        colorfulHelper.addListener(this);
        onSwitch();
    }


    @Override
    public void onSwitch() {
        ColorfulHelper colorfulHelper = ColorfulHelper.getColorfulHelper();
        btn.setBackground(colorfulHelper.getDrawable(R.drawable.button_selector));
        imageView.setImageDrawable(colorfulHelper.getDrawable(R.drawable.telephone));
        text.setTextColor(colorfulHelper.getColor(R.color.text_color));
    }
}
