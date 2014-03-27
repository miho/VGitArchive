/*
 * VParamUtil.java
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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;


/**
 * <p>
 * This utility class allows parameter validation.
 * </p>
 * <p><b>Example (Java code):</b></p>
 * <p>Validate that all parameters are not null:</p>
 * <code>
 * <pre>
 * class SampleClass {
 *   
 *   public SampleClass(Object a, Object b, Object c) {
 *       VParamUtil.throwIfNotNull(a,b,c);
 *   }
 * }
 * </pre>
 * </code>
 * <p>Validate that all parameters are instances of Integer:</p>
 * <code>
 * <pre>
 * class SampleClass {
 *   
 *   public SampleClass(Object a, Object b, Object c) {
 *       VParamUtil.throwIfNotValid(VParamUtil.VALIDATOR_INSTANCEOF, Integer.class, a, b, c);
 *   }
 * }
 * </pre>
 * </code>
 * 
 * <b>Note:</b> By providing custom implementations of {@link ParameterValidator} this
 * framework can be easily extended.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class VParamUtil {

    /**
     * Validates that the specified parameter is not null.
     */
    public static final ParameterValidator VALIDATOR_NOT_NULL =
            new NotNullValidator();
    /**
     * Validates that the specified parameter is a file object.
     */
    public static final ParameterValidator VALIDATOR_FILE =
            new FileValidator();
    /**
     * Validates that the specified parameter is a file object that represents
     * an existing file.
     */
    public static final ParameterValidator VALIDATOR_EXISTING_FILE =
            new ExistingFileValidator();
    /**
     * Validates that the specified parameter is a file object that represents 
     * an existing folder.
     */
    public static final ParameterValidator VALIDATOR_EXISTING_FOLDER =
            new ExistingFolderValidator();
    
    
    /**
     * Validates that the specified parameter is a class object
     */
    public static final ParameterValidator VALIDATOR_CLASS =
            new ClassValidator();
    
    /**
     * Validates that the specified parameter is an instance of the class
     * that has been specified as validationArg
     */
    public static final ParameterValidator VALIDATOR_INSTANCEOF =
            new InstanceOfValidator();

    /**
     * Validates that the specified objects are not null. If one or more objects 
     * are null an {@link IllegalArgumentException} will be thrown.
     * @param params parameters to validate
     */
    public static void throwIfNull(Object... params) {
        throwIfNotValid(VALIDATOR_NOT_NULL, null, params);
    }

    /**
     * Validates that the specified objects meet the requested conditions.
     * If the given validator invalidates one or more of the objects to validate
     * an {@link IllegalArgumentException} will be thrown.
     * @param v validator
     * @param validationArg validation argument 
     * (may be null, usage depends on the validator implementation)
     * @param params objects to validate
     */
    public static void throwIfNotValid(
            ParameterValidator v,
            Object validationArg,
            Object... params) {

        for (Object o : params) {
            ValidationResult r = v.validate(o, validationArg);

            if (!r.isValid()) {
                throw new IllegalArgumentException(r.getMessage());
            }
        }
    }

    /**
     * Validates that the specified objects meet the requested conditions.
     * If the given validator invalidates one or more of the objects to validate
     * an {@link IllegalArgumentException} will be thrown.
     * @param v validator
     * @param validationArg validation argument 
     * (may be null, usage depends on the validator implementation)
     * @param params objects to validate
     */
    public static ValidationResult validate(
            ParameterValidator v,
            Object validationArg,
            Object... params) {

        for (Object o : params) {
            ValidationResult r = v.validate(o, validationArg);

            if (!r.isValid()) {
                return r;
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates that the specified objects meet the requested conditions.
     * @param v validator
     * @param validationArg validation argument (may be null, usage depends on the validator implementation)
     * @param params objects to validate
     * @return validation result
     */
    public static ValidationResult validate(
            ParameterValidator v,
            Object validationArg,
            Collection<?> params) {

        for (Object o : params) {
            ValidationResult r = v.validate(o, validationArg);

            if (!r.isValid()) {
                return r;
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates that the specified objects meet the requested conditions.
     * @param v validator
     * @param params objects to validate
     * @return validation result
     */
    public static ValidationResult validate(
            ParameterValidator v,
            Collection<?> params) {
        return validate(v, null, params);
    }

    /**
     * Validates that the specified objects meet the requested conditions.
     * If the given validator invalidates one or more of the objects to validate
     * an {@link IllegalArgumentException} will be thrown.
     * @param v validator
     * @param validationArg validation argument 
     * (may be null, usage depends on the validator implementation)
     * @param params objects to validate
     */
    public static void throwIfNotValid(
            ParameterValidator v,
            Object validationArg,
            Collection<?> params) {

        for (Object o : params) {
            ValidationResult r = v.validate(o, validationArg);

            if (!r.isValid()) {
                throw new IllegalArgumentException(r.getMessage());
            }
        }
    }

    /**
     * Validates that the specified objects meet the requested conditions.
     * If the given validator invalidates one or more of the objects to validate
     * an {@link IllegalArgumentException} will be thrown.
     * @param v validator
     * @param params objects to validate
     */
    public static void throwIfNotValid(
            ParameterValidator v,
            Collection<?> params) {
        throwIfNotValid(v, null, params);
    }
}
class ParameterValidatorImpl implements ParameterValidator {

    private HashSet<ParameterValidator> dependencies =
            new HashSet<ParameterValidator>();

    public ParameterValidatorImpl() {
        // 
    }

    

    public ParameterValidatorImpl(ParameterValidator... validators) {
        VParamUtil.validate(
                VParamUtil.VALIDATOR_NOT_NULL, Arrays.asList(validators));
        
        dependencies.addAll(Arrays.asList(validators));
    }

    @Override
    public final ValidationResult validate(Object p) {
        return validate(p, null);
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {
        for (ParameterValidator v : dependencies) {

            if (v == null) {
                throw new IllegalArgumentException("***");
            }
            
            ValidationResult r = v.validate(p, validationArg);

            if (!r.isValid()) {
                return r;
            }
        }

        return ValidationResult.VALID;
    }
}

class NotNullValidator extends ParameterValidatorImpl {

    public NotNullValidator() {
        // no dependencies
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {
        
        ValidationResult r = super.validate(p,validationArg);

        if (!r.isValid()) {
            return r;
        }
        
        if (p == null) {
            return new ValidationResult(
                    false, p, "Argument \"null\" is not supported.");
        }

        return ValidationResult.VALID;
    }
}

class ClassValidator extends ParameterValidatorImpl {
    public ClassValidator() {
        super(VParamUtil.VALIDATOR_NOT_NULL);
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {
        
        ValidationResult r = super.validate(p,validationArg);

        if (!r.isValid()) {
            return r;
        }

        if (!(p instanceof Class<?>)) {
            return new ValidationResult(
                    false, p, "Argument is no class object.");
        }
        
        return ValidationResult.VALID;
    }
}

class InstanceOfValidator extends ParameterValidatorImpl {

    public InstanceOfValidator() {
        super(VParamUtil.VALIDATOR_NOT_NULL);
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {

        ValidationResult r = VParamUtil.validate(
                VParamUtil.VALIDATOR_CLASS, null, validationArg);
                
        if (!r.isValid()) {
            return r;
        }

        r = super.validate(p,validationArg);

        if (!r.isValid()) {
            return r;
        }

        Class<?> clazz = (Class<?>)validationArg;
        
        if (!clazz.isAssignableFrom(p.getClass())) {
            return new ValidationResult(
                    false, p, 
                    "Argument is no instance of \"" + clazz.getName() + "\".");
        }
        
        return ValidationResult.VALID;
    }
}

class ExistingFolderValidator extends ParameterValidatorImpl {

    public ExistingFolderValidator() {
        super(VParamUtil.VALIDATOR_FILE);
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {

        ValidationResult r = super.validate(p,validationArg);

        if (!r.isValid()) {
            return r;
        }

        if (!((File) p).isDirectory()) {
            return new ValidationResult(false, p, "Argument is no directory: " + p);
        }

        return ValidationResult.VALID;
    }
}

class ExistingFileValidator extends ParameterValidatorImpl {

    public ExistingFileValidator() {
        super(VParamUtil.VALIDATOR_FILE);
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {

        ValidationResult r = super.validate(p, validationArg);

        if (!r.isValid()) {
            return r;
        }

        if (!((File) p).exists() || ((File) p).isDirectory()) {
            return new ValidationResult(
                    false, p, "Argument is no existing file: " + p);
        }

        return ValidationResult.VALID;
    }
}

class FileValidator extends ParameterValidatorImpl {

    public FileValidator() {
        super(VParamUtil.VALIDATOR_NOT_NULL);
    }

    @Override
    public ValidationResult validate(Object p, Object validationArg) {

        ValidationResult r = super.validate(p, validationArg);

        if (!r.isValid()) {
            return r;
        }

        if (!(p instanceof File)) {
            return new ValidationResult(
                    false, p, "Argument is no file object.");
        }

        return ValidationResult.VALID;
    }
}
