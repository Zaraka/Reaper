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
 * Define names of classes in databases & another usefull data
 * @author zaraka
 */
public enum DatabaseClasses {
    RESOURCE {
        @Override
        public String getType() {
            return "V";
        }

        @Override
        public String getName() {
            return "Resource";
        }
    },
    FORM {
        @Override
        public String getType() {
            return "V";
        }
        @Override
        public String getName() {
            return "Form";
        }
    },
    BLACWHITEKLIST {
        @Override
        public String getType() {
            return "V";
        }

        @Override
        public String getName() {
            return "BlackWhiteList";
        }
    },
    PROJECT {
        @Override
        public String getType() {
            return "V";
        }
        @Override
        public String getName() {
            return "Project";
        }
    },
    INCLUDES {
        @Override
        public String getType() {
            return "E";
        }

        @Override
        public String getName() {
            return "Includes";
        }
    },
    ROOT {

        @Override
        public String getType() {
            return "E";
        }

        @Override
        public String getName() {
            return "Root";
        }
    },
    LINKTO {
        @Override
        public String getType() {
            return "E";
        }

        @Override
        public String getName() {
            return "LinkTo";
        }
    },
    LINKQUE {
        @Override
        public String getType() {
            return "D";            
        }

        @Override
        public String getName() {
            return "LinkQue";
        }
        
    },
    LINKSET {
        @Override
        public String getType() {
            return "D";
        }

        @Override
        public String getName() {
            return "LinkSet";
        }
        
    };
    public abstract String getType();
    public abstract String getName();
}
