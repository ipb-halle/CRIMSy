/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.device.job;

import java.util.Date;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tokengenerator generates a HMAC based access token 
 * from a shared secret. This access token must be transmitted 
 * over encrypted channels (HTTPS) and must not be used in 
 * untrustworthy environments. It provides no protection 
 * against replay attacts (the window of vulnerability is 
 * several minutes wide) or message tampering (only the
 * token itself is protected).
 */
public class TokenGenerator {

    private final static long TIME_DELTA = 100000L;
    private final static String SEPARATOR = "#";
    private final static String MAC_ALGORITHM = "HmacSHA256";
    private final static String DIGEST_ALGORITHM = "SHA-256";
    private final static String CIPHER_ALGORITHM = "AES";

    /**
     * check a given token against current time and a the shared secret
     */
    public static boolean checkToken(String token, String secret) {
        try {
            String[] parts = token.split(SEPARATOR);
            long localTime = new Date().getTime();
            long remoteTime = Long.parseLong(parts[0], 16);
            if (( remoteTime < (localTime - TIME_DELTA)) || (remoteTime > (localTime + TIME_DELTA))) {
                // invalid token time
                return false;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(parts[0]);
            sb.append(SEPARATOR);
            sb.append(parts[1]);
            sb.append(SEPARATOR);
            return token.equals(computeMac(sb, secret)); 
        } catch(Exception e) {
            // ignore
        }
        return false;
    }

    /**
     * obtain a token for the given secret
     */
    public static String getToken(String secret) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(new Date().getTime()));
        sb.append(SEPARATOR);
        sb.append(Long.toHexString(random.nextLong()));
        sb.append(Long.toHexString(random.nextLong()));
        sb.append(SEPARATOR);
        return computeMac(sb, secret);
    }

    /**
     * compute a HMAC from the string in the StringBuilder
     */
    private static String computeMac(StringBuilder sb, String secret) {
        try {
            Key key = SecretKeyFactory.getInstance(CIPHER_ALGORITHM)
                .generateSecret(new SecretKeySpec(
                    MessageDigest.getInstance(DIGEST_ALGORITHM).digest(secret.getBytes()), 
                    CIPHER_ALGORITHM));
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            mac.init(key);
            byte[] hmac = mac.doFinal(sb.toString().getBytes());

            for (byte b : hmac) {
                sb.append(Integer.toHexString((b & 0x000000ff) + 0x100).substring(1));
            }
            return sb.toString();
        } catch(Exception e) {
            // e.printStackTrace();
            // ignore, will fail save
        }
        return null;
    }
}
