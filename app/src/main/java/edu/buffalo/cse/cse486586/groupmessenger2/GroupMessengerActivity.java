package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GroupMessengerActivity extends Activity {

    private static int SERVER_PORT = 10000;
    //private static List<String> CLIENT_PORTS = Collections.synchronizedList(Arrays.asList("11108", "11112", "11116", "11120", "11124"));
    private static List<String> CLIENT_PORTS = new ArrayList<String>(Arrays.asList("11108", "11112", "11116", "11120", "11124"));

    private static String MY_PORT;

    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    static final String BREAK_MSG_SEND = "!#!#!#";
    static final String BREAK_MSG_PROPOSAL = "!~!~!~";
    static final String BREAK_MSG_FINAL = "!@!@!@";
    static final String ACK = "!@#$!@#$";

    final Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

    // thread safe queue of proposed messages
    Queue<Message> hold_back_queue = new ConcurrentLinkedQueue<Message>();
    Queue<Message> debug_delivery_queue = new ConcurrentLinkedQueue<Message>();

    int current_message_id;

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
        for (int i = 0; i < CLIENT_PORTS.size(); i++) {
            if (CLIENT_PORTS.get(i).equals(port)) { return i; }
        }

        // shouldn't return here
        return -1;
    }


    public synchronized void remove_client(String client) {
        for ( int i = 0;  i < CLIENT_PORTS.size(); i++) {
            String temp = CLIENT_PORTS.get(i);
            if (temp.equals(client)) {
                CLIENT_PORTS.remove(i);
            }
        }
    }


    private void dump_handler() {
        final TextView tv = (TextView) findViewById(R.id.textView1);
        Cursor result = getContentResolver().query(uri, null, Integer.toString(0), null, null);
        if (result == null) {
            Log.e(TAG, "empty cursor returned");
            return;
        }

        tv.setText("");
        tv.append("PID: " + get_pid_from_port(MY_PORT) + " PORT: " + MY_PORT + '\n');
        while (result.moveToNext()) {
            int idIndex = result.getColumnIndex(Key_Value_Contract.UID);
            int keyIndex = result.getColumnIndex(Key_Value_Contract.COLUMN_KEY);
            int valueIndex = result.getColumnIndex(Key_Value_Contract.COLUMN_VALUE);
            String returnId = result.getString(idIndex);
            String returnKey = result.getString(keyIndex);
            String returnValue = result.getString(valueIndex);

            tv.append(result.getPosition() + ": id is: " + returnId +
                    " key is: " + returnKey +
                    " value is: " + returnValue + '\n');
        }

        result.close();
    }


    private void order_handler() {

    }


    private void initialize_ui_elements() {

        final TextView tv  = (TextView) findViewById(R.id.textView1);
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

        Button dump_db = (Button) findViewById(R.id.dump_db);
        dump_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dump_handler();
            }
        });

        Button order = (Button) findViewById(R.id.order);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_handler();
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


    private Message get_msg_from_hold_back(int pid, int msg_id) {

        for (Message holding_msg : hold_back_queue) {

            // find matching message
            int holding_pid = holding_msg.get_process_id();
            int holding_msg_id = holding_msg.get_message_id();

            // matching message
            if ((pid == holding_pid) && (msg_id == holding_msg_id)) {
                return holding_msg;
            }

        }

        // message is missing, dump queue contents for debug
        Log.e(TAG, "HOLD_BACK_QUEUE DUMP START");
        for (Message msg: hold_back_queue) {
            int msg_pid = msg.get_process_id();
            int id = msg.get_message_id();
            Log.e(TAG, "MISSING: pid: " + msg_pid + " msg_id " + id);
        }
        Log.e(TAG, "HOLD_BACK_QUEUE DUMP END");

        return null;

    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        public void handle_send(String message, Socket socket) {
            String[] msg_segs = message.split(BREAK_MSG_SEND);
            String msg = msg_segs[0];
            String sender_pid = msg_segs[1];
            String sender_msg_id = msg_segs[2];

            int pid = get_pid_from_port(MY_PORT);

            Log.d(TAG, "pid:" + Integer.toString(pid) + " HANDLE_SEND: " + message +
                    " sender_pid: " + sender_pid +
                    " sender_msg_id: " + sender_msg_id);

            boolean i_am_sender = sender_pid.equals(Integer.toString(get_pid_from_port(MY_PORT)));

            // add new message to the hold-back queue

            //double proposed_seq = (++current_message_id) + ((double) Integer.parseInt(sender_pid) / 10);

            int proposed_msg_id;
            if (i_am_sender) {
                proposed_msg_id = current_message_id;
            } else {
                proposed_msg_id = ++current_message_id;
                Message new_message = new Message(msg,
                        0.0,
                        Integer.parseInt(sender_msg_id),
                        Integer.parseInt(sender_pid));

                hold_back_queue.add(new_message);
            }

            String response_msg = sender_msg_id + BREAK_MSG_PROPOSAL +
                    Integer.toString(proposed_msg_id) + BREAK_MSG_PROPOSAL +
                    Integer.toString(get_pid_from_port(MY_PORT)) + BREAK_MSG_PROPOSAL;

            // send back the proposed sequence number
            try {
                Log.d(TAG, "pid:" + Integer.toString(pid) + " RECEIVED_SEND: " + msg + " sending back proposal: " + Integer.toString(proposed_msg_id) +
                        " for sender_pid: " + sender_pid +
                        " sender_msg_id: " + sender_msg_id);

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(response_msg);
                //writer.close();
            } catch (IOException e) {
                Log.e(TAG, "error getting printwriter");
            }

        }


        public void handle_message_final(String message, Socket socket) {
            String[] msg_segs = message.split(BREAK_MSG_FINAL);
            int msg_id = Integer.parseInt(msg_segs[0]);
            int pid = Integer.parseInt(msg_segs[1]);
            String final_seq_num = msg_segs[2];

            String my_pid = Integer.toString(get_pid_from_port(MY_PORT));

            // send ACK for final message
            try {
                Log.d(TAG, "FINAL_HANDLER: sending ACK");
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(ACK);
            } catch (IOException e) {
                Log.e(TAG, "error sending ACK");
            }

            // find this message in the hold back queue
            Message final_message = get_msg_from_hold_back(pid, msg_id);
            if (final_message == null) {
                Log.e(TAG, "FINAL_HANDLER: missing pid: " + pid + " msg_id " + msg_id);
                return;
            }

            Log.d(TAG, "pid:" + my_pid + " FINAL_HANDLER: " + final_message.getMessage() +
                    " msg_id: " + msg_id +
                    " pid: " + pid +
                    " final_seq_num: " + final_seq_num);


            hold_back_queue.remove(final_message);
            debug_delivery_queue.add(final_message);

            ContentValues new_entry = new ContentValues();
            new_entry.put(Key_Value_Contract.UID, Double.parseDouble(final_seq_num));
            new_entry.put(Key_Value_Contract.COLUMN_VALUE, final_message.getMessage());

            Uri result = getContentResolver().insert(uri, new_entry);
            if (result == null) {
                Log.e(TAG, "error inserting value into content provider");
                return;
            } else {
                Log.d(TAG, "pid:" + my_pid + " FINAL_HANDLER: inserted " + final_message.getMessage() +
                        "with sequence: " + final_message.get_seq_num());
                publishProgress(final_message.getMessage());
            }
            Log.d(TAG, result.toString());

        }


        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            try {

                while (true) {

                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    //Log.d(TAG, "server accepted client");

                    String message;
                    while ((message = in.readLine()) != null) {

                        //Log.d(TAG, "server accepted client message");

                        /*try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "error with wait");
                        }*/

                        boolean is_send = message.contains(BREAK_MSG_SEND);
                        boolean is_final = message.contains(BREAK_MSG_FINAL);

                        if (is_send) {
                            handle_send(message, clientSocket);
                        } else if (is_final) {
                            handle_message_final(message, clientSocket);
                        }

                        //publishProgress(message);
                    }
                }

            } catch (NullPointerException err) {
                Log.e(TAG, "client socket was not initialized properly");
            } catch (IOException err) {
                Log.e(TAG, "client socket was not initialized properly");
            }

            return null;
        }

        protected void onProgressUpdate(String... strings) {
            final TextView tv  = (TextView) findViewById(R.id.textView1);
            tv.append(strings[0] + '\n');
        }

    }


    private class ClientTask extends AsyncTask<String, Void, Void> {


        public void deliver_final_message(Message final_msg) {

            Log.d(TAG, "DELIVERING_FINAL: " + final_msg.getMessage() +
                    " sequence number: " + final_msg.get_seq_num());

            // send pid and msg_id as identifiers, and report final sequence id
            String announce_final_message =
                    Integer.toString(final_msg.get_message_id()) + BREAK_MSG_FINAL +
                            Integer.toString(final_msg.get_process_id()) + BREAK_MSG_FINAL +
                            Double.toString(final_msg.get_seq_num()) + BREAK_MSG_FINAL;

            // send out the final message
            try {
                for (String destination_port : CLIENT_PORTS) {
                    Log.d(TAG, "pid:" + get_pid_from_port(MY_PORT) + " SEND_FINAL: " + final_msg.getMessage() +
                            " to " + get_pid_from_port(destination_port));
                    Socket client_socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(destination_port));
                    PrintWriter writer = new PrintWriter(client_socket.getOutputStream(), true);
                    writer.println(announce_final_message);

                    /*try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "error with wait");
                    }*/

                    writer.flush();

                    client_socket.setSoTimeout(1000);

                    BufferedReader in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                    String input;
                    if ((input = in.readLine()) != null) {

                        if (input.equals(ACK)) {
                            Log.d(TAG, "SEND_FINAL: " + final_msg.getMessage() +
                                " server ACKed back");
                        }

                    } else {
                        Log.d(TAG, "SEND_FINAL: " + final_msg.getMessage() + " no ACK from server, resending...");
                        writer.println(announce_final_message);
                    }

                    writer.flush();
                    writer.close();
                    client_socket.close();
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "error connecting sockets for final message");
            } catch (IOException e) {
                Log.e(TAG, "error connecting sockets for final message");
            }

        }


        public void handle_message_proposal(String message) {

            String[] msg_segs = message.split(BREAK_MSG_PROPOSAL);
            String sender_msg_id = msg_segs[0];
            String proposed_msg_id = msg_segs[1];
            int responder_pid = Integer.parseInt(msg_segs[2]);

            int my_pid = get_pid_from_port(MY_PORT);

            Message new_message = get_msg_from_hold_back(my_pid, Integer.parseInt(sender_msg_id));
            if (new_message == null) {
                Log.e(TAG, "sent message could not be found in hold-back_queue");
                return;
            }

            Log.d(TAG, "pid:" + Integer.toString(my_pid) + " HANDLE_PROPOSAL: " + new_message.getMessage() +
                    " FROM " + Integer.toString(responder_pid) +
                    " proposal_sequence: " + proposed_msg_id);

            // check if proposed id is higher than current seq_num
            if (Integer.parseInt(proposed_msg_id) > new_message.get_seq_num()) {
                new_message.set_seq_num(Integer.parseInt(proposed_msg_id));
            }

            // set bit in delivery status for respective process response
            int delivery_status = new_message.get_delivery_status();
            delivery_status |= 1 << responder_pid;
            new_message.set_delivery_status(delivery_status);

            Log.d(TAG, "new delivery status is: " + delivery_status + " responder pid was: " + responder_pid);

            // check if all clients have responded with proposal
            if (new_message.is_deliverable()) {

                // prioritize sequence number by process id
                double seq_num = new_message.get_seq_num();
                seq_num += ((double) my_pid / 10);
                new_message.set_seq_num(seq_num);

                deliver_final_message(new_message);
            }

        }


        @Override
        protected Void doInBackground(String... msgs) {

            // get message
            String message = msgs[0];
            String msg_id = Integer.toString(++current_message_id);

            // add new message to queue
            Message new_message = new Message(message,
                    Integer.parseInt(msg_id),
                    Integer.parseInt(msg_id),
                    get_pid_from_port(MY_PORT));

            hold_back_queue.add(new_message);

            List<String> clients_copy = new ArrayList<String>(CLIENT_PORTS);

            for (String destination_port : clients_copy) {

                String message_wrapper = message + BREAK_MSG_SEND +
                        Integer.toString(get_pid_from_port(MY_PORT)) + BREAK_MSG_SEND +
                        msg_id + BREAK_MSG_SEND;

                Log.d(TAG, "pid:" + get_pid_from_port(MY_PORT) + " SEND_MESSAGE: " + message +
                        " target:" + get_pid_from_port(destination_port));

                Socket client_socket;
                //BufferedReader in;
                //PrintWriter out;
                try {
                    client_socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(destination_port));

                    PrintWriter out = new PrintWriter(client_socket.getOutputStream(), true);
                    out.println(message_wrapper);

                    /*try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "error with wait");
                    }*/

                    out.flush();

                    // set 500ms timeout for response
                    client_socket.setSoTimeout(2200);

                    BufferedReader in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                    String input;
                    if ((input = in.readLine()) != null) {

                        Log.d(TAG, "just got proposal back from server");
                        handle_message_proposal(input);

                    } else {
                        Log.d(TAG, "empty proposal from server");
                    }

                    out.close();
                    in.close();
                    client_socket.close();

                } catch (SocketException e) {
                    Log.e(TAG, "Socket exception (timeout): " + e.getMessage());
                } catch (SocketTimeoutException e) {
                    Log.e(TAG, "pid:" + Integer.toString(get_pid_from_port(MY_PORT)) +
                            " SEND_MSG_TIMEOUT: " + message +
                            " destination: " + get_pid_from_port(destination_port));
                    // remove port from loop
                    //remove_client(destination_port);
                } catch (IOException e) {
                    Log.e(TAG, "io exception connecting client socket: " + e.getMessage());
                    return null;
                }

            }

            return null;

        }
    }

}