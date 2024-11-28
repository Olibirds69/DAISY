/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.DAISY

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class GestureRecognizerTest {


    private val expectedCategoriesForImageAndLiveStreamMode = listOf(
        Category.create(0.8105f, 0, "Thumb_Up", ""),
    )

    private val expectedCategoryForVideoMode = listOf(
        Category.create(0.8193482f, 0, "Thumb_Up", ""),
    )
    private lateinit var lock: ReentrantLock
    private lateinit var condition: Condition

    @Before
    fun setup() {
        lock = ReentrantLock()
        condition = lock.newCondition()
    }

    /**
     * Verify that the result returned from the Gesture Recognizer Helper with
     * LIVE_STREAM mode is within the acceptable range to the expected result.
     */
    @Test
    @Throws(Exception::class)
    fun recognizerLiveStreamModeResultFallsWithinAcceptedRange() {
        var recognizerResult: GestureRecognizerHelper.ResultBundle? = null
        val gestureRecognizerHelper = GestureRecognizerHelper(
            context = ApplicationProvider.getApplicationContext(),
            runningMode = RunningMode.LIVE_STREAM,
            gestureRecognizerListener = object : GestureRecognizerHelper.GestureRecognizerListener {
                override fun onError(error: String, errorCode: Int) {
                    // Print out the error
                    println(error)
                    // Release the lock
                    lock.withLock {
                        condition.signal()
                    }
                }

                override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
                    recognizerResult = resultBundle
                    // Release the lock and start verifying the result
                    lock.withLock {
                        condition.signal()
                    }
                }
            })


        // Expecting two hands for this test case
        val results = recognizerResult!!.results
        assert(results.size == 2)  // Verify we have results for two hands

        // Verify the categories and scores for each hand
        for (i in results.indices) {
            val categories = results[i].gestures().first()
            assert(categories.isNotEmpty())
            assertEquals(
                expectedCategoriesForImageAndLiveStreamMode[i].score(),
                categories.first().score(),
                0.01f
            )
            assertEquals(
                expectedCategoriesForImageAndLiveStreamMode[i].categoryName(),
                categories.first().categoryName()
            )
        }
    }

}
