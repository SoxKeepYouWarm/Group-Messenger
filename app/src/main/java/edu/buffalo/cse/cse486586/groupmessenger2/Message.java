package edu.buffalo.cse.cse486586.groupmessenger2;

public class Message implements Comparable<Message> {

    private String message;
    private double seq_num;
    private int msg_id;
    private int process_id;
    private int DELIVERY_STATUS;

    private static int DELIVERY_REQUIREMENT = 31;

    public Message(String msg, double seq, int msg_id, int p_id) {
        this.message = msg;
        this.seq_num = seq;
        this.msg_id = msg_id;
        this.process_id = p_id;

        this.DELIVERY_STATUS = 0;
    }

    public String getMessage() {
        return message;
    }

    public double get_seq_num() {
        return seq_num;
    }

    public void set_seq_num(double num) {
        this.seq_num = num;
    }

    public int get_message_id() {
        return msg_id;
    }

    public int get_process_id() {
        return process_id;
    }

    public int get_delivery_status() {
        return this.DELIVERY_STATUS;
    }

    public void set_delivery_status(int num) {
        this.DELIVERY_STATUS = num;
    }

    public static void set_delivery_requirement(int num) {
        DELIVERY_REQUIREMENT = num;
    }

    public static int get_delivery_requirement() {
        return DELIVERY_REQUIREMENT;
    }

    public boolean is_deliverable() {
        return this.DELIVERY_STATUS == DELIVERY_REQUIREMENT;
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
