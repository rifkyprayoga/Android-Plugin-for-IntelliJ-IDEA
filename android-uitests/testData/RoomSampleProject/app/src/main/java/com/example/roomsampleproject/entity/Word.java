package com.example.roomsampleproject.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName="word_table")
public class Word {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name="word")
    private String mWord;

    public Word(String word){
        this.mWord = word;
    }

    public String getWord(){
        return mWord;
    }

}


