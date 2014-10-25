package org.cnodejs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.cnodejs.api.Constants;
import org.cnodejs.api.GsonRequest;
import org.cnodejs.api.model.Topic;
import org.cnodejs.api.model.TopicList;

public class MainActivity extends ActionBarActivity implements
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";

    private SwipeRefreshLayout swipingLayout;

    private TopicListAdapter topicsAdapter;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupSwipingLayout();
        setupTopicsView();

        // 初始化 Volley 请求队列
        queue = Volley.newRequestQueue(this);

        loadTopics();
    }

    private void setupSwipingLayout() {
        swipingLayout = (SwipeRefreshLayout) findViewById(R.id.swiping);
        swipingLayout.setOnRefreshListener(this);
    }

    private void setupTopicsView() {
        topicsAdapter = new TopicListAdapter(this);

        ListView topicsView = (ListView) findViewById(R.id.topics);
        topicsView.setAdapter(topicsAdapter);
        topicsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openTopic(topicsAdapter.getItem(position));
            }
        });
    }

    private void loadTopics() {
        swipingLayout.setRefreshing(true);
        queue.add(new GsonRequest<TopicList>(
                Request.Method.GET, Constants.API_V1 + "/topics", TopicList.class,
                new Response.Listener<TopicList>() {
                    @Override
                    public void onResponse(TopicList response) {
                        Log.d(TAG, "loaded " + response.data.size() + " topics");
                        swipingLayout.setRefreshing(false);
                        topicsAdapter.clear();
                        topicsAdapter.addAll(response.data);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "error loading topics", error);
                        swipingLayout.setRefreshing(false);
                        Toast.makeText(
                                MainActivity.this,
                                R.string.error_loading,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    @Override
    public void onRefresh() {
        loadTopics();
    }

    private void openTopic(Topic topic) {
        Intent intent = new Intent(this, TopicActivity.class);
        intent.putExtra("id", topic.id);
        intent.putExtra("title", topic.title);
        intent.putExtra("content", topic.content);
        intent.putExtra("user", topic.author.loginname);
        intent.putExtra("avatar", topic.author.avatarUrl);
        startActivity(intent);
    }

}
