package com.gameos.icons;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  TextView label = new TextView(this);
  label.setText("GameOS Comic Icons\n\nOpen Good Lock > Theme Park > Icon > Iconpack > Third party icon packs to select this pack.\n\nThis app does not replace your launcher.");
  label.setTextSize(20);
  label.setPadding(36,64,36,36);
  setContentView(label);
 }
}