package local.lynxmsk.sport;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import local.lynxmsk.sport.models.Article;
import local.lynxmsk.sport.models.SubArticle;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArticleActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "local.lynxmsk.sport.url";

    private MaterialProgressBar progressBar;
    private SubArticleRecyclerViewAdapter adapter;

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        progressBar = findViewById(R.id.progressBar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        adapter = new SubArticleRecyclerViewAdapter(new SubArticle[]{});
        recyclerView.setAdapter(adapter);
        makeRequest();
    }


    private void makeRequest() {
        OkHttpClient client = new OkHttpClient();
        String article = getIntent().getStringExtra(EXTRA_URL);
        String url = new Uri.Builder().scheme("http").authority("mikonatoruri.win").appendPath("post.php").appendQueryParameter("article", article).build().toString();
        Request request = new Request.Builder().url(url).build();
        progressBar.setVisibility(View.VISIBLE);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                e.printStackTrace();
                String errorMessage = ArticleActivity.this.getResources().getString(R.string.errorRetrievingData);
                processResult(errorMessage, null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                Article article = null;
                String errorMessage = null;
                try {
                    article = new Gson().fromJson(response.body().charStream(), Article.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = ArticleActivity.this.getResources().getString(R.string.errorRetrievingData);
                } finally {
                    processResult(errorMessage, article);
                }
            }
        });
    }

    private void processResult(final String errorMessage, final Article article) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                if (errorMessage != null) {
                    Toast.makeText(ArticleActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }
                if (article == null) {
                    return;
                }
                Resources res = getResources();
                String tournamentAndTime = String.format(res.getString(R.string.tournamentAndTime), article.getTournament(), article.getTime());
                ((TextView) findViewById(R.id.textViewTournamentAndTime)).setText(tournamentAndTime);
                ((TextView) findViewById(R.id.textViewTeam1)).setText(article.getTeam1());
                ((TextView) findViewById(R.id.textViewTeam2)).setText(article.getTeam2());
                String place = String.format(res.getString(R.string.place), article.getPlace());
                Spanned placeHtml;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    placeHtml = Html.fromHtml(place, Html.FROM_HTML_MODE_LEGACY);
                } else {
                    placeHtml = Html.fromHtml(place);
                }
                ((TextView) findViewById(R.id.textViewPlace)).setText(placeHtml);
                findViewById(R.id.textViewHyphen).setVisibility(View.VISIBLE);
                if (article.getArticle() == null) {
                    return;
                }
                adapter.setData(article.getArticle());
            }
        });
    }


    private class SubArticleRecyclerViewAdapter extends RecyclerView.Adapter<SubArticleViewHolder> {
        SubArticle[] mList;

        SubArticleRecyclerViewAdapter(SubArticle[] data) {
            mList = data;
        }

        @NonNull
        @Override
        public SubArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SubArticleViewHolder(getLayoutInflater().inflate(R.layout.list_item_sub_article, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SubArticleViewHolder holder, int position) {
            holder.bind(mList[position]);
        }

        @Override
        public int getItemCount() {
            return mList.length;
        }

        public void setData(SubArticle[] data) {
            mList = data;
            notifyDataSetChanged();
        }
    }

    class SubArticleViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHeader, textViewText;

        SubArticleViewHolder(View itemView) {
            super(itemView);
            textViewHeader = itemView.findViewById(R.id.textViewHeader);
            textViewText = itemView.findViewById(R.id.textViewText);
        }

        void bind(SubArticle subArticle) {
            textViewHeader.setText(subArticle.getHeader());
            textViewText.setText(subArticle.getText());
        }
    }
}
