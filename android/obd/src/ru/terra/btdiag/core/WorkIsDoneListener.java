package ru.terra.btdiag.core;

public interface WorkIsDoneListener {
    public void workIsDone(int action, Exception e, String... params);
}
