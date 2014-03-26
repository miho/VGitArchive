/*
 * ValidationResult.java
 *
 * Copyright 2013-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */

package eu.mihosoft.vgitarchive;

/**
 * Validation result.
 * @see VParamUtil
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
class ValidationResult {
    private String message;
    private boolean valid;
    private Object parameter;
    
    /**
     * Result that represents a valid parameter.
     */
    public static final ValidationResult VALID =
            new ValidationResult(true, null, null);
    
    /**
     * Result that represents an invalid parameter.
     */
    public static final ValidationResult INVALID = 
            new ValidationResult(false, null, null);

    /**
     * Constructor.
     * @param valid defines whether this result represents a valid parameter
     * @param message result message (may be <code>null</code>)
     * @param parameter validated parameter (may be <code>null</code>)
     */
    public ValidationResult(boolean valid, Object parameter, String message) {
        this.message = message;
        this.valid = valid;
        this.parameter = parameter;
    }
    
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the validation state
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return the parameter
     */
    public Object getParameter() {
        return parameter;
    }
}
