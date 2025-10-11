package org.cage.eaglemq.nameserver.utils;

import org.cage.eaglemq.nameserver.common.CommonCache;

/**
 * @Author idea
 * @Date: Created in 16:34 2024/5/18
 * @Description
 */
public class NameserverUtils {

    public static boolean isVerify(String user, String password) {
        String rightUser = CommonCache.getNameserverProperties().getNameserverUser();
        String rightPassword = CommonCache.getNameserverProperties().getNameserverPwd();
        if (!rightUser.equals(user) || !rightPassword.equals(password)) {
            return false;
        }
        return true;
    }

}
