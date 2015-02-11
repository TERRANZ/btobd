package ru.terra.btdiag.net.dto;

/**
 * Date: 17.11.14
 * Time: 2:45
 */
public class OBDInfoDto extends CommonDTO {
    public String deviceId = "";
    public Double lat = 0d, lon = 0d;
    public String command = "";
    public String result = "";
    public Integer userId = 0;

    public OBDInfoDto() {
    }
}
