/*
 * The MIT License
 *
 * Copyright 2015 Reaper.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public enum PreferenceKeys {
    CREATE_SITEMAP {
        @Override
        public String getKey() {
            return "createSitemap";
        }

        @Override
        public Object getValue() {
            return true;
        }
    },
    DB_HOST {
        @Override
        public String getKey() {
            return "dbHost";
        }

        @Override
        public Object getValue() {
            return "remote:localhost/ReaperTest";
        }
    },
    DB_USER {
        @Override
        public String getKey() {
            return "dbUser";
        }

        @Override
        public Object getValue() {
            return "admin";
        }
    },
    DB_PASS {
        @Override
        public String getKey() {
            return "dbPass";
        }

        @Override
        public Object getValue() {
            return "root";
        }
    },
    PHANTOM_PATH {
        @Override
        public String getKey() {
            return "phantomPath";
        }        

        @Override
        public Object getValue() {
            return "";
        }
    },
    GALLERY_PATH {
        @Override
        public String getKey(){
            return "galleryPath";
        }

        @Override
        public Object getValue() {
            return System.getProperty("user.home") + "/Reaper/";
        }
    },
    AUTO_CONNECT {
        @Override
        public String getKey() {
            return "autoConnect";
        }

        @Override
        public Object getValue() {
            return false;
        }
    };
    public abstract String getKey();
    public abstract Object getValue();
}
