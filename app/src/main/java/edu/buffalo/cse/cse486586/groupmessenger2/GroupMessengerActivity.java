package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GroupMessengerActivity extends Activity {

    private static int SERVER_PORT = 10000;
    private static String[] CLIENT_PORTS = {"11108","11112","11116","11120","11124"};
    private static String MY_PORT;

    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    static final String BREAK_MSG_PROPOSAL = "!~!~!~";
    static final String BREAK_MSG_FINAL = "!@!@!@";

    // thread safe queue of proposed messages
    Queue<Message> hold_back_queue = new ConcurrentLinkedQueue<Message>();
    Queue<Message> debug_delivery_queue = new ConcurrentLinkedQueue<Message>();

    int current_message_id;
    int proposed_sequence_number;


    private void set_my_port() {
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        MY_PORT = String.valueOf((Integer.parseInt(portStr) * 2));
    }


    private static Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


    private int get_pid_from_port(String port) {
        for (int i = 0; i < CLIENT_PORTS.length; i++) {
            if (CLIENT_PORTS[i].equals(port)) { return i; }
        }

        // shouldn't return here
        return -1;
    }


    private void initialize_ui_elements() {

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        final EditText input=(EditText)findViewById(R.id.editText1);

        Button send =(Button)findViewById(R.id.button4);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = input.getText().toString();
                input.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);
            }
        });
    }


    private void start_server_task() {
        try {

            Log.d(TAG, "initiating server task");
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        current_message_id = 0;

        set_my_port();

        initialize_ui_elements();

        start_server_task();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        public void handle_message_proposal(String message, Socket socket) {
            String[] msg_segs = message.split(BREAK_MSG_PROPOSAL);
            String msg = msg_segs[0];
            String sender_pid = msg_segs[1];
            String msg_id = msg_segs[2];

            // add new message to the hold-back queue
            int pid = Integer.parseInt(sender_pid);
            double proposed_seq = (++proposed_sequence_number) + ((double) pid / 10);

            Message new_message = new Message(msg,
                    proposed_seq,
                    Integer.parseInt(msg_id),
                    pid);

            hold_back_queue.add(new_message);

            // send back the proposed sequence number
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(Double.toString(proposed_seq));
            } catch (IOException e) {
                Log.e("ERROR", "error getting printwriter");
            }

        }


        public void handle_message_final(String message) {
            String[] msg_segs = message.split(BREAK_MSG_FINAL);
            String msg = msg_segs[0];
            int pid = Integer.parseInt(msg_segs[1]);
            int msg_id = Integer.parseInt(msg_segs[2]);
            String final_seq_num = msg_segs[3];
            String destination_port = msg_segs[4];

            for (Message holding_msg : hold_back_queue) {

                // find matching message
                int holding_pid = holding_msg.getProcess_id();
                int holding_msg_id = holding_msg.message_id();

                // matching message
                if ((pid == holding_pid) && (msg_id == holding_msg_id)) {
                    hold_back_queue.remove(holding_msg);
                    debug_delivery_queue.add(holding_msg);
                    Log.d("OUTPUT", msg + " received with final_seq_num: " + final_seq_num);
                    // TODO: Save this message to content provider
                }

            }

        }


        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            try {

                while (true) {

                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String message;
                    if ((message = in.readLine()) != null) {

                        boolean is_proposal = message.contains(BREAK_MSG_PROPOSAL);
                        if (is_proposal) {
                            handle_message_proposal(message, clientSocket);
                        } else {
                            handle_message_final(message);
                        }

                        publishProgress(message);
                    }

                }

            } catch (NullPointerException err) {
                Log.d("error", "client socket was not initialized properly");
            } catch (IOException err) {
                Log.d("error", "client socket was not initialized properly");
            }

            return null;
        }

        protected void onProgressUpdate(String... strings) {

        }

    }


    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                // get message
                String message = msgs[0];
                double final_sequence_number = 0;
                String msg_id = Integer.toString(++current_message_id);

                for (String destination_port : CLIENT_PORTS) {
                    Socket client_socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(destination_port));
                    Log.d(TAG, "sending message to " + destination_port);

                    // send out formatted message
                    String message_wrapper = message + BREAK_MSG_PROPOSAL +
                            Integer.toString(get_pid_from_port(MY_PORT)) +
                            msg_id + BREAK_MSG_PROPOSAL;

                    PrintWriter out = new PrintWriter(client_socket.getOutputStream(), true);
                    out.println(message_wrapper);

                    // set 500ms timeout for response
                    client_socket.setSoTimeout(500);

                    try {

                        // wait for response
                        BufferedReader in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                        String input;
                        if ((input = in.readLine()) != null) {
                            // check for new max sequence num
                            Double proposed_sequence_number = Double.parseDouble(input);
                            final_sequence_number = Math.max(final_sequence_number, proposed_sequence_number);

                        }
                        in.close();

                    } catch (SocketException e) {
                        Log.e("ERROR", "timeout expired");
                    }

                    out.close();
                    client_socket.close();
                }


                for (String destination_port : CLIENT_PORTS) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(destination_port));

                    String message_wrapper = message + BREAK_MSG_FINAL +
                            Integer.toString(get_pid_from_port(MY_PORT)) + BREAK_MSG_FINAL +
                            msg_id + BREAK_MSG_FINAL +
                            final_sequence_number + BREAK_MSG_FINAL +
                            destination_port;

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(message_wrapper);

                    out.close();
                    socket.close();
                }


            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;

        }
    }

}
