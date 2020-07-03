package com.example.receipt2go;

public class Order {

    private String orderNumber;
    private String orderTime;
    private String orderCustomerName;

    public Order() {
        this.orderNumber = "";
        this.orderTime = "";
        this.orderCustomerName = "";
    }

    public Order(String orderNumber, String orderTime, String orderCustomerName) {
        this.orderNumber = orderNumber;
        this.orderTime = orderTime;
        this.orderCustomerName = orderCustomerName;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public String getOrderCustomerName() {
        return orderCustomerName;
    }
}
