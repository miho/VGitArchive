/*
 * ConfigurationFileImpl.java
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class ConfigurationFileImpl implements ConfigurationFile {

    private Map<String, String> properties = new HashMap<String, String>();
//    Properties properties = new Properties();
    private File file;

    public ConfigurationFileImpl(File f) {
        file = f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean load() {

        boolean result = false;

        if (!file.isFile()) {
            return false;
        }

        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigurationFileImpl.class.getName()).
                    log(Level.SEVERE, null, ex);

            try {
                in.close();
            } catch (Exception ex1) {
                Logger.getLogger(ConfigurationFileImpl.class.getName()).
                        log(Level.SEVERE, null, ex1);
            }

            // Throw exception
            //throw ex;

        }
//        try {
//            properties.load(in);
//        } catch (IOException ex) {
//            Logger.getLogger(PluginConfigurationImpl.class.getName()).
//                    log(Level.SEVERE, null, ex);
//            try {
//                in.close();
//            } catch (IOException ex1) {
//                throw ex;
//            }
//        }

        XMLDecoder d = new XMLDecoder(in);

        Object o = null;

        try {
            o = d.readObject();
        } catch (Exception ex) {
            Logger.getLogger(ConfigurationFileImpl.class.getName()).
                    log(Level.SEVERE, null, ex);
            try {
                d.close();
            } catch (Exception ex1) {
                Logger.getLogger(ConfigurationFileImpl.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
            
            return false;
        }

        if (!(o instanceof Map<?, ?>)) {
            try {
                throw new IOException("Data format not recognized!");
            } catch (IOException ex) {
                Logger.getLogger(ConfigurationFileImpl.class.getName()).
                        log(Level.SEVERE, null, ex);

                try {
                    d.close();
                } catch (Exception ex1) {
                    Logger.getLogger(ConfigurationFileImpl.class.getName()).
                            log(Level.SEVERE, null, ex1);
                }

                // Throw exception
                //throw ex;
            }
        } else {
            properties = (Map<String, String>) o;
            result = true;
        }

        d.close();

        return result;
    }

    @Override
    public ConfigurationFile setProperty(String key, String value) {
        properties.put(key, value);

        return this;
    }

    @Override
    public boolean save() {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigurationFileImpl.class.getName()).
                    log(Level.SEVERE, null, ex);

            try {
                out.close();
            } catch (Exception ex1) {
                Logger.getLogger(ConfigurationFileImpl.class.getName()).
                        log(Level.SEVERE, null, ex1);
            }

            return false;
        }

        XMLEncoder e = new XMLEncoder(out);

        e.writeObject(properties);

        e.flush();
        e.close();

        return true;
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Iterable<String> getKeys() {
        return properties.keySet();
    }

    @Override
    public Iterable<String> getValues() {
        return properties.values();
    }

    @Override
    public ConfigurationFile removeProperty(String key) {
        properties.remove(key);
        return this;
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }
}
