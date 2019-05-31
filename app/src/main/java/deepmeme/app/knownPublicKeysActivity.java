package deepmeme.app;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class knownPublicKeysActivity extends AppCompatActivity {

    ListView listview;
    List listKnownKeys = new ArrayList();
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_known_public_keys);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "todo: add public key directly", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listview = (ListView)findViewById(R.id.list_known_keys);

        listKnownKeys.add("April");
        listKnownKeys.add("Edward");
        listKnownKeys.add("Edgar");
        listKnownKeys.add("Frank");
        listKnownKeys.add("John");
        listKnownKeys.add("Jacob");
        listKnownKeys.add("Louis");
        listKnownKeys.add("Oscar");
        listKnownKeys.add("Patrick");

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listKnownKeys);
        listview.setAdapter(adapter);
    }
}
