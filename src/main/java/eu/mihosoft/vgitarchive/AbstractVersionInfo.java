/*
 * AbstractVersionInfo.java
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
 * Defines a VRL version information. This class specifies how to compare
 * versions. This is used to check file format compatibility or plugin version
 * etc.
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
interface AbstractVersionInfo {

    /**
     * Compares file versions.
     * <p>
     * Versions are compared as follows:
     * </p>
     * The number of elements to compare is defined by the minimum version array
     * size. Iterate through all elements to compare. If elements differ then
     * break and return
     * </p>
     * <p>
     * <ul>
     * <li><code>-1</code> if the first element is smaller then the second
     * element</li>
     * <li><code>1</code> if the first element is greater then the second
     * element</li>
     * </ul>
     * </p>
     * @param o the version to compare to
     * @return <code>-1</code> if this version info is smaller,
     * <code>1</code> if this version info is bigger,
     * <code>0</code> if both version infos are equal
     */
    int compareTo(Object o);

    /**
     * Returns the version description.
     * @return the version description
     */
    String getDescription();

    /**
     * Returns the version number string.
     * @return the version
     */
    String getVersion();

    /**
     * Indicates whether the currently specified version string is valid.
     * @return <code>true</code> if the version string is valid;
     * <code>false</code> otherwise
     */
    boolean isVersionValid();

}
