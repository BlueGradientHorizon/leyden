/*
 * Copyright (c) 2019, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#ifndef SHARE_UTILITIES_CLASSPATHSTREAM_HPP
#define SHARE_UTILITIES_CLASSPATHSTREAM_HPP

class ClasspathStream : public StackObj {
  const char* _class_path;
  int _len;
  int _start;
  int _end;

public:
  ClasspathStream(const char* class_path) {
    _class_path = class_path;
    _len = (int)strlen(class_path);
    _start = 0;
    _end = 0;
  }

  bool has_next() {
    return _start < _len;
  }

  const char* get_next();

  // Number of characters have been consumed from the input (this->_classpath)
  // ClasspathStream cps("a:b:c");
  // cps.next();    /* returns "a" */
  // cps.next();    /* returns "b" */
  // cps.num_chars_consumed() == 3;
  size_t num_chars_consumed() {
    return (size_t)_start;
  }
};

#endif // SHARE_UTILITIES_CLASSPATHSTREAM_HPP
