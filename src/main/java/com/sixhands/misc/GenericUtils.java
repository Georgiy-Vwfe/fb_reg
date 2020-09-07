package com.sixhands.misc;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.StreamSupport;

public class GenericUtils {
    //https://stackoverflow.com/a/2216603
    public static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            throw new
                    NullPointerException("Entity passed for initialization is null");
        }

        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
                    .getImplementation();
        }
        return entity;
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    //https://stackoverflow.com/a/157202
    public static String randomAlphaNumString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }


}
