package com.sixhands.misc;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    //https://stackoverflow.com/a/10650881
    public static Map<TimeUnit,Long> computeDiff(Date date1, Date date2) {

        long diffInMillies = date2.getTime() - date1.getTime();

        //create the list
        List<TimeUnit> units = new ArrayList<>(EnumSet.of(TimeUnit.DAYS,TimeUnit.HOURS));
        Collections.reverse(units);

        //create the result map of TimeUnit and difference
        Map<TimeUnit,Long> result = new LinkedHashMap<>();
        long milliesRest = diffInMillies;

        for ( TimeUnit unit : units ) {

            //calculate difference in millisecond
            long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;

            //put the result in the map
            result.put(unit,diff);
        }

        return result;
    }
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
    public static Date parseDateFromTHStr(String s) throws ParseException {
        return dateFormat.parse(s);
    }
}
