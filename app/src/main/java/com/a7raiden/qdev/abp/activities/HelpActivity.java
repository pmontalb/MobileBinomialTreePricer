package com.a7raiden.qdev.abp.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.a7raiden.qdev.abp.R;
import com.a7raiden.qdev.abp.utils.FontManager;

public class HelpActivity extends AppCompatActivity {

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setButtonClickListenerToUrl(Button button, String url) {
        button.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setupActionBar();

        // Read the arguments from the Intent object.
        TextView textView = findViewById(R.id.helpTextView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(fromHtml(getResources().getString(R.string.help_content)));

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.setFontToContainer(findViewById(R.id.contactsLinearLayout), iconFont);

        String githubUrl = getResources().getString(R.string.github_url);
        setButtonClickListenerToUrl(findViewById(R.id.githubButton), githubUrl);

        String linkedInUrl = getResources().getString(R.string.linkedIn_url);
        setButtonClickListenerToUrl(findViewById(R.id.linkedInButton), linkedInUrl);
    }
}
