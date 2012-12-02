package com.surfing.httpconnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.surfing.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class HttpConnectionUtil
{
    public static enum HttpMethod
    {
        GET, POST
    }

    private Handler asyHandler = null;
    private Handler mainhandler = null;
    private RequestThread mrequestthread = null;
    public String MUrl;
    public HttpMethod mMethod;
    public Map<String, String> mParams;
    public HttpConnectionCallback mCallback;
    public static final String CONNECT_FAILED = "connect_failed";
    public static final String RETURN_FAILED = "failed";
    private Context mContext;
    private static final int EVENT_NETWORK_ERROR = 3;

    private class MainHandler extends Handler
    {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 2:
                {
                    mrequestthread.callback.execute((String) msg.obj);
                    mrequestthread.stop();
                }
                    break;
                case EVENT_NETWORK_ERROR:
                {
                    // Toast.makeText(mContext,mContext.getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
                    break;
                default:
                    break;
            }
        }

    }

    public HttpConnectionUtil(Context context)
    {
        mContext = context;
        mainhandler = new MainHandler();
    }

    private class RequestThread extends Thread
    {
        public HttpConnectionCallback callback;
        private Handler mHandler;

        public RequestThread(Handler handler)
        {
            mHandler = handler;
        }

        @Override
        public void run()
        {
            Log.i("HttpConnectionUtil", "RequestThread current thread is : " + Thread.currentThread().getName());
            callback = mCallback;
            syncConnect(MUrl, mParams, mMethod, new HttpConnectionCallback()
            {
                @Override
                public void execute(final String response)
                {
                    mHandler.post(new Runnable()
                    {
                        public void run()
                        {
                            callback.execute(response);
                        }
                    });
                }

            });
        }

    }

    public static boolean goodNet(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable())
        {
            Log.i("HttpConnectionUtil", "no NetWork available");

            return false;
        }
        return true;
    }

    public void asyncConnect(final String url, final HttpMethod method, final HttpConnectionCallback callback)
    {
        if (goodNet(mContext))
        {
            asyncConnect(url, null, method, callback);
        }
        else
        {
            if (callback != null)
            {
                mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
                callback.execute(CONNECT_FAILED);
            }
        }

    }

    public void syncConnect(final String url, final HttpMethod method, final HttpConnectionCallback callback)
    {
        if (goodNet(mContext))
        {
            syncConnect(url, null, method, callback);
        }
        else
        {
            if (callback != null)
            {
                mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
                callback.execute(CONNECT_FAILED);
            }
        }

    }

    public void asyncConnect(final String url, final Map<String, String> params, final HttpMethod method, final HttpConnectionCallback callback)
    {
        if (!goodNet(mContext))
        {
            mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
            if (callback != null)
            {
                callback.execute(CONNECT_FAILED);
            }
            return;
        }
        MUrl = url;
        mParams = params;
        mMethod = method;
        mCallback = callback;
        mrequestthread = new RequestThread(mainhandler);
        mrequestthread.start();

    }

    public void syncConnect(final String url, final Map<String, String> params, final HttpMethod method, final HttpConnectionCallback callback)
    {
        int retry = 3;

        int count = 0;

        if (!goodNet(mContext))
        {
            mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
            if (callback != null)
            {

                callback.execute(CONNECT_FAILED);
            }
            return;
        }
        while (count < retry)
        {

            count += 1;

            try
            {
                executeHttpGet(url, params, method, callback);
                return;

            }
            catch (Exception e)
            {
                if (count < retry)
                {
                    Log.i("HttpConnectionUtil", "Retry");
                }
                else
                {
                    callback.execute(CONNECT_FAILED);
                    mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
                    return;
                }

            }

        }
    }

    public void executeHttpGet(final String url, final Map<String, String> params, final HttpMethod method, final HttpConnectionCallback callback) throws Exception
    {

        String json = null;
        BufferedReader reader = null;
        if (!goodNet(mContext))
        {
            mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
            if (callback != null)
            {
                callback.execute(CONNECT_FAILED);
            }
            return;
        }
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpUriRequest request = getRequest(url, params, method);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder sb = new StringBuilder();
                for (String s = reader.readLine(); s != null; s = reader.readLine())
                {
                    sb.append(s);
                }
                json = sb.toString();
            }
        }
        catch (ClientProtocolException e)
        {
            Log.i("HttpConnectionUtil", "ClientProtocolException: " + e.getMessage(), e);
            json = CONNECT_FAILED;
            mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
        }
        catch (IOException e)
        {
            Log.i("HttpConnectionUtil", "IOException: " + e.getMessage(), e);
            json = CONNECT_FAILED;
            mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
        }
        catch (Exception e)
        {
            Log.i("HttpConnectionUtil", "Exception: " + e.getMessage(), e);
            json = CONNECT_FAILED;
            mainhandler.sendEmptyMessage(EVENT_NETWORK_ERROR);
        }
        finally
        {
            try
            {
                if (reader != null)
                {

                    reader.close();
                }
            }
            catch (IOException e)
            {
            }
        }
        callback.execute(json);
    }

    private HttpUriRequest getRequest(String url, Map<String, String> params, HttpMethod method)
    {
        if (method.equals(HttpMethod.POST))
        {
            List<NameValuePair> listParams = new ArrayList<NameValuePair>();
            if (params != null)
            {
                for (String name : params.keySet())
                {
                    listParams.add(new BasicNameValuePair(name, params.get(name)));
                }
            }
            try
            {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(listParams, HTTP.UTF_8);
                HttpPost request = new HttpPost(url);
                request.setEntity(entity);
                return request;
            }
            catch (UnsupportedEncodingException e)
            {
                // Should not come here, ignore me.
                throw new java.lang.RuntimeException(e.getMessage(), e);
            }
        }
        else
        {
            if (url.indexOf("?") < 0)
            {
                url += "?";
            }
            if (params != null)
            {
                for (String name : params.keySet())
                {
                    url += "&" + name + "=" + URLEncoder.encode(params.get(name));
                }
            }
            HttpGet request = new HttpGet(url);
            return request;
        }
    }

}