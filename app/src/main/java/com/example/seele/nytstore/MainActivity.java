package com.example.seele.nytstore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.nytimes.android.external.store.base.impl.BarCode;
import com.nytimes.android.external.store.base.impl.Store;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRV;
    private Store<RedditData, BarCode> nonPersistedStore;
    private Store<RedditData, BarCode> PersistedStore;
    private List<Post> mDatas = new ArrayList<>();
    private CommonAdapter<Post> adapter;
    private BarCode awwRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        awwRequest = new BarCode(RedditData.class.getSimpleName(), "aww");
        mRV = (RecyclerView) findViewById(R.id.rv);
        adapter = new CommonAdapter<Post>(this, R.layout.listitem, mDatas) {

            @Override
            protected void convert(ViewHolder holder, Post post, int position) {
                Log.d("PostTitle: ", post.title);
                holder.setText(R.id.tv, post.title);
            }
        };
        mRV.setLayoutManager(new LinearLayoutManager(this));
        mRV.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initStore();
        loadPosts();
//        loadPersistedPosts();
    }

    private void loadPosts() {
        this.nonPersistedStore
                .get(awwRequest)
                .flatMap(new Func1<RedditData, Observable<Post>>(){

                    @Override
                    public Observable<Post> call(RedditData redditData) {
                        return Observable.from(redditData.getData().children)
                                        .map(new Func1<Children, Post>() {
                                            @Override
                                            public Post call(Children children) {
                                                return children.data;
                                            }
                                        });
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Post>>() {
                    @Override
                    public void call(List<Post> posts) {
                       mDatas.clear();
                       mDatas.addAll(posts);
                        adapter.notifyDataSetChanged();

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
    }

    private void loadPersistedPosts(){
        this.PersistedStore
                .get(awwRequest)
                .flatMap(new Func1<RedditData, Observable<Post>>(){

                    @Override
                    public Observable<Post> call(RedditData redditData) {
                        return Observable.from(redditData.getData().children)
                                .map(new Func1<Children, Post>() {
                                    @Override
                                    public Post call(Children children) {
                                        return children.data;
                                    }
                                });
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Post>>() {
                    @Override
                    public void call(List<Post> posts) {
                        mDatas.clear();
                        mDatas.addAll(posts);
                        adapter.notifyDataSetChanged();

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
    }

    private void initStore() {
        if (this.nonPersistedStore == null){
            this.nonPersistedStore = ((MyApplication)getApplication()).getNonPersistedStore();
        }

        if (this.PersistedStore == null){
            this.PersistedStore = ((MyApplication)getApplication()).getPersistedStore();
        }
    }
}
