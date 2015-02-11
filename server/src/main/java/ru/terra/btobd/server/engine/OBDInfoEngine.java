package ru.terra.btobd.server.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.terra.btobd.server.db.ObdJpaController;
import ru.terra.btobd.server.dto.OBDInfoDto;
import ru.terra.btobd.server.entity.OBDInfo;
import ru.terra.btobd.server.entity.User;
import ru.terra.server.engine.AbstractEngine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Date: 17.11.14
 * Time: 2:47
 */
public class OBDInfoEngine extends AbstractEngine<OBDInfo, OBDInfoDto> {
    private UsersEngine usersEngine = new UsersEngine();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public OBDInfoEngine() {
        super(new ObdJpaController());
    }

    @Override
    public OBDInfoDto getDto(Integer integer) {
        try {
            return entityToDto(dbController.get(integer));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void dtoToEntity(OBDInfoDto obdInfoDto, OBDInfo obdInfo) {
        if (obdInfo == null)
            return;
        try {
            obdInfo.setUserId(usersEngine.getUser(obdInfoDto.userId));
            obdInfo.setDeviceId(obdInfoDto.deviceId);
            obdInfo.setId(obdInfoDto.id);
            obdInfo.setLat(obdInfoDto.lat);
            obdInfo.setLon(obdInfoDto.lon);
            obdInfo.setCommand(obdInfoDto.command);
            obdInfo.setResult(obdInfoDto.result);
            obdInfo.setPostdate(new Date(obdInfoDto.date));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public OBDInfoDto entityToDto(OBDInfo obdInfo) {
        if (obdInfo == null)
            return null;
        OBDInfoDto dto = new OBDInfoDto();
        dto.deviceId = obdInfo.getDeviceId();
        dto.lat = obdInfo.getLat();
        dto.lon = obdInfo.getLon();
        dto.command = obdInfo.getCommand();
        dto.result = obdInfo.getResult();
        dto.userId = obdInfo.getUserId().getId();
        dto.date = obdInfo.getPostdate().getTime();
        return dto;
    }

    public OBDInfoDto getCurrent(User uid) {
        return entityToDto(((ObdJpaController) dbController).getCurrent(uid));
    }

    public List<OBDInfoDto> listDtos(Boolean all, Integer page, Integer perPage, User user) {
        try {
            List<OBDInfoDto> ret = new ArrayList<>();
            for (OBDInfo info : ((ObdJpaController) dbController).listByUser(all, page, perPage, user))
                ret.add(entityToDto(info));
            return ret;
        } catch (Exception e) {
            logger.error("Unable to list beans", e);
            return new ArrayList<>();
        }
    }

    public List<String> getParams(User u) {
        return ((ObdJpaController) dbController).getParams(u);
    }

    public List<String> getParamValues(User currentUser, String command) {
        return ((ObdJpaController) dbController).getParamValues(currentUser, command);
    }
}
