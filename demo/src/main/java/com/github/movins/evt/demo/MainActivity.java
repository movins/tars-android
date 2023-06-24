package com.github.movins.evt.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.movins.tars.api.client.CommunicatorConfig;
import com.github.movins.tars.core.client.Communicator;
import com.github.movins.tars.core.client.CommunicatorFactory;

import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testConnect();
    }

    private void testConnect() {
        // Start configuration locally
        CommunicatorConfig cfg = new CommunicatorConfig();
        // Start communicator locally
        Communicator communicator = CommunicatorFactory.getInstance().getCommunicator(cfg);
        //warn If the deployment is started on the tars, you can only use the following constructor to get the communicator
        //Communicator communicator = CommunicatorFactory.getInstance().getCommunicator();
        HelloPrx proxy = communicator.stringToProxy(HelloPrx.class, "TestApp.HelloServer.HelloObj@tcp -h 127.0.0.1 -p 18601 -t 60000");
        //Synchronous call
        String ret = proxy.hello(1000, "Hello World");
        System.out.println(ret);
        proxy.promise_hello(1000, "hello world").thenCompose(x -> {
            return CompletableFuture.completedFuture(0);
        });
    }
}