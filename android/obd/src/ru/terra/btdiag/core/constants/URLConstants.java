package ru.terra.btdiag.core.constants;

public class URLConstants {
    public static final String SERVER_DOMAIN = "46.36.222.173";
    public static final String SERVER_URL = "http://" + SERVER_DOMAIN + ":8182/btobd";

    public class DoJson {
        public class Login {
            public static final String LOGIN_DO_LOGIN_JSON = "/login/do.login.json";
            public static final String LOGIN_DO_REGISTER_JSON = "/login/do.register.json";

            public static final String LOGIN_PARAM_USER = "user";
            public static final String LOGIN_PARAM_PASS = "pass";
            public static final String LOGIN_PARAM_CAPTCHA = "captcha";
            public static final String LOGIN_PARAM_CAPVAL = "capval";
        }

        public class Obd {
            public static final String ADD = "/obd/do.create.json";
            public static final String DO_REPORT_TROUBLE = "/obd/do.report_trouble";
        }
    }
}