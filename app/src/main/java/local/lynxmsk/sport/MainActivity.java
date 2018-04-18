package local.lynxmsk.sport;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import local.lynxmsk.sport.models.Event;
import local.lynxmsk.sport.models.SportNews;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EventRecyclerViewAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();
    private Uri.Builder urlBuilder = new Uri.Builder().scheme("http")
            .authority("mikonatoruri.win")
            .appendPath("list.php");
    private String category;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                makeRequest();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new EventRecyclerViewAdapter(new Event[]{});
        recyclerView.setAdapter(adapter);
    }

    private void makeRequest() {
        String url = urlBuilder.appendQueryParameter("category", category).build().toString();
        Request request = new Request.Builder().url(url).build();
        swipeRefreshLayout.setRefreshing(true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage = MainActivity.this.getResources().getString(R.string.errorRetrievingData);
                processResult(errorMessage, null);
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String errorMessage = null;
                SportNews sportNews = null;
                try {
                    Gson gson = new Gson();
                    sportNews = gson.fromJson(response.body().charStream(), SportNews.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = MainActivity.this.getResources().getString(R.string.errorRetrievingData);
                } finally {
                    processResult(errorMessage, sportNews);
                }
            }
        });
    }

    private void processResult(final String errorMessage, final SportNews sportNews) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                if (errorMessage != null) {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }
                if (sportNews != null && sportNews.getEvents() != null) {
                    adapter.setData(sportNews.getEvents());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sportCategoryTitles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = getResources().getStringArray(R.array.sportCategoryValues)[position];
                makeRequest();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return true;
    }


    private class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventViewHolder> {
        Event[] mList;

        EventRecyclerViewAdapter(Event[] data) {
            mList = data;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EventViewHolder(getLayoutInflater().inflate(R.layout.list_item_event, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            holder.bind(mList[position]);
        }

        @Override
        public int getItemCount() {
            return mList.length;
        }

        public void setData(Event[] data) {
            mList = data;
            notifyDataSetChanged();
        }
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewTitle, textViewPlace, textViewTime;
        String articleUrl;

        EventViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewPlace = itemView.findViewById(R.id.textViewPlace);
            itemView.setOnClickListener(this);
        }

        void bind(Event event) {
            textViewTitle.setText(event.getTitle());
            textViewTime.setText(event.getTime());
            textViewPlace.setText(event.getPlace());
            articleUrl = event.getArticle();
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            Intent intent = ArticleActivity.getIntent(MainActivity.this, articleUrl);
            startActivity(intent);
        }
    }
}
