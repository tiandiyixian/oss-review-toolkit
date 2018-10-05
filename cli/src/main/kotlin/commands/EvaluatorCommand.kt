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

package com.here.ort.commands

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

import com.here.ort.CommandWithHelp
import com.here.ort.model.OrtResult
import com.here.ort.model.readValue
import com.here.ort.utils.PARAMETER_ORDER_MANDATORY

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback

import java.io.File

import javax.script.ScriptEngineManager

@Parameters(commandNames = ["evaluate"], commandDescription = "Evaluate rules on ORT result files.")
object EvaluatorCommand : CommandWithHelp() {
    @Parameter(description = "The ORT result file to use.",
            names = ["--ort-result-file", "-i"],
            required = true,
            order = PARAMETER_ORDER_MANDATORY)
    private lateinit var ortResultFile: File

    @Parameter(description = "The rules file to use.",
            names = ["--rules-file", "-r"],
            required = true,
            order = PARAMETER_ORDER_MANDATORY)
    private lateinit var rulesFile: File

    private val engine = ScriptEngineManager().getEngineByExtension("kts")

    override fun runCommand(jc: JCommander) {
        setIdeaIoUseFallback()

        val result = ortResultFile.readValue<OrtResult>()
        engine.put("result", result)

        engine.eval("""
            import com.here.ort.model.OrtResult
            val result = bindings["result"] as OrtResult
            println(result.analyzer?.config)
        """)
    }
}
