package edu.buffalo.cse.cse486586.groupmessenger2;

public class Message implements Comparable<Message> {

    private String message;
    private double seq_num;
    private int msg_id;
    private int process_id;

    public Message(String msg, double seq, int msg_id, int p_id) {
        this.message = msg;
        this.seq_num = seq;
        this.msg_id = msg_id;
        this.process_id = p_id;
    }

    public String getMessage() {
        return message;
    }

    public double get_seq_num() {
        return seq_num;
    }

    public int message_id() {
        return msg_id;
    }

    public int getProcess_id() {
        return process_id;
    }

    public int compareTo(Message other) {

        double diff = this.seq_num - other.get_seq_num();
        if (diff == 0) {
            return 0;
        } else if (diff > 0) {
            return 1;
        } else {
            return -1;
        }

    }
}
