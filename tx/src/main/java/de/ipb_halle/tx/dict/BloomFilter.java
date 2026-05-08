/*
 * Text eXtractor
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
package de.ipb_halle.tx.dict;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class BloomFilter {

    private final static String ALGORITHM = "SHA-256";


    private BitSet                  filter;
    private int                     nKeys;
    private int                     size;

    /**
     * private constructor
     */
    private BloomFilter(int size, int nKeys) {
        this(size, nKeys, new BitSet(1 << size));
    }

    /**
     * private constructor
     */
    private BloomFilter(int size, int nKeys, BitSet bitSet) {
        this.size = size;
        this.nKeys = nKeys;
        this.filter = bitSet;
    }


    /**
     * add a value to the Bloom filter
     * @param bytes the value
     */
    public void addValue(byte[] bytes) {
        int[] keys = computeFingerprint(bytes);
        for (int k : keys) {
            filter.set(k);
        }
    }

    public void addValue(String st) {
        addValue(st.getBytes());
    }

    /**
     * check if a value is present in this filter
     * @param bytes the value
     * @return false if the value is not present in the filter. If the
     * method returns true, the value may have been added to the filter,
     * however there is a certain false positive rate.
     */
    public boolean checkValue(byte[] bytes) {
        boolean result = true;
        int[] keys = computeFingerprint(bytes);
        for (int k : keys) {
            result &= filter.get(k);
        }
        return result;
    }

    public boolean checkValue(String st) {
        return checkValue(st.getBytes());
    }

    private int[] computeFingerprint(byte[] bytes) {
        try {
            MessageDigest algo = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = algo.digest(bytes);
            int length = hash.length;
            if (length < 32) {
                throw new RuntimeException("hash is to short");
            }
            byte[] hashPadded = new byte[hash.length * 2];
            for (int i = 0; i < length; i++) {
                hashPadded[i] = hash[i];
                hashPadded[length + i] = hash[i];
            }
            ByteBuffer hashBuffer = ByteBuffer.wrap(hashPadded);
            int mod = 1 << size;
            int keys[] = new int[nKeys];
            for (int i = 0; i < nKeys; i++) {
                keys[i] = Math.abs(hashBuffer.getInt(i)) % mod;
            }
            return keys;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * (re-)construct a BloomFilter from a given InputStream
     * @param is the stream in format: "SIZE, NKEYS, {bit, bit, bit, ...}"
     * @return the reconstructed BloomFilter
     */
    public static BloomFilter fromInputStream(InputStream is) {
        return fromScanner(new Scanner(is).useDelimiter(Pattern.compile("[{}, ]+")));
    }

    /**
     * reconstruct a BloomFilter from a Scanner
     * @param scanner the preconfigured scanner (delimiter etc.)
     * @return the reconstructed BloomFilter
     */
    private static BloomFilter fromScanner(Scanner scanner) {
        int size = scanner.nextInt();
        if ((size < 10) || (size > 30)) {
            throw new IllegalArgumentException("Invalid filter size (allowed range: 9 < n < 31)");
        }

        int nKeys = scanner.nextInt();
        if ((nKeys < 1) || (nKeys > 32)) {
            throw new IllegalArgumentException("Invalid number of hash functions (allowed range: 0 < n < 33)");
        }

        BitSet bs = new BitSet(1 << size);
        while (scanner.hasNextInt()) {
            bs.set(scanner.nextInt());
        }
        return new BloomFilter(size, nKeys, bs);
    }

    /**
     * (re-)construct a BloomFilter from a given String
     * @param st the string in format: "SIZE, NKEYS, {bit, bit, bit, ...}"
     * @return the reconstructed BloomFilter
     */
    public static BloomFilter fromString(String st) {
        return fromScanner(new Scanner(st).useDelimiter(Pattern.compile("[{}, ]+")));
    }

    /**
     * construct a new BloomFilter with the given parameters
     * @param size the size of the filter (2^size bits)
     * @param nKeys the number of hash functions to apply
     * @return the empty BloomFilter
     */
    public static BloomFilter getBloomFilter(int size, int nKeys) {
        if ((size < 10) || (size > 30)) {
            throw new IllegalArgumentException("Invalid filter size (allowed range: 9 < n < 31)");
        }
        if ((nKeys < 1) || (nKeys > 32)) {
            throw new IllegalArgumentException("Invalid number of hash functions (allowed range: 0 < n < 33)");
        }
        return new BloomFilter(size, nKeys);
    }

    /**
     * @return a string representation of this Bloom filter. The output
     * format is "SIZE, NKEYS, {BIT, BIT, BIT, BIT, ...}"
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String sep = ", ";
        sb.append(Integer.toString(this.size));
        sb.append(sep);
        sb.append(Integer.toString(this.nKeys));
        sb.append(sep);
        sb.append(this.filter.toString());
        return sb.toString();
    }
}
