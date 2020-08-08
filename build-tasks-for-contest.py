#!/usr/bin/env python3

import sys
import os
from os import listdir
from os.path import isfile, join
from pathlib import Path
import shutil

if len(sys.argv) != 5:
    print("rtfm")
    exit(2)

contestId = sys.argv[1]  # "c1"
source = sys.argv[2]  # "./statements"
target = sys.argv[3]  # "./compiled-tasks"
solution = sys.argv[4]  # "./src"

sourceFolder = source + "/" + contestId
targetFolder = target + "/" + contestId

texfiles = [f for f in listdir(sourceFolder) if
            isfile(join(sourceFolder, f)) and f.endswith('tex') and (f != "problems.tex")]

template = """
<?xml version="1.0" encoding="utf-8" ?>
<problem
        package=""
        id="{taskId}"
        type="standard">

    <statement language="ru_RU">

        <title>{title}</title>

        <description>
            <p>
                {description}
            </p>
        </description>

        <input_format>
            <p>
                {inputFormat}
            </p>
        </input_format>

        <output_format>
            <p>
                {outputFormat}
            </p>
        </output_format>

    </statement>

    <examples>

        <example>
            <input>
                {inputExample}
            </input>
            <output>
                {outputExample}
            </output>
        </example>

    </examples>

</problem>
"""

for texfile in texfiles:
    # CREATING STATEMENT.XML AND TEST FILES
    taskPrefix = texfile.split("-")[0]
    taskId = texfile.split("-")[1].split(".")[0]
    print("======== Task " + taskPrefix + " : " + taskId + " ========")
    targetFolderForTask = targetFolder + "/" + taskPrefix

    idx = 0
    lst = [""] * 7

    f = open(sourceFolder + "/" + texfile, 'r')
    lines = f.readlines()
    f.close

    title = ""
    for line in lines:
        if line.startswith("\\begin{problem}"):
            title = line.split("}{")[1]
            idx += 1
        if line.startswith("\\InputFile"):
            idx += 1
        if line.startswith("\\OutputFile"):
            idx += 1
        if line.startswith("\\Example"):
            idx += 1
        if line.startswith("}{"):
            idx += 1
        if line.startswith("\\Note"):
            idx += 1
        if line[0] != '\\' and line[0] != '}' and line:
            lst[idx] = lst[idx] + line

    # for i in range(1, 7):
    #     print(str(i) + ": " + lst[i])

    Path(targetFolderForTask).mkdir(parents=True, exist_ok=True)

    inputExample = lst[4]
    inputExample = os.linesep.join([s for s in inputExample.splitlines() if s])

    outputExample = lst[5]
    outputExample = os.linesep.join([s for s in outputExample.splitlines() if s])

    taskXml = template.format(
        taskId=taskId,
        title=title,
        description=lst[1],
        inputFormat=lst[2],
        outputFormat=lst[3],
        inputExample=inputExample,
        outputExample=outputExample
    )
    taskXmlFilePath = targetFolderForTask + "/statement.xml"
    taskXmlFile = open(taskXmlFilePath, "w")
    n = taskXmlFile.write(taskXml)
    taskXmlFile.close()
    print("statement.xml written to " + taskXmlFilePath)

    # COPYING SOLUTION
    solutionFilename = solution + "/" + contestId + taskPrefix + ".kt"
    solutionTargetFilename = targetFolderForTask + "/solution.kt"
    shutil.copyfile(solutionFilename, solutionTargetFilename)
    print("solution file " + solutionFilename + " copied to " + solutionTargetFilename)

    # CREATING TESTS
    targetFolderForTests = targetFolderForTask + "/tests"
    Path(targetFolderForTests).mkdir(parents=True, exist_ok=True)
    
    testInputFilePath = targetFolderForTests + "/01"
    testInputFile = open(testInputFilePath, "w")
    n2 = testInputFile.write(inputExample + os.linesep)
    testInputFile.close()

    testOutputFilePath = targetFolderForTests + "/01.a"
    testOutputFile = open(testOutputFilePath, "w")
    n3 = testOutputFile.write(outputExample + os.linesep)
    testOutputFile.close()

    print("written files " + testInputFilePath + " and " + testOutputFilePath)

    # BUILDING ARCHIVE
    targetArchivePath = targetFolderForTask
    shutil.make_archive(targetArchivePath, 'zip', targetFolderForTask)
    print("built archive " + targetArchivePath)

    shutil.rmtree(targetFolderForTask)

    print()
