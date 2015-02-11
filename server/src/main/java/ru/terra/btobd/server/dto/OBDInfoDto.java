package ru.terra.btobd.server.dto;

import ru.terra.server.dto.CommonDTO;

/**
 * Date: 17.11.14
 * Time: 2:45
 */
public class OBDInfoDto extends CommonDTO {
    public String deviceId;
    public Double lat, lon;
    public String command;
    public String result;
    public Integer userId;
    public Long date;

    public OBDInfoDto() {
    }
}
