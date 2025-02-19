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


package org.herodbx.scram100b2.common.exception;


import org.herodbx.scram100b2.common.message.ServerFinalMessage;


/**
 * This class represents an error when parsing SCRAM messages
 */
public class ScramServerErrorException extends ScramException {
    private final ServerFinalMessage.Error error;

    private static String toString(ServerFinalMessage.Error error) {
        return "Server-final-message is an error message. Error: " + error.getErrorMessage();
    }

    /**
     * Constructs a new instance of ScramServerErrorException with a detailed message.
     * @param error The SCRAM error in the message
     */
    public ScramServerErrorException(ServerFinalMessage.Error error) {
        super(toString(error));
        this.error = error;
    }

    /**
     * Constructs a new instance of ScramServerErrorException with a detailed message and a root cause.
     * @param error The SCRAM error in the message
     * @param ex The root exception
     */
    public ScramServerErrorException(ServerFinalMessage.Error error, Throwable ex) {
        super(toString(error), ex);
        this.error = error;
    }

    public ServerFinalMessage.Error getError() {
        return error;
    }
}
