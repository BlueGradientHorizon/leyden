/*
 * Copyright (c) 2023, 2025, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test In Leyden repo, classes that are linked during the assembly phase can
 *       skip verification constraint checks. See JDK-8317269.
 *       NOTE: this feature is not included when JDK-8315737 is upstreamed to the mainline.
 * @requires vm.cds
 * @requires vm.cds.supports.aot.class.linking
 * @summary Test for verification of classes that are aot-linked
 * @library /test/jdk/lib/testlibrary
 *          /test/lib
 *          /test/hotspot/jtreg/runtime/cds/appcds
 *          /test/hotspot/jtreg/runtime/cds/appcds/test-classes
 * @build GoodOldClass BadOldClass BadOldClass2 BadNewClass BadNewClass2
 * @build AOTClassLinkingVerification
 * @run driver jdk.test.lib.helpers.ClassFileInstaller -jar WhiteBox.jar jdk.test.whitebox.WhiteBox
 * @run driver jdk.test.lib.helpers.ClassFileInstaller -jar app1.jar
 *                 AOTClassLinkingVerificationApp
 *                 Unlinked UnlinkedSuper
 *                 BadOldClass
 *                 BadOldClass2
 *                 BadNewClass
 *                 BadNewClass2
 *                 GoodOldClass Vehicle Car
 *                 Util
 * @run driver jdk.test.lib.helpers.ClassFileInstaller -jar app2.jar
 *                 UnlinkedSub
 *                 Foo NotFoo
 * @run driver AOTClassLinkingVerification
 */

import java.io.File;
import java.lang.invoke.MethodHandles;
import jdk.test.lib.helpers.ClassFileInstaller;
import jdk.test.whitebox.WhiteBox;

public class AOTClassLinkingVerification {
    static final String app1Jar = ClassFileInstaller.getJarPath("app1.jar");
    static final String app2Jar = ClassFileInstaller.getJarPath("app2.jar");
    static final String wbJar = TestCommon.getTestJar("WhiteBox.jar");
    static final String bootAppendWhiteBox = "-Xbootclasspath/a:" + wbJar;
    static final String mainClass = AOTClassLinkingVerificationApp.class.getName();

    public static void main(String[] args) throws Exception {
        // Dump without app2.jar so:
        //  - Unlinked can be resolved, but UnlinkedSuper UnlinkedSub cannot be resolved,
        //    so Unlinked cannot be verified at dump time.
        //  - BadOldClass2 can be resolved, but Foo and NotFoo cannot be resolved,
        //    so BadOldClass2 cannot be verified at dump time.
        //  - BadNewClass2 can be resolved, but Foo and NotFoo cannot be resolved,
        //    so BadNewClass2 cannot be verified at dump time.
        TestCommon.testDump(app1Jar, TestCommon.list("Unlinked",
                                                     "BadOldClass",
                                                     "BadOldClass2",
                                                     "BadNewClass",
                                                     "BadNewClass2",
                                                     "GoodOldClass"),
                            bootAppendWhiteBox,
                            "-XX:+AOTClassLinking",
                            "-Xlog:cds+class=debug");

        TestCommon.run("-cp", app1Jar + File.pathSeparator + app2Jar,
                       "-XX:+UnlockDiagnosticVMOptions",
                       "-XX:+WhiteBoxAPI",
                       bootAppendWhiteBox,
                       "AOTClassLinkingVerificationApp", app1Jar)
            .assertNormalExit();
    }
}

class AOTClassLinkingVerificationApp {
    static WhiteBox wb = WhiteBox.getWhiteBox();
    static ClassLoader classLoader = AOTClassLinkingVerificationApp.class.getClassLoader();
    static File app1Jar;

    public static void main(String[] args) throws Exception {
        app1Jar = new File(args[0]);
        assertNotShared(UnlinkedSub.class);
        assertShared(UnlinkedSuper.class);
        assertNotShared(Unlinked.class); // failed verification during dump time
        assertNotShared(Foo.class);
        assertNotShared(NotFoo.class);
        String s = Unlinked.doit();
        if (!s.equals("heyhey")) {
            throw new RuntimeException("Unlinked.doit() returns wrong result: " + s);
        }

        Class cls_BadOldClass = Class.forName("BadOldClass", false, classLoader);
        assertNotShared(cls_BadOldClass); // failed verification during dump time
        try {
            cls_BadOldClass.newInstance();
            throw new RuntimeException("BadOldClass cannot be verified");
        } catch (VerifyError expected) {}

        Class cls_BadOldClass2 = Class.forName("BadOldClass2", false, classLoader);
        assertNotShared(cls_BadOldClass2); // failed verification during dump time
        try {
            cls_BadOldClass2.newInstance();
            throw new RuntimeException("BadOldClass2 cannot be verified");
        } catch (VerifyError expected) {}

        Class cls_BadNewClass = Class.forName("BadNewClass", false, classLoader);
        assertNotShared(cls_BadNewClass); // failed verification during dump time
        try {
            cls_BadNewClass.newInstance();
            throw new RuntimeException("BadNewClass cannot be verified");
        } catch (VerifyError expected) {}

        Class cls_BadNewClass2 = Class.forName("BadNewClass2", false, classLoader);
        assertNotShared(cls_BadNewClass2); // failed verification during dump time
        try {
            cls_BadNewClass2.newInstance();
            throw new RuntimeException("BadNewClass2 cannot be verified");
        } catch (VerifyError expected) {}


        // Although Vehicle and Car are not specified in the classlist, they are archived as
        // they were used during dumptime verification of GoodOldClass
        assertAlreadyLoaded("Vehicle");
        assertAlreadyLoaded("Car");
        assertAlreadyLoaded("GoodOldClass");

        assertShared(GoodOldClass.class);
        assertShared(Vehicle.class);
        assertShared(Car.class);

        GoodOldClass.doit(); // Should not fail
    }

    static void assertShared(Class c) {
        if (!wb.isSharedClass(c)) {
            throw new RuntimeException("wb.isSharedClass(" + c.getName() + ") should be true");
        }
    }

    static void assertNotShared(Class c) {
        if (wb.isSharedClass(c)) {
            throw new RuntimeException("wb.isSharedClass(" + c.getName() + ") should be false");
        }
    }

    static void assertAlreadyLoaded(String className) throws Exception {
        byte[] data = Util.getClassFileFromJar(app1Jar, className);
        try {
            MethodHandles.lookup().defineClass(data);
        } catch (LinkageError e) {
            if (e.getMessage().contains("duplicate class definition for " + className)) {
                return;
            } else {
                throw e;
            }
        }
        throw new RuntimeException(className + " must have already been loaded");
    }
}


class Unlinked {
    static String doit() {
        UnlinkedSuper sup = new UnlinkedSub();
        return sup.doit();
    }
}

abstract class UnlinkedSuper {
    abstract String doit();
}

class UnlinkedSub extends UnlinkedSuper {
    String doit() {
        return "heyhey";
    }
}

class Foo {}
class NotFoo {}

class Vehicle {}
class Car extends Vehicle {}
