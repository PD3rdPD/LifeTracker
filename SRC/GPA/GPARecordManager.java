package GPA;

import java.util.ArrayList;

public class GPARecordManager {

    private ArrayList<GPARecord> records;

    public GPARecordManager() {
        records = new ArrayList<>();
    }

    public void addRecord(GPARecord record) {
        records.add(record);
    }

    public ArrayList<GPARecord> getRecords() {
        return records;
    }

    public GPARecord getLatestRecord() {
        if (records.isEmpty()) {
            return null;
        }

        return records.get(records.size() - 1);
    }
}