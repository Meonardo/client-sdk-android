/*
 * Copyright 2023-2024 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc

import android.content.Context
import org.mockito.Mockito

object WebRTCInitializer {
    fun initialize(context: Context = Mockito.mock(Context::class.java)) {
        try {
            ContextUtils.initialize(context)
            NativeLibraryLoaderTestHelper.initialize()
        } catch (e: Throwable) {
            // do nothing. this is expected.
        }
    }
}
