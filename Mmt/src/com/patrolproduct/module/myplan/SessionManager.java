package com.patrolproduct.module.myplan;

import com.patrolproduct.module.myplan.entity.PatrolTask;

import java.util.ArrayList;
import java.util.Hashtable;

public class SessionManager {
    public final static ArrayList<PatrolTask> patrolTaskList = new ArrayList<>();
    public final static ArrayList<Hashtable<String, String>> taskStateTable = new ArrayList<>();
}
