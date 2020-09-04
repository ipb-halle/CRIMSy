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

import de.ipb_halle.tx.TxModule;

import java.io.InputStream;
import java.util.Random;
import java.util.BitSet;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.rabinfingerprint.polynomial.Polynomial;

public class BloomFilter {

    /* 
     * irreducible polynomial of degree < 54 produced by throwing random 
     * long values to Polynomial.createFromLong(l) until the condition
     * p.getReducibility() == Polynomial.Reducibility.IRREDUCIBLE 
     * is fulfilled.
     */
    private final static long polynomial = 13994254734449057L;

    /*
     * just random long values 
     */
    private final static long[] random = {
                1084016744274457255L, 6848157825034131277L, 5289167972936664505L, 2737274229124826695L, 
                7770867406251104201L, 862958235986621271L, 5130944805743769117L, 8108478473292398703L, 
                5517900856849572015L, 1777198858200335313L, 2274866572042975285L, 4225438158837473663L, 
                618023298619761283L, 5925373925768950805L, 8380525927381377999L, 538607135012445719L, 
                4339025683209542027L, 3191157533823981085L, 1625073559458085339L, 2409766076249031059L, 
                6767058762505734327L, 7674541281992680289L, 4453932523813185837L, 7992945002136417553L, 
                3130817799042650095L, 1586386836960354457L, 4332117293366900169L, 5682427273528174407L, 
                6894067557228884081L, 7313678887233372903L, 7547968463809087121L, 3415206839363569827L};

    private BitSet                  filter;
    private RabinFingerprintLong    fpFunction;
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
        this.fpFunction = new RabinFingerprintLong(
                    Polynomial.createFromLong(this.polynomial));
    }


    /**
     * add a value to the Bloom filter
     * @param bytes the value
     */
    public void addValue(byte[] bytes) {
        long mod = (1 << this.size) - 1;
        synchronized(this.fpFunction) {
            this.fpFunction.reset();
            this.fpFunction.pushByte((byte) 0xFF);
            this.fpFunction.pushBytes(bytes);
            long longHash = this.fpFunction.getFingerprintLong();
            for(int i = 0; i < nKeys; i++) {
                int hash = (int) ((longHash ^ this.random[i]) % mod);
                this.filter.set(hash);
            }
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
     * however there is a certian false positive rate.
     */
    public boolean checkValue(byte[] bytes) {
        long mod = (1 << this.size) - 1;
        boolean result = true;
        synchronized(this.fpFunction) {
            this.fpFunction.reset();
            this.fpFunction.pushByte((byte) 0xff);
            this.fpFunction.pushBytes(bytes);
            long longHash = this.fpFunction.getFingerprintLong();
            for(int i = 0; i < nKeys; i++) {
                int hash = (int) ((longHash ^ this.random[i]) % mod);
                result &= this.filter.get(hash); 
            }
        }
        return result;
    }

    public boolean checkValue(String st) {
        return checkValue(st.getBytes());
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
