package com.k.xmlrpc6;

public class Record {

    private int id;
    private String name;
    private String barcode;
    private int employee_id;
    private int uid;
    private int tag;
    private String date;
    private String date_displayed;

    public int getUid() {return uid;}
    public void setUid(int uid) {this.uid = uid;}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getEmployee_id() {
        return employee_id;
    }
    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getTag() {
        return tag;
    }
    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getDate_displayed() {
        return date_displayed;
    }
    public void setDate_displayed(String date_displayed) {
        this.date_displayed = date_displayed;
    }
}
