package me.modernpage.util;

public class Constants {
    private Constants() {
    }

    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public static class Regex {
        public static final String USERNAME = "^(?=.*[A-Za-z0-9+_.])(?=\\S+$).{6,15}$";
        public static final String PASSWORD = "^(?=.*[0-9])(?=.*[[a-zA-Z]])(?=\\S+$).{8,20}$";
        public static final String EMAIL = "^([\\p{L}-_\\.\\d]+){1,64}@([\\p{L}-_\\.]+){2,255}.[a-z]{2,}$";
    }

    public static class Network {
        //        public static final String WS_ENDPOINT = "ws://localhost:8080/ws";
//        public static final String ENDPOINT = "http://192.168.100.13:8080/";
        public static final String ENDPOINT = "http://192.168.1.108:8080/";
        //        public static final String ENDPOINT = "http://192.168.43.223:8080/";
        public static final String ENDPOINT_POSTS = ENDPOINT + "posts";
        public static final String ENDPOINT_PRIVATE_GROUPS = ENDPOINT + "privategroups";

    }
}
