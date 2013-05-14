package com.adonai.GsmNotify;

public class Device
{
    public Device(String num)
    {
        number = num;
    }

    public String name;
    public String number;
    public String password;
    public String tempLimit;
    public String tMin, tMax;
    public String mode;
    public boolean sendSMS;
}
