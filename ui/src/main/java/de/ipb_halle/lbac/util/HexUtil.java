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
package de.ipb_halle.lbac.util;

/**
 * This class provides some utility methods to convert Strings to 
 * hex strings and vice versa.
 */
public class HexUtil {

    /**
     * convert a hex string into a byte array. This method is case 
     * insensitive and will work on all characters which will 
     * be transformed to values in the range 0 .. 0xf by the 
     * following algorithm:
     *
     *   nibble = char &amp; 0x4f;
     *   nibble -= (nibble &gt; 9) ? 0x37 : 0;
     *
     * Input strings of odd length and haracters which do not fulfill this 
     * requirement will trigger an IllegalArgumentException.
     * 
     * @param h the hex string
     * @return an array of bytes or null if h is null
     * @throws IllegalArgumentException
     */
    public static byte[] fromHex(String h) {
        if (h == null) {
            return null;
        }

        if ((h.length() & 1) == 1) {
            // Odd number of characters
            throw new IllegalArgumentException("HexUtil.fromHex: odd number of digits"); 
        }

        int l = h.length() >> 1;
        byte[] result = new byte[l];
        for(int i=0; i<l; i++) {
            byte upper = (byte) (h.charAt(i*2) & 0x4f);
            byte lower = (byte) (h.charAt((i*2) + 1) & 0x4f);
            upper -= (upper > 9) ? 0x37 : 0;
            lower -= (lower > 9) ? 0x37 : 0;
            if((upper < 0) || (lower < 0) || (upper > 0xf) || (lower > 0xf)) {
                throw new IllegalArgumentException("HexUtil.fromHex: illegal character");
            }
            result[i] = (byte) ((upper << 4) | lower);
        }

        return result;
    }

    /**
     * convert byte array to hex string
     *
     * @param bytes the byte array
     * @return string
     */
    public static String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            hexString.append(Integer.toHexString((bytes[i] & 0xff) + 0x100).substring(1));
        }

        return hexString.toString();
    }

    /**
     * convert byte to String of length 2
     *
     * @param b byte
     * @return string with length=2
     */
    public static String toHex(byte b) {
        return Integer.toHexString((b & 0x000000ff) + 0x100).substring(1);
    }

}
