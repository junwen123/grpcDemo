package grpc.liu.com.grpcdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import grpc.liu.com.grpcdemo.HelloWorldClient;
import grpc.liu.com.grpcdemo.HelloWorldServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GrpcDemo";

    private static final int PROT = 55055;
    private static final String NAME = "linjw";
    private static final String HOST = "localhost";
    EditText editText;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.qidong);
        textView = findViewById(R.id.tv_receive);
        editText = findViewById(R.id.et_send);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startClient();
                startServer();
            }
        });

//        startClient(HOST, PROT, NAME);

    }

    private void startClient(){
        try {
            HelloWorldClient.ss();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private EditText getEditText(){
        return editText;
    }

    private void setText(final String name){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(name);
            }
        });

    }

    HelloWorldServer.ReceiveCallBack receiveCallBack = null;
    private void startServer(){
        try {
            try {
                if(receiveCallBack == null){
                    receiveCallBack = new HelloWorldServer.ReceiveCallBack() {
                        @Override
                        public String getName() {
                            return getEditText().getText().toString();
                        }

                        @Override
                        public void setMes(String mes) {
                            setText(mes);
                        }
                    };
                    HelloWorldServer.setCallBack(receiveCallBack);
                }
                HelloWorldServer.ss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }

    private void startServer(int port){
        try {
            Server server = ServerBuilder.forPort(port)
                    .addService(new GreeterImpl())
                    .build()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }

    private void startClient(String host, int port, String name){
        new GrpcTask(host, port, name).execute();
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            responseObserver.onNext(sayHello(request));
            responseObserver.onCompleted();
        }

        private HelloReply sayHello(HelloRequest request) {
            return HelloReply.newBuilder()
                    .setMessage("hello "+ request.getName())
                    .build();
        }
    }

    private class GrpcTask extends AsyncTask<Void, Void, String> {
        private String mHost;
        private String mName;
        private int mPort;
        private ManagedChannel mChannel;

 public GrpcTask(String host, int port, String name) {
            this.mHost = host;
            this.mName = name;
            this.mPort = port;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... nothing) {
            try {
                mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                        .usePlaintext(true)
                        .build();
                GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(mChannel);
                HelloRequest message = HelloRequest.newBuilder().setName(mName).build();
                HelloReply reply = stub.sayHello(message);
                return reply.getMessage();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return "Failed... : " + System.lineSeparator() + sw;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Log.d(TAG, result);
        }
    }
}
