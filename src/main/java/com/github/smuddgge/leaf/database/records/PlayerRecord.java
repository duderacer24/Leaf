package com.github.smuddgge.leaf.database.records;

import com.github.smuddgge.squishydatabase.record.Record;
import com.github.smuddgge.squishydatabase.record.RecordFieldAnnotation;
import com.github.smuddgge.squishydatabase.record.RecordFieldType;

/**
 * Represents a players record in the database table.
 */
public class PlayerRecord extends Record {

    @RecordFieldAnnotation(type = RecordFieldType.PRIMARY)
    public String uuid;

    public String name;

    /**
     * Added at version 2.2.0
     */
    public String toggleCanMessage = "true";

    public String toggleSeeSpy = "false";
}
