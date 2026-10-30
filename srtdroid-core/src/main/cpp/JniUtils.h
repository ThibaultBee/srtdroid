/*
 * Copyright (C) 2021 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#pragma once

#include <jni.h>

/**
 * Clears the Java exception pending on `env`, if any, and logs its toString() (class and message)
 * prefixed with `where`. Does nothing when no exception is pending.
 *
 * A native callback must not return to the caller, detach its thread or make another JNI call with
 * an exception pending: that aborts under CheckJNI. Whatever happens while describing the
 * exception, none is left pending on return.
 */
void clearPendingException(JNIEnv *env, const char *where);
