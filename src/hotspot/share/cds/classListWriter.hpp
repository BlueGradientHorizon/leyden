/*
 * Copyright (c) 2020, 2024, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_CDS_CLASSLISTWRITER_HPP
#define SHARE_CDS_CLASSLISTWRITER_HPP

#include "runtime/javaThread.hpp"
#include "runtime/mutexLocker.hpp"
#include "utilities/ostream.hpp"

class ClassFileStream;

class ClassListWriter {
#if INCLUDE_CDS
  class IDTable;
  class WriteResolveConstantsCLDClosure;

  static fileStream* _classlist_file;
  static IDTable* _id_table;
  static int _total_ids;
  MutexLocker _locker;

  static int get_id(const InstanceKlass* k);
  static bool has_id(const InstanceKlass* k);
  static void assert_locked() { assert_lock_strong(ClassListFile_lock); }
  static void write_resolved_constants_for(InstanceKlass* klass);
  static void write_array_info_for(InstanceKlass* klass);
  static void write_reflection_data_for(InstanceKlass* klass);
  static void write_loader_negative_lookup_cache_for(oop loader, const char* loader_type);
public:
  ClassListWriter() : _locker(Thread::current(), ClassListFile_lock, Mutex::_no_safepoint_check_flag) {}

  outputStream* stream() {
    return _classlist_file;
  }

  void handle_class_unloading(const InstanceKlass* klass);

  static bool is_enabled() {
    return _classlist_file != nullptr && _classlist_file->is_open();
  }

#else
public:
  static bool is_enabled() {
    return false;
  }
#endif // INCLUDE_CDS


  static void init() NOT_CDS_RETURN;
  static void write(const InstanceKlass* k, const ClassFileStream* cfs) NOT_CDS_RETURN;
  static void write_to_stream(const InstanceKlass* k, outputStream* stream, const ClassFileStream* cfs = nullptr) NOT_CDS_RETURN;
  static void write_resolved_constants() NOT_CDS_RETURN;
  static void write_reflection_data() NOT_CDS_RETURN;
  static void write_loader_negative_lookup_cache() NOT_CDS_RETURN;
  static void delete_classlist() NOT_CDS_RETURN;
};

#endif // SHARE_CDS_CLASSLISTWRITER_HPP
