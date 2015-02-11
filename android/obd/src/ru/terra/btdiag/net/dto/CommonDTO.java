package ru.terra.btdiag.net.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Date: 20.11.14
 * Time: 12:48
 */
public class CommonDTO implements Serializable {
    public String errorMessage = "";
    public Integer errorCode = 0;
    public String status = "";
    public Long timestamp = new Date().getTime();
    public Integer id = -1;

    public CommonDTO() {
    }
}
