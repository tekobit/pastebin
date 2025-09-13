package com.zufarov.pastebinV1.pet.util;

public class PermissionCacheKeyGenerator {
    public static String generateCacheKey(String pasteId, String username) {
        if (pasteId == null || username == null) {
            return null;
        }
        return pasteId+'_'+username;
    }

}
