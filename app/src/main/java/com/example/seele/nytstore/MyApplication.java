package com.example.seele.nytstore;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nytimes.android.external.fs.SourcePersisterFactory;
import com.nytimes.android.external.store.base.Fetcher;
import com.nytimes.android.external.store.base.Parser;
import com.nytimes.android.external.store.base.Persister;
import com.nytimes.android.external.store.base.impl.BarCode;
import com.nytimes.android.external.store.base.impl.MemoryPolicy;
import com.nytimes.android.external.store.base.impl.Store;
import com.nytimes.android.external.store.base.impl.StoreBuilder;
import com.nytimes.android.external.store.middleware.GsonParserFactory;
import com.nytimes.android.external.store.middleware.GsonSourceParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Jet Wang on 2017/5/4.
 */

public class MyApplication extends Application {

    private Store<RedditData, BarCode> nonPersistedStore;
    private Store<RedditData, BarCode> persistedStore;
    private Persister<BufferedSource, BarCode> persister;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();

        gson = new Gson();
        initPersister();
        this.nonPersistedStore = provideRedditStore();
        this.persistedStore = providePersistedRedditStore();
    }

    public Store<RedditData, BarCode> getNonPersistedStore(){
        return this.nonPersistedStore;
    }

    public Store<RedditData, BarCode> getPersistedStore(){
        return this.persistedStore;
    }

    private Store<RedditData, BarCode> providePersistedRedditStore() {
        return StoreBuilder.<BarCode, BufferedSource, RedditData>parsedWithKey()
                        .fetcher(new Fetcher<BufferedSource, BarCode>() {
                            @Nonnull
                            @Override
                            public Observable<BufferedSource> fetch(@Nonnull BarCode barCode) {
                                return provideRetrofit().fetchSubredditForPersister(barCode.getKey(), "10").map(new Func1<ResponseBody, BufferedSource>() {
                                    @Override
                                    public BufferedSource call(ResponseBody responseBody) {
                                        return responseBody.source();
                                    }
                                });
                            }
                        })
                        .persister(persister)
                        .parser(new Parser<BufferedSource, RedditData>() {
                            @Override
                            public RedditData call(BufferedSource bufferedSource) {
                                try (InputStreamReader reader = new InputStreamReader(bufferedSource.inputStream())) {
                                    return gson.fromJson(reader, RedditData.class);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        })
                        .open();



    }

    private void initPersister() {
        try {
            persister = SourcePersisterFactory.create(getApplicationContext().getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Store<RedditData, BarCode> provideRedditStore() {
        return StoreBuilder.<RedditData>barcode()
                        .fetcher(new Fetcher<RedditData, BarCode>() {
                            @Nonnull
                            @Override
                            public Observable<RedditData> fetch(@Nonnull BarCode barCode) {
                                return provideRetrofit().fetchSubreddit(barCode.getKey(), "10");
                            }
                        })
                        .memoryPolicy(MemoryPolicy
                                .builder()
                                .setExpireAfter(10)
                                .setExpireAfterTimeUnit(TimeUnit.MINUTES)
                                .build())
                        .open();


    }

    private ApiCall provideRetrofit() {
        return ApiCall.Factory.create();
    }
}
