package com.example.seele.nytstore;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Jet Wang on 2017/5/4.
 */

public interface ApiCall {

    @GET("/r/{subreddit}/new/.json")
    Observable<RedditData> fetchSubreddit(@Path("subreddit") String subreddit, @Query("limit") String limit);

    @GET("/r/{subreddit}/new/.json")
    Observable<ResponseBody> fetchSubredditForPersister(@Path("subreddit") String subreddit, @Query("limit") String limit);


    class Factory{

        public static ApiCall create(){
            return new Retrofit.Builder()
                            .baseUrl("http://www.reddit.com")
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .validateEagerly(BuildConfig.DEBUG)
                            .build()
                            .create(ApiCall.class);
        }
    }

}
