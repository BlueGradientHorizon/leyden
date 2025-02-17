/*
 * Copyright (c) 2002, 2024, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_RUNTIME_PERFDATA_INLINE_HPP
#define SHARE_RUNTIME_PERFDATA_INLINE_HPP

#include "runtime/perfData.hpp"

#include "services/management.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/growableArray.hpp"

inline int PerfDataList::length() {
  return _set->length();
}

inline void PerfDataList::append(PerfData *p) {
  _set->append(p);
}

inline PerfData* PerfDataList::at(int index) {
  return _set->at(index);
}

inline bool PerfDataManager::exists(const char* name) {
  if (_all != nullptr) {
    return _all->contains(name);
  } else {
    return false;
  }
}

inline jlong PerfTickCounters::elapsed_counter_value_ms() const {
  return Management::ticks_to_ms(_elapsed_counter->get_value());
}

inline jlong PerfTickCounters::thread_counter_value_ms() const {
  return Management::ticks_to_ms(_thread_counter->get_value());
}

inline jlong PerfTickCounters::elapsed_counter_value_us() const {
  return Management::ticks_to_us(_elapsed_counter->get_value());
}

inline jlong PerfTickCounters::thread_counter_value_us() const {
  return Management::ticks_to_us(_thread_counter->get_value());
}

#endif // SHARE_RUNTIME_PERFDATA_INLINE_HPP
