/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package org.herodbx.scram100b2.common.util;


import static org.herodbx.scram100b2.common.util.Preconditions.checkNotNull;


public class UsAsciiUtils {
    /**
     * Removes non-printable characters from the US-ASCII String.
     * @param value The original String
     * @return The possibly modified String, without non-printable US-ASCII characters.
     * @throws IllegalArgumentException If the String is null or contains non US-ASCII characters.
     */
    public static String toPrintable(String value) throws IllegalArgumentException {
        checkNotNull(value, "value");

        char[] printable = new char[value.length()];
        int i = 0;
        for(char chr : value.toCharArray()) {
            int c = (int) chr;
            if (c < 0 || c >= 127) {
                throw new IllegalArgumentException("value contains character '" + chr + "' which is non US-ASCII");
            } else if (c > 32) {
                printable[i++] = chr;
            }
        }

        return i == value.length() ? value : new String(printable, 0, i);
    }
}
