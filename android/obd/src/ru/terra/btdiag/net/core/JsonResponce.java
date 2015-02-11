package ru.terra.btdiag.net.core;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: terranz
 * Date: 07.06.13
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 */
public class JsonResponce {
    public InputStream json;
    public int code;

    public JsonResponce(InputStream json) {
        this.json = json;
    }

    public JsonResponce() {
    }
}
