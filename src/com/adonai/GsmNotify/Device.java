package com.adonai.GsmNotify;

public class Device
{
    public class PhoneSettings
    {
        public String phoneNum;
        public Boolean info, manage, confirm;
    }

    public class InputSettings
    {
        public Integer timeToWaitBeforeCall;
        public Integer timeToRearm;
        public Boolean constantControl, innerSound;
        public String smsText;
    }

    public class OutputSettings
    {
        public Integer outputMode;
        public Integer timeToEnableOnDisarm;
        public Integer timeToEnableOnAlert;
    }

    // Common
    public String name;
    public String number;

    // page 1
    public Integer timeToArm;
    public Integer inputManager;
    public Boolean sendSmsOnPowerLoss;
    public Integer timeToWaitOnPowerLoss;
    public Boolean smsAtArm, smsAtDisarm, smsAtWrongKey;
    public String devicePassword;

    // page 2
    public PhoneSettings phones[] = new PhoneSettings[] { new PhoneSettings(), new PhoneSettings(), new PhoneSettings(), new PhoneSettings(), new PhoneSettings() };
    public Integer recallCycles;
    public Integer recallWait;
    public String checkBalanceNum;

    //page 3
    public InputSettings inputs[] = new InputSettings[] { new InputSettings(), new InputSettings(), new InputSettings(), new InputSettings() };

    //page 4
    public OutputSettings outputs[] = new OutputSettings[] { new OutputSettings(), new OutputSettings() };

    // page 5
    public Boolean enableTC;
    public Double tempLimit;
    public Integer tempMode;
    public Boolean tcSendSms, tcActivateAlert, tcActivateInnerSound;
    public Double tMin, tMax;
}
