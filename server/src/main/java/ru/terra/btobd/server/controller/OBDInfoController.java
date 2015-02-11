package ru.terra.btobd.server.controller;

import com.sun.jersey.api.core.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.terra.btobd.server.constants.URLConstants;
import ru.terra.btobd.server.dto.OBDInfoDto;
import ru.terra.btobd.server.engine.OBDInfoEngine;
import ru.terra.btobd.server.engine.TroublesEmailReporteEngine;
import ru.terra.btobd.server.entity.OBDInfo;
import ru.terra.btobd.server.entity.User;
import ru.terra.server.constants.CoreUrlConstants;
import ru.terra.server.constants.ErrorConstants;
import ru.terra.server.controller.AbstractController;
import ru.terra.server.dto.CommonDTO;
import ru.terra.server.dto.ListDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.List;

/**
 * Date: 17.11.14
 * Time: 2:53
 */
@Path(URLConstants.OBD.OBD)
public class OBDInfoController extends AbstractController<OBDInfo, OBDInfoDto, OBDInfoEngine> {
    public OBDInfoController() {
        super(OBDInfoEngine.class, true, OBDInfo.class, OBDInfoDto.class);
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Path(URLConstants.OBD.GET_CURRENT)
    @GET
    public OBDInfoDto getCurrent(@Context HttpContext hc) {
        if (isAuthorized(hc)) {
            User u = (User) getCurrentUser(hc);
            OBDInfoDto ret = engine.getCurrent(u);
            if (ret == null) {
                ret = new OBDInfoDto();
                ret.errorCode = 3;
                ret.errorMessage = "Not found";
                return ret;
            } else
                return ret;
        }
        OBDInfoDto ret = new OBDInfoDto();
        ret.errorCode = ErrorConstants.ERR_NOT_AUTHORIZED_ID;
        ret.errorMessage = ErrorConstants.ERR_NOT_AUTHORIZED_MSG;
        return ret;
    }

    @Override
    @GET
    @Path(CoreUrlConstants.DoJson.DO_LIST)
    public ListDTO<OBDInfoDto> list(@Context HttpContext hc, @QueryParam("all") Boolean all, @QueryParam("page") Integer page, @QueryParam("perpage") Integer perpage) {
        if (!isAuthorized(hc)) {
            ListDTO<OBDInfoDto> ret = new ListDTO<>();
            ret.errorCode = ErrorConstants.ERR_NOT_AUTHORIZED_ID;
            ret.errorMessage = ErrorConstants.ERR_NOT_AUTHORIZED_MSG;
            return ret;
        }
        ListDTO<OBDInfoDto> ret = new ListDTO<>();
        if (all == null)
            all = true;
        if (page == null)
            page = -1;
        if (perpage == null)
            perpage = -1;
        ret.setData(engine.listDtos(all, page, perpage, (User) getCurrentUser(hc)));
        return ret;
    }


    @Path(URLConstants.OBD.GET_PARAMS)
    @GET
    public ListDTO<String> getParams(@Context HttpContext hc) {
        if (!isAuthorized(hc)) {
            ListDTO<String> ret = new ListDTO<>();
            ret.errorCode = ErrorConstants.ERR_NOT_AUTHORIZED_ID;
            ret.errorMessage = ErrorConstants.ERR_NOT_AUTHORIZED_MSG;
            return ret;
        }
        ListDTO<String> ret = new ListDTO<>();
        List<String> data = engine.getParams((User) getCurrentUser(hc));
        if (data != null)
            ret.data = data;
        return ret;
    }

    @Path(URLConstants.OBD.GET_PARAM_VALUES)
    @GET
    public ListDTO<String> getParamValues(@Context HttpContext hc, @QueryParam("param") String param) {
        if (!isAuthorized(hc)) {
            ListDTO<String> ret = new ListDTO<>();
            ret.errorCode = ErrorConstants.ERR_NOT_AUTHORIZED_ID;
            ret.errorMessage = ErrorConstants.ERR_NOT_AUTHORIZED_MSG;
            return ret;
        }
        ListDTO<String> ret = new ListDTO<>();
        List<String> data = engine.getParamValues((User) getCurrentUser(hc), param);
        if (data != null)
            ret.data = data;
        return ret;
    }

    @Path(URLConstants.OBD.DO_REPORT_TROUBLE)
    @GET
    public CommonDTO reportTrouble(@Context HttpContext hc, @QueryParam("trouble") String trouble) {
        if (!isAuthorized(hc)) {
            CommonDTO ret = new CommonDTO();
            ret.errorCode = ErrorConstants.ERR_NOT_AUTHORIZED_ID;
            ret.errorMessage = ErrorConstants.ERR_NOT_AUTHORIZED_MSG;
            return ret;
        }
        new TroublesEmailReporteEngine(((User) getCurrentUser(hc)).getEmail()).notify(trouble);
        return new CommonDTO();
    }
}
