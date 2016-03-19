package com.example.android.pathfinder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HelpActivity extends AppCompatActivity {
    private static String FORMAT_BREAK = "<br>";
    private String HELP_TEXT = "<p>When Pathfinder starts a blue marker will be placed at the current position. and the &quot;Start Address&quot; field will be filled in.</p>\n" +
                    "\n" +
                    "<p>The user may place a destination marker by either: 1. Press and Hold on the map to place a red marker. 2. An address may be entered in the &quot;Going to...&quot; field of the search area.</p>\n" +
                    "\n" +
                    "<p>The red and blue markers may be moved by pressing and dragging them to a new location. They may also be moved by re-entering a new address.</p>\n" +
                    "\n" +
                    "<p>Pathfinder routes can be created by using the current date/time &quot;Leaving now&quot; or by pressing on the drop down menu to select &quot;Leaving at...&quot; or &quot;Arrive at...&quot; options.</p>\n" +
                    "\n" +
                    "<p>Selecting &quot;Leaving&quot; or &quot;Arriving&quot; will allow the Date and Time selector buttons to become active.</p>\n" +
                    "\n" +
                    "<p>Once both Start and Destination markers have been placed, a Continue button will be displayed. When pressed, each of the routes found will be displayed.</p>\n" +
                    "\n" +
                    "<p>Selecting one of the routes found will update the map highlighting. &quot;Pressing and Holding&quot; a route will display the detailed information about the route.</p>\n" +
                    "\n" +
                    "<p>&quot;Pressing&quot; on a detail will further expand it revealing even more details about the selected route.</p>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UpdateHelp();
    }

    private void UpdateHelp() {
        TextView view = (TextView) findViewById(R.id.help_textView);
        String text = "";

        text += FORMAT_BREAK;
        text += HELP_TEXT;
//        text += getResources().getString(R.string.help_activity_text);

        Spanned result = Html.fromHtml(text);
        view.setText(result);
        view.setMovementMethod(new ScrollingMovementMethod());
    }
}
