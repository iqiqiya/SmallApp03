package com.iqiqiya.smallapp03;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtv_show;
    private HttpURLConnection connection;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    //处理不同进程数据交换
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == GET_DATA_SUCCESS){
                String data = msg.getData().getString("data");
                Log.i("MainActivity",data);
                mtv_show.setText(data);
            }
            return false;
        }
    });
    private int GET_DATA_SUCCESS = 101;//获取数据成功的标志
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化控件
        initUI();

        //初始化数据
        initData();
    }

    private void initUI() {
        //获取文本框
        mtv_show = findViewById(R.id.tv_show);
        //获取按钮并绑定监听者对象
        findViewById(R.id.btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        initData();
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = getDataFromServer();

                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();//就是捆一捆数据
                    bundle.putString("data",data);
                    message.setData(bundle);
                    message.what = GET_DATA_SUCCESS;
                    //向主线程发信息
                    mHandler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //从服务器获取数据
    private String getDataFromServer() throws IOException {
        try{
            //1.创建URL
            URL url = new URL("https://v1.hitokoto.cn/");
            //2.访问网页
            connection = (HttpURLConnection) url.openConnection();
            //3.判断并处理结果
            if (connection.getResponseCode()==200){
                //获取输入流
                inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();
                for (String line = "";(line = bufferedReader.readLine())!=null;){
                    stringBuilder.append(line);
                }
                String swap_string = stringBuilder.toString();

                String pattern = "hitokoto\": \"(.*?)\"";

                Pattern r = Pattern.compile(pattern);

                Matcher m = r.matcher(swap_string);
                if(m.find()){
                    //Log.i("MainActivity",m.group(0));
                    //Log.i("MainActivity",m.group(1));
                    return m.group(1);
                }
                return "未获取到数据，请重试";
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (bufferedReader!=null)bufferedReader.close();
            if (connection!=null)connection.disconnect();
            if (inputStream!=null)inputStream.close();
        }
        return "";
    }
}
