package com.xty.tagscontainer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xty.library.BjTagsContainView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BjTagsContainView timeTags,timeTagsMulti,colorTagsMultiExclude;
    private List<String> timeTagStrs,colorTagStrs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeTags = findViewById(R.id.timeTags);
        initTimeTagsData();
        timeTags.setTags(timeTagStrs);

        timeTagsMulti = findViewById(R.id.timeTagsMulti);
        timeTagsMulti.setTags(timeTagStrs);

        colorTagsMultiExclude = findViewById(R.id.colorTagsMultiExclude);
        initColorTagsData();
        colorTagsMultiExclude.setTags(colorTagStrs);
        colorTagsMultiExclude.setExcludePosition(0);
    }


    private void initTimeTagsData(){
        timeTagStrs = new ArrayList<>();
        timeTagStrs.add("3天内");
        timeTagStrs.add("1周内");
        timeTagStrs.add("2周内");
        timeTagStrs.add("一月内");
        timeTagStrs.add("3月内");
        timeTagStrs.add("半年内");
    }

    private void initColorTagsData(){
        colorTagStrs = new ArrayList<>();
        colorTagStrs.add("不需要");
        colorTagStrs.add("白色");
        colorTagStrs.add("红色");
        colorTagStrs.add("黄色");
        colorTagStrs.add("蓝色");
        colorTagStrs.add("绿色");
    }
}
