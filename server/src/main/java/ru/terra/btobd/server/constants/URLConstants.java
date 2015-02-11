package ru.terra.btobd.server.constants;

import ru.terra.server.constants.CoreUrlConstants;

public class URLConstants extends CoreUrlConstants {
    public static final String SERVER_DOMAIN = "xn--80aafhfrpg0adapheyc1nya.xn--p1ai";
    public static final String SERVER_URL = "http://" + SERVER_DOMAIN + ":8182/btobd/";

    public class Login {
        public static final String LOGIN = "/login/";
        public static final String LOGIN_DO_LOGIN_JSON = "do.login.json";
        public static final String LOGIN_DO_REGISTER_JSON = "do.register.json";
        public static final String LOGIN_PARAM_USER = "user";
        public static final String LOGIN_PARAM_PASS = "pass";
        public static final String LOGIN_PARAM_EMAIL = "email";
    }

    public class DoJson {
    }

    public class Resources {
        public static final String RESOURCES = "resources/";
        public static final String PICZ = RESOURCES + "picz/";
    }

    public class UI {
        public static final String UI = "/ui/";
        public static final String MAIN = "main";
        public static final String CHAT = "chat";
    }

    public class OBD {
        public static final String OBD = "/obd/";
        public static final String GET_CURRENT = "current.get.json";
        public static final String GET_PARAMS = "get.params.json";
        public static final String GET_PARAM_VALUES = "get.paramvalues.json";
        public static final String DO_REPORT_TROUBLE = "do.report_trouble";
    }

    public class JabberWebClient {
        public static final String JWC = "/jwc/";
        public static final String MAIN = "jwc";
    }
}
