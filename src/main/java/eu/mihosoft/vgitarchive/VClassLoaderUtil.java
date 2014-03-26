/*
 * VClassLoaderUtil.java
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

import java.io.ObjectStreamClass;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
class VClassLoaderUtil {

    private static final Map<String, Class<?>> primitiveClasses =
            new HashMap<String, Class<?>>();
    private static final Map<String, Class<?>> primitiveWrapperClasses =
            new HashMap<String, Class<?>>();
    private static final Map<String, Class<?>> primitiveSignatures =
            new HashMap<String, Class<?>>();
    private static final Map<String, String> primitiveSignaturesToFullNames =
            new HashMap<String, String>();

    static {

        primitiveClasses.put("boolean", boolean.class);
        primitiveClasses.put("byte", byte.class);
        primitiveClasses.put("short", short.class);
        primitiveClasses.put("char", char.class);
        primitiveClasses.put("int", int.class);
        primitiveClasses.put("long", long.class);
        primitiveClasses.put("float", float.class);
        primitiveClasses.put("double", double.class);
        primitiveClasses.put("void", void.class);

        primitiveWrapperClasses.put("boolean", Boolean.class);
        primitiveWrapperClasses.put("byte", Byte.class);
        primitiveWrapperClasses.put("short", Short.class);
        primitiveWrapperClasses.put("char", Character.class);
        primitiveWrapperClasses.put("int", Integer.class);
        primitiveWrapperClasses.put("long", Long.class);
        primitiveWrapperClasses.put("float", Float.class);
        primitiveWrapperClasses.put("double", Double.class);

        // only primitive version of void is used by vrl
        primitiveWrapperClasses.put("void", void.class);
        

        primitiveSignatures.put("Z", boolean.class);
        primitiveSignatures.put("B", byte.class);
        primitiveSignatures.put("C", char.class);
        primitiveSignatures.put("D", double.class);
        primitiveSignatures.put("F", float.class);
        primitiveSignatures.put("I", int.class);
        primitiveSignatures.put("J", long.class);
        primitiveSignatures.put("S", short.class);
        primitiveSignatures.put("V", void.class);

        primitiveSignaturesToFullNames.put("Z", "boolean");
        primitiveSignaturesToFullNames.put("B", "byte");
        primitiveSignaturesToFullNames.put("C", "char");
        primitiveSignaturesToFullNames.put("D", "double");
        primitiveSignaturesToFullNames.put("F", "float");
        primitiveSignaturesToFullNames.put("I", "int");
        primitiveSignaturesToFullNames.put("J", "long");
        primitiveSignaturesToFullNames.put("S", "short");
        primitiveSignaturesToFullNames.put("V", "void");

    }

    /**
     * A replacement for {@link java.lang.ClassLoader#loadClass(java.lang.String)}
     * which handles array syntax correcectly, e.g.,
     * <code>[LString;</code>. <p> Evaluation of Bug 6500212 suggests
     * {@link Class#forName(java.lang.String, boolean, java.lang.ClassLoader) }.
     * But this method does cache classes in the initiating classloader rather
     * than the specified one. This makes it impossible to use the method if
     * several versions of a class shall be loaded at runtime. </p>
     *
     * @param clsName
     * @param classLoader
     * @return
     * @throws ClassNotFoundException
     * @see http://bugs.sun.com/view_bug.do?bug_id=6500212
     * @see
     * http://blog.bjhargrave.com/2007/09/classforname-caches-defined-class-in.html
     */
    public static Class<?> forName(String clsName, ClassLoader classLoader)
            throws ClassNotFoundException {

        // is array
        if (clsName.startsWith("[")) {

            // compute array dim
            int arrayDim = arrayDimension(clsName);

            String elementClsName =
                    clsName.substring(arrayDim, clsName.length());

            Class<?> elementClass = null;

            // if non-primitive remove 'L' from beginning and ';' from end
            if (elementClsName.endsWith(";")) {
                elementClsName =
                        elementClsName.substring(1, elementClsName.length() - 1);

                elementClass = classLoader.loadClass(elementClsName);
            } else {
                elementClass = primitiveClassForName(elementClsName);
            }

            Class<?> result =
                    Array.newInstance(
                    elementClass, new int[arrayDim]).getClass();

            return result;
        }

        return classLoader.loadClass(clsName);
    }

    /**
     * Computes the dimension of the array class specified by name.
     *
     * @param clsName name, e.g.,
     * <code>[[I</code> or
     * <code>[LString;</code>
     * @return the dimension of the array class specified by name
     */
    private static int arrayDimension(String clsName) {

        int arrayDim = 0;

        // is array
        if (clsName.startsWith("[")) {

            // compute array dim
            for (int i = 0; i < clsName.length(); i++) {
                if (clsName.charAt(i) != '[') {
                    break;
                }

                arrayDim = i + 1;
            }
        }

        return arrayDim;
    }

    /**
     * Converts the class name from array syntax to code syntax (java/groovy).
     * I.e., it converts
     * <code>[[Ljava.lang.String;</code> to
     * <code>java.lang.String[][]</code>.
     *
     * @param clsName
     * @return the class name in code syntax
     */
    public static String arrayClass2Code(String clsName) {

        int arrayDimension = arrayDimension(clsName);
        String elementClassName = elementClassName(clsName);

        String result = elementClassName;

        for (int i = 0; i < arrayDimension; i++) {
            result += "[]";
        }

        return result;
    }

    /**
     * Returns the name of the element class of the specified class name. If the
     * specified name does not denote an array class the name is returned
     * without modification.
     *
     * @param clsName name of the array class
     * @return name of the element class of the specified class name or the
     * specified cass name if it does not denote an array class
     */
    private static String elementClassName(String clsName) {
        int arrayDimension = arrayDimension(clsName);

        if (arrayDimension == 0) {
            return clsName;
        }


        String elementClsName =
                clsName.substring(arrayDimension, clsName.length());

        // if non-primitive remove 'L' from beginning and ';' from end
        if (elementClsName.endsWith(";")) {
            elementClsName =
                    elementClsName.substring(1, elementClsName.length() - 1);

        } else {
            elementClsName = primitiveSignaturesToFullNames.get(elementClsName);
        }

        return elementClsName;

    }

    /**
     * Checks whether a given class object is a primitive and returns its
     * wrapper class (except for void where always the primitive class is used).
     * If the class is no primitive the class will be returned without changes.
     *
     * @param clazz the class to convert
     * @return the wrapper class
     */
    public static Class<?> convertPrimitiveToWrapper(Class<?> clazz) {
        Class<?> result = clazz;

        if (clazz.isPrimitive()) {
//            if (clazz.getName().equals("boolean")) {
//                result = Boolean.class;
//            } else if (clazz.getName().equals("short")) {
//                result = Short.class;
//            } else if (clazz.getName().equals("int")) {
//                result = Integer.class;
//            } else if (clazz.getName().equals("long")) {
//                result = Long.class;
//            } else if (clazz.getName().equals("float")) {
//                result = Float.class;
//            } else if (clazz.getName().equals("double")) {
//                result = Double.class;
//            } else if (clazz.getName().equals("byte")) {
//                result = Byte.class;
//            } else if (clazz.getName().equals("char")) {
//                result = Character.class;
//            }

            return primitiveWrapperClasses.get(clazz.getName());
        }

        return result;
    }

    /**
     * Returns the class object of the primitive type that is specified by name.
     * Short names as used by array syntax, e.g.,
     * <code>I,D,Z</code> and long names, e.g.,
     * <code>int,double,boolean</code> are supported.
     *
     * @param name the name of the requested class
     * @return the class
     * @throws IllegalArgumentException if the specified string does not match a
     * primitive class name
     */
    private static Class<?> primitiveClassForName(
            String name) {

        Class<?> result = primitiveClasses.get(name);

        if (result == null) {
            result = primitiveSignatures.get(name);
        }

        if (result == null) {
            throw new IllegalArgumentException(
                    "Given name does not specifiy a primitive type.");
        }

        return result;
    }
}
