package com.adonai.GsmNotify.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by adonai on 26.01.15.
 */
@DatabaseTable(tableName = "history")
public class HistoryEntry {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, index = true)
    private String deviceName;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_LONG)
    private Date eventDate;

    @DatabaseField(canBeNull = false)
    private String smsText;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }
}
