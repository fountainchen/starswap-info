package org.starcoin.utils;

import com.alibaba.fastjson.JSON;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.starcoin.api.Result;
import org.starcoin.bean.TransactionEntity;
import org.starcoin.bean.UserTransactionEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MoveScanClient {
    private String scheme;
    private String host;
    private int port;

    private OkHttpClient client;

    public MoveScanClient(String scheme, String url, int port) {
        this.scheme = scheme;
        this.host = url;
        this.port = port;
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).build();
    }

    public List<TransactionEntity> getSwapTxn(long startIndex, int count) throws IOException {
//        v1/transaction/aptos_devnet/page/1?start=425708
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .addPathSegment("v1")
                .addPathSegment("transaction")
                .addPathSegment("starcoin_main")
                .addPathSegment("page")
                .addPathSegment("1")
                .addQueryParameter("start", String.valueOf(startIndex))
                .addQueryParameter("count", String.valueOf(count))
                .addQueryParameter("txn_type", "ScriptFunction")
                .build();
        Request request = new Request.Builder()
                .url(httpUrl)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String res = response.body().string();
            Result result = JSON.parseObject(res, Result.class);
            List<TransactionEntity> transactionEntityList = new ArrayList<>();
            for (Object jb: result.getContents()) {
                TransactionEntity entity = JSON.parseObject(String.valueOf(jb), TransactionEntity.class);
                transactionEntityList.add(entity);
            }
            return transactionEntityList;
        }
    }

    public UserTransactionEntity getUserTxn(String hash) throws IOException {
//        v1/transaction/starcoin_main/user/0xae2064f4d41d24c88429083ead5
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .addPathSegment("v1")
                .addPathSegment("transaction")
                .addPathSegment("starcoin_main")
                .addPathSegment("user")
                .addPathSegment(hash)
                .build();
        Request request = new Request.Builder()
                .url(httpUrl)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String res = response.body().string();
            return JSON.parseObject(res, UserTransactionEntity.class);
        }
    }
}
