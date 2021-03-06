/*
 * Copyright (C) 2017-2018 HERE Europe B.V.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.ort.utils

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class RedirectionTest : WordSpec({
    "redirecting output" should {
        // Use a relatively large number of lines that results in more than 64k to be written to test against the pipe
        // buffer limit on Linux, see https://unix.stackexchange.com/a/11954/53328.
        val numberOfLines = 10000

        "work for stdout only" {
            val stdout = redirectStdout {
                for (i in 1..numberOfLines) System.out.println("stdout: $i")
            }

            // The last printed line has a newline, resulting in a trailing blank line.
            val stdoutLines = stdout.lines().dropLast(1)
            stdoutLines.count() shouldBe numberOfLines
            stdoutLines.last() shouldBe "stdout: $numberOfLines"
        }

        "work for stderr only" {
            val stderr = redirectStderr {
                for (i in 1..numberOfLines) System.err.println("stderr: $i")
            }

            // The last printed line has a newline, resulting in a trailing blank line.
            val stderrLines = stderr.lines().dropLast(1)
            stderrLines.count() shouldBe numberOfLines
            stderrLines.last() shouldBe "stderr: $numberOfLines"
        }

        "work for stdout and stderr at the same time" {
            var stderr = ""
            val stdout = redirectStdout {
                stderr = redirectStderr {
                    for (i in 1..numberOfLines) {
                        System.out.println("stdout: $i")
                        System.err.println("stderr: $i")
                    }
                }
            }

            // The last printed line has a newline, resulting in a trailing blank line.
            val stdoutLines = stdout.lines().dropLast(1)
            stdoutLines.count() shouldBe numberOfLines
            stdoutLines.last() shouldBe "stdout: $numberOfLines"

            // The last printed line has a newline, resulting in a trailing blank line.
            val stderrLines = stderr.lines().dropLast(1)
            stderrLines.count() shouldBe numberOfLines
            stderrLines.last() shouldBe "stderr: $numberOfLines"
        }

        "work when trapping exit calls" {
            var exitCode : Int? = null

            val stdout = redirectStdout {
                exitCode = trapSystemExitCall {
                    for (i in 1..numberOfLines) System.out.println("stdout: $i")
                    System.exit(42)
                }
            }

            exitCode shouldBe 42

            // The last printed line has a newline, resulting in a trailing blank line.
            val stdoutLines = stdout.lines().dropLast(1)
            stdoutLines.count() shouldBe numberOfLines
            stdoutLines.last() shouldBe "stdout: $numberOfLines"
        }
    }
})
