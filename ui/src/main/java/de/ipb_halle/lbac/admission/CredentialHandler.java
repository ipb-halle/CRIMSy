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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.util.HexUtil;
import java.io.Serializable;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

/**
 * This class provides salted and multi round digested passwords, which should
 * be compatible with the corresponding Tomcat codes (package
 * java.org.apache.catalina.realm). However this class supports only a single
 * type of password digests: "Salt$Iterations$Digest".
 *
 * SHA-256, a 8 byte salt and 2 iterations are used by default.
 */
public class CredentialHandler implements Serializable {

    private final static String DEFAULT_ALGORITHM = "SHA-256";
    private final static int DEFAULT_ITERATIONS = 2;
    private final static int DEFAULT_SALT_LENGTH = 8;

    private static Random random = new Random(new Date().getTime());
    private static Charset charset = Charset.forName("UTF-8");
    private String digestAlgorithm;
    private int iterations;
    private int saltLength;

    public CredentialHandler() {
        this.digestAlgorithm = DEFAULT_ALGORITHM;
        this.iterations = DEFAULT_ITERATIONS;
        this.saltLength = DEFAULT_SALT_LENGTH;
    }

    /**
     * compute a digest for the given credential using the current settings for
     * salt length and iterations. This can be used to transform a user provided
     * password for storage in the password database.
     *
     * @param credential the unencrypted password
     * @return a digest string
     */
    public String computeDigest(String credential) {
        return computeDigest(credential, computeSalt(), this.iterations);
    }

    /**
     * compute a digest for a credential string given a specific salt and
     * iteration count. This method is used to check credentials.
     *
     * @param c the credential string
     * @param s the salt string (in hex)
     * @param iter the iterations
     * @return the digest string
     */
    private String computeDigest(String c, String s, int iter) {
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        sb.append("$");
        sb.append(Integer.toString(iter));
        sb.append("$");

        byte[] salt = HexUtil.fromHex(s);
        byte[] credential = c.getBytes(CredentialHandler.charset);
        byte[] input = new byte[salt.length + credential.length];
        System.arraycopy(salt, 0, input, 0, salt.length);
        System.arraycopy(credential, 0, input, salt.length, credential.length);

        try {
            MessageDigest md = MessageDigest.getInstance(this.digestAlgorithm);

            for (int i = 0; i < iter; i++) {
                input = md.digest(input);
            }
            sb.append(HexUtil.toHex(input));
        } catch (Exception e) {
            return "";
        }
        return sb.toString();
    }

    /**
     * compute a new random salt. The algorithm uses the statically initialized
     * pseudorandom number generator java.util.Random. Initialization is done
     * with the current system time (<code>new Date().getTime()</code>).
     *
     * @return hex string
     */
    private String computeSalt() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < this.saltLength; i += 4) {
            int r = CredentialHandler.random.nextInt();
            sb.append(Integer.toHexString(0x10000000 | r));
        }
        return sb.toString().substring(2 * (i - this.saltLength));
    }

    /**
     * extract the salt and the iteration count from storedCredential and call
     * computeDigest with salt and count for the given credential.Compare
     * computed digest with StoredCredential.
     *
     * @param credential
     * @param storedCredential
     * @return true if storedCredential and
     */
    public boolean match(String credential, String storedCredential) {
        if ((credential == null) || (credential.length() == 0)) {
            return false;
        }
        if ((storedCredential == null) || (storedCredential.length() == 0)) {
            return false;
        }

        int sep1 = storedCredential.indexOf('$');
        int sep2 = storedCredential.indexOf('$', sep1 + 1);

        if ((sep1 < 0) || (sep2 < 0)) {
            return false;
        }

        String salt = storedCredential.substring(0, sep1);
        int iter = Integer.parseInt(storedCredential.substring(sep1 + 1, sep2));

        if (iter < 1) {
            return false;
        }

        return computeDigest(credential, salt, iter).equalsIgnoreCase(storedCredential);
    }

    /**
     * set the digest algorithm
     *
     * @param a the algorithm (e.g. "MD5", "SHA1", "SHA-256", "SHA-512")
     * @return this object
     */
    public CredentialHandler setDigestAlgorithm(String a) {
        this.digestAlgorithm = a;
        return this;
    }

    /**
     * set the number of iterations
     *
     * @param i the number of iterations
     * @return this object
     */
    public CredentialHandler setIterations(int i) {
        this.iterations = i;
        return this;
    }

    /**
     * set the length of the salt in bytes
     *
     * @param i the length of the salt in bytes
     * @return this object
     */
    public CredentialHandler setSaltLength(int i) {
        this.saltLength = i;
        return this;
    }
}
