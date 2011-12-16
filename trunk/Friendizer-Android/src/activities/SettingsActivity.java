/**
 * 
 */
package activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author Guy
 *
 */
public class SettingsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Artists tab");
        setContentView(textview);
    }

}
