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


package org.herodbx.scram100b2.common.gssapi;


import org.herodbx.scram100b2.common.util.CharAttribute;


/**
 * Possible values of a GS2 Cbind Flag (channel binding; part of GS2 header).
 * These values are sent by the client, and so are interpreted from this perspective.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
public enum Gs2CbindFlag implements CharAttribute {
    /**
     * Client doesn't support channel binding.
     */
    CLIENT_NOT('n'),

    /**
     * Client does support channel binding but thinks the server does not.
     */
    CLIENT_YES_SERVER_NOT('y'),

    /**
     * Client requires channel binding. The selected channel binding follows "p=".
     */
    CHANNEL_BINDING_REQUIRED('p')
    ;

    private final char flag;

    Gs2CbindFlag(char flag) {
        this.flag = flag;
    }

    @Override
    public char getChar() {
        return flag;
    }

    public static Gs2CbindFlag byChar(char c) {
        switch(c) {
            case 'n':   return CLIENT_NOT;
            case 'y':   return CLIENT_YES_SERVER_NOT;
            case 'p':   return CHANNEL_BINDING_REQUIRED;
        }

        throw new IllegalArgumentException("Invalid Gs2CbindFlag character '" + c + "'");
    }
}
