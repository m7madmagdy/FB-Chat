package com.example.socialmedia.firebase;

import static com.example.socialmedia.utils.Constants.AUTHORIZATION_KEY;
import static com.example.socialmedia.utils.Constants.CONTENT_TYPE;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({CONTENT_TYPE, AUTHORIZATION_KEY})

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
