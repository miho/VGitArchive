/*
 * FileVersionInfo.java
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
 * Defines a VRL file version.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public final class FileVersionInfo implements AbstractVersionInfo {

    private String version;
    private String description;

    /**
     * Constructor.
     */
    public FileVersionInfo() {
    }

    /**
     * Constructor.
     * @param version the version number
     * @param description the description
     */
    public FileVersionInfo(String version, String description) {
        setVersion(version);
//        if (!isVersionValid()) {
//            throw new IllegalArgumentException(
//                    "Version string has wrong format!");
//        }
        this.description = description;
    }

    /**
     * Returns the version number.
     * @return the version
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Defines the version number.
     * @param version the version number to set
     */
    public void setVersion(String version) {
        boolean result = new VersionInfo(version, "").isVersionValid();

        if (result == false) {
            throw new IllegalArgumentException(
                    "Version string has wrong format!");
        }
        this.version = version;
    }

    /**
     * Returns the version description.
     * @return the version description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Defines the version description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(Object o) {
        return new VersionInfo(version, description).compareTo(o);
    }

    @Override
    public boolean isVersionValid() {
        return new VersionInfo(version, description).isVersionValid();
    }
}
